package fql.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.FQLException;
import fql.Pair;
import fql.cat.Denotation;
import fql.decl.Edge;
import fql.decl.Instance;
import fql.decl.Mapping;
import fql.decl.Node;
import fql.decl.Signature;

/**
 * 
 * @author ryan
 *
 * PSM for full sigma.  Note that this cannot
 * actually be implemented by a real RDBMS.
 */
public class FullSigma extends PSM {
	
	Mapping f;
	String pre;
	String inst;
	//Environment env;

	public FullSigma(Mapping f, String pre, String inst) {
		this.f = f;
		this.pre = pre;
		this.inst = inst;
		if (f.source.attrs.size() > 0) {
			throw new RuntimeException("Cannot SIGMA with attributes");
		}
	}

	@Override
	public void exec(Map<String, Set<Map<Object, Object>>> state) {
		Signature C = f.source;
		Signature D = f.target;
		List<Pair<String, List<Pair<Object, Object>>>> I0 = PSMGen.gather(inst, C, state);

		try {
			Instance I = new Instance(C, I0);
			Denotation d = new Denotation(f, I);
		//	System.out.println("Exucuting fs on " + I);
			//System.out.println("GUID is " + PSMInterp.guid);
//			System.out.println(d);
			Instance J = d.sigma();
		//	System.out.println("done " + J);
			//System.out.println("GUID is " + PSMInterp.guid);

			for (Node n : D.nodes) {
				state.put(pre + "_" + n.string, conv(J.data.get(n.string)));
			}
			for (Edge n : D.edges) {
				state.put(pre + "_" + n.name, conv(J.data.get(n.name)));
			}
			
		} catch (FQLException e) {
			throw new RuntimeException(e);
		}
		
	}

	private Set<Map<Object, Object>> conv(Set<Pair<Object, Object>> set) {
		Set<Map<Object, Object>> ret = new HashSet<>();
		for (Pair<Object, Object> p : set) {
			Map<Object, Object> m = new HashMap<>();
			m.put("c0", p.first);
			m.put("c1", p.second);
			ret.add(m);
		}
		return ret;
	}

	@Override
	public String toPSM() {
		throw new RuntimeException("Cannot generate SQL for full sigma");
	}

}
