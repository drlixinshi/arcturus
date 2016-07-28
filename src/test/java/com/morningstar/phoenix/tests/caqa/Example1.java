package com.morningstar.phoenix.tests.caqa;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.morningstar.arcturus.Arcturus;
import com.morningstar.arcturus.TestResult;
import com.morningstar.automation.base.core.utils.Logger;

public class Example1 {
	private String currentPackage = this.getClass().getCanonicalName()
			.replaceAll("\\." + this.getClass().getSimpleName() + "$", "");
	private static final Logger LOGGER = Logger.getLogger(Example1.class);

	@Test
	public void Example_001(ITestContext context, Method method) {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("User", "Lixin.shi@morningstar.com");
		variables.put("Passwd", "rock1234");
		variables.put("Lang", "en");
		variables.put("UserName", "Lixin");
		Arcturus arcturus = new Arcturus(new FirefoxDriver(), variables, currentPackage, LOGGER);
		String tpName = "MStarCA.Home()";
		TestResult result = arcturus.runTestProc(method.getName(), tpName);
		Assert.assertEquals(result, TestResult.PASS);
	}

}
