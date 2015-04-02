package putked;

import java.util.ArrayList;

import putked.Interop.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

class EditorCreator
{
	public static FieldEditor createEditor(MemInstance mi, Field field, int index, boolean asArray)
	{
		field.setArrayIndex(index);
		if (asArray)
			return new ArrayEditor(mi, field);
		
		switch (field.getType())
		{
			case 5:
				return new StructEditor(field.getStructInstance(mi), field.getName());
			case 3:
				return new PointerEditor(mi, field, index);
			case 0:
				return new Int32Editor(mi, field, index);
			case 9:
				return new FloatEditor(mi, field, index);
			default:
				return new StringEditor(mi, field, index);
		}
	}
	
	public static Node makeFieldLabel(Field fi)
	{
		Label lbl = new Label(fi.getName());
		lbl.setMinWidth(120);
//		lbl.setMinHeight(24);
		lbl.setAlignment(Pos.CENTER_LEFT);
		return lbl;
	}
	
	public static Node makeArrayFieldLabel(Field fi, int index)
	{
		Label lbl = new Label(fi.getName() + "[" + index + "]");
		lbl.setMinWidth(120);
		return lbl;
	}
	
	public static Node makeLabel(Field fi, int index)
	{
		if (fi.isArray())
			return makeArrayFieldLabel(fi, index);
		else
			return makeFieldLabel(fi);
	}
}

class StringEditor implements FieldEditor
{
	MemInstance m_mi;
	Field m_f;
	int m_index;
	
	public StringEditor(MemInstance mi, Field f, int index)
	{
		m_mi = mi;
		m_f = f;
		m_index = index;
	}
	
	@Override
	public Node createUI()
	{
		m_f.setArrayIndex(m_index);
		TextField tf = new TextField(m_f.getString(m_mi));
		return tf;
	}	
}

class Int32Editor implements FieldEditor
{
	MemInstance m_mi;
	Field m_f;
	int m_index;
	
	public Int32Editor(MemInstance mi, Field f, int index)
	{
		m_mi = mi;
		m_f = f;
		m_index = index;
	}
	
	@Override
	public Node createUI()
	{
		m_f.setArrayIndex(m_index);
		TextField tf = new TextField(new Integer(m_f.getInt32(m_mi)).toString());
		return tf;
	}
}

class FloatEditor implements FieldEditor
{
	MemInstance m_mi;
	Field m_f;
	int m_index;
	
	public FloatEditor(MemInstance mi, Field f, int index)
	{
		m_mi = mi;
		m_f = f;
		m_index = index;
	}
	
	@Override
	public Node createUI()
	{
		m_f.setArrayIndex(m_index);
		TextField tf = new TextField(new Float(m_f.getFloat(m_mi)).toString());
		return tf;
	}
}

class PointerEditor implements FieldEditor
{
	MemInstance m_mi;
	Field m_f;
	int m_index;
	
	public PointerEditor(MemInstance mi, Field f, int index)
	{
		m_mi = mi;
		m_f = f;
		m_index = index;
	}
	
	@Override
	public Node createUI() 
	{
		ArrayList<Node> al = new ArrayList<>();
		
		HBox ptrbar = new HBox();
		m_f.setArrayIndex(m_index);
		
		TextField tf = new TextField(m_f.getPointer(m_mi));
		tf.setEditable(false);
		tf.setDisable(true);
		HBox.setHgrow(tf,  Priority.ALWAYS);
		
		ptrbar.getChildren().setAll(tf);
		al.add(ptrbar);
		
		if (m_f.isAuxPtr())
		{
			m_f.setArrayIndex(m_index);
			String ref = m_f.getPointer(m_mi);
			if (ref.length() > 0)
			{
				MemInstance mi = Interop.s_wrap.load(ref);
				if (mi != null)
				{
					StructEditor se = new StructEditor(mi, "AUX");
				
					ArrayList<Node> tmp = new ArrayList<>();
					tmp.add(se.createUI());
					VBox aux = new VBox();
					aux.setFillWidth(true);
					aux.getChildren().setAll(tmp);
					aux.setPadding(new Insets(4));
					aux.setStyle("-fx-border-insets: 1;");
					aux.setVisible(false);
					aux.setManaged(false);
					al.add(aux);
					
					Button expand = new Button("V");			
					expand.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							aux.setVisible(!aux.isVisible());
							aux.setManaged(aux.isVisible());
						}
					});			
					ptrbar.getChildren().setAll(expand, tf);
				}
				else
				{
					tf.setStyle("-fx-background-color: red");
				}
			}
		}
		
		VBox tot = new VBox();
		tot.setFillWidth(true);
		tot.setMaxWidth(Double.MAX_VALUE);
//		tot.setStyle("-fx-background-color: green");
		tot.getChildren().setAll(al);
		return tot;
	}	
}

class ArrayEditor implements FieldEditor
{
	MemInstance m_mi;
	Field m_f;
	
	public ArrayEditor(MemInstance mi, Field field)
	{
		m_mi = mi;
		m_f = field;
	}
	
