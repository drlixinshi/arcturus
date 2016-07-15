package com.morningstar.phoenix.tests.caqa.testproc.AWS;
public class KeepFirstWindowOnly {
 static public String xml = "<testproc package=\"AWS\" name=\"KeepFirstWindowOnly\">\n"
  +"<desc></desc>	<params>\n"
  +"</params>	<ss>\n"
  +"<s><l></l><c>VerifyElement(\"[3]\")</c><e></e><a>CONT</a><t>[100]GOTO no_w3</t><n></n></s>\n"
  +"<s><l></l><c>CloseWindow(\"[3]\")</c><e></e><a></a><t></t><n></n></s>\n"
  +"<s><l>no_w3</l><c>VerifyElement(\"[2]\")</c><e></e><a>CONT</a><t>[100]GOTO no_w2</t><n></n></s>\n"
  +"<s><l></l><c>CloseWindow(\"[2]\")</c><e></e><a></a><t></t><n></n></s>\n"
  +"<s><l>no_w2</l><c>VerifyElement(\"[1]\")</c><e></e><a>CONT</a><t>[100]CONT</t><n></n></s>\n"
  +"</ss>\n"
  +"	<owner>admin 2015.09.30</owner>	<modifier>admin 2015.09.30</modifier>\n"
  +"</testproc>";
}
