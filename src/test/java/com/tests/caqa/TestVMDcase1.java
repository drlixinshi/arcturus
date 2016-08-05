package com.tests.caqa;

import java.util.HashMap;
import java.util.Map;

import com.arcturus.Arcturus;
import com.arcturus.TestResult;

public class TestVMDcase1 {
	public static void main(String[] args) {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("Lang", "fr");
		variables.put("user", "MSTR2");
		variables.put("pass", "nexa123");

		Arcturus arcturus = new Arcturus(variables, "com.morningstar.phoenix.tests.caqa");
		TestResult result = arcturus.runTestProc("TestVMDcase1", "Cobrands.VDM.MyAccounts_AutoComplete()");
		System.out.println(result.getResult());
	}
}
