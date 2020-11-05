package org.eclipse.kura.presenml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.kura.type.TypedValue;
import org.eclipse.kura.type.TypedValues;
import org.eclipse.kura.wire.WireEnvelope;
import org.eclipse.kura.wire.WireRecord;


public class PreSenml {
	private List<PreSenmlPattern> patternList;
	private List<Pattern> ignoreList;
	private List<Pattern> setDefaultList;
	private Boolean greedMode;
	
	public PreSenml(List<PreSenmlPattern> pls, List<Pattern> igl, List<Pattern> sdl, Boolean grm) {
		patternList = pls;
		ignoreList = igl;
		setDefaultList = sdl;
		greedMode = grm;
	}

	private Boolean inIgnore(String key) {
		for (Pattern pat : ignoreList) {
			if(pat.matcher(key).matches())
				return true;
		}
		return false;
	}
	
	private Boolean inSetDefault(String key) {
		for (Pattern pat : setDefaultList) {
			if(pat.matcher(key).matches())
				return true;
		}
		return false;
	}
	
	//for less alocation made
	private void rerfactorOneOptimised(String key, Object value, Map<String, Object> ls) {
		
		if (inSetDefault(key)) {
			ls.put(key, value);
			return;
		}
		
		Boolean used = false;
		
		for (PreSenmlPattern a : patternList) {
			Matcher ps = a.pattern.matcher(key);
			
			
			if(!ps.matches())
				continue;
			
			try {
				Integer groupTake = 1;
				
				if (a.pos != PreSenmlConstants.TAKE_FROM_VALUE) {
					groupTake = (a.pos == 2) ? 1 : 2;
					value = (Object) ps.group(a.pos);
				}
				
				String keyName = ps.group(groupTake);
				if (inIgnore(keyName))
					continue;
				
				ls.put(String.format(a.senMlPattern, keyName), value);
				used = true;
				
			}catch (Exception e){
				e.printStackTrace();
				continue;
			}
		}
		
		if (greedMode && !used)
			ls.put(key, value);
		
	}
	
	//for compatibility
	public Map<String, Object> refactorOne(String key, Object value) {
		Map<String, Object> ls = new HashMap<String, Object>();
		rerfactorOneOptimised(key, value, ls);
		return ls;
	}
	
	public WireEnvelope changeBody(WireEnvelope body) {
		List<WireRecord> records = body.getRecords(); 
		List<WireRecord> refactoredRecords = new ArrayList<WireRecord>(); 
		
		for (WireRecord record : records) {
			
			Map<String, TypedValue<?>> prop = record.getProperties();
			Map<String, TypedValue<?>> refactoredProps = new HashMap<String, TypedValue<?>>();
			
			Map<String, Object> rf = new HashMap<String, Object>();
			
			//for each property in Record
			for (Map.Entry<String, TypedValue<?>> entry : prop.entrySet()) {
				
				rerfactorOneOptimised(entry.getKey(), entry.getValue(), rf);
				
			}
			
			for(Map.Entry<String, Object> refactorEntry : rf.entrySet()) {
				//pushing all refactored properties into refactoredProps map
				TypedValue<?> tv;
				//we put as value TypedValue<> but refactorOne can return String as value, bcs it can take data from key if it setted by user
				//so we should check what type we get and resolve it to TypedValue type
				if (refactorEntry.getValue() instanceof  TypedValue<?>)
					tv = (TypedValue<?>) refactorEntry.getValue();
				else
					tv = TypedValues.newTypedValue(refactorEntry.getValue());
				
				refactoredProps.put(refactorEntry.getKey(), tv);
			}
			
			refactoredRecords.add(new WireRecord(refactoredProps));	
			
		}
		
		return new WireEnvelope(body.getEmitterPid(), refactoredRecords);
	}
}


