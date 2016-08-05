package com.phoenix.tests.caqa.testproc.Cobrands.Disnat;
public class MyAccounts_AutoComplete {
 static public String xml = "<testproc package=\"Cobrands.Disnat\" name=\"MyAccounts_AutoComplete\">\n"
  +"<desc></desc>	<params>\n"
  +"<param name=\"Lang\" value=\"fr\"/>\n"
  +"</params>	<ss>\n"
  +"<s><l></l><c>CALL Cobrands.Disnat.Login()</c><e></e><a></a><t></t><n></n></s>\n"
  +"<s><l></l><c>CALL Cobrands.Component.AutoComplete({Lang})</c><e></e><a></a><t></t><n></n></s>\n"
  +"</ss>\n"
  +"	<owner>mengdi 2016.06.07</owner>	<modifier>mengdi 2016.06.07</modifier>\n"
  +"</testproc>";
}
