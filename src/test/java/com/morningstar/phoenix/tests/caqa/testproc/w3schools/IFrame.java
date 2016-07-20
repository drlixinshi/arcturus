package com.morningstar.phoenix.tests.caqa.testproc.w3schools;

public class IFrame {
	static public String xml = "<testproc package=\"w3schools\" name=\"IFrame\">\n" + "<desc></desc>	<params>\n"
			+ "</params>	<ss>\n"
			+ "<s><l></l><c>Open(\"http://www.w3schools.com/tags/tag_iframe.asp\")</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>Click(\"//a[starts-with(normalize-space(.),'Try it Yourself')]\")</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>VerifyElement(\"[2]//div[@id='container']\")</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>SLEEP 1000</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>VerifyElement(\"//iframe//a[@class='w3schools-logo']\")</c><e></e><a></a><t></t><n></n></s>\n"
			+ "<s><l></l><c>VerifyContent(\"//div/p\")</c><e>The language for building web pages</e><a></a><t></t><n></n></s>\n"
			+ "</ss>\n" + "	<owner>admin 2016.07.18</owner>	<modifier>admin 2016.07.18</modifier>\n" + "</testproc>";
}
