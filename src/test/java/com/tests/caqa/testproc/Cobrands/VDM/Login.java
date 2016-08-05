package com.tests.caqa.testproc.Cobrands.VDM;

public class Login {
	static public String xml = "<testproc package=\"Cobrands.VDM\" name=\"VMD_Login\">\n"
			+ "<desc>v2 created on 2016-06-06\n" + "</desc>	<params>\n" + "<param name=\"Lang\" value=\"fr\"/>\n"
			+ "<param name=\"user\" value=\"MSTR2\"/>\n" + "<param name=\"pass\" value=\"nexa123\"/>\n"
			+ "</params>	<ss>\n" + "<s><l></l><c>SET _L =</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>SET Entrer = Entrer</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>SET Acc\u00e9der = Acc\u00e9der</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l>#</l><c></c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>IF {Lang}==fr</c><e></e><a>GOTO n0</a><t></t><n></n></s>\n"
			+ "<s><l></l><c>SET _L = en</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>SET Entrer = Enter</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>SET Acc\u00e9der = Go</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l>n0</l><c></c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l>#</l><c>VerifyElement(\"//img[@src='/solution9-fs/images/logo-S9FS-{Lang}@2x.png']\")</c><e></e><a>CONT</a><t>[3000]GOTO no_win1</t><n></n></s>\n"
			+ "<s><l></l><c></c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>VerifyElement(\"[3]\")</c><e></e><a>CONT</a><t>[1000]GOTO no_win3</t><n></n></s>\n"
			+ "<s><l></l><c>CloseWindow(\"[3]\")</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l>no_win3</l><c>VerifyElement(\"[2]\")</c><e></e><a>CONT</a><t>[1000]GOTO no_win2</t><n></n></s>\n"
			+ "<s><l></l><c>CloseWindow(\"[2]\")</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l>no_win2</l><c>VerifyElement(\"[1]\")</c><e></e><a>GOTO no_win1</a><t>[1000]GOTO no_win1</t><n></n></s>\n"
			+ "<s><l></l><c>VerifyElement(\"//a[@id='btnQtr']\")</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>VerifyContent(\"//a[@id='btnLng']\")</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>IF {Lang}==fr AND {$1}==English</c><e></e><a>PASS</a><t>CONT</t><n></n></s>\n"
			+ "<s><l></l><c>IF {Lang}==en AND {$1}==Fran\u00e7ais</c><e></e><a>PASS</a><t>CONT</t><n></n></s>\n"
			+ "<s><l></l><c>Click(\"//a[@id='btnLng']\")</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>VerifyElement(\"//img[@src='/solution9-fs/images/logo-S9FS-{Lang}@2x.png']\")</c><e></e><a>CONT</a><t>[3000]GOTO no_win1</t><n></n></s>\n"
			+ "<s><l></l><c>RET PASS</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l>no_win1</l><c></c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c></c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>Open(\"https://www.vmdconseil.ca/{_L}\")</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>ClickIfExist(\"//A[@id='loginBtn']\")</c><e></e><a>CONT</a><t>[1000]CONT</t><n></n></s>\n"
			+ "<s><l></l><c>ClickIfExist(\"//button[@class='btn btn-primary btn-bold cadenas popover-login-open']\")</c><e></e><a></a><t>[1000]</t><n></n></s>\n"
			+ "<s><l></l><c>Click(\"//A[@textContents='{Entrer}']\")</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l>#</l><c>Input(\"//input[@id='searchterm']\",\"Christian\")</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>Input(\"//input[@id='j_username']\",\"{user}\")</c><e></e><a></a><t></t><n>input username</n></s>\n"
			+ "<s><l></l><c>Input(\"//input[@id='j_password']\",\"{pass}\")</c><e></e><a></a><t></t><n>input pasword</n></s>\n"
			+ "<s><l></l><c>Click(\"//button[@class='btn btn-primary btn-bold']\")</c><e></e><a></a><t></t><n></n></s>\n"
			+ "</ss>\n" + "	<owner>slx 2016.06.06</owner>	<modifier>mengdi 2016.06.10</modifier>\n" + "</testproc>";
}