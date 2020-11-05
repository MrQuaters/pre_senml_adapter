package org.eclipse.kura.presenml;

import java.util.regex.Pattern;

public class PreSenmlPattern {
	public Integer pos;
	public Pattern pattern;
	public String senMlPattern;
	
	public PreSenmlPattern(String pattern, String senMlPattern, Integer pos) {
		this.pattern = Pattern.compile(pattern);
		this.pos = pos;
		this.senMlPattern = senMlPattern;
	}
}
