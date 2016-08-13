package com.tests.caqa;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.arcturus.Arcturus;
import com.arcturus.Logger;
import com.arcturus.TestResult;

public class TcExample1 {
	private String currentPackage = this.getClass().getCanonicalName()
			.replaceAll("\\." + this.getClass().getSimpleName() + "$", "");
	private static final Logger LOGGER = Logger.getLogger(TcExample1.class);

	public static void main(String[] args) {
		new TcExample1().Example_001();
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
	}
}
