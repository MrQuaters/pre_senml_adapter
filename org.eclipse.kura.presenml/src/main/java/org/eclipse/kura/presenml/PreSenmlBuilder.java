package org.eclipse.kura.presenml;

import java.util.List;
import java.util.regex.Pattern;
import java.util.ArrayList;

public class PreSenmlBuilder {
	
	private Boolean unitSet, timeSet;
	private Boolean greedMode;
	private List<PreSenmlPattern> ls;
	private List<Pattern> ignoreList;
	private List<Pattern> setDefaultLists;

	private static String NAME_PATTERN = "sn:m:%s";
	private static String UNITS_PATTERN = "sn:u:%s";
	private static String TIME_PATTERN = "sn:t:%s";

	public static PreSenmlBuilder create(String nameR) {
		return PreSenmlBuilder.createEmpty()
				.addCustomPattern(nameR, PreSenmlConstants.TAKE_FROM_VALUE, NAME_PATTERN);
	}
	
	public static PreSenmlBuilder createEmpty() {
		PreSenmlBuilder a = new PreSenmlBuilder();
		a.ls = new ArrayList<PreSenmlPattern>();
		a.setDefaultLists = new ArrayList<Pattern>();
		a.ignoreList = new ArrayList<Pattern>();
		a.unitSet = a.timeSet = false;
		a.greedMode = false;
		return a;
	}
	
	public PreSenmlBuilder addUnitReg(String unitR, Integer pos) {
		if (unitSet)
			return this;
		unitSet = true;
		return addCustomPattern(unitR, pos, UNITS_PATTERN);
	}
	
	public PreSenmlBuilder beGreed() {
		this.greedMode = true;
		return this;
	}
	
	public PreSenmlBuilder addTimeReg(String timeR) {
		if (timeSet)
			return this;
		timeSet = true;
		return addCustomPattern(timeR, PreSenmlConstants.TAKE_FROM_VALUE, TIME_PATTERN);
	}
	
	public PreSenmlBuilder addCustomPattern(String cusR, Integer pos, String pattern) {
		ls.add(new PreSenmlPattern(cusR, pattern, pos));
		return this;
	}
	

	public PreSenmlBuilder addIgnoreRecord(String ignoreR) {
		ignoreList.add(Pattern.compile(ignoreR));
		return this;
	}
	
	public PreSenmlBuilder addNoRefactorRecord(String noRefR) {
		setDefaultLists.add(Pattern.compile(noRefR));
		return this;
	}
	
	public PreSenml build() {
		return new PreSenml(ls, ignoreList, setDefaultLists, greedMode);
	}
}
