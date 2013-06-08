package fql.examples;

public class WeirdExample extends Example {

	@Override
	public String getName() {
		return "Weird";
	}

	@Override
	public String getText() {
		return s;
	}
String s = "schema Z2 = {"
		+"\n	r: G -> G;"
		+"\n	G.r.r = G"
		+"\n}"
		+"\n"
		+"\nschema Z4 = {	r: G -> G;"
		+"\n	G.r.r.r.r = G"
		+"\n}"
		+"\n"
		+"\nmapping F:Z2->Z4 = {"
		+"\n	G->G;"
		+"\n	r->G.r.r"
		+"\n}"
		+"\n"
		+"\n"
		+"\n/*control*/"
		+"\n"
		+"\ninstance J:Z2 = {"
		+"\n 	G={0};"
		+"\n 	r={(0,0)}"
		+"\n}"
		+"\ninstance Pi_FJ:Z4 = pi F J"
		+"\n"
		+"\ninstance I0:Z2 = {"
		+"\n 	G={1,-1};"
		+"\n 	r={(1,-1),(-1,1)}"
		+"\n}"
		+"\ninstance Pi_FI0:Z4 = pi F I0"
		+"\n"
		+"\ninstance I1:Z2 = {"
		+"\n 	G={a0,1,-1};"
		+"\n 	r={(a0,a0),(1,-1),(-1,1)}"
		+"\n}"
		+"\ninstance Pi_FI1:Z4 = pi F I1"
		+"\n"
		+"\ninstance I2:Z2 = {"
		+"\n 	G={a0,b0,1,-1};"
		+"\n 	r={(a0,a0),(b0,b0),(1,-1),(-1,1)}"
		+"\n}"
		+"\ninstance Pi_FI2:Z4 = pi F I2"
		+"\n"
		+"\ninstance I3:Z2 = {"
		+"\n 	G={a0,b0,c0,1,-1};"
		+"\n 	r={(a0,a0),(b0,b0),(c0,c0),(1,-1),(-1,1)}"
		+"\n}"
		+"\ninstance Pi_FI3:Z4 = pi F I3"
		+"\n"
		+"\ninstance I4:Z2 = {"
		+"\n 	G={a0,b0,c0,d0,1,-1};"
		+"\n 	r={(a0,a0),(b0,b0),(c0,c0),(d0,d0),(1,-1),(-1,1)}"
		+"\n}"
		+"\ninstance Pi_FI4:Z4 = pi F I4";
}