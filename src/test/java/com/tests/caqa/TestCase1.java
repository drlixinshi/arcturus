package com.phoenix.tests.caqa;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.arcturus.Arcturus;
import com.arcturus.TestResult;
import com.tests.Logger;

import junit.framework.Assert;

public class TestCase1 {
	private String currentPackage = this.getClass().getCanonicalName()
			.replaceAll("\\." + this.getClass().getSimpleName() + "$", "");
	private static final Logger LOGGER = Logger.getLogger(TestCase1.class);

	public static void main(String[] args) {
		new TestCase1().Example_001();
	}

	private void Example_001() {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("User", "Lixin.shi@morningstar.com");
		variables.put("Passwd", "rock1234");
		variables.put("Lang", "en");
		variables.put("UserName", "Lixin");
		WebDriver driver = new FirefoxDriver();
		Arcturus arcturus = new Arcturus(driver, variables, currentPackage);
		Object[] params = new Object[] { "Example_001" };
		arcturus.setCallback(LOGGER, "printToFile", params);
		String tpName = "MStarCA.Home()";
		TestResult result = arcturus.runTestProc(tpName);
		driver.quit();
		Assert.assertEquals(result, TestResult.PASS);
	}
}

