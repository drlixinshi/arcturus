package com.tests.caqa;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
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
		WebDriver driver = new FirefoxDriver();
		Arcturus arcturus = new Arcturus(driver, variables, currentPackage);
		Object[] params = new Object[] { "Example_002" };
		arcturus.setCallback(LOGGER, "printToFile", params);
		String tpName = "w3schools.IFrame()";
		TestResult result = arcturus.runTestProc(tpName);
		driver.quit();
		Assert.assertEquals(result, TestResult.PASS);
	}

}
