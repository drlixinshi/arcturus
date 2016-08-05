package com.tests.caqa.testproc.Cobrands.VDM;

public class MyAccounts_AutoComplete {
	static public String xml = "<testproc package=\"Cobrands.VDM\" name=\"MyAccounts_AutoComplete\">\n"
			+ "<desc>v2 created on 2016-06-07\n" + "</desc>	<params>\n" + "<param name=\"Lang\" value=\"fr\"/>\n"
			+ "</params>	<ss>\n"
			+ "<s><l></l><c>CALL Login({Lang},MSTR2,nexa123)</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>CALL Cobrands.Component.AutoComplete({Lang})</c><e></e><a></a><t></t><n></n></s>\n"
			+ "</ss>\n" + "	<owner>mengdi 2016.06.07</owner>	<modifier>mengdi 2016.06.07</modifier>\n"
			+ "</testproc>";
}
