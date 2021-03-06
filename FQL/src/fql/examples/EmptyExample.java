package fql.examples;

public class EmptyExample extends Example {

	@Override
	public String getName() {
		return "All Syntax";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = 
			"//illustrates all possible FQL declarations"
					+ "\n"
					+ "\nschema C = {nodes; attributes; arrows; equations;}"
					+ "\nschema C1 = void"
					+ "\nschema C2 = unit"
					+ "\nschema C3 = (C + C)"
					+ "\nschema C4 = (C * C)"
					+ "\n//schema C4 = (C ^ C)"
					+ "\n"
					+ "\nmapping F = id C"
					+ "\nmapping F1 = (F then F)"
					+ "\nmapping F2 = {nodes; attributes; arrows;} : C -> C"
					+ "\nmapping F3 = inl C C"
					+ "\nmapping F4 = inr C C"
					+ "\nmapping F5 = (F3 + F4)"
					+ "\nmapping F6 = fst C C"
					+ "\nmapping F7 = snd C C"
					+ "\nmapping F8 = (F6 * F7)"
					+ "\nmapping F9 = void C"
					+ "\nmapping F10= unit C"
					+ "\n//mapping F11= eval C C"
					+ "\n//mapping F12= curry id (C*C)"
					+ "\n"
					+ "\nquery q = delta F pi F sigma F"
					+ "\nquery p = (q then q)"
					+ "\n"
					+ "\ninstance I  = { nodes; attributes; arrows; } : C"
					+ "\ninstance I1  = delta F I"
					+ "\ninstance I2  = pi F I"
					+ "\ninstance I3  = sigma F I"
					+ "\ninstance I4  = relationalize I"
					+ "\ninstance I5  = SIGMA F I"
					+ "\ninstance I8  = (I + I)"
					+ "\n//instance I9 = (I * I)"
					+ "\n//instance I10 = (I ^ I)"
					+ "\ninstance I11 = unit C"
					+ "\ninstance I12 = void C"
					+ "\n//instance I3 = prop C"
					+ "\ninstance I6  = external C name"
					+ "\ninstance I7  = eval q I"
					+ "\n"
					+ "\ntransform t1 = id I"
					+ "\ntransform t2 = (t1 then t1)"
					+ "\ntransform t3 = {nodes;} : I -> I"
					+ "\ntransform t4 = I8.inl"
					+ "\ntransform t5 = I8.inr"
					+ "\ntransform t6 = I8.(t4+t5)"
					+ "\n//transform t9 = I9.(t7*t9)"
					+ "\n//transform t10= I9.fst"
					+ "\n//transform t11= I9.snd"
					+ "\n//transform t12 = I11.unit I"
					+ "\ntransform t13 = I12.void I"
					+ "\n//transform t14 = I8.(t4*t5)"
					+ "\n//transform curry"
					+ "\n//transform eval"
					
					+ "\n\ndrop I t1\n\n";
					


}
