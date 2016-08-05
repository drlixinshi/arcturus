package com.tests.caqa;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.arcturus.Arcturus;
import com.arcturus.TestResult;
import com.tests.Logger;

import junit.framework.Assert;

public class TestMyAccounts {
	private String currentPackage = this.getClass().getCanonicalName()
			.replaceAll("\\." + this.getClass().getSimpleName() + "$", "");
	private static final Logger LOGGER = Logger.getLogger(TestCaseDisnatLogin.class);

	public static void main(String[] args) {
		new TestMyAccounts().Example_003();
	}

	private void Example_003() {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("Lang", "fr");
		variables.put("pass", "12345b");
		variables.put("user", "MSTR1");

		WebDriver driver = new FirefoxDriver();
		Arcturus arcturus = new Arcturus(driver, variables, currentPackage);
		Object[] params = new Object[] { "Example_002" };
		arcturus.setCallback(LOGGER, "printToFile", params);
		String tpName = "Cobrands.Disnat.MyAccounts_AutoComplete()";
		TestResult result = arcturus.runTestProc(tpName);
		driver.quit();
		Assert.assertEquals(result, TestResult.PASS);
	}
}