	@Override
	public Node createUI() 
	{
		ArrayList<Node> out = new ArrayList<>();

		/*
		HBox hdr = new HBox();
		hdr.getChildren().add(EditorCreator.makeFieldLabel(m_f));
		hdr.getChildren().add(new Label("Array of " + m_f.getArraySize(m_mi) + " item(s)"));
		out.add(hdr);
		*/
		
		Label hl = new Label(m_f.getName() + ": Array of " + m_f.getArraySize(m_mi) + " items(s)");
		hl.setMaxWidth(Double.MAX_VALUE);
		hl.setAlignment(Pos.BASELINE_CENTER);
		hl.setPrefHeight(30);
		out.add(hl);
		
		GridPane gridpane = new GridPane();
		gridpane.setMaxWidth(Double.MAX_VALUE);
		
		ColumnConstraints column0 = new ColumnConstraints(-1,-1,Double.MAX_VALUE);
		ColumnConstraints column1 = new ColumnConstraints(100,100,Double.MAX_VALUE);
	    column1.setHgrow(Priority.SOMETIMES);
	    gridpane.getColumnConstraints().setAll(column0, column1);
	     
		int size = m_f.getArraySize(m_mi);
		
		for (int i=0;i<size;i++)
		{
			Label lbl = new Label(" " + i);
			lbl.setMaxHeight(Double.MAX_VALUE);
			lbl.setStyle("-fx-background-color: #fee; -fx-border-color: #fbb;");
			lbl.setAlignment(Pos.CENTER);
			
			gridpane.add(lbl,  0,  i);
			GridPane.setValignment(lbl, VPos.TOP);
			FieldEditor fe = EditorCreator.createEditor(m_mi, m_f, i, false);
			Node ed = fe.createUI();
			gridpane.add(ed,  1,  i);
			
			Button rm_btn = new Button("-");
			gridpane.add(rm_btn, 2, i);		
			GridPane.setValignment(rm_btn,  VPos.TOP);
		}
		
		out.add(gridpane);
		
		Button add = new Button("+" + m_f.getName());
		out.add(add);
		
		VBox box = new VBox();
		box.getChildren().setAll(out);
		box.setFillWidth(true);
		return box;
	}
}

class StructEditor implements FieldEditor
{
	MemInstance m_mi;
	String m_name;
	
	public StructEditor(MemInstance mi, String name)
	{
		m_mi = mi;
		m_name = name;
	}
	
	@Override
	public Node createUI() 
	{
		ArrayList<Node> nodes = new ArrayList<>();
		
		boolean giveRect = false;

	
		if (m_name != null && !m_name.equals("parent"))
		{
			Label header = new Label(m_name + " (" + m_mi.getType().getName() + ")");
			header.setMaxWidth(Double.MAX_VALUE);
			header.setStyle("-fx-background-color: #ddf; -fx-border-insets: 2");
			header.setAlignment(Pos.CENTER);
			header.setMaxHeight(Double.MAX_VALUE);
			nodes.add(header);
			giveRect = true;
		}


		for (int i=0;true;i++)
		{
			Interop.Field f = m_mi.getType().getField(i);
			if (f == null || !f.showInEditor())
				break;
			
			FieldEditor fe = EditorCreator.createEditor(m_mi, f, 0, f.isArray());
			Node ed = fe.createUI();
			
			
			// array or struct or pointer dont get labels.
			if (f.isArray() || f.getType() == 5)
			{
				nodes.add(ed);
			}
			else
			{
				if (f.getType() == 3 && f.isAuxPtr())
				{
					// aux objs get vbox
					VBox b = new VBox();
					b.setMaxWidth(Double.MAX_VALUE);
					b.setFillWidth(true);
					b.getChildren().setAll(EditorCreator.makeLabel(f, 0), ed);
					nodes.add(b);
				}
				else
				{
					// fields get hbox
					HBox hb = new HBox();
					hb.setMaxWidth(Double.MAX_VALUE);
					hb.getChildren().setAll(EditorCreator.makeLabel(f, 0), ed);
					HBox.setHgrow(ed, Priority.ALWAYS);
					nodes.add(hb);
				}
			}
		}
	
		VBox box = new VBox();
		box.setFillWidth(true);
		box.getChildren().setAll(nodes);
		
		if (giveRect)
		{
//			box.setStyle("-fx-background-color: #e0f0f0;");
			box.setMaxWidth(Double.MAX_VALUE);			
		}
		return box;
	}
}

public class ObjectEditor
{
	VBox m_props;
	MemInstance m_mi;
	StructEditor m_root;
	
	public ObjectEditor(MemInstance mi)
	{
		m_props = new VBox();
		m_mi = mi;
		
		m_root = new StructEditor(mi, null);		
		m_props.getChildren().setAll(m_root.createUI());
		m_props.setMinWidth(400);
	}
	
	public void constructField(Interop.Field f)
	{
		
	}
	
	public Parent getRoot()
	{
		return m_props;
	}

}
