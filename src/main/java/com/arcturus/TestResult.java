package com.arcturus;

public class TestResult {
	public static final String STR_PASS = "PASS";
	public static final String STR_FAIL = "FAIL";
	public static final String STR_BLOCK = "BLOCK";
	public static final String STR_CONT = "CONT";
	public static final String STR_OTHER = "OTHER";
	public static final String STR_GOTO_ = "GOTO ";
	private String result;
	private String label;
	private String note;

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getResult() {
		return result;
	}

	public String getLabel() {
		return label;
	}

	public static TestResult theTestResult(String str) {
		if (str == null || str.trim().length() == 0 || str.equals(STR_CONT)) return CONT;
		else if (str.equals(STR_PASS)) return PASS;
		else if (str.equals(STR_FAIL)) return FAIL;
		else if (str.equals(STR_BLOCK)) return BLOCK;
		else return new TestResult(str);
	}

	private TestResult(String str) {
		if (str == null || str.trim().length() == 0 || str.equals(STR_CONT)) result = STR_CONT;
		else if (str.startsWith(STR_GOTO_)) {
			result = STR_GOTO_;
			label = str.substring(5).trim();
		}
		else if (str.equals(STR_PASS) || str.equals(STR_FAIL) || str.equals(STR_BLOCK)) result = str;
		else result = STR_OTHER;
	}

	public boolean isTeminate() {
		return result.equals(STR_PASS) || result.equals(STR_FAIL) || result.equals(STR_BLOCK);
	}

	public boolean isGoto() {
		return result.equals(STR_GOTO_);
	}

	public static TestResult PASS = new TestResult(STR_PASS);
	public static TestResult FAIL = new TestResult(STR_FAIL);
	public static TestResult BLOCK = new TestResult(STR_BLOCK);
	public static TestResult CONT = new TestResult(STR_CONT);
}
