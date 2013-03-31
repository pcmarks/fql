package fql;

import net.categoricaldata.api.BackEnd;


public class FQLBackEnd implements BackEnd {

	@Override
	public String delta(String instance, String mapping) throws Exception {
		Mapping m = Mapping.fromjson(mapping);
		Instance i = Instance.fromjson(instance);		
		Instance j = new Instance("", m.source, m.evalDelta(i));
		return j.tojson();
	}

	@Override
	public String sigma(String instance, String mapping) throws Exception {
		Mapping m = Mapping.fromjson(mapping);
		Instance i = Instance.fromjson(instance);		
		Instance j = new Instance("", m.target, m.evalSigma(i));
		return j.tojson();
	}

	@Override
	public String pi(String instance, String mapping) throws Exception {
		Mapping m = Mapping.fromjson(mapping);
		Instance i = Instance.fromjson(instance);		
		Instance j = new Instance("", m.target, m.evalPi(i));
		return j.tojson();
	}

	@Override
	public String iso(String instance1, String instance2) throws Exception {
		Instance i = Instance.fromjson(instance1);
		Instance j = Instance.fromjson(instance2);
		return Boolean.toString(i.iso(j));
	}

	@Override
	public String version() {
		return "FQL, using SQL-ish queries";
	}
	
	@Override
	public String readme() {
		return "Note: in FQL, data values cannot contain spaces, and provenance tags are not allowed.";
	}

}