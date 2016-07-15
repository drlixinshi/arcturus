package com.morningstar.arcturus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.morningstar.automation.base.core.utils.Logger;

public class Arcturus {

	WebDriver driver;
	Logger LOGGER;
	String pkg;
	Map<String, Object> variables;
	int currentWindow = -1;
	List<String> windowHandles = new ArrayList<String>();
	List<WebElement[]> frames;

	public Arcturus(WebDriver driver, Map<String, Object> variables, String pkg, Logger LOGGER) {
		this.driver = driver;
		this.variables = variables;
		this.LOGGER = LOGGER;
		this.pkg = pkg;
	}

	public TestResult execution(Sentence sent) {
		if (sent.cmdTarget != null) sent.cmdTarget = replaceVariable(sent.cmdTarget);
		if (sent.cmdValue != null) sent.cmdValue = replaceVariable(sent.cmdValue);
		System.out.println("===>" + sent.cmd + " " + sent.cmdTarget + " " + sent.cmdValue);
		WebElement element;
		if (sent.cmd == null) return TestResult.CONT;
		else if (sent.cmd.equals("SET")) {
			variables.put(sent.cmdTarget, sent.cmdValue);
			System.out.println(String.format("[%s] <- [%s]", sent.cmdTarget, sent.cmdValue));
			return TestResult.CONT;
		}
		else if (sent.cmd.equals("Open")) {
			driver.get(sent.cmdTarget);
			return TestResult.CONT;
		}
		else if (sent.cmd.equals("ClearSendKeys")) {
			element = getElement(sent.cmdTarget, 0);
			if (element == null) return TestResult.theTestResult(sent.act2);
			element.sendKeys(sent.cmdValue);
			return TestResult.theTestResult(sent.act1);
		}
		else if (sent.cmd.equals("PushButton")) {
			element = getElement(sent.cmdTarget, 0);
			if (element == null) return TestResult.theTestResult(sent.act2);
			element.click();
			return TestResult.theTestResult(sent.act1);
		}
		else if (sent.cmd.equals("VerifyContent")) {
			element = getElement(sent.cmdTarget, 0);
			if (element == null) return TestResult.theTestResult(sent.act2);
			if (sent.expr == null || sent.expr.isEmpty()
					|| Pattern.compile(sent.expr).matcher(element.getText()).find())
				return TestResult.theTestResult(sent.act1);
			return TestResult.theTestResult(sent.act2);
		}
		else if (sent.cmd.equals("VerifyElement")) {
			element = getElement(sent.cmdTarget, 0);
			return TestResult.theTestResult(element == null ? sent.act2 : sent.act1);
		}
		else if (sent.cmd.equals("IF")) {
			return TestResult.CONT;
		}
		else return TestResult.BLOCK;

	}

	public HashSet<String> getVarSet() {
		return new HashSet<String>(variables.keySet());
	}

	public void resetVars(HashSet<String> set) {
		for (String key : variables.keySet())
			if (!key.startsWith("$") && !set.contains(key)) variables.remove(key);
	}

