package fql.examples;

public class PiExample extends Example {

	@Override
	public String getName() {
		return "Pi";
	}

	@Override
	public String getText() {
		return s;
	}
	
	public static final String s = 
			"schema C = { "
					+ "\n nodes"
					+ "\n	C1, C2;"
					+ "\n attributes;"
					+ "\n arrows"
					+ "\n 	c : C1 -> C2, "
					+ "\n 	cc : C1 -> C2;"
					+ "\n equations;"
					+ "\n}"
					+ "\n"
					+ "\ninstance I = {"
					+ "\n nodes"
					+ "\n	C1 -> { c1A, c1B, c1C },"
					+ "\n	C2 -> { c2x, c2y};"
					+ "\n attributes;"
					+ "\n arrows"
					+ "\n	c ->  { (c1A,c2x), (c1B,c2x), (c1C,c2x) },"
					+ "\n	cc -> { (c1A,c2x), (c1B,c2x), (c1C,c2y) };"
					+ "\n} : C"
					+ "\n"
					+ "\nschema D = { "
					+ "\n nodes"
					+ "\n 	D1,"
					+ "\n 	D2,"
					+ "\n 	D3;"
					+ "\n attributes;"
					+ "\n arrows"
					+ "\n	d : D1 -> D2, "
					+ "\n	dd : D1 -> D2, "
					+ "\n	ddd : D2 -> D3;"
					+ "\n equations;"
					+ "\n}"
					+ "\n"
					+ "\nmapping F = {"
					+ "\n nodes"
					+ "\n  C1 -> D1,"
					+ "\n  C2 -> D3;"
					+ "\n  attributes;"
					+ "\n  arrows"
					+ "\n  c  -> D1.d.ddd,"
					+ "\n  cc -> D1.dd.ddd;"
					+ "\n} : C -> D"
					+ "\n"
					+ "\ninstance J = pi F I\n";


}
