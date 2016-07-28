package com.morningstar.arcturus;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.morningstar.automation.base.core.utils.Logger;

public class Arcturus {

	Logger LOGGER;
	String pkg;
	Map<String, Object> variables;
	String methodName;
	MyDriver myDriver;

	public Arcturus(WebDriver driver, Map<String, Object> variables, String pkg, Logger LOGGER) {
		this.myDriver = new MyDriver(driver);
		this.variables = variables;
		this.LOGGER = LOGGER;
		this.pkg = pkg;
	}

	@SuppressWarnings("unused")
	private TestResult cmdSET(Sentence sent) {
		variables.put(sent.cmdTarget, sent.cmdValue);
		LOGGER.printToFile(methodName, String.format("\t%s <- [%s]", sent.cmdTarget, sent.cmdValue));
		return TestResult.CONT;
	}

	@SuppressWarnings("unused")
	private TestResult cmdOpen(Sentence sent) {
		myDriver.open(sent.cmdTarget);
		return TestResult.CONT;
	}

	@SuppressWarnings("unused")
	private TestResult cmdCloseWindow(Sentence sent) {
		WebElement element = myDriver.getElement(sent.cmdTarget, sent.tmout);
		myDriver.close();
		return TestResult.CONT;
	}

	@SuppressWarnings("unused")
	private TestResult cmdClearTypeKeys(Sentence sent) {
		WebElement element = myDriver.getElement(sent.cmdTarget, sent.tmout);
		if (element == null) return TestResult.theTestResult(sent.act2);
		element.sendKeys(sent.cmdValue);
		return TestResult.theTestResult(sent.act1);
	}

	@SuppressWarnings("unused")
	private TestResult cmdClearSendKeys(Sentence sent) {
		WebElement element = myDriver.getElement(sent.cmdTarget, sent.tmout);
		if (element == null) return TestResult.theTestResult(sent.act2);
		element.sendKeys(sent.cmdValue);
		return TestResult.theTestResult(sent.act1);
	}

	@SuppressWarnings("unused")
	private TestResult cmdClick(Sentence sent) {
		WebElement element = myDriver.getElement(sent.cmdTarget, sent.tmout);
		if (element == null) return TestResult.theTestResult(sent.act2);
		element.click();
		return TestResult.theTestResult(sent.act1);
	}

	@SuppressWarnings("unused")
	private TestResult cmdVerifyContent(Sentence sent) {
		WebElement element = myDriver.getElement(sent.cmdTarget, sent.tmout);
		if (element == null) return TestResult.theTestResult(sent.act2);
		String text = element.getText();
		LOGGER.printToFile(methodName, "\t$1 <- " + text);
		try {
			if (sent.expr == null || sent.expr.isEmpty() || Pattern.compile(sent.expr).matcher(text).find())
				return TestResult.theTestResult(sent.act1);
		}
		catch (Exception e) {
			System.out.println("** Ex:" + e.getMessage());
			return TestResult.BLOCK;
		}
		return TestResult.theTestResult(sent.act2);
	}

	@SuppressWarnings("unused")
	private TestResult cmdVerifyElement(Sentence sent) {
		WebElement element = myDriver.getElement(sent.cmdTarget, sent.tmout);
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
		this.methodName = methodName;
		TestResult result = TestResult.BLOCK;
		LOGGER.printToFile(methodName, "== Start " + tpName);
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
			long t0 = System.nanoTime();
			LOGGER.printToFile(methodName, String.format("%s\t%s", tp.getStepInfo(), s.note));
			if (s.cmd == null) continue;
			s = s.clone();
			if (s.cmdTarget != null) s.cmdTarget = replaceVariable(s.cmdTarget);
			if (s.cmdValue != null) s.cmdValue = replaceVariable(s.cmdValue);
			if (s.expr != null) s.expr = replaceVariable(s.expr);
			if (s.note != null) s.note = replaceVariable(s.note);
			// System.out.println(String.format("===>%s\t%s\t%s", s.cmd, s.cmdTarget, s.cmdValue));
			if (s.cmd.equals("CALL")) {
				// HashSet<String> set = getVarSet();
				result = runTestProc(methodName,
						s.cmdTarget.matches("\\w+\\.\\w+.*") ? s.cmdTarget : (tp.getPackage() + "." + s.cmdTarget));
				// resetVars(set); // Variables defined in sub-testproc will be removed
				if (result.equals(TestResult.PASS)) result = TestResult.CONT;
			}
			else if (s.cmd == null || s.cmd.length() == 0) result = TestResult.CONT;
			else {
				try {
					java.lang.reflect.Method method = this.getClass().getDeclaredMethod("cmd" + s.cmd, Sentence.class);
					method.setAccessible(true);
					result = (TestResult) method.invoke(this, s);
				}
				catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					LOGGER.printToFile(methodName, "Exception: " + e.getMessage());
					result = TestResult.BLOCK;
				}
			}
			long t1 = System.nanoTime();
			LOGGER.printToFile(methodName,
					t1 - t0 > 1000000
							? String.format("%s\t=> %s (%.2fs)", s, result.getResult(), (t1 - t0) / 1000000000.0)
							: String.format("%s\t=> %s", s, result.getResult()));

			if (result.isTeminate()) break;
			if (result.isGoto()) {
				tp.setLabel(result.getLabel());
				continue;
			}
		}
		if (result.equals(TestResult.CONT)) result = TestResult.PASS;
		LOGGER.printToFile(methodName, "== End of " + m.group(1));
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
			Thread.sleep(millis);
		}
		catch (InterruptedException e) {
			;
		}
	}

}
