package com.morningstar.phoenix.tests.caqa.testproc.MStarCA;

public class Login {
	static public String xml = "<testproc package=\"MStarCA\" name=\"Login\">\n"
			+ "<desc>v5 created on 2016-05-12 - support french\n"
			+ "v4 created on 2015-12-31 temp fixed  id=\"ctl00_MainContent_email_textbox\" on cartmaprdwb6002 and id=\"MainContent_email_textbox\" on cartmaprdwb6003\n"
			+ "v3 created on 2015-11-24\n" + "v2 created on 2015-07-15\n" + "</desc>	<params>\n"
			+ "<param name=\"User\" value=\"lixin.shi@morningstar.com\"/>\n"
			+ "<param name=\"Passwd\" value=\"{lixin.ca.pwd}\"/>\n" + "<param name=\"UserName\" value=\"Lixin\"/>\n"
			+ "<param name=\"Lang\" value=\"en\"/>\n" + "</params>	<ss>\n"
			+ "<s><l></l><c>CALL AWS.KeepFirstWindowOnly()</c><e></e><a></a><t></t><n>Oct14'15</n></s>\n"
			+ "<s><l></l><c>SET Welcome = Welcome</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>IF {Lang}==en</c><e></e><a>GOTO nt0</a><t></t><n></n></s>\n"
			+ "<s><l></l><c>SET Welcome = Bienvenue</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l>nt0</l><c></c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>SET url = members-qa</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>IF {TestEnv}==QA</c><e></e><a>GOTO nt1</a><t></t><n></n></s>\n"
			+ "<s><l></l><c>SET url = members-uat</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>IF {TestEnv}==STG OR {TestEnv}==Stage</c><e></e><a>GOTO nt1</a><t></t><n></n></s>\n"
			+ "<s><l></l><c>SET url = members</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l>nt1</l><c>VerifyContent(\"css=div.headerwrap&gt;div.h_Logo&gt;div&gt;div.greeter\")</c><e>{Welcome} {UserName}!</e><a>PASS</a><t>[1000]CONT</t><n>&lt;Login&gt;</n></s>\n"
			+ "<s><l></l><c>Open(\"http://{url}.morningstar.ca/login.aspx?culture={Lang}-CA\")</c><e></e><a></a><t></t><n>Open URL</n></s>\n"
			+ "<s><l></l><c>ClearSendKeys(\"//INPUT[@id='ctl00_MainContent_email_textbox' or @id='MainContent_email_textbox']\",\"{User}\")</c><e></e><a></a><t></t><n>Enter Email Address</n></s>\n"
			+ "<s><l></l><c>ClearSendKeys(\"//INPUT[@id='ctl00_MainContent_pwd_textbox' or @id='MainContent_pwd_textbox']\",\"{Passwd}\")</c><e></e><a></a><t></t><n>Enter Password</n></s>\n"
			+ "<s><l></l><c>PushButton(\"//INPUT[@id='ctl00_MainContent_go_button' or id='MainContent_go_button']\")</c><e></e><a></a><t></t><n>Click on Log in Button</n></s>\n"
			+ "<s><l>#</l><c>VerifyContent(\"css=div.headerwrap&gt;div.h_Logo&gt;div&gt;div.greeter\")</c><e>(Welcome|Bienvenue) {UserName}!</e><a></a><t></t><n>Verify Greet Msg</n></s>\n"
			+ "<s><l>#</l><c></c><e></e><a></a><t></t><n></n></s>\n" + "</ss>\n"
			+ "	<owner>mengdi 2016.05.12</owner>	<modifier>mengdi 2016.05.12</modifier>\n" + "</testproc>";
}
