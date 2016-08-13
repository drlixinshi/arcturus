package com.arcturus;

import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Arcturus {

	private String tpPkg;
	private Map<String, Object> variables;
	private MyDriver myDriver;

	private Object callbackObject = null;
	private String callbackMethod;
	private Object[] callbackParams;

	public Arcturus(WebDriver driver, Map<String, Object> variables, String pkg) {
		this.myDriver = new MyDriver(driver);
		this.variables = variables;
		this.tpPkg = pkg;
	}

	private void callbackLogger(String msg) {
		if (callbackObject == null) return;
		try {
			java.lang.reflect.Method method;
			if (callbackParams == null || callbackParams.length == 0) {
				method = callbackObject.getClass().getDeclaredMethod(callbackMethod, String.class);
				method.invoke(callbackObject, msg);
			}
			else if (callbackParams.length == 1) {
				method = callbackObject.getClass().getDeclaredMethod(callbackMethod, callbackParams[0].getClass(),
						String.class);
				method.invoke(callbackObject, callbackParams[0], msg);
			}
			else if (callbackParams.length >= 2) {
				method = callbackObject.getClass().getDeclaredMethod(callbackMethod, callbackParams[0].getClass(),
						callbackParams[1].getClass(), String.class);
				method.invoke(callbackObject, callbackParams[0], callbackParams[1], msg);
			}
		}
		catch (Exception ex) {

		}
	}

	public void setCallback(Object callbackObject, String callbackMethod, Object[] callbackParams) {
		this.callbackObject = callbackObject;
		this.callbackMethod = callbackMethod;
		this.callbackParams = callbackParams;
	}

	@SuppressWarnings("unused")
	private TestResult cmdSET(Sentence sent) {
		variables.put(sent.cmdTarget, sent.cmdValue);
		callbackLogger(String.format("\t%s <- [%s]", sent.cmdTarget, sent.cmdValue));
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
		try {
			// // take off the brackets
			// sent.cmdTarget = sent.cmdTarget.replaceAll("^\\(|\\)$", "");
			// // System.out.println("!!!!!!!!!!!!" + sent.cmdTarget);
			WebElement e = myDriver.getElement(sent.cmdTarget, sent.tmout);
			e.clear();
			// WebElement element = findByXPath(sent.cmdTarget);

			if (sent.cmdValue.indexOf("{") >= 0) {
				sent.cmdValue = replaceVariable(sent.cmdValue);
				sent.cmdValue = sent.cmdValue.replace("\"", "");
				System.out.println("hereherehere" + sent.cmdValue);
			}

			for (int i = 0; i < sent.cmdValue.length(); i++) {
				char k = sent.cmdValue.charAt(i);
				String m = Character.toString(k);
				System.out.println("Print charac: " + m);
				e.sendKeys(m);
				Thread.sleep(100);
			}

		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return TestResult.FAIL;
		}
		return TestResult.CONT;
	}

	@SuppressWarnings("unused")
	private TestResult cmdClickIfExist(Sentence sent) {
		WebElement element = myDriver.getElement(sent.cmdTarget, sent.tmout);
		if (element == null) {
			return TestResult.theTestResult(sent.act2);
		}
		else {
			element.click();
			return TestResult.theTestResult(sent.act1);
		}
	}

	@SuppressWarnings("unused")
	private TestResult cmdINC(Sentence sent) {
		if (variables.containsKey(sent.cmdTarget)) {
			int j = Integer.parseInt(variables.get(sent.cmdTarget).toString());
			variables.replace(sent.cmdTarget.trim(), j + 1);
			System.out.println("@@@@: " + variables.get(sent.cmdTarget));
			return TestResult.CONT;
		}
		else return TestResult.FAIL;
	}

	@SuppressWarnings("unused")
	private TestResult cmdClearSendKeys(Sentence sent) {
		WebElement element = myDriver.getElement(sent.cmdTarget, sent.tmout);
		if (element == null) return TestResult.theTestResult(sent.act2);
		element.clear();
		element.sendKeys(sent.cmdValue);
		return TestResult.theTestResult(sent.act1);
	}

	@SuppressWarnings("unused")
	private TestResult cmdInput(Sentence sent) {
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
		callbackLogger("\t$1 <- " + text);
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
		try {
			boolean x = false;
			if (sent.cmdTarget.contains(" OR ")) {
				x = false;
				for (String str : sent.cmdTarget.split(" OR "))
					x |= checkBoolean(str);
			}
			else if (sent.cmdTarget.contains(" AND ")) {
				x = true;
				for (String str : sent.cmdTarget.split(" AND "))
					x &= checkBoolean(str);
			}
			return TestResult.theTestResult(x ? sent.act2 : sent.act1);
		}
		catch (Exception e) {
			return TestResult.BLOCK;
		}
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

	public TestResult runTestProc(String tpName) {
		TestResult result = TestResult.BLOCK;
		callbackLogger("== Start " + tpName);
		Pattern pat = Pattern.compile("^([\\w\\.]+)\\s*\\((.*)\\)\\s*$");
		// ..............................1---------1.......2--2........
		Matcher m = pat.matcher(tpName);
		if (!m.find()) return result;
		String xml;
		tpName = m.group(1);
		try {
			xml = (String) Class.forName(tpPkg + ".testproc." + m.group(1)).getDeclaredField("xml").get(null);
		}
		catch (IllegalArgumentException e) {
			callbackLogger("Exception: " + e.getMessage());
			return result;
		}
		catch (IllegalAccessException e) {
			callbackLogger("Exception: " + e.getMessage());
			return result;
		}
		catch (NoSuchFieldException e) {
			callbackLogger("Exception: " + e.getMessage());
			return result;
		}
		catch (SecurityException e) {
			callbackLogger("Exception: " + e.getMessage());
			return result;
		}
		catch (ClassNotFoundException e) {
			callbackLogger("Exception: " + e.getMessage());
			return result;
		}

		TestProc tp = new TestProc(xml);
		Sentence s;
		while ((s = tp.getSentence()) != null) {
			long t0 = System.nanoTime();
			callbackLogger(String.format("%s\t%s", tp.getStepInfo(), s.note));
			if (s.cmd == null) continue;
			s = s.clone();
			if (s.cmdTarget != null) s.cmdTarget = replaceVariable(s.cmdTarget);
			if (s.cmdValue != null) s.cmdValue = replaceVariable(s.cmdValue);
			if (s.expr != null) s.expr = replaceVariable(s.expr);
			if (s.note != null) s.note = replaceVariable(s.note);
			// System.out.println(String.format("===>%s\t%s\t%s", s.cmd,
			// s.cmdTarget, s.cmdValue));
			if (s.cmd == null || s.cmd.length() == 0) result = TestResult.CONT;
			else if (s.cmd.equals("CALL")) {
				// HashSet<String> set = getVarSet();
				result = runTestProc(
						s.cmdTarget.matches("\\w+\\.\\w+.*") ? s.cmdTarget : (tp.getPackage() + "." + s.cmdTarget));
				// resetVars(set); // Variables defined in sub-testproc will be
				// removed
				if (result.equals(TestResult.PASS)) result = TestResult.CONT;
			}
			else {
				java.lang.reflect.Method method;
				try {
					method = this.getClass().getDeclaredMethod("cmd" + s.cmd, Sentence.class);
					method.setAccessible(true);
					result = (TestResult) method.invoke(this, s);
				}
				catch (NoSuchMethodException e) {
					callbackLogger("Exception: " + e.getMessage());
					result = TestResult.BLOCK;
				}
				catch (SecurityException e) {
					callbackLogger("Exception: " + e.getMessage());
					result = TestResult.BLOCK;
				}
				catch (Exception e) {
					callbackLogger("Exception: " + e.getMessage());
					result = TestResult.BLOCK;
				}
			}

			long t1 = System.nanoTime();
			callbackLogger(t1 - t0 > 1000000
					? String.format("%s\t=> %s (%.2fs)", s, result.getResult(), (t1 - t0) / 1000000000.0)
					: String.format("%s\t=> %s", s, result.getResult()));

			if (result.isTeminate()) break;
			if (result.isGoto()) {
				tp.setLabel(result.getLabel());
				continue;
			}
		}
		if (result.equals(TestResult.CONT)) result = TestResult.PASS;
		callbackLogger("== End of " + m.group(1));
		return result;
	}

	private boolean checkBoolean(String str) {
		str = replaceVariable(str);
		str = replaceBoolean(str);
		if (str.equals("false")) return false;
		if (str.equals("true")) return true;

		Pattern p = Pattern.compile("([^=<>!]+)(==|=|!=|>|<|>=|<=|<>)(.+)");
		//------ --------------------1--------12-----------------2-3-3
		Matcher match = p.matcher(str);
		if (!match.find()) return false;
		String a = match.group(1).trim();
		String op = match.group(2);
		String b = match.group(3).trim();

		if (op.equals("==") || op.equals("=")) return a.equals(b);
		if (op.equals("<>") || op.equals("!=")) return !a.equals(b);
		try {
			float f1 = Float.parseFloat(a);
			float f2 = Float.parseFloat(b);
			if (op.equals(">")) return f1 > f2;
			if (op.equals(">=")) return f1 >= f2;
			if (op.equals("<")) return f1 < f2;
			if (op.equals("<=")) return f1 <= f2;
		}
		catch (Exception ex) {
			;
		}
		return false;
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

	private String replaceBoolean(String str) {
		Pattern p2 = Pattern.compile("\\$(NotDef|Def)\\((.+)\\)");
		Matcher match2 = p2.matcher(str);
		if (match2.find()) {
			if (match2.group(1).equals("Def")) {
				if (variables.containsKey(match2.group(2))) {
					return "true";
				}
				;
				return "false";
			}
			else {
				if (variables.containsKey(match2.group(2))) {
					return "false";
				}
				;
				return "true";
			}
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
