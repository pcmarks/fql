package fql;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 
 * @author ryan
 *
 * Contains global constants for debugging.
 */
public class DEBUG {
	
	//TODO make sure equality takes node order into account

	//TODO cat to sig should output equalitites
	
	public static String prelude = 
			"DROP DATABASE FQL; CREATE DATABASE FQL; USE FQL; SET @guid := 0;";
	
	public static  boolean MultiView = true;

	public static int varlen = 128;

	//public static boolean SHOW_QUERY_PATHS = true;
	
	//public static  Intermediate INTERMEDIATE = Intermediate.NONE;

	public static  boolean VALIDATE = true;
	
	public static  int MAX_PATH_LENGTH = 8;

	public static  int MAX_DENOTE_ITERATIONS = 64;
	
	public static boolean ALL_GR_PATHS = false;

//	public static boolean VALIDATE_WITH_EDS = false;
	
	//public static int MAX_JOIN_SIZE = 1024;
	
	public static  boolean ALLOW_INFINITES = false; 
	
	//public static boolean CHECK_MAPPINGS = false;
	
//	public static boolean DO_NOT_GUIDIFY = false;
	
	
	public static boolean schema_graphical = true;
	public static boolean schema_tabular = true;
	public static boolean schema_textual = true;
	public static boolean schema_denotation = true;
	public static boolean schema_ed = true;

	public static boolean mapping_graphical = true;
	public static boolean mapping_tabular = true;
	public static boolean mapping_textual = true;
	public static boolean mapping_ed = true;

	public static boolean inst_graphical = true;
	public static boolean inst_tabular = true;
	public static boolean inst_textual = true;
	public static boolean inst_joined = true;
	public static boolean inst_gr = true;
	public static boolean inst_obs = true;

	
	//public enum Intermediate { SOME, NONE, ALL };
	
	static String label1text = "If un-checked, the schemas in the viewer for queries will not contain any arrows.";
	static String label2text = "<html>The none and some options only shows declarations from the input program.<br>The all option shows all declarations including those generated by query composition.<br>The some option suppresses identity mappings.</html>";
	static String label3text = "<html>Instances in FQL must have globally unique keys.<br>To ensure this, FQL (and the generated SQL) will often compute new isomorphic instances with freshly chosen keys.<br>For debugging purposes it is sometimes useful to suppress this behavior.";
	static String label4text = "<html>By not performing the check that a mapping takes path equivalences to path equivalences,<br>it is possible to compute Delta for infinite schemas.<br>See the employees example.</html>";
	static String label5text = "This is an internal consistency check that checks if intermediate categories generated by FQL do in fact obey the category axioms.";
	static String label6text = "Bounds the maximum length that the paths in a schema can be.";
	static String label7text = "Bounds the maximum size of the category denoted by schemas.";
	static String label8text = "Sets the size of Strings in the SQL output (used for ID columns and string columns).";
	static String labelMtext = "Allows multiple viewers for the same editor.";
	
