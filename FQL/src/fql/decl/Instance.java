package fql.decl;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import fql.FQLException;
import fql.Pair;
import fql.cat.Arr;
import fql.cat.FDM;
import fql.cat.FinCat;
import fql.cat.Inst;
import fql.cat.Value;
import fql.gui.Viewable;
import fql.parse.FqlTokenizer;
import fql.parse.JSONParsers;
import fql.parse.Jsonable;
import fql.parse.PrettyPrinter;

public class Instance implements Viewable<Instance>, Jsonable {

	public void conformsTo(Signature s) throws FQLException {
		for (Node n : s.nodes) {
			Set<Pair<String, String>> i = data.get(n.string);
			for (Pair<String, String> p : i) {
				if (!p.first.equals(p.second)) {
					throw new FQLException("Not reflexive: " + s.name0 + " in "
							+ s + " and " + this);
				}
			}
			// identity
		}
		for (Edge e : s.edges) {
			Set<Pair<String, String>> i = data.get(e.name);
			for (Pair<String, String> p1 : i) {
				for (Pair<String, String> p2 : i) {
					if (p1.first.equals(p2.first)) {
						if (!p2.second.equals(p2.second)) {
							throw new FQLException("Not functional: " + s.name0
									+ " in " + s + " and " + this);
						}
					}
				}
				// functional

				if (!contained(p1.first, data.get(e.source.string))) {
					throw new FQLException("Domain has non foreign key: "
							+ s.name0 + " in " + s + " and " + this);
				}
				if (!contained(p1.second, data.get(e.target.string))) {
					throw new FQLException("Range has non foreign key: \n"
							+ p1.second + "\n\n " + data.get(e.target.string)
							+ "\n\n " + s.name0 + " \n\n " + s + " \n\n " + this);
				}
			}
		}
		for (Eq eq : s.eqs) {
			Set<Pair<String, String>> lhs = evaluate(eq.lhs);
			Set<Pair<String, String>> rhs = evaluate(eq.rhs);
			if (!lhs.equals(rhs)) {
				throw new FQLException("Violates constraints: " + s.name0
						+ " in " + s + " and " + this);
			}
		}
		
		//toFunctor();
	}

	private Set<Pair<String, String>> evaluate(Path p) {
		Set<Pair<String, String>> x = data.get(p.source.string);
		if (x == null) {
			System.out.println("Couldnt find " + p.source.string);
		}
		for (Edge e : p.path) {
			if (data.get(e.name) == null) {
				System.out.println("Couldnt find " + e.name);
			}

			x = compose(x, data.get(e.name));
		}
		return x;
	}

	private static Set<Pair<String, String>> compose(
			Set<Pair<String, String>> x, Set<Pair<String, String>> y) {
		Set<Pair<String, String>> ret = new HashSet<Pair<String, String>>();

		for (Pair<String, String> p1 : x) {
			for (Pair<String, String> p2 : y) {
				if (p1.second.equals(p2.first)) {
					Pair<String, String> p = new Pair<String, String>(p1.first,
							p2.second);
					ret.add(p);
				}
			}
		}
		return ret;
	}

	private boolean contained(String s, Set<Pair<String, String>> set) {
		for (Pair<String, String> p : set) {
			if (p.first.equals(s) && p.second.equals(s)) {
				return true;
			}
		}
		return false;
	}

	Map<String, Set<Pair<String, String>>> data;

	public Signature thesig;
	
	public Instance(String n, Signature thesig,
			Map<String, Set<Pair<String, String>>> data)
			throws FQLException {
		this.data = new HashMap<String, Set<Pair<String, String>>>();
		for (String p : data.keySet()) {
			this.data.put(p, new HashSet<Pair<String, String>>(data.get(p)));
		}
		this.thesig = thesig;
		if (!typeCheck(thesig)) {
			throw new FQLException("Type-checking failure " + n);
		}
		conformsTo(thesig);
	}

	public Instance(String n, Signature thesig,
			List<Pair<String, List<Pair<String, String>>>> data)
			throws FQLException {
		this.data = new HashMap<String, Set<Pair<String, String>>>();
		for (Pair<String, List<Pair<String, String>>> p : data) {
			this.data.put(p.first, new HashSet<Pair<String, String>>(p.second));
		}
		this.thesig = thesig;
		if (!typeCheck(thesig)) {
			throw new FQLException("Type-checking failure " + n);
		}
		conformsTo(thesig);
	}

