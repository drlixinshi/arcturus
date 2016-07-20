package com.morningstar.arcturus;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.morningstar.automation.base.core.utils.Logger;

public class Arcturus {

	WebDriver driver;
	Logger LOGGER;
	String pkg;
	Map<String, Object> variables;
	int currentWindow = -1;
	List<String> windowHandles = new ArrayList<String>();
	List<WebElement[]> frames;

	private WebDriverWait wait1sec;

	public Arcturus(WebDriver driver, Map<String, Object> variables, String pkg, Logger LOGGER) {
		this.driver = driver;
		this.variables = variables;
		this.LOGGER = LOGGER;
		this.pkg = pkg;
		this.wait1sec = new WebDriverWait(driver, 1);
	}

	public TestResult execution(Sentence sent) {
		if (sent.cmdTarget != null) sent.cmdTarget = replaceVariable(sent.cmdTarget);
		if (sent.cmdValue != null) sent.cmdValue = replaceVariable(sent.cmdValue);
		System.out.println(String.format("===>{0}\t{1}\t{2}", sent.cmd, sent.cmdTarget, sent.cmdValue));
		if (sent.cmd == null || sent.cmd.length() == 0) return TestResult.CONT;

		try {
			java.lang.reflect.Method method = this.getClass().getMethod("cmd" + sent.cmd, Sentence.class);
			return (TestResult) method.invoke(sent);
		}
		catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			System.out.println(e.getMessage());
			return TestResult.BLOCK;
		}

	}

	@SuppressWarnings("unused")
	private TestResult cmdSET(Sentence sent) {
		variables.put(sent.cmdTarget, sent.cmdValue);
		System.out.println(String.format("[%s] <- [%s]", sent.cmdTarget, sent.cmdValue));
		return TestResult.CONT;
	}

	@SuppressWarnings("unused")
	private TestResult cmdOpen(Sentence sent) {
		driver.get(sent.cmdTarget);
		return TestResult.CONT;
	}

	@SuppressWarnings("unused")
	private TestResult cmdClearTypeKeys(Sentence sent) {
		WebElement element = getElement(sent.cmdTarget, 0);
		if (element == null) return TestResult.theTestResult(sent.act2);
		element.sendKeys(sent.cmdValue);
		return TestResult.theTestResult(sent.act1);
	}

	@SuppressWarnings("unused")
	private TestResult cmdClick(Sentence sent) {
		WebElement element = getElement(sent.cmdTarget, 0);
		if (element == null) return TestResult.theTestResult(sent.act2);
		element.click();
		return TestResult.theTestResult(sent.act1);
	}

	@SuppressWarnings("unused")
	private TestResult cmdVerifyContent(Sentence sent) {
		WebElement element = getElement(sent.cmdTarget, 0);
		if (element == null) return TestResult.theTestResult(sent.act2);

		if (sent.expr == null || sent.expr.isEmpty() || Pattern.compile(sent.expr).matcher(element.getText()).find())
			return TestResult.theTestResult(sent.act1);
		return TestResult.theTestResult(sent.act2);
	}

	@SuppressWarnings("unused")
	private TestResult cmdVerifyElementContent(Sentence sent) {
		WebElement element = getElement(sent.cmdTarget, 0);
		return TestResult.theTestResult(element == null ? sent.act2 : sent.act1);
	}

	@SuppressWarnings("unused")
	private TestResult cmdIF(Sentence sent) {
		return TestResult.CONT;
	}

	@SuppressWarnings("unused")
	private TestResult cmdSLEEP(Sentence sent) {
		sleep(Integer.parseInt(sent.cmdTarget));
		return TestResult.CONT;
	}

	@SuppressWarnings("unused")
	private TestResult cmdGOTO(Sentence sent) {
		return TestResult.theTestResult(sent.cmd + " " + sent.cmdTarget);
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
						s.cmdTarget.matches("\\w+\\.\\w+.*") ? s.cmdTarget : (tp.getPackage() + "." + s.cmdTarget));
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
		Pattern p = Pattern.compile("\\{(\\$\\d+|\\$*\\w+|\\w+[\\.\\w]*)\\}");
		// do {simple_var} first, $? => $* May06'16

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
		if (timeout == 0) timeout = 10000;
		try {
			Pattern pat = Pattern.compile("(&#(\\d+);)");
			Matcher m = pat.matcher(uid);
			while (m.find()) {
				char ch = (char) Integer.parseInt(m.group(2));
				uid = uid.replace(m.group(1), Character.toString(ch));
				m = pat.matcher(uid);
			}

			// switch window if needed
			pat = Pattern.compile("^(\\[(\\+|\\-|\\d+)\\])(.*)$");
			// ......................1...2------------2...13--3
			m = pat.matcher(uid);
			if (m.find()) {
				uid = m.group(3);
				int cur = 0;
				String win = m.group(2);
				if (win.equals("+")) cur = currentWindow + 1;
				else if (win.equals("-")) cur = currentWindow - 1;
				else cur = Integer.parseInt(win) - 1;
				if (cur < 0) cur = 0;

				long t0 = System.nanoTime();
				Set<String> handles;
				while (true) { // waiting for i-th window appears
					handles = driver.getWindowHandles();
					if (handles != null && cur < handles.size()) break;
					if ((System.nanoTime() - t0) / 1000000 > timeout) return null;
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
			if (uid.toLowerCase().matches("(//iframe//|iframe ).*")) {
				frames = new ArrayList<WebElement[]>();
				uid = uid.replaceAll("^((//)?iframe\\s*)", "");
				AddToFrameList(frames, new WebElement[0]);
				if (frames.size() == 0) frames = null;
			}

			m = Pattern.compile("^(//iframe(\\[([^\\]]*)\\]))([/ ]*.*)").matcher(uid);
			// ....................1--------2---3------3----214-------4
			if (m.find()) {
				uid = m.group(4).trim();
				// (main-frame)
				// [sub-frame id='f1']
				// [sub-sub-frame id='s11']
				// [sub-sub-frame id='s12']
				// [sub-frame id='f2']
				// [sub-sub-frame id='s21']
				//
				// //iframe[/] - switch to main frame
				// //iframe[@id='f1'] - switch to f1 from current (main)
				// //iframe[1] - switch to f1 from current (main)
				// //iframe[/;@id='f1'] - switch to f1 (from any)
				// //iframe[/;1] - switch to f1 (from any)
				// //iframe[@id='f1';@id='s12'] - switch to sub-sub-frame s12
				// from current (main)
				// //iframe[1;2] - switch to sub-sub-frame s12 from current
				// (main)
				// //iframe[/;@id='f1';@id='s12'] - switch to sub-sub-frame s12
				// from any
				// //iframe[/;2;1] - switch to sub-sub-frame s21 from any
				//
				WebDriverWait wait = new WebDriverWait(driver, Math.max(1, timeout / 1000));
				for (String ff : m.group(3).split(";")) {
					if (ff.trim() == "/") driver.switchTo().defaultContent();
					else {
						String xpath = (ff.trim().length() == 0) ? "//iframe" : ("//iframe[" + ff + "]");
						WebElement frame = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
						if (frame == null) return null;
						driver.switchTo().frame(frame);
					}
				}
			}

			return WaitWebElement(uid, timeout, frames);
		}
		catch (Exception ex) {
			return null;
		}
	}

	void SwitchFrame(WebElement[] framePath) {
		try {
			System.out.println("Switch frame to:" + framePath.length);
			driver.switchTo().defaultContent();
			System.out.println("---");
			for (WebElement f : framePath) {
				System.out.print("<" + f.getAttribute("id"));
				driver.switchTo().frame(f);
				System.out.println(">");
			}
			System.out.println("===");
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	void AddToFrameList(List<WebElement[]> frames, WebElement[] framePath) {
		int start = frames.size();
		int n = 0;
		SwitchFrame(framePath);
		if (GetElementBy("//iframe", "xpath") == null) return;
		List<WebElement> ff = driver.findElements(By.xpath("//iframe"));
		for (WebElement e : ff) {
			if (!e.isDisplayed()) continue;
			int s = framePath.length;
			WebElement[] path = new WebElement[s + 1];
			for (int i = 0; i < s; i++)
				path[i] = framePath[i];
			path[s] = e;
			frames.add(path);
			n++;
		}
		for (int i = 0; i < n; i++) {
			AddToFrameList(frames, frames.get(start + i));
		}
	}

	private WebElement GetElementBy(String uid, String by) {
		if (driver == null) return null;
		try {
			switch (by) {
			case "id":
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.id(uid)));
			case "class":
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.className(uid)));
			case "name":
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.name(uid)));
			case "tag":
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.tagName(uid)));
			case "css":
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(uid)));
			case "link":
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.linkText(uid)));
			case "linktext":
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.partialLinkText(uid)));
			case "xpath":
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.xpath(uid)));
			}
		}
		catch (TimeoutException e) {
			;
		}
		return null;
	}

	private int currentTimeout = 0;

	private WebElement WaitWebElement(String uid, int timeout, List<WebElement[]> frames) {
		if (driver == null) return null;
		Matcher m;

		if (uid.isEmpty()) uid = "//body";

		long startTime = System.nanoTime();
		if (currentTimeout == 0 || currentTimeout != timeout) {
			if (timeout == 0) timeout = 60000;
			driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.MILLISECONDS);
			currentTimeout = timeout;
		}
		while (true) {
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
					// .....................12.................3..........3.2.4....................415..........5
				}

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
			catch (Exception ex) {
				;
				// Fixed the issue caused by "Element is no longer attached
				// to the DOM selenium" with no return
				// return null;
			}
			if ((System.nanoTime() - startTime) / 1000000 > timeout) break;
			sleep(500);
		}
		return null;
	}
}
