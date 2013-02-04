package fql;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

public class Mapping implements Viewable<Mapping> {
	
	
	
	Map<Node, Node> nm = new HashMap<Node, Node>();
	Map<Edge, Path> em = new HashMap<Edge, Path>();
	Signature source, target;
	String name;
	boolean isId = false;
	
	//TODO (DEFER) add everywhere else identity mapping form?
	
	public Mapping(String name, Environment env, MappingDecl md) throws FQLException {
		this.name = name;
		switch (md.kind) {
		case COMPOSE : 
			Mapping m1 = env.getMapping(md.source);
			Mapping m2 = env.getMapping(md.target);
		
			if (!m2.target.equals(m1.source)) {
				throw new FQLException("Ill-typed: " + md);
			}
			this.source = m2.source;
			this.target = m1.target;
			for (Node k : m1.source.nodes) {
				Node v = m1.nm.get(k);
				nm.put(k, m2.nm.get(v));
			}
			for (Edge k : m1.source.edges) {
				Path v = m1.em.get(k);
				Path p0 = expand(v, m2.nm, m2.em);
				em.put(k, p0);
			}
			
			break;
		case ID : 
			Signature s = env.getSchema(md.schema);
			identity(env, s); break;
		case MORPHISM :
			morphism(env.getSchema(md.source), env.getSchema(md.target), md.objs, md.arrows);
		}
		abortIfNotWf();
	}
	
	private Path expand(Path v, Map<Node, Node> nm2, Map<Edge, Path> em2) {
		Node newhead = nm2.get(v.source);
		Node newtarget = nm2.get(v.target);
		List<Edge> newedges = new LinkedList<Edge>();
		for (Edge e : v.path) {
			Path p = em2.get(e);
			newedges.addAll(p.path);
			newtarget = p.target;
		}
		return new Path(newhead, newtarget, newedges);
	}

	public Mapping(Environment env, Signature s) throws FQLException {
		identity(env, s);
	}
	
	public Mapping(String name, Signature source, Signature target,
			List<Pair<String, String>> objs,
			List<Pair<String, List<String>>> arrows) throws FQLException {
		this.name = name;
		morphism(source, target, objs, arrows);
	}

	private void morphism(Signature source, Signature target,
			List<Pair<String, String>> objs,
			List<Pair<String, List<String>>> arrows) throws FQLException {
		this.source = source;
		this.target = target;
		for (Pair<String, String> p : objs) {
			Node sn = this.source.getNode(p.first);
			Node tn = this.target.getNode(p.second);
			nm.put(sn, tn);
		}
		for (Pair<String, List<String>> arrow : arrows) {
			Edge e = this.source.getEdge(arrow.first);
			Path p = new Path(this.target, arrow.second);
			em.put(e, p);
		}
		for (Node n : this.source.nodes) {
			if (nm.get(n) == null) {
				throw new FQLException("Missing node mapping from " + n + " in " + name + "\n" + this);
			}
		}
		for (Edge e : this.source.edges) {
			if (em.get(e) == null) {
				throw new FQLException("Missing edge mapping from " + e + " in " + name);
			}
		}
	}
	
	public void abortIfNotWf() throws FQLException {
		//TODO: (DEFER) eq-paths check for mappings
	}

	
	private void identity(Environment env, Signature s)  throws FQLException {
		for (Node n : s.nodes) {
			nm.put(n, n);
		}
		for (Edge e : s.edges) {
			em.put(e, new Path(s, e));
		}
		this.source = s;
		this.target = s;
		isId = true;
	}

	@Override
	public JPanel view() {
		Object[][] arr = new Object[nm.size()][2];
		int i = 0;
		for (Entry<Node, Node> eq : nm.entrySet()) {
			arr[i][0] = eq.getKey();
			arr[i][1] = eq.getValue();
			i++;
		}
		JTable nmC = new JTable(arr, new Object[] { "Source node in " + source.name0 , "Target node in " + target.name0});
		MouseListener[] listeners = nmC.getMouseListeners();
		for (MouseListener l : listeners)
		{
		    nmC.removeMouseListener(l);
		}
		
		Object[][] arr2 = new Object[em.size()][2];
		int i2 = 0;
		for (Entry<Edge, Path> eq : em.entrySet()) {
			arr2[i2][0] = eq.getKey();
			arr2[i2][1] = eq.getValue().toLong();
			i2++;
		}
		JTable emC = new JTable(arr2, new Object[] { "Source edge in " + source.name0 , "Target path in " + target.name0});
		listeners = emC.getMouseListeners();
		for (MouseListener l : listeners)
		{
		    emC.removeMouseListener(l);
		}
		
		JPanel p = new JPanel(new GridLayout(1,2));
		//p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		//JSplitPane p = new FQLSplit(.5, JSplitPane.VERTICAL_SPLIT);
		
//		nmC.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
	//	emC.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
		JScrollPane q1 = new JScrollPane(nmC);
		JScrollPane q2 = new JScrollPane(emC);
		
		JPanel j1 = new JPanel(new GridLayout(1,1));
		j1.add(q1);
		j1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Node mapping"));		
		p.add(j1);

		JPanel j2 = new JPanel(new GridLayout(1,1));
		j2.add(q2);
		j2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Edge mapping"));
		p.add(j2);
		
	
		
		
		
		
		
//		p1.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	//	p2.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		
//		JPanel px = new JPanel(new GridLayout(1,1));
//		px.add(p);
//		px.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
//		
		return p;
	}

