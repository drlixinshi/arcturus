package com.arcturus;

import java.util.ArrayList;
import java.util.List;
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

public class MyDriver {

	private WebDriver driver;
	private int currentWindow = -1;
	private List<String> windowHandles = new ArrayList<String>();

	private WebDriverWait wait1sec;
	private int defaultTimeout = 30000;

	public MyDriver(WebDriver driver) {
		this.driver = driver;
		this.wait1sec = new WebDriverWait(driver, 1);

	}

	public MyDriver(WebDriver driver, int timeout) {
		this.driver = driver;
		this.defaultTimeout = timeout;
		this.wait1sec = new WebDriverWait(driver, 1);
	}

	public void open(String url) {
		driver.get(url);
	}

	public void close() {
		driver.close();
		currentWindow--;
		if (currentWindow >= 0)
			driver.switchTo().window(windowHandles.get(currentWindow));
	}

	public WebElement getElement(String uid, int timeout) {
		if (timeout == 0)
			timeout = defaultTimeout;
		try {
			Pattern pat = Pattern.compile("(&#(\\d+);)");
			Matcher m = pat.matcher(uid);
			while (m.find()) {
				char ch = (char) Integer.parseInt(m.group(2));
				uid = uid.replace(m.group(1), Character.toString(ch));
				m = pat.matcher(uid);
			}

			// switch window if needed
			pat = Pattern.compile("^((//BrowserApplication)?\\[(\\+|\\-|\\d+)\\])(.*)$");
			// ......................12--------------------2....3------------3...14--4
			m = pat.matcher(uid);

			if (m.find()) {
				uid = m.group(4);
				int cur = 0;
				String win = m.group(3);
				if (win.equals("+"))
					cur = currentWindow + 1;
				else if (win.equals("-"))
					cur = currentWindow - 1;
				else
					cur = Integer.parseInt(win) - 1;
				if (cur < 0)
					cur = 0;

				long t0 = System.nanoTime();
				Set<String> handles;
				while (true) { // waiting for i-th window appears
					handles = driver.getWindowHandles();
					if (handles != null && cur < handles.size())
						break;
					if ((System.nanoTime() - t0) / 1000000 > timeout)
						return null;
					Thread.sleep(200);
				}

				if (cur != currentWindow) {
					List<String> rms = new ArrayList<String>();
					// remove no-more-existing windows
					for (String x : windowHandles)
						if (!handles.contains(x))
							rms.add(x);
					for (String x : rms)
						windowHandles.remove(x);
					// add new opened windows
					for (String x : handles)
						if (!windowHandles.contains(x))
							windowHandles.add(x);
					currentWindow = cur;
					driver.switchTo().window(windowHandles.get(currentWindow));
				}
			} else if (currentWindow == -1) {
				windowHandles.add(driver.getWindowHandle());
				currentWindow = 0;
			}

			// switch iframe if needed
			List<WebElement[]> frames = null;
			if (uid.toLowerCase().matches("(//iframe//|iframe ).*")) {
				frames = new ArrayList<WebElement[]>();
				uid = uid.replaceAll("^((//)?iframe\\s*)", "");
				driver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);
				// wait.until seem doesn't work for xpath "//frame", so set the
				// driver timeout to 1000 ms
				AddToFrameList(frames, new WebElement[0]);
				driver.manage().timeouts().implicitlyWait(defaultTimeout, TimeUnit.MILLISECONDS);
				if (frames.size() == 0)
					frames = null;
			} else if ((m = Pattern.compile("^(//iframe(\\[([^\\]]*)\\]))([/ ]*.*)").matcher(uid)).find()) {
				// .............................1--------2---3------3----214-------4
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
				driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.MILLISECONDS);
				for (String ff : m.group(3).split(";", -1)) { // -1 in split
																// enable ending
																// empty string
					if (ff.trim().equals("/"))
						driver.switchTo().defaultContent();
					else {
						String xpath = (ff.trim().length() == 0) ? "//iframe" : ("//iframe[" + ff + "]");
						try {
							WebElement frame = driver.findElement(By.xpath(xpath));
							if (frame == null) {
								driver.manage().timeouts().implicitlyWait(defaultTimeout, TimeUnit.MILLISECONDS);
								return null;
							}
							driver.switchTo().frame(frame);
						} catch (Exception e) {
							driver.manage().timeouts().implicitlyWait(defaultTimeout, TimeUnit.MILLISECONDS);
							return null;
						}
					}
				}
				driver.manage().timeouts().implicitlyWait(defaultTimeout, TimeUnit.MILLISECONDS);
			}