	public Instance(String name, Query thequery, Instance theinstance)
			throws FQLException {
		if (!thequery.getSource().equals(theinstance.thesig)) {
			throw new FQLException("Incompatible types. Expected "
					+ thequery.getSource() + " received " + theinstance.thesig);
		}
		thesig = thequery.getTarget();
		data = thequery.eval(theinstance);
		conformsTo(thesig);

	}

	public Instance(String name, Mapping m, Instance i, String type)
			throws FQLException {
		if (type.equals("delta")) {
			if (!m.target.equals(i.thesig)) {
				throw new FQLException("Incompatible types. Expected "
						+ m.target + " received " + i.thesig);
			}
			thesig = m.source;
			data = m.evalDelta(i);
			conformsTo(thesig);

		} else if (type.equals("sigma")) {
			if (!m.source.equals(i.thesig)) {
				throw new FQLException("Incompatible types. Expected "
						+ m.source + " received " + i.thesig);
			}
			thesig = m.target;
			data = m.evalSigma(i);

			conformsTo(thesig);

		} else if (type.equals("pi")) {
			if (!m.source.equals(i.thesig)) {
				throw new FQLException("Incompatible types. Expected "
						+ m.source + " received " + i.thesig);
			}
			thesig = m.target;
			data = m.evalPi(i);
			conformsTo(thesig);

		} else {
			throw new FQLException("Unknown type " + type);
		}
	//	toFunctor().morphs(toFunctor(), toFunctor());
	}

	//this is the json one
	public Instance(
			Signature sig,
			List<Pair<String, List<String>>> ob,
			List<Pair<Pair<Pair<String, String>, String>, List<Pair<String, String>>>> mo) 
	throws FQLException
	{
		
		this(null, sig, jsonmap(ob, mo));		
	}

	private static Map<String, Set<Pair<String, String>>> jsonmap(
			List<Pair<String, List<String>>> ob,
			List<Pair<Pair<Pair<String, String>, String>, List<Pair<String, String>>>> mo) {
		Map<String, Set<Pair<String, String>>> map = new HashMap<>();
		for (Pair<String, List<String>> o : ob) {
			map.put(o.first, dupl(o.second));
		}
		for (Pair<Pair<Pair<String, String>, String>, List<Pair<String, String>>> o : mo) {
			String arr = o.first.second;
			Set<Pair<String, String>> set = map.get(arr);
			if (set == null) {
				set = new HashSet<>();
				map.put(arr,  set);
			}
			for (Pair<String, String> oo : o.second) {
				set.add(oo);
			}
		}
		return map;
	}

	private static Set<Pair<String, String>> dupl(List<String> x) {
		Set<Pair<String, String>> ret = new HashSet<>();
		for (String s : x) {
			ret.add(new Pair<>(s, s));
		}
		return ret;
	}

	private boolean typeCheck(Signature thesig2) {
		for (String s : data.keySet()) {
			if (!thesig2.contains(s)) {
				return false;
			}
		}
		for (String s : thesig2.all()) {
			if (null == data.get(s)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Instance other = (Instance) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("{ ");

		boolean first = true;
		for (String k : data.keySet()) {
			Set<Pair<String, String>> v = data.get(k);
			if (!first) {
				sb.append("; ");
			}
			first = false;
			sb.append(k);
			sb.append(" = { ");
			sb.append(printSet(v));
			sb.append(" }");
		}

		sb.append(" }");
		return sb.toString();

	}

	private String printSet(Set<Pair<String, String>> v) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (Pair<String, String> p : v) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append("(");
			sb.append(maybeQuote(p.first));
			sb.append(",");
			sb.append(maybeQuote(p.second));
			sb.append(")");
		}
		return sb.toString();
	}

	private String maybeQuote(String s) {
		if (s.contains(" ") || s.contains("\n") || s.contains("\r") || s.contains("\t")) {
			return "\"" + s + "\"";
		}
		return s;
	}