	static String printNicely(Map<String, RA> r) {
		String s = "";
		for (String k : r.keySet()) {
			RA e = r.get(k);
			s += (k + " = " + e + "\n\n");
		}
		return s;
	}

	@Override
	public JPanel text() {
//		String s = toString().replace(";", "\n\n;\n\n");
		
		String[] t = toString().split(";");
		String ret = "";
		for (String a : t) {
		  ret += (a.trim() + ";\n\n");
		}
		
		JPanel tap = new JPanel(new GridLayout(2,2));

		JTextArea ta = new JTextArea(ret);
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);
		JScrollPane xxx = new JScrollPane(ta);		
		JPanel p = new JPanel(new GridLayout(1,1));
		p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Mapping " + name + " : " + source.name0 + " -> " + target.name0 ));
		p.add(xxx);
		tap.add(p);

		
		String delta = "";
		try {
			delta = printNicely(RA.delta(this));
		} catch (Exception e) {
			delta = e.toString();
		}
		
		String sigma = "";
		try {
			sigma = printNicely(RA.sigma(this));
		} catch (Exception e) {
			sigma = e.toString();
		}
		
		String pi = "";
		try {
			pi = printNicely(RA.pi(this));
		} catch (Exception e) {
			pi = e.toString();
		}

		JTextArea ta2 = new JTextArea(delta);
		ta2.setWrapStyleWord(true);
		ta2.setLineWrap(true);
		JScrollPane xxx2 = new JScrollPane(ta2);		
		JPanel p2 = new JPanel(new GridLayout(1,1));
		p2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Delta " + name + " : " + target.name0 + " -> " + source.name0));
		p2.add(xxx2);
		tap.add(p2);
		

		JTextArea ta3 = new JTextArea(pi);
		ta3.setWrapStyleWord(true);
		ta3.setLineWrap(true);
		JScrollPane xxx3 = new JScrollPane(ta3);		
		JPanel p3 = new JPanel(new GridLayout(1,1));
		p3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Pi "  + name + " : " + source.name0 + " -> " + target.name0));
		p3.add(xxx3);
		tap.add(p3);
		
		JTextArea ta4 = new JTextArea(sigma);
		ta4.setWrapStyleWord(true);
		ta4.setLineWrap(true);
		JScrollPane xxx4 = new JScrollPane(ta4);		
		JPanel p4 = new JPanel(new GridLayout(1,1));
		p4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Sigma " + name + " : " + source.name0 + " -> " + target.name0));
		p4.add(xxx4);
		tap.add(p4);


		
//		
//		tap.setBorder(BorderFactory.createEmptyBorder());
	//	xxx.setBorder(BorderFactory.createEmptyBorder());
//		
//		tap.setSize(600, 600);

//		JPanel q3 = new FQLTextPanel("Delta",delta);
	//	JPanel q4 = new FQLTextPanel("Sigma", sigma);
		//JPanel q5 = new FQLTextPanel("Pi", pi);

		//p1.add(q1);
		//p2.add(q2);
		
//		JPanel s2 = new JPanel(new GridLayout(1,3));
	//	s2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	
		