	public TestResult runTestProc(String methodName, String tpName) {
		TestResult result = TestResult.BLOCK;
		System.out.println(tpName);
		Pattern pat = Pattern.compile("^([\\w\\.]+)\\s*\\((.*)\\)\\s*$");
		// ..............................1---------1.......2--2........
		Matcher m = pat.matcher(tpName);
		if (!m.find()) return result;
		String xml;
		try {
			tpName = m.group(1);
			xml = (String) Class.forName(pkg + ".testproc." + m.group(1)).getDeclaredField("xml").get(null);
		}
		catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			LOGGER.printToFile(methodName, "Exception: " + e.getMessage());
			return result;
		}
		TestProc tp = new TestProc(xml);
		Sentence s;
		while ((s = tp.getSentence()) != null) {
			LOGGER.printToFile(methodName, s.toString());
			if (s.cmd == null) continue;
			if (s.cmd.equals("CALL")) {
				// HashSet<String> set = getVarSet();
				result = runTestProc(methodName,
						s.cmdTarget.indexOf('/') == -1 ? (tp.getPackage() + "." + s.cmdTarget) : s.cmdTarget);
				// resetVars(set); // Variables defined in sub-testproc
				// will be removed
				if (result.equals(TestResult.PASS)) result = TestResult.CONT;
			}
			else result = execution(s);

			LOGGER.printToFile(methodName, s + "\t => " + result.getResult());
			if (result.isTeminate()) break;
			if (result.isGoto()) {
				tp.setLabel(result.getLabel());
				continue;
			}
		}
		if (result.equals(TestResult.CONT)) result = TestResult.PASS;
		LOGGER.printToFile(methodName, "End of " + m.group(1));
		return result;
	}

	private String replaceVariable(String str) {
		String ss = "!Err";
		Pattern p = Pattern.compile("\\{(\\$\\d+|\\$*\\w+|\\w+[\\.\\w]*)\\}"); // do
																				// {simple_var}
																				// first,
																				// //
																				// $?
																				// =>
																				// $*
																				// May06'16
		Matcher match = p.matcher(str);
		if (match.find()) {
			String val = match.group(1);
			if (variables.containsKey(val)) ss = variables.get(val).toString();
			return replaceVariable(str.replace(match.group(0), ss));
		}
		return str;
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(200);
		}
		catch (InterruptedException e) {
			;
		}
	}

	private WebElement getElement(String uid, int timeout) {
		uid = uid.replace("&nbsp;", "\u00a0").replace("&lt;", "<").replace("&gt;", "<").replace("&amp;", "&");
		Pattern pat = Pattern.compile("(&#(\\d+);)");
		Matcher m = pat.matcher(uid);
		while (m.find()) {
			char ch = (char) Integer.parseInt(m.group(2));
			uid = uid.replace(m.group(1), Character.toString(ch));
			m = pat.matcher(uid);
		}

		// switch window if needed
		pat = Pattern.compile("^(\\[(\\+|\\-|\\d+)\\])(.*)$");
		// 1 2------------2 13--3
		m = pat.matcher(uid);
		if (m.find()) {
			uid = m.group(3);
			int cur = 0;
			String win = m.group(2);
			if (win.equals("+")) cur = currentWindow + 1;
			else if (win.equals("-")) cur = currentWindow - 1;
			else cur = Integer.parseInt(win);
			if (cur < 0) cur = 0;

			long t0 = System.nanoTime();
			Set<String> handles;
			while (true) { // waiting for i-th window appears
				handles = driver.getWindowHandles();
				if (handles != null && cur < handles.size()) break;
				if ((System.nanoTime() - t0) / 1000 > timeout) return null;
				sleep(200);
			}

			if (cur != currentWindow) {
				List<String> rms = new ArrayList<String>();
				// remove no-more-existing windows
				for (String x : windowHandles)
					if (!handles.contains(x)) rms.add(x);
				for (String x : rms)
					windowHandles.remove(x);
				// add new opened windows
				for (String x : handles)
					if (!windowHandles.contains(x)) windowHandles.add(x);
				currentWindow = cur;
				driver.switchTo().window(windowHandles.get(currentWindow));
			}
		}
		else if (currentWindow == -1) {
			windowHandles.add(driver.getWindowHandle());
			currentWindow = 0;
		}

		// switch iframe if needed
		if (uid.toLowerCase().matches("//iframe//|iframe ")) {
			frames = new ArrayList<WebElement[]>();
			uid = uid.replaceAll("^(//)?iframe\\s*)", "");
			AddToFrameList(frames, new WebElement[0]);
			if (frames.size() == 0) frames = null;
		}

		m = Pattern.compile("^(//iframe(\\[([^\\]]*)\\]))[/ ]*(.*)").matcher(uid);
		if (m.find()) {
			/*
			 * (main-frame) [sub-frame id='f1'] [sub-sub-frame id='s11']
			 * [sub-sub-frame id='s12'] [sub-frame id='f2'] [sub-sub-frame
			 * id='s21']
			 * 
			 * //iframe[/] - switch main frame //iframe[@id='f1'] - switch to
			 * f01 from current (main) //iframe[1] //iframe[/;@id='f1'] - switch
			 * to f01 (from any) //iframe[@id='f1';@id='s12'] - switch to
			 * sub-sub-frame s12 from current (main) //iframe[1;2]
			 * //iframe[/;@id='f1';@id='s12'] - switch to sub-sub-frame s12 from
			 * any //iframe[/;2;1] //iframe[1] - switch to first iframe
			 * //iframe[]
			 */
			for (String ff : m.group(3).split(";")) {
				if (ff.trim() == "/") driver.switchTo().defaultContent();
				else {
					// WebElement frame = WaitWebElement(ff.Trim().Length == 0 ?
					// "//iframe" : "//iframe[" + ff + "]", timeout);
					// if (obj == null || obj.Element == null) return null;
					;// driver.SwitchTo().Frame((IWebElement)obj.Element);
				}
			}
		}
		// ...
		return WaitWebElement(uid, timeout, null);
	}

	void SwitchFrame(WebElement[] framePath) {
		driver.switchTo().defaultContent();
		for (WebElement f : framePath)
			driver.switchTo().frame(f);
	}

	void AddToFrameList(List<WebElement[]> frames, WebElement[] framePath) {
		int start = frames.size();
		int n = 0;
		SwitchFrame(framePath);
		for (WebElement e : driver.findElements(By.xpath("//iframe"))) {
			int s = framePath.length;
			WebElement[] path = new WebElement[n + 1];
			for (int i = 0; i < s; i++)
				path[i] = framePath[i];
			path[s + 1] = e;
			frames.add(path);
			n++;
		}
		for (int i = 0; i < n; i++) {
			AddToFrameList(frames, frames.get(start + i));
		}
	}

	private WebElement GetElementBy(String uid, String by) {
		if (driver == null) return null;
		switch (by) {
		case "id":
			return driver.findElement(By.id(uid));
		case "class":
			return driver.findElement(By.className(uid));
		case "name":
			return driver.findElement(By.name(uid));
		case "tag":
			return driver.findElement(By.tagName(uid));
		case "css":
			return driver.findElement(By.cssSelector(uid));
		case "link":
			return driver.findElement(By.linkText(uid));
		case "linktext":
			return driver.findElement(By.partialLinkText(uid));
		case "xpath":
			return driver.findElement(By.xpath(uid));
		}
		return null;
	}

	private int currentTimeout = 0;

	private WebElement WaitWebElement(String uid, int timeout, List<WebElement[]> frames) {
		if (driver == null) return null;
		int index = 0;
		String match = null;
		Matcher m;
		if ((m = Pattern.compile("^(.+)(\\[(\\d+)\\])$").matcher(uid)).find()) {
			uid = m.group(1);
			index = Integer.parseInt(m.group(3));
		}

		if (uid.isEmpty()) uid = "//body";

		long startTime = System.nanoTime();
		if (currentTimeout == 0 || currentTimeout != timeout) {
			if (timeout == 0) timeout = 60000;
			driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.MILLISECONDS);
			currentTimeout = timeout;
		}
		while ((System.nanoTime() - startTime) / 1000 < timeout) {
			WebElement obj = null;
			try {
				String by = "xpath";
				if ((m = Pattern.compile("^((id|class|name|tag|css|link|linktext|xpath)=)(.*)").matcher(uid)).find()) {
					uid = m.group(3);
					by = m.group(2);
				}
				else {
					if (uid.matches("^(\\[(\\d+|\\+|\\-)\\])?\\s*//.*")) by = "xpath";
					else if (uid.matches("^(([\\.#:]?[\\w\\-]+(\\(\\d+\\))?)|(\\w+[\\.#:][\\w\\-]+))(\\s+|>|\\+).*"))
						by = "css";
					// 12.................3..........3.2.4....................415..........5
				}

				if (by != "xpath") {
					obj = GetElementBy(uid, by);

					if (obj == null && frames != null) {
						for (WebElement[] x : frames) {
							SwitchFrame(x);
							obj = GetElementBy(uid, by);
							if (obj != null) break;
						}
					}
					if (obj != null) return obj;
				}
				else {
					/////////////////////////////////////////////////

					List<WebElement> xx = driver.findElements(By.xpath(uid));

					/////////////////////////////////////////////////

					if (xx.size() == 0 && frames != null) {
						for (WebElement[] x : frames) {
							SwitchFrame(x);
							xx = driver.findElements(By.xpath(uid));
							if (xx.size() > 0) break;
						}
					}

					int ii = index;
					for (int i = 0; i < xx.size(); i++) {
						WebElement x = xx.get(i);
						if (match == null || Pattern.compile(match, Pattern.MULTILINE).matcher(x.getText()).find()) {
							if (ii <= 1) {
								if (x.isDisplayed()) return x;
								if (index == 0) continue;
								// index=0 means not use index, so skip
								// invisible items
								return null;
							}
							else ii--;
						}
					}
				}
			}
			catch (Exception ex) {
				;
				// Fixed the issue caused by "Element is no longer attached
				// to the DOM selenium" with no return
				// return null;
			}
			sleep(500);
		}
		return null;

	}
}