	@Override
	public JPanel view() throws FQLException {
		List<JPanel> panels = new LinkedList<JPanel>();
		// Map<String, Set<Pair<String,String>>> data;
		LinkedList<String> sorted = new LinkedList<String>(data.keySet());
		Collections.sort(sorted, new Comparator<String>()
                {
            public int compare(String f1, String f2)
            {
                return f1.toString().compareTo(f2.toString());
            }        
        });
		for (String k : sorted) {
			Set<Pair<String, String>> xxx = data.get(k);
			List<Pair<String, String>> table = new LinkedList<>(xxx);
			Collections.sort(table, new Comparator<Pair<String, String>>()
	                {
	            public int compare(Pair<String,String> f1, Pair<String,String> f2)
	            {
	                return f1.first.toString().compareTo(f2.first.toString());
	            }        
	        });

			String[][] arr = new String[table.size()][2];
			int i = 0;
			for (Pair<String, String> p : table) {
				arr[i][0] = p.first.trim();
				arr[i][1] = p.second.trim();
				i++;
			}
			Pair<String, String> cns = thesig.getColumnNames(k);
			JTable t = new JTable(arr, new Object[] { cns.first, cns.second });
			t.setRowSelectionAllowed(false);
			t.setColumnSelectionAllowed(false);
			MouseListener[] listeners = t.getMouseListeners();
			for (MouseListener l : listeners) {
				t.removeMouseListener(l);
			}
			t.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			JPanel p = new JPanel(new GridLayout(1, 1));
			p.add(new JScrollPane(t));
			p.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEmptyBorder(2, 2, 2, 2), k));
			panels.add(p);
			p.setSize(60, 60);
		}

		int x = (int) Math.ceil(Math.sqrt(panels.size()));
		JPanel panel = new JPanel(new GridLayout(x, x));
		for (JPanel p : panels) {
			panel.add(p);
		}
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		return panel;
	}

	@Override
	public JPanel join() throws FQLException {
		// Map<String, Set<Pair<String,String>>> data;
		
		prejoin(); 
		
		List<JPanel> pans = makePanels();
	
		int x = (int) Math.ceil(Math.sqrt(pans.size()));
		JPanel panel = new JPanel(new GridLayout(x, x));
		for (JPanel p : pans) {
			panel.add(p);
		}
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		return panel;		
	}

	private List<JPanel> makePanels() {
		List<JPanel> ret = new LinkedList<>();
		
		Comparator<String> strcmp = new Comparator<String>()  {
	        public int compare(String f1, String f2) {
	                return f1.compareTo(f2);
	            }        
	        };
	        
	        List<String> xxx = new LinkedList<>(joined.keySet());
	        Collections.sort(xxx, strcmp);
	        
		for (String name : xxx) {
			JTable t = joined.get(name);
			JPanel p = new JPanel(new GridLayout(1,1));
			//p.add(t);
			p.add(new JScrollPane(t));
	//		p.setMaximumSize(new Dimension(200,200));
			p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), name));
			ret.add(p);
		}
		
		
		return ret;
	}

	private void prejoin() {
		if (joined != null) {
			return;
		}
		vwr.setLayout(cards);
		vwr.add(new JPanel(), "");
		cards.show(vwr, "");
		Map<String, Map<String, Set<Pair<String, String>>>> jnd 
		= new HashMap<>();
		Map<String, Set<Pair<String, String>>> nd
		= new HashMap<>();
		
		List<String> names = new LinkedList<>();
		
		for (Node n : thesig.nodes) {
			nd.put(n.string, data.get(n.string));
			jnd.put(n.string, new HashMap<String, Set<Pair<String, String>>>());
			names.add(n.string);
		}
		
		for (Edge e : thesig.edges) {
			jnd.get(e.source.string).put(e.name, data.get(e.name));
	//		names.add(e.name);
		}
		
//		System.out.println(joined);
	//	System.out.println(nd);
		
		Comparator<String> strcmp = new Comparator<String>()  {
	        public int compare(String f1, String f2) {
	                return f1.compareTo(f2);
	            }        
	        };
		Collections.sort(names, strcmp);

		joined = makejoined(jnd, nd, names);
		
	}

	private Map<String, JTable> makejoined(
			Map<String, Map<String, Set<Pair<String, String>>>> joined,
			Map<String, Set<Pair<String, String>>> nd, List<String> names) {
		Comparator<String> strcmp = new Comparator<String>()  {
	        public int compare(String f1, String f2) {
	                return f1.compareTo(f2);
	            }        
	        };
	        Map<String, JTable> ret = new HashMap<>();
		for (String name : names) {
//			System.out.println("Name " + name);
			Map<String, Set<Pair<String, String>>> m = joined.get(name);
	//		System.out.println("m " + m);
			Set<Pair<String, String>> ids = nd.get(name);
		//	System.out.println("ids " + ids);
			String[][] arr = new String[ids.size()][m.size() + 1];
			Set<String> cols = m.keySet();
	//		System.out.println("cols " + cols);
			List<String> cols2 = new LinkedList<>(cols);
			Collections.sort(cols2, strcmp);
			cols2.add(0, "ID");
	//		System.out.println("cols2 " + cols2);
			Object[] cols3 = cols2.toArray();
	//		System.out.println("cols3 " + cols3);
			
			int i = 0;
			for (Pair<String, String> id : ids) {
//				System.out.println("id " + id);
				arr[i][0] = id.first;
//				System.out.println(" i " + i + " j " + 0 + " val " + arr[i][0]);

				int j = 1;
				for (String col : cols2) {
					if (col.equals("ID")) {
						continue;
					}
				//	System.out.println("col " + col);
					Set<Pair<String, String>> coldata = m.get(col);
					for (Pair<String, String> p : coldata) {
				//		System.out.println("p " + p);
						if (p.first.equals(id.first)) {
							arr[i][j] = p.second;
//							System.out.println(" i " + i + " j " + j + " val " + arr[i][j]);
							break;
						}
					}
					j++;
				}
				i++;
			}
			
			Arrays.sort(arr, new Comparator<String[]>() {

				@Override
				public int compare(String[] o1, String[] o2) {
					return o1[0].compareTo(o2[0]);
				}
				
			});
			
			JTable t = new JTable(arr, cols3) {
				  public Dimension getPreferredScrollableViewportSize() {
					  Dimension d = getPreferredSize();
				  return new Dimension(d.width, d.height * 2);
				  }
				  };
				  
				//  cards.(name, t);
				  
				  JTable foo = new JTable(t.getModel()) {
					  public Dimension getPreferredScrollableViewportSize() {
						  Dimension d = getPreferredSize();
					  return new Dimension(d.width, d.height * 2);
					  }
					  };
			JPanel p = new JPanel(new GridLayout(1,1));
			//p.add(t);
			p.add(new JScrollPane(foo));
	//		p.setMaximumSize(new Dimension(200,200));
			p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), name));
			vwr.add(p, name);
			
