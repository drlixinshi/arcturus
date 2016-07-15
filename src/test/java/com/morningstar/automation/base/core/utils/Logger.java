package com.morningstar.automation.base.core.utils;

public class Logger {

	private Object obj = null;

	public Logger(Object obj) {
		this.setObj(obj);
	}

	public void info(Object msg) {
	}

	public void error(Object msg) {
	}

	public void warn(Object msg) {
	}

	public static Logger getLogger(Object obj) {
		return new Logger(obj);
	}

	public void printToFile(String testCaseId, String msg) {
		System.out.println(testCaseId + "\t" + msg);
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}
}
