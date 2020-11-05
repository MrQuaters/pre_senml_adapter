package org.eclipse.kura.presenml.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.kura.presenml.PreSenml;
import org.eclipse.kura.presenml.PreSenmlBuilder;
import org.junit.Test;

public class SenmlTest {

	@Test
	public void TestModbusLike() {
		PreSenml c = PreSenmlBuilder.create("^([\\w&&[^_]]+):[\\w&&[^_]]+$")
				.addUnitReg("^([\\w&&[^_]]+):([\\w&&[^_]]+)$", 2).addTimeReg("^([\\w&&[^_]]+):[\\w&&[^_]]+_time$")
				.addIgnoreRecord(".*?ing.*?").addIgnoreRecord("^nottest$").addNoRefactorRecord("^assert.*").build();
		// test for samples like:
		// temprature: 123
		// temprature_timestamp: 1e12
		// eurotech modbus driver uses this type of naming

		Map<String, Object> ls = new HashMap<String, Object>();
		Map<String, Object> result;
		ls.put("sn:t:test", 1244);
		result = c.refactorOne("test:er_time", 1244);
		assertTrue("1. Test time parsing - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
		ls.clear();

		ls.put("sn:m:test", 1225);
		ls.put("sn:u:test", "erqw");
		result = c.refactorOne("test:erqw", 1225);
		assertTrue("2. Test units and value parsing - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
		ls.clear();

		result = c.refactorOne("assetName", 1244);
		assertTrue("3. Test data shouldnt parse - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
		result = c.refactorOne("BindBand_time", 1244);
		assertTrue("4. Test data shouldnt parse - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
		result = c.refactorOne("HollyShit123", 1244);
		assertTrue("5. Test data shouldnt parse - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));

		result = c.refactorOne("nottest:er_time", 1244);
		assertTrue("6. Test data should ignored - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
		result = c.refactorOne("nottest:er", 1244);
		assertTrue("7. Test data should ignored - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
		result = c.refactorOne("ningt:er", 1244);
		assertTrue("8. Test data should ignored - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));

		result = c.refactorOne("assertName", 1244);
		ls.put("assertName", 1244);
		assertTrue("9. Test data should set default - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));

	}

	@Test
	public void TestStmpleDataLike() {
		PreSenml c = PreSenmlBuilder.create("^([\\w&&[^_]]+)$").addUnitReg("^unit_([\\w&&[^_]]+)$", -1)
				.addTimeReg("^time_([\\w&&[^_]]+)$").addIgnoreRecord("^asset.*").build();
		// test for samples like:
		// temprature: 123
		// time_temprature: 1e12
		// unit_temprature: "C"

		Map<String, Object> ls = new HashMap<String, Object>();
		Map<String, Object> result;
		ls.put("sn:t:test", 1244);
		result = c.refactorOne("time_test", 1244);
		assertTrue("1. Test time parsing - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
		ls.clear();

		ls.put("sn:m:test", 1225);
		result = c.refactorOne("test", 1225);
		assertTrue("2. Test value parsing - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
		ls.clear();

		ls.put("sn:u:test", "C");
		result = c.refactorOne("unit_test", "C");
		assertTrue("3. Test value parsing - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
		ls.clear();

		result = c.refactorOne("BindBand_time", 1244);
		assertTrue("4. Test data shouldnt parse - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
		result = c.refactorOne("HollyShit123:asd", 1244);
		assertTrue("5. Test data shouldnt parse - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));

		result = c.refactorOne("assetName", 1244);
		assertTrue("6. Test data should ignored - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
	}

	@Test
	public void CustomTemplatesTest() {
		PreSenml c = PreSenmlBuilder.createEmpty().addCustomPattern("^(\\w+)-abc-.*", -1, "abc-%s")
				.addCustomPattern("^(\\w+)-cde$", -1, "time-%s").addCustomPattern("^(\\w+)-cde-(\\w+)$", 2, "units-%s")
				.addNoRefactorRecord("^assetName$").addIgnoreRecord("^.*ads.*$").build();

		Map<String, Object> ls = new HashMap<String, Object>();
		Map<String, Object> result;
		ls.put("abc-time", 1244);
		result = c.refactorOne("time-abc-a", 1244);
		assertTrue("1. Test time parsing - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
		ls.clear();

		ls.put("units-time", "a");
		result = c.refactorOne("time-cde-a", 1225);
		assertTrue("2. Test value parsing - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
		ls.clear();

		result = c.refactorOne("ads-cde-a", 1244);
		assertTrue("3. Test data shouldnt parse - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));

		result = c.refactorOne("assetName", 1244);
		ls.put("assetName", 1244);
		assertTrue("4. Test data should set default - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
		ls.clear();

		ls.put("time-time", 1225);
		result = c.refactorOne("time-cde", 1225);
		assertTrue("2. Test value parsing - BAD : waited " + ls.toString() + " got " + result.toString(),
				result.equals(ls));
		ls.clear();

	}

}