//			foo.setMaximumSize(new Dimension(600,200));
			
			ret.put(name, t);
		}
		
		return ret;
	}

	
	@Override
	public JPanel text() {
		// String s = toString().replace('{', ' ').replace('}', ' ').trim();
		String[] t = toString().split(";");
		String ret = "";
		for (String a : t) {
			ret += (a.trim() + ";\n\n");
		}

		JTextArea ta = new JTextArea(ret);
		JPanel tap = new JPanel(new GridLayout(1, 1));
		ta.setBorder(BorderFactory.createEmptyBorder());
		//
		tap.setBorder(BorderFactory.createEmptyBorder());
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);
		JScrollPane xxx = new JScrollPane(ta);
		// xxx.setBorder(BorderFactory.createEmptyBorder());
		//
		tap.add(xxx);
		// tap.setSize(600, 600);

		return tap;
	}



	public static boolean iso(Instance i1, Instance i2) {
		sameNodes(i1, i2);
		sameEdges(i1, i2);

		Signature sig = i1.thesig;
		
		Map<String, List<Map<String, String>>> subs1 = new HashMap<>();
		Map<String, List<Map<String, String>>> subs2 = new HashMap<>();
		for (Node n : sig.nodes) {
			String k = n.string;
			
			List<Map<String, String>> i1i2 = Inst.bijections(dedupl(i1.data.get(k)),
									                         dedupl(i2.data.get(k)));
			List<Map<String, String>> i2i1 = Inst.bijections(dedupl(i2.data.get(k)),
															dedupl(i1.data.get(k)));
			
			subs1.put(k, i1i2);
			subs2.put(k, i2i1);		 
		}
		
		Subs subs1X = new Subs(subs1);
		Subs subs2X = new Subs(subs2);
		Map<String, Map<String, String>> sub;

		boolean flag = false;
		while ((sub = subs1X.next()) != null) {
			try {
				Instance iX = i1.apply(sub);
				if (iX.equals(i2)) {
					flag = true;
					break;
				}
			} catch (Exception e) { }
		}
		if (!flag) {
			return false;
		}
		
		flag = false;
		while ((sub = subs2X.next()) != null) {
			try {
				Instance iX = i2.apply(sub);
				if (iX.equals(i1)) {
					flag = true;
					break;
				}
			} catch (Exception e) { }
		}
		if (!flag) {
			return false;
		}

		return true;
	}

	static class Subs {
		private Map<String, List<Map<String, String>>> sub;
		private LinkedList<String> keys;
		private int[] counters;
		private int[] sizes;

		public Subs(Map<String, List<Map<String, String>>> subs0) {
			this.sub = subs0;
			this.keys = new LinkedList<>(sub.keySet());

			this.counters = makeCounters(keys.size() + 1);
			this.sizes = makeSizes(keys, sub);
		}
		
		public Map<String, Map<String, String>> next() {
			if (counters[keys.size()] == 1) {
				return null;
			}

			Map<String, Map<String, String>> s = new HashMap<>();
			for (String k : keys) {
				s.put(k, sub.get(k).get(counters[keys.indexOf(k)]));
			}
			
			inc5(counters, sizes);
			
			return s;
		}
	}
	

	public static void printnice(int[] x) {
		for (int i = 0; i < x.length; i++) {
			System.out.print(x[i]);
			System.out.print(" ");
		}
		System.out.println();
	}
	
	private static int[] makeSizes(List<String> keys,
			Map<String, List<Map<String, String>>> sub) {
		int[] ret = new int[keys.size()];
		int i = 0;
		for (String k : keys) {
			ret[i++] = sub.get(k).size();
		}
		return ret;
	}

	private static void inc5(int[] counters, int[] sizes) {
		counters[0]++;
		for (int i = 0; i < counters.length - 1; i++) {
			if (counters[i] == sizes[i]) {
				counters[i] = 0;
				counters[i + 1]++;
			}
		}
	}


	private static int[] makeCounters(int size) {
		int[] ret = new int[size];
		for (int i = 0; i < size; i++) {
			ret[i++] = 0;
		}
		return ret;
	}

	private Instance apply(Map<String, Map<String, String>> sub) throws FQLException {
		List<Pair<String, List<Pair<String, String>>>> ret = new LinkedList<>();
		
		for (Node n : thesig.nodes) {
			ret.add(new Pair<>(n.string, apply(data.get(n.string), sub.get(n.string), sub.get(n.string))));
		}
		for (Edge e : thesig.edges) {
			ret.add(new Pair<>(e.name, apply(data.get(e.name), sub.get(e.source.string), sub.get(e.target.string))));
		}
		
		return new Instance(null, thesig, ret);
	}

	private static List<Pair<String, String>> apply(Set<Pair<String, String>> set,
			Map<String, String> s1, Map<String, String> s2) {
		List<Pair<String, String>> ret = new LinkedList<>();
		
		for (Pair<String, String> p : set) {
			ret.add(new Pair<>(s1.get(p.first), s2.get(p.second)));
		}
		
		return ret;
	}

	private static List<String> dedupl(Set<Pair<String, String>> set) {
		List<String> ret = new LinkedList<>();
		for (Pair<String, String> p : set) {
			ret.add(p.first);
		}
		return ret;
	}

	private static void sameEdges(Instance i1, Instance i2) {
		for (Edge e1 : i1.thesig.edges) {
			if (!i2.thesig.edges.contains(e1)) {
				throw new RuntimeException("Missing " + e1 + " in " + i2 + ")");
			}
		}
		for (Edge e2 : i2.thesig.edges) {
			if (!i1.thesig.edges.contains(e2)) {
				throw new RuntimeException("Missing " + e2 + " in " + i1 + ")");
			}
		}
	}

	private static void sameNodes(Instance i1, Instance i2) {
		for (Node n1 : i1.thesig.nodes) {
			if (!i2.thesig.nodes.contains(n1)) {
				throw new RuntimeException("Missing " + n1 + " in " + i2 + ")");
			}
		}
		for (Node n2 : i2.thesig.nodes) {
			if (!i1.thesig.nodes.contains(n2)) {
				throw new RuntimeException("Missing " + n2 + " in " + i1 + ")");
			}
		}
	}


	@Override
	public JPanel pretty() throws FQLException {
		return makeViewer();
	}

	@Override
	public String type() {
		return "instance";
	}

	public Graph<String, String> build() {
		// Graph<V, E> where V is the type of the vertices

		Graph<String, String> g2 = new DirectedSparseMultigraph<String, String>();
		for (Node n : thesig.nodes) {
			g2.addVertex(n.string);
		}

		for (Edge e : thesig.edges) {
			g2.addEdge(e.name, e.source.string, e.target.string);
		}

		return g2;
	}

	public JPanel makeViewer() {
		Graph<String, String> g = build();
		return doView(g);
	}

	public JPanel doView(Graph<String, String> sgv) {
		// Layout<V, E>, BasicVisualizationServer<V,E>
		// Layout<String, String> layout = new KKLayout(sgv);

		// Layout<String, String> layout = new FRLayout(sgv);
		 Layout<String, String> layout = new ISOMLayout<String,String>(sgv);
		//Layout<String, String> layout = new CircleLayout<>(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(layout);
		vv.setPreferredSize(new Dimension(600, 400));
		//vv.getRenderContext().setEdgeLabelRerderer(new MyEdgeT());
		// Setup up a new vertex to paint transformer...
		Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
			public Paint transform(String i) {
				return Environment.colors.get(thesig.name0);
			}
		};
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
      //  gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        gm.setMode(Mode.PICKING);
