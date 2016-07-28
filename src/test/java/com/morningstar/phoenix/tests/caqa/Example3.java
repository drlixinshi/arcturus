package com.morningstar.phoenix.tests.caqa;

import java.lang.reflect.Method;

import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.morningstar.arcturus.TestResult;

public class Example3 {
	@Test
	public void Example_001(ITestContext context, Method method) {
		TestResult result = TestResult.PASS;
		SoftAssert soft = new SoftAssert();
		soft.assertEquals(result, TestResult.PASS);
		soft.assertAll();
	}
}
