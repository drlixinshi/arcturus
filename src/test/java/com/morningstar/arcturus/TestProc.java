package com.morningstar.arcturus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestProc {
	private List<Sentence> sent = new ArrayList<Sentence>();
	private Map<String, Integer> labelMap = new HashMap<String, Integer>();;
	private Map<String, Object> params = new HashMap<String, Object>();
	private String pkg;
	private String name;
	private int current;
	private String label;

	public TestProc(String xml) {
		Pattern hd = Pattern.compile("<testproc package=\\\"(\\w+)\\\" name=\\\"(\\w+)\\\".*>");
		Pattern pa = Pattern.compile("<param name=\\\"(\\w+)\\\" value=\\\"([^\\\"]*)\\\"/>");
		for (String line : xml.split("[\\r\\n]+")) {
			if (line.matches("<s>.*</s>")) {
				Sentence s = new Sentence(line);
				if (s.lab != null && !s.lab.isEmpty()) labelMap.put(s.lab, sent.size());
				sent.add(s);
				continue;
			}
			Matcher m = pa.matcher(line);
			if (m.find()) {
				params.put(m.group(1), m.group(2));
				continue;
			}
			m = hd.matcher(line);
			if (m.find()) {
				pkg = m.group(1);
				name = m.group(2);
				continue;
			}
		}
	}

	@Override
	public String toString() {
		return String.format("%s.%s, #param:%d, #sent:%d", pkg, name, params.size(), sent.size());
	}

	public void setDefaultParameters(Map<String, Object> variables) {
		for (String key : params.keySet())
			if (!variables.containsKey(key)) variables.put(key, params.get(key));
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPackage() {
		return pkg;
	}

	public Sentence getSentence() {
		if (sent == null || label == null && current == sent.size()) return null;
		if (label != null) {
			if (labelMap == null && !labelMap.containsKey(label)) return null;
			current = labelMap.get(label);
			label = null;
		}
		return sent.get(current++);
	}
}