//        gm.add(new AnnotatingGraphMousePlugin(vv.getRenderContext()) {
//
//		
//        	
//        }.);
               

		// Set up a new stroke Transformer for the edges
		// float dash[] = { 10.0f };
		// final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
		// BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		// Transformer<String, Stroke> edgeStrokeTransformer = new
		// Transformer<String, Stroke>() {
		// public Stroke transform(String s) {
		// return edgeStroke;
		// }
		// };
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		// vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		vv.getRenderContext().setVertexLabelRenderer(new MyVertexT());
		// vv.getRenderContext().setVertexLabelTransformer(new
		// ToStringLabeller());
		
//				new MyEdgeT()); // {

//		vv.getRenderContext().setEdgeLabelTransformer(new MyEdgeT2(vv.getPickedEdgeState()));
		//vv.getRenderContext().setVertexLabelTransformer(new MyVertexT(vv.getPickedVertexState()));
		// vv.getRenderer().getVertexRenderer().
		vv.getRenderContext().setLabelOffset(20);
		vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<String>());
//		vv.getRenderContext().getEdgeLabelRenderer().
		// vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

		JSplitPane newthing = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		newthing.setDividerLocation(.9d);
		newthing.add(vv);
		newthing.add(vwr);
		JPanel xxx = new JPanel(new GridLayout(1,1));
		xxx.add(newthing);
