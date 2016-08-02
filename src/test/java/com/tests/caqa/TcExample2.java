package com.tests.caqa;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.firefox.FirefoxDriver;

import com.arcturus.Arcturus;
import com.arcturus.TestResult;
import com.tests.Logger;

import junit.framework.Assert;

public class TcExample2 {
	private String currentPackage = this.getClass().getCanonicalName()
			.replaceAll("\\." + this.getClass().getSimpleName() + "$", "");
	private static final Logger LOGGER = Logger.getLogger(TcExample2.class);

	public static void main(String[] args) {
		new TcExample2().Example_002();
	}

	private void Example_002() {
		Map<String, Object> variables = new HashMap<String, Object>();
		Arcturus arcturus = new Arcturus(new FirefoxDriver(), variables, currentPackage);
		Object[] params = new Object[] { "Example_002" };
		arcturus.setCallback(LOGGER, "printToFile", params);
		String tpName = "w3schools.IFrame()";
		TestResult result = arcturus.runTestProc(tpName);
		Assert.assertEquals(result, TestResult.PASS);
	}

}
