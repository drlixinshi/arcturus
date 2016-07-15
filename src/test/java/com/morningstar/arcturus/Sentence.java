package com.morningstar.arcturus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sentence {
	public String lab;
	public String cmd;
	public String expr;
	public String act1;
	public String act2;
	public int tmout;
	public String note;
	public String cmdTarget;
	public String cmdValue;

	private static Pattern patSent = Pattern
			.compile("<s>(<l>(.*)</l>)?(<c>(.*)</c>)?(<e>(.*)</e>)?(<a>(.*)</a>)?(<t>(.*)</t>)?(<n>(.*)</n>)?</s>");
	// .................
	// 1...2--2....1.3...4--4....3.5...6--6....5.7...8--8....7.9...0--0....9.1...2--2....1
	private static Pattern patCmd = Pattern.compile(
			"^(\\w+)(\\s*\\(\\s*(([^\\\"]*)|(\\\"([^\\\"]+)\\\"(\\s*,\\s*\\\"([^\\\"]+)\\\")?))\\s*\\)|\\s+([\\S]+.*))\\s*$");
	// .......1----12...........34--------4.5....6--------6....7.............8--------8....7.53............9------92
	private static Pattern patSet = Pattern.compile("^(ARRAY)?\\s*([^=\\s]+)\\s*=\\s*(.*)$");

	// ...............................................1-----1.....2-----2.........3--3
	public Sentence(String line) {
		Matcher m = patSent.matcher(line);
		if (!m.find()) return;
		lab = m.group(2);
		cmd = m.group(4);
		expr = m.group(6);
		act1 = m.group(8);
		act2 = m.group(10);
		note = m.group(12);
		if (act2 != null && act2.matches("\\[\\d+\\].*")) {
			int i = act2.indexOf(']');
			tmout = Integer.parseInt(act2.substring(1, i));
			act2 = act2.substring(i + 1).trim();
		}
		if ((act1 == null || act1.isEmpty()) && (act2 == null || act2.isEmpty())) act2 = TestResult.STR_FAIL;
		if (cmd == null) return;
		m = patCmd.matcher(cmd);
		if (m.find()) {
			cmd = m.group(1);
			cmdTarget = m.group(4);
			if (cmdTarget == null) cmdTarget = m.group(6);
			if (cmdTarget == null) cmdTarget = m.group(9);
			cmdValue = m.group(8);
		}
		if (cmd.equals("SET")) {
			m = patSet.matcher(cmdTarget);
			if (m.find()) {
				cmdTarget = m.group(1) == null ? m.group(2) : (m.group(1) + " " + m.group(2));
				cmdValue = m.group(3);
			}
		}
	}

	@Override
	public String toString() {
		return String.format("%s\t<%s> %s|%s %s 1:%s 2:%s o:%d\t%s", lab == null ? "" : (lab + ":"), cmd, cmdTarget,
				cmdValue, expr == null ? "" : ("[" + expr + "]"), act1, act2, tmout,
				note == null ? "" : ("(" + note + ")"));
	}
}