//		xxx.setMaximumSize(new Dimension(400,400));
		return xxx;
	}
	
	private class MyVertexT implements VertexLabelRenderer{
	  
		
	    public MyVertexT(){
	    }

	    @Override
	    public <T> Component getVertexLabelRendererComponent(
				JComponent arg0, Object arg1, Font arg2, boolean arg3,
				T arg4) {
	    	if (arg3) {
	    		prejoin();
	    	//	Map<String, JPanel> panels = makejoined();
//			    	 if (pi.isPicked((String) arg4)) {

	    		cards.show(vwr, (String)arg4);
	    		
	            return new JLabel((String)arg4);

	    		
//	    		JTable t = joined.get(arg4);
//	    		
//				JPanel p = new JPanel(new GridLayout(1,1));
//				//p.add(t);
//				p.add(new JScrollPane(t));
//		//		p.setMaximumSize(new Dimension(200,200));
//				p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), (String)arg4));
//
//	    		
//	   // 		JPanel p = new JPanel(new GridLayout(1,1));
//	    //		p.add(new JScrollPane(joined.get(arg4)));
//	    	//	p.setMaximumSize(new Dimension(100,100));
//	  //  		p.setPreferredSize(new Dimension(100,100));
//	    	//	p.setSize(new Dimension(100,100));
//			    	return p;
		        }
		        else {
		          return new JLabel((String)arg4);
		        }
		    }
	    }
	