		return tap;

	}

	
	
	@Override
	public String plan() {
		// TODO (DEFER) plan for mappings
		return "todo, plan for mappings";
	}

	@Override
	public boolean equals0(Mapping view2) {
//		if (Equality.which.equals(Equality.syntactic)) {
//			return equals(view2);
//		}
		return false;
		//TODO (DEFER) eq mapping
	}

	@Override
	public boolean iso(Mapping view) {
		// TODO (DEFER) iso mapping
		return false;
	}

	@Override
	public String isos(Mapping view) {
		// TODO (DEFER) isos mapping
		return "todo - isos mapping";
	}

	@Override
	public String homos(Mapping view) {
		// TODO (DEFER) morphs mapping
		return "todo - morphs mapping";
	}

	public Signature getSource() {
		return source;
	}

	public Signature getTarget() {
		return target;
	}


	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("{ ");
		boolean first = true;
		for (Node k : nm.keySet()) {
			Node v = nm.get(k);
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append("(");
			sb.append(k);
			sb.append(",");
			sb.append(v);
			sb.append(")");
		}
		
		sb.append(" ; ");
		first = true;
		for (Edge k : em.keySet()) {
			Path v = em.get(k);
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append("(");
			sb.append(k);
			sb.append(",");
			sb.append(v);
			sb.append(")");
		}
		sb.append(" }");
		return sb.toString();
	}

	public Path appy(Path path) {
		List<Edge> r = new LinkedList<Edge>();
		for (Edge e : path.path) {
			Path p = em.get(e);
			r.addAll(p.path);
		}
		
		return new Path(nm.get(path.source), nm.get(path.target), r);
	}
	
	public Triple<FinFunctor<String, List<List<String>>, String, List<List<String>>>, Pair<FinCat<String, List<List<String>>>, Pair<Pair<Map<String, String>, Map<String, String>>, Pair<Map<String, Arr<String, List<List<String>>>>, Map<Arr<String, List<List<String>>>, String>>>>, Pair<FinCat<String, List<List<String>>>, Pair<Pair<Map<String, String>, Map<String, String>>, Pair<Map<String, Arr<String, List<List<String>>>>, Map<Arr<String, List<List<String>>>, String>>>>> toFunctor() throws FQLException {
		HashMap<String, String> objMapping = new HashMap<String, String>();
		HashMap<Arr<String,List<List<String>>>, Arr<String,List<List<String>>>> arrowMapping = new HashMap<>();
		
		for (Entry<Node, Node> e : nm.entrySet()) {
			objMapping.put(e.getKey().string, e.getValue().string);
		}
		
		Pair<FinCat<String, List<List<String>>>, Pair<Pair<Map<String, String>, Map<String, String>>, Pair<Map<String, Arr<String, List<List<String>>>>, Map<Arr<String, List<List<String>>>, String>>>> srcCat0 = source.toCategory();
		Pair<FinCat<String, List<List<String>>>, Pair<Pair<Map<String, String>, Map<String, String>>, Pair<Map<String, Arr<String, List<List<String>>>>, Map<Arr<String, List<List<String>>>, String>>>> dstCat0 = target.toCategory();
		
		FinCat<String, List<List<String>>> srcCat = srcCat0.first;
		FinCat<String, List<List<String>>> dstCat = dstCat0.first;
		
		for (Arr<String, List<List<String>>> arroweqc : srcCat.arrows) {
			List<String> arrow = arroweqc.arr.get(0);
			
			List<String> mapped = apply(arrow);
			Arr<String, List<List<String>>> mappedeqc = findeqc(dstCat, mapped);
			arrowMapping.put(arroweqc, mappedeqc);
		}
		
		return new Triple<>(new FinFunctor<>(objMapping, arrowMapping, srcCat, dstCat), srcCat0, dstCat0);
	}

	private Arr<String, List<List<String>>> findeqc(
			FinCat<String, List<List<String>>> cat, List<String> path) {
		for (Arr<String, List<List<String>>> eqc : cat.arrows) {
			if (eqc.arr.contains(path)) {
				return eqc;
			}
		}
		throw new RuntimeException("No equivalence class for " + path + " in " + cat);
	}

	private List<String> apply(List<String> arrow) {
		List<String> ret = new LinkedList<String>();
		for (Entry<Node, Node> e : nm.entrySet()) {
			if (e.getKey().string.equals(arrow.get(0))) {
				ret.add(e.getValue().string);
				break;
			}
		}
		
		for (int i = 1; i < arrow.size(); i++) {
			String s = arrow.get(i);
			for (Entry<Edge, Path> e : em.entrySet()) {
				if (e.getKey().name.equals(s)) {
					List<String> x = e.getValue().asList();
					x.remove(0);
					ret.addAll(x);
				}
			}
		}
		return ret;
	}
	
	
	
	
	public Map<String, Set<Pair<String, String>>> evalDelta(Instance theinstance) throws FQLException {
		
		//System.out.println(FDM.delta(this.toFunctor(), theinstance.toFunctor()));
		
		Map<String, RA> x = RA.delta(this);
		Map<String, Set<String[]>> i0 = Query.convert0(theinstance);
		Map<String, Set<String[]>> i1 = RA.eval0(x, i0);
		return Query.convert(i1);
	}
	

	public Map<String, Set<Pair<String, String>>> evalSigma(Instance theinstance) throws FQLException {
		Map<String, RA> x = RA.sigma(this);
		Map<String, Set<String[]>> i0 = Query.convert0(theinstance);
		Map<String, Set<String[]>> i1 = RA.eval0(x, i0);
		
		//System.out.println(FDM.sigma(this.toFunctor(), theinstance.toFunctor()));

		return Query.convert(i1);
	}
	
	public Map<String, Set<Pair<String, String>>> evalPi(Instance theinstance) throws FQLException {
		

		Map<String, RA> x = RA.pi(this);
		Map<String, Set<String[]>> i0 = Query.convert0(theinstance);
		Map<String, Set<String[]>> i1 = RA.eval0(x, i0);
		
		//System.out.println(FDM.grothendieck(FDM.pi(this.toFunctor().first, theinstance.toFunctor())));

		return Query.convert(i1);
	}
	
	@Override
	public JPanel pretty() throws FQLException {
			Graph<String, String> g = build();
			return doView(g);
	}

	@Override
	public String type() {
		return "mapping";
	}
	

	public Graph<String,String> build() {
		// Graph<V, E> where V is the type of the vertices
	
		Graph<String, String> g2 = new DirectedSparseMultigraph<String, String>();
		for (Node n : source.nodes) {
			g2.addVertex("@source" + "." + n.string);
		}
		
		for (Edge e : source.edges) {
			g2.addEdge("@source" + "." + e.name, "@source" + "." + e.source.string, "@source" + "." + e.target.string);
		}
		
		for (Node n : target.nodes) {
			g2.addVertex("@target" + "." + n.string);
		}
		
		for (Edge e : target.edges) {
			g2.addEdge("@target" + "." + e.name, "@target" + "." + e.source.string, "@target" + "." + e.target.string);
		}
		
		for (Node n : nm.keySet()) {
			Node m = nm.get(n);
			g2.addEdge(n.string + " " + m.string, "@source" + "." + n.string, "@target" + "." + m.string);
		}
		
		return g2;
	}

	public  JPanel doView(Graph<String,String> sgv) {
		// Layout<V, E>, BasicVisualizationServer<V,E>
		Layout<String, String> layout = new FRLayout<>(sgv);
		//Layout<String, String> layout = new KKLayout(sgv);
		//Layout<String, String> layout = new SpringLayout(sgv);

		//	Layout<String, String> layout = new ISOMLayout<String,String>(sgv);
	//			Layout<String, String> layout = new CircleLayout(sgv);
		layout.setSize(new Dimension(600, 400));
		BasicVisualizationServer<String, String> vv = new BasicVisualizationServer<String, String>(
				layout);
		vv.setPreferredSize(new Dimension(600, 400));
		// Setup up a new vertex to paint transformer...
		Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
			public Paint transform(String i) {
				return which(i);
			}

			private Color which(String t) {
				int i = t.indexOf(".");
				//String j = t.substring(i+1);
				String p = t.substring(0, i);
				if (p.equals("@source")) {
					return Environment.colors.get(source.name0);
				} 
					return Environment.colors.get(target.name0);
			}
		};
		// Set up a new stroke Transformer for the edges
		float dash[] = { 10.0f };
		final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		final Stroke bs = new BasicStroke();

		Transformer<String, Stroke> edgeStrokeTransformer = new Transformer<String, Stroke>() {
			public Stroke transform(String s) {
				if (s.contains(" ")) {
					return edgeStroke;
				} 
				return bs;
			}
		};
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>() {
			@Override
			public String transform(String t) {
				int i = t.indexOf(".");
				String j = t.substring(i+1);
				String p = t.substring(0, i);
				if (p.equals("@source")) {
					j = source.name0 + "." + j;
				} else {
					j = target.name0 + "." + j;
				}
				return j;
			}
		});
		vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<String>() {
			
			@Override
			public String transform(String t) {
//				if (t.contains(" ")) {
//					return "";
//				}
//				return t;
				return "";
			}
		});
		//vv.getRenderer().getVertexRenderer().
	//	vv.getRenderContext().setLabelOffset(20);
	//	vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		
		return vv;
	}

	public static Mapping compose(String string, Mapping l, Mapping r) throws FQLException {
		if (!l.getTarget().equals(r.getSource())) {
			throw new RuntimeException(l.getTarget() + "\n\n" + r.getSource());
		}
		
		List<Pair<String, String>> xxx = new LinkedList<>();
		List<Pair<String, List<String>>> yyy = new LinkedList<>();
		
		for (Node n : l.getSource().nodes) {
			xxx.add(new Pair<>(n.string, r.nm.get(l.nm.get(n)).string));
		}
		
		for (Edge e : l.getSource().edges) {
			Path p = l.em.get(e);
			yyy.add(new Pair<>(e.name, r.appy(p).asList()));
		}
		
		return new Mapping(string, l.getSource(), r.getTarget(), xxx, yyy);
	}
	
	
}