			return WaitWebElement(uid, timeout, frames);
		} catch (Exception e) {
			return null;
		}
	}

	private String checkPathIndex(String uid) {
		Matcher match;
		Pattern p = Pattern.compile("(.+)(\\[\\d\\])(.*)");
		match = p.matcher(uid);
		while (match.matches()) {
			System.out.println("get in :" + uid);
			uid = String.format("(%s)%s%s", match.group(1), match.group(2), match.group(3));
			System.out.println("get out: " + uid);
			return uid;
		}
		// System.out.println("%%%%%%%%%%print path: "+uid);
		// if (uid.matches(".*[\\d].*")) {
		// System.out.println("yesyes");
		//
		//
		// return uid;
		// }
		return uid;
	}

	private void SwitchFrame(WebElement[] framePath) {
		try {
			driver.switchTo().defaultContent();
			for (WebElement f : framePath) {
				driver.switchTo().frame(f);
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	private void AddToFrameList(List<WebElement[]> frames, WebElement[] framePath) {
		int start = frames.size();
		int n = 0;
		SwitchFrame(framePath);
		List<WebElement> ff = driver.findElements(By.xpath("//iframe"));
		if (ff == null || ff.isEmpty())
			return;
		for (WebElement e : ff) {
			if (!e.isDisplayed())
				continue;
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
		if (driver == null)
			return null;
		try {
			if (by.equals("id"))
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.id(uid)));
			else if (by.equals("class"))
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.className(uid)));
			if (by.equals("name"))
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.name(uid)));
			if (by.equals("tag"))
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.tagName(uid)));
			if (by.equals("css"))
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(uid)));
			if (by.equals("link"))
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.linkText(uid)));
			if (by.equals("linktext"))
				return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.partialLinkText(uid)));
			if (by.equals("xpath"))
				uid = checkPathIndex(uid);
			return wait1sec.until(ExpectedConditions.presenceOfElementLocated(By.xpath(uid)));

		} catch (TimeoutException e) {
			;
		}
		return null;
	}

	static private Pattern ptnTextContents = Pattern.compile("\\[(.*)@(textContents|text)='([^']+)'(.*)\\]");
	// ...........................................................1--1.2-----------------2..3-----3.4--4
	static private Pattern ptnClass = Pattern
			.compile("\\[(.*)@class='([a-zA-Z0-9 _\\-\\.\\?\\*]*[\\?\\*][a-zA-Z0-9 _\\-\\.\\?\\*]*)'(.*)\\]");

	// ....................................................1--1........2------------------------------------------------------------2.3--3
	private WebElement WaitWebElement(String uid, int timeout, List<WebElement[]> frames) {
		if (driver == null)
			return null;
		Matcher m;

		if (uid.isEmpty())
			uid = "//body";
		String by = "xpath";
		if ((m = Pattern.compile("^((id|class|name|tag|css|link|linktext|xpath)=)(.*)").matcher(uid)).find()) {
			uid = m.group(3);
			by = m.group(2);
		} else {
			if (uid.matches("^(\\[(\\d+|\\+|\\-)\\])?\\s*//.*"))
				by = "xpath";
			else if (uid.matches("^(([\\.#:]?[\\w\\-]+(\\(\\d+\\))?)|(\\w+[\\.#:][\\w\\-]+))(\\s+|>|\\+).*"))
				by = "css";
			// .....................12.................3..........3.2.4....................415..........5
		}

		// Reform xpath with @text/textContent or @class
		m = ptnTextContents.matcher(uid);
		if (m.find()) {
			String m1 = m.group(1);
			String m3 = m.group(3).replace("\\r", "\r").replace("\\n", "\n");
			String m4 = m.group(4);
			if (m3.indexOf('|') >= 0) { // SLX@Dec29'15 ( "|" OR in text )
				String tmp = "";
				for (String mm3 : m3.split("|")) {
					if (tmp.length() > 0)
						tmp = tmp + " or ";
					if (mm3.endsWith("*"))
						tmp = tmp + "starts-with(normalize-space(.),'" + mm3.replace("*", "") + "')";
					else if (mm3.indexOf('*') >= 0)
						tmp = tmp + "contains(normalize-space(.),'" + mm3.replace("*", "") + "')";
					else
						tmp = tmp + "normalize-space(.)='" + mm3 + "'";
				}
				uid = uid.replace(m.group(0), "[" + m1 + tmp + m4 + "]");
			} else {
				m3 = m3.replace("\\*", "\0x1");
				String tmp = "";
				if (m3.indexOf('?') >= 0 || m3.indexOf('*') >= 0) {
					boolean first = true;
					for (String mm3 : m3.split("[\\?\\*]")) {
						if (mm3.length() >= 0) {
							if (first)
								tmp = "starts-with(normalize-space(.),'" + mm3 + "')";
							else {
								if (tmp.length() > 0)
									tmp += " and ";
								tmp += "contains(normalize-space(.),'" + mm3 + "')";
							}
						}
						first = false;
					}
				} else
					tmp = "normalize-space(.)='" + m3 + "'";
				uid = uid.replace(m.group(0), "[" + m1 + (tmp.replace((char) 1, '*')) + m4 + "]");
			}
		}
		m = ptnClass.matcher(uid);
		if (m.find()) {
			String m1 = m.group(1);
			String m2 = m.group(2);
			String m3 = m.group(3);
			String tmp = "";
			if (m2.indexOf('?') >= 0 || m2.indexOf('*') >= 0) {
				boolean first = true;
				for (String mm2 : m2.split("[\\?\\*]")) {
					if (mm2.length() >= 0) {
						if (first)
							tmp = "starts-with(@class,'" + mm2 + "')";
						else {
							if (tmp.length() > 0)
								tmp += " and ";
							tmp += "contains(@class,'" + mm2 + "')";
						}
					}
					first = false;
				}
				uid = uid.replace(m.group(0), "[" + m1 + (tmp.replace((char) 1, '*')) + m3 + "]");
			}
		}

		long startTime = System.nanoTime();
		while (true) {
			WebElement obj = null;
			try {
				obj = GetElementBy(uid, by);
				if (obj == null && frames != null) {
					for (WebElement[] x : frames) {
						SwitchFrame(x);
						obj = GetElementBy(uid, by);
						if (obj != null)
							break;
					}
				}
				if (obj != null)
					return obj;
			} catch (Exception ex) {
				// "Element is no longer attached to the DOM Selenium" with no
				// return
			}
			if ((System.nanoTime() - startTime) / 1000000 > timeout)
				break;

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		return null;
	}

}