	public static void showOptions() {
		JPanel p = new JPanel(new GridLayout(11, 2));
	/*
		JCheckBox ed = new JCheckBox("", VALIDATE_WITH_EDS);
		ed.setToolTipText("Validates Data Migration using Embedded Dependencies");
		JLabel edL = new JLabel("Validate using EDs:");
		p.add(edL);
		p.add(ed);
		*/
		JCheckBox gr = new JCheckBox("", ALL_GR_PATHS);
		gr.setToolTipText("Show all paths in category of elements");
		JLabel grL = new JLabel("Show all paths in Grothendieck:");
		p.add(grL);
		p.add(gr);
		
		JCheckBox jcbM = new JCheckBox("", MultiView);
		jcbM.setToolTipText(labelMtext);
		JLabel labelM = new JLabel("Allow multiple viewers per editor:");
		p.add(labelM);
		p.add(jcbM);
		
		
		
		
		
		/* JCheckBox jcbX = new JCheckBox("", DO_NOT_GUIDIFY);
			JPanel p0 = new JPanel();
		JLabel label3 = new JLabel("Do not GUIDify (dangerous):");
		label3.setToolTipText(label3text);
			p.add(label3);
			p.add(jcbX); */
		
		JCheckBox jcb0 = new JCheckBox("", ALLOW_INFINITES);
	//	JPanel p0 = new JPanel();
		JLabel label4 = new JLabel("Do not validate mappings (allows infinte schemas):"); 
		label4.setToolTipText(label4text);
		p.add(label4);
		p.add(jcb0);
		
		JCheckBox jcb = new JCheckBox("", VALIDATE);
	//	JPanel p0 = new JPanel();
		JLabel label5 = new JLabel("Validate all categories:");
		label5.setToolTipText(label5text);
		p.add(label5);
		p.add(jcb);
		//p.add(jcb);
		
		//JPanel p1 = new JPanel();
		JTextField plen = new JTextField(Integer.toString(MAX_PATH_LENGTH));
		JLabel label6 = new JLabel("Maximum path length:");
		label6.setToolTipText(label6text);
		p.add(label6);
		p.add(plen);
		//p.add(p1);
		
		//JPanel p2 = new JPanel();
		JTextField iter = new JTextField(Integer.toString(MAX_DENOTE_ITERATIONS));
		JLabel label7 = new JLabel("Maximum category size:");
		label7.setToolTipText(label7text);
		p.add(label7);
		p.add(iter);
		//p.add(p2);
		
		JTextField vlen = new JTextField(Integer.toString(varlen));
		JLabel label8 = new JLabel("VARCHAR size:");
		label8.setToolTipText(label8text);
		p.add(label8);
		p.add(vlen);
		
		JTextField area = new JTextField(12);
		//JTextArea area = new JTextArea(1, 14);
		area.setText(prelude);
		JLabel areaLabel = new JLabel("Generated SQL prelude:");
		areaLabel.setToolTipText("Set the prelude for the generated SQL.");
		p.add(areaLabel);
		p.add(area);
		area.setMaximumSize(new Dimension(200,300));
		
		//JPanel schemaArea = new JPanel();
		JPanel schemaArea = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JCheckBox schema_graphical_box = new JCheckBox("Graph", schema_graphical);
		JCheckBox schema_textual_box = new JCheckBox("Text", schema_textual);
		JCheckBox schema_tabular_box = new JCheckBox("Table", schema_tabular);
		JCheckBox schema_ed_box = new JCheckBox("ED", schema_ed);
		JCheckBox schema_denotation_box = new JCheckBox("Denotation", schema_denotation);
		schemaArea.add(schema_graphical_box);
		schemaArea.add(schema_textual_box);
		schemaArea.add(schema_tabular_box);
		schemaArea.add(schema_ed_box);
		schemaArea.add(schema_denotation_box);
		
		JLabel schema_label = new JLabel("Schema viewer panels:");
		schema_label.setToolTipText("Sets which viewers to use for schemas.");
		p.add(schema_label);
		p.add(schemaArea);

		JPanel mappingArea = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JCheckBox mapping_graphical_box = new JCheckBox("Graph", mapping_graphical);
		JCheckBox mapping_textual_box = new JCheckBox("Text", mapping_textual);
		JCheckBox mapping_tabular_box = new JCheckBox("Table", mapping_tabular);
		JCheckBox mapping_ed_box = new JCheckBox("ED", mapping_ed);
		mappingArea.add(mapping_graphical_box);
		mappingArea.add(mapping_textual_box);
		mappingArea.add(mapping_tabular_box);
		mappingArea.add(mapping_ed_box);
		
		JLabel mapping_label = new JLabel("Mapping viewer panels:");
		schema_label.setToolTipText("Sets which viewers to use for mappings.");
		p.add(mapping_label);
		p.add(mappingArea);
		
		JPanel instArea = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JCheckBox inst_graphical_box = new JCheckBox("Graph", inst_graphical);
		JCheckBox inst_textual_box = new JCheckBox("Text", inst_textual);
		JCheckBox inst_tabular_box = new JCheckBox("Table", inst_tabular);
		JCheckBox inst_joined_box = new JCheckBox("Joined", inst_joined);
		JCheckBox inst_gr_box = new JCheckBox("Elements", inst_gr);
		JCheckBox inst_obs_box = new JCheckBox("Observables", inst_obs);
		instArea.add(inst_graphical_box);
		instArea.add(inst_textual_box);
		instArea.add(inst_tabular_box);
		instArea.add(inst_joined_box);
		instArea.add(inst_gr_box);
		instArea.add(inst_obs_box);
		
		JLabel inst_label = new JLabel("Instance viewer panels:");
		inst_label.setToolTipText("Sets which viewers to use for instances.");
		p.add(inst_label);
		p.add(instArea);


		

//		JTextField sz = new JTextField(Integer.toString(MAX_JOIN_SIZE));
//		p.add(new JLabel("Maximum potential join size:"));
//		p.add(iter);
		
		int ret = JOptionPane.showConfirmDialog(null, p, "Options", JOptionPane.OK_CANCEL_OPTION);
		if (ret == JOptionPane.YES_OPTION) {
			int a = MAX_PATH_LENGTH;
			int b = MAX_DENOTE_ITERATIONS;
			int d = varlen;
			//int c = MAX_JOIN_SIZE;
			try {
				a = Integer.parseInt(plen.getText());
				b = Integer.parseInt(iter.getText());
				d = Integer.parseInt(vlen.getText());
		//		c = Integer.parseInt(sz.getText());
			} catch (NumberFormatException nfe) {
				return;
			}
//			VALIDATE_WITH_EDS = ed.isSelected();
			ALL_GR_PATHS = gr.isSelected();
			ALLOW_INFINITES = jcb0.isSelected();
			VALIDATE = jcb.isSelected();
			//DO_NOT_GUIDIFY = jcbX.isSelected();
			//SHOW_QUERY_PATHS = jcb1.isSelected();
			MultiView = jcbM.isSelected();
		
			
	//		CHECK_MAPPINGS = jcb1.isSelected();
			MAX_PATH_LENGTH = a;
			MAX_DENOTE_ITERATIONS = b;
			varlen = d;
			prelude = area.getText();
			
			schema_denotation = schema_denotation_box.isSelected();
			schema_ed = schema_ed_box.isSelected();
			schema_graphical = schema_graphical_box.isSelected();
			schema_tabular = schema_tabular_box.isSelected();
			schema_textual = schema_textual_box.isSelected();
			
			mapping_ed = mapping_ed_box.isSelected();
			mapping_graphical = mapping_graphical_box.isSelected();
			mapping_tabular = mapping_tabular_box.isSelected();
			mapping_textual = mapping_textual_box.isSelected();
			
			inst_graphical = inst_graphical_box.isSelected();
			inst_tabular = inst_tabular_box.isSelected();
			inst_textual = inst_textual_box.isSelected();
			inst_joined = inst_joined_box.isSelected();
			inst_gr = inst_gr_box.isSelected();
			inst_obs = inst_obs_box.isSelected();

		

			//MAX_JOIN_SIZE = c;
		}
	}
	
	public static void showAbout() {
		JOptionPane.showMessageDialog(null, about, "About", JOptionPane.INFORMATION_MESSAGE);
	}
	
	static String about = "FQL IDE Copyright (C) 2013 David Spivak and Ryan Wisnesky"
			              + "\nLicense: Creative-Commons Attribution-NonCommercial-NoDerivs 3.0 Unported"
			 	          + "\n\nLibraries used:\n\nJParsec (parsing)\nJUNG (graph visualization)\nRSyntaxTextArea (code editor)";

	public static int chase_limit = 64;


	

}