//	private class MyEdgeT extends DefaultEdgeLabelRenderer {
//	   // private final PickedInfo<String> pi;
//
//	    public MyEdgeT(){
//	    	super(Color.GRAY, false);
//	      //  this.pi = pi;
//	    }
//
//	    @Override
//	    public <T> Component getEdgeLabelRendererComponent(
//				JComponent arg0, Object arg1, Font arg2, boolean arg3,
//				T arg4) {
//	    //	if (true) throw new RuntimeException();
//	    	if (arg3) {
////			    	 if (pi.isPicked((String) arg4)) {
//			    		 Vector<String> ld = new  Vector<>();
//
//			    		 Set<Pair<String, String>> table = data.get(arg4);
//			    		 
//
//			    		 String s = (String) arg4;
//			    		 boolean b = false;
//			    		 s += " = ";
//			    		 for (Pair<String, String> x : table) {
//			    			 if (b) {
//			    				 s += ", ";
//			    			 }
//			    			 b = true;
//			    			 s += x.first;
//			    			 ld.add(x.first);
//			    		 }
//			    		 JList<String> jl = new JList<>(ld);
//			    		 JPanel p = new JPanel(new GridLayout(1,1));
//			    		 p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), (String) arg4));
//			    		 p.add(new JScrollPane(jl));
//			    		// p.add(jl);
//			    		 
////					JLabel x = new JLabel(s);
//					// x.setFont(new Font("Arial", 8, Font.PLAIN));
//		//			return x;
////					return new JTextArea(s);
//			    		// return p;
//			    		 return new JLabel("ZZZZ");
//		        }
//		        else {
//		        	return new JLabel("HHHH");
//		         // return new JLabel("ZZZZZ" + (String)arg4);
//		        }
//		    }
//
//	    boolean b = false;
//		@Override
//		public boolean isRotateEdgeLabels() {
//			return b;
//		}
//
//		@Override
//		public void setRotateEdgeLabels(boolean arg0) {
//			this.b = arg0;
//		}
//	    }
//	
//	private  class MyEdgeT2 implements Transformer<String,String>{
//	    private final PickedInfo<String> pi;
//
//	    public MyEdgeT2( PickedInfo<String> pi ){
//	        this.pi = pi;
//	    }
//
//	    @Override
//	    public String transform(String t) {
//	        if (pi.isPicked(t)) {
//				Set<Pair<String, String>> table = data.get(t);
//
//				String s = t;
//				boolean b = false;
//				s += " = ";
//				for (Pair<String, String> x : table) {
//					if (b) {
//						s += ", ";
//					}
//					b = true;
//					s += x.first;
//					s += " -> ";
//					s += x.second;
//				}
////				JLabel x = new JLabel(s);
//				// x.setFont(new Font("Arial", 8, Font.PLAIN));
//	//			return x;
//				return s;
//
//	        }
//	        else {
//	          return t;
//	        }
//	    }
//	}

	
	


//	private static List<Pair<String,String>> dupl(Map<String, String> map) {
//		List<Pair<String,String>> ret = new LinkedList<Pair<String,String>>();
//		for (String k : map.keySet()) {
//			ret.add(new Pair<>(k,map.get(k)));
//		}
//		return ret;	
//	}
//
//	private static List<Pair<String,String>> dupl(Set<String> set) {
//		List<Pair<String,String>> ret = new LinkedList<Pair<String,String>>();
//		for (String s : set) {
//			ret.add(new Pair<>(s,s));
//		}
//		return ret;		
//	}

	public static Instance terminal(Signature s) throws FQLException {
		List<Pair<String, List<Pair<String, String>>>> ret = new LinkedList<>();

		int i = 0;
		Map<Node, String> map = new HashMap<>();
		for (Node node : s.nodes) {
			List<Pair<String, String>> tuples = new LinkedList<>();
			String g = Integer.toString(i);
			tuples.add(new Pair<>(g, g));
			ret.add(new Pair<>(node.string, tuples));
			map.put(node, g);
			i++;
		}

		for (Edge e : s.edges) {
			List<Pair<String, String>> tuples = new LinkedList<>();
			tuples.add(new Pair<>(map.get(e.source.string), map
					.get(e.target.string)));
			ret.add(new Pair<>(e.name, tuples));
		}

		return new Instance(s.name0 + "_terminal", s, ret);
	}

	public Inst<String, List<List<String>>, String, String> toFunctor() throws FQLException {
		FinCat<String, List<List<String>>> cat = thesig.toCategory().first;
		
		Map<String, Set<Value<String, String>>> objM = new HashMap<>();
		for (String obj : cat.objects) {
			objM.put(obj, conv(data.get(obj)));
		}
		
		Map<Arr<String, List<List<String>>>, Map<Value<String, String>, Value<String, String>>> arrM = new HashMap<>();
		for (Arr<String, List<List<String>>> arr : cat.arrows) {
			List<String> es = arr.arr.get(0);
			
			String h = es.get(0);
			Set<Pair<String, String>> h0 = data.get(h);
			for (int i = 1; i < es.size(); i++) {
				h0 = compose(h0, data.get(es.get(i)));
			}
			Map<Value<String, String>, Value<String, String>> xxx = FDM.degraph(h0);
			arrM.put(arr, xxx);
		}
		
		return new Inst<String, List<List<String>>, String, String>(objM, arrM, cat);
	}

	private Set<Value<String,String>> conv(Set<Pair<String, String>> set) {
		Set<Value<String,String>> ret = new HashSet<>();
		for (Pair<String, String> p : set) {
			ret.add(new Value<String,String>(p.first));
		}
		return ret;
	}
	
	JPanel vwr = new JPanel();
	CardLayout cards = new CardLayout();
	Map<String, JTable> joined;

	@Override
	public String tojson() {
		List<String> l = new LinkedList<String>();
		for (Node kk : thesig.nodes) {
			String k = kk.string;
			Set<Pair<String, String>> v = data.get(k);
			boolean first = true;
			String s = "\"" + k + "\" : [";
			for (Pair<String, String> tuple : v) {
				if (!first) {
					s += ",";
				}
				first = false;
				
				s += "\"" + tuple.first + "\"";
			}
			s += "]";
			l.add(s);
		}
		
		String s = PrettyPrinter.sep0(",\n", l);
		
		String onobjects = "\"onObjects\" : {\n" + s + "\n}";
		
		l = new LinkedList<String>();
		for (Edge kk : thesig.edges) {
			String k = kk.name;
			Set<Pair<String, String>> v = data.get(k);
			boolean first = true;
			s = "{\"arrow\":" + kk.tojson() + ",\n\"map\" : {";
			for (Pair<String, String> tuple : v) {
				if (!first) {
					s += ",";
				}
				s += "\"" + tuple.first + "\":" +  "\"" + tuple.second + "\"";
				first = false;
			}
			s += "}}\n";
			l.add(s); 
		}
		
		String onmorphisms = "\"onMorphisms\":[\n" + PrettyPrinter.sep0(",", l) + "]\n}\n";
		
		String ret = "{\n\"ontology\":" + thesig.tojson() + ",\n" + onobjects + ",\n" + onmorphisms;
		
//		try {
//			System.out.println(new JSONParsers.JSONInstParser().parse(new Tokens(ret)));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return ret;
		
	}

	@Override
	public JPanel json() {
		JTextArea q = new JTextArea(tojson());		
		q.setWrapStyleWord(true);
		q.setLineWrap(true);
		JPanel p = new JPanel(new GridLayout(1,1));
		JScrollPane jsc = new JScrollPane(q);
	//	jsc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		p.add(jsc);
		return p;
	}

	public static Instance fromjson(String instance) throws Exception {
		return new JSONParsers.JSONInstParser().parse(new FqlTokenizer(instance)).value;
	}


}