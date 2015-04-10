package putked;

import java.util.ArrayList;

import putked.Interop.*;
import javafx.beans.value.ChangeListener;
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
			// 8 => bool
			// 7 => file
			case 9:
				return new FloatEditor(mi, field, index);
			case 10:
				return new EnumEditor(mi, field, index);
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
	
	public static String makeInlineAuxTitle(MemInstance mi)
	{
		return mi.getPath() + " (" + mi.getType().getName() + ")";
	}
	
	public static String makeInlineTitle(MemInstance mi)
	{
		return mi.getPath() + " (" + mi.getType().getName() + ")";
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
		tf.textProperty().addListener( (obs, oldValue, newValue) -> {
			m_f.setArrayIndex(m_index);
			m_f.setString(m_mi, newValue);
		});
		return tf;
	}	
}

class EnumEditor implements FieldEditor
{
	MemInstance m_mi;
	Field m_f;
	int m_index;
	
	public EnumEditor(MemInstance mi, Field f, int index)
	{
		m_mi = mi;
		m_f = f;
		m_index = index;
	}
	
	@Override
	public Node createUI()
	{
		m_f.setArrayIndex(m_index);
		ComboBox<String> cb = new ComboBox<>();
		ArrayList<String> values = new ArrayList<>();
		
		int i=0;
		while (true)
		{
			String s = m_f.getEnumPossibility(i);
			if (s == null)
				break;
			values.add(s);
			i++;
		}

		cb.getItems().setAll(values);
		cb.setValue(m_f.getEnum(m_mi));
		cb.valueProperty().addListener( (obs, oldValue, newValue) -> {
			m_f.setArrayIndex(m_index);			
			m_f.setEnum(m_mi,  newValue);
		});

		return cb;
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
		tf.getStyleClass().add("int32-field");
		tf.textProperty().addListener( (obs, oldValue, newValue) -> {
			try 
			{
				int val = Integer.parseInt(newValue);
				m_f.setArrayIndex(m_index);
				m_f.setInt32(m_mi, val);
				tf.getStyleClass().remove("error");
			}
			catch (NumberFormatException u)
			{
				tf.getStyleClass().add("error");
			}
		});
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
		tf.getStyleClass().add("float-field");
		tf.textProperty().addListener( (obs, oldValue, newValue) -> {
			try 
			{
				float f = Float.parseFloat(newValue);
				m_f.setArrayIndex(m_index);
				m_f.setFloat(m_mi, f);
				tf.getStyleClass().remove("error");
			}
			catch (NumberFormatException u)
			{
				tf.getStyleClass().add("error");
			}
		});		
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
		VBox tot = new VBox();

		HBox ptrbar = new HBox();
		m_f.setArrayIndex(m_index);
		
		TextField tf = new TextField(m_f.getPointer(m_mi));
		tf.setEditable(false);
		tf.setDisable(true);
		HBox.setHgrow(tf,  Priority.ALWAYS);
		
		Button clear = new Button("X");
		Button point = new Button("*");
		
		ptrbar.getChildren().setAll(tf, point, clear);
		tot.getChildren().setAll(ptrbar);
		
		clear.setOnAction( (evt) -> {
            	m_f.setArrayIndex(m_index);
            	m_f.setPointer(m_mi, "");
            	tf.textProperty().set("");
            	tot.getChildren().setAll(ptrbar);
            	ptrbar.getChildren().setAll(tf, point);
        });
		
		point.setOnAction( (evt) -> {

			if (m_f.isAuxPtr())
			{
				Interop.Type t = Main.s_instance.askForSubType(Interop.s_wrap.getTypeByName(m_f.getRefType()), true);
				if (t != null)
				{
					MemInstance naux = m_mi.createAuxInstance(t);
	
					m_f.setArrayIndex(m_index);
					m_f.setPointer(m_mi, naux.getPath());
					tf.textProperty().set(EditorCreator.makeInlineAuxTitle(naux));
					
					VBox aux = makeObjNode(naux);					
					aux.setVisible(true);
					aux.setManaged(true);
					
					Button expand = new Button("V");			
					expand.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							aux.setVisible(!aux.isVisible());
							aux.setManaged(aux.isVisible());
						}
					});
					
					// configure for this
					ptrbar.getChildren().setAll(expand, tf, point, clear);
					tot.getChildren().setAll(ptrbar, aux);				
				}
			}
			else
			{
				String path = Main.s_instance.askForInstancePath(Interop.s_wrap.getTypeByName(m_f.getRefType()));
				if (path != null)
				{
					m_f.setArrayIndex(m_index);
					m_f.setPointer(m_mi, path);
					tf.textProperty().set(path);
					ptrbar.getChildren().setAll(tf, point, clear);
					tot.getChildren().setAll(ptrbar);
				}
			}
		});
		
		if (m_f.isAuxPtr())
		{
			m_f.setArrayIndex(m_index);
			String ref = m_f.getPointer(m_mi);
			if (ref.length() > 0)
			{
				MemInstance mi = Interop.s_wrap.load(ref);
				if (mi != null)
				{
					VBox aux = makeObjNode(mi);					
		
					Button expand = new Button("V");			
					expand.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							aux.setVisible(!aux.isVisible());
							aux.setManaged(aux.isVisible());
						}
					});
					
					// configure for this
					ptrbar.getChildren().setAll(expand, tf, point, clear);
					tot.getChildren().setAll(ptrbar, aux);
					tf.getStyleClass().remove("error");
					tf.textProperty().set(EditorCreator.makeInlineAuxTitle(mi));
				}
				else
				{
					tf.getStyleClass().add("error");
				}
			}
		}

		tot.setFillWidth(true);
		tot.setMaxWidth(Double.MAX_VALUE);
		return tot;
	}	
	
	private VBox makeObjNode(MemInstance mi)
	{
		VBox aux = new VBox();
		StructEditor se = new StructEditor(mi, "AUX");
		ArrayList<Node> tmp = new ArrayList<>();
		tmp.add(se.createUI());
		aux.setFillWidth(true);
		aux.getChildren().setAll(tmp);
		aux.setPadding(new Insets(4));
		aux.setStyle("-fx-border-insets: 1;");
		aux.setVisible(false);
		aux.setManaged(false);
		return aux;
	}
}

class ArrayEditor implements FieldEditor
{
	MemInstance m_mi;
	Field m_f;
	ArrayList<Node> m_editors;
	VBox m_box;
	
	public ArrayEditor(MemInstance mi, Field field)
	{
		m_mi = mi;
		m_f = field;
	}
	
	@Override
	public Node createUI() 
	{
	    m_editors = new ArrayList<>();
		int size = m_f.getArraySize(m_mi);		
		for (int i=0;i<size;i++)
		{
			FieldEditor fe = EditorCreator.createEditor(m_mi, m_f, i, false);
			m_editors.add(fe.createUI());
		}	
		m_box = new VBox();
		rebuild();
		return m_box;
	}
	
	private void rebuild()
	{	
		Label hl = new Label(m_f.getName() + ": Array of " + m_f.getArraySize(m_mi) + " items(s)");
		hl.setMaxWidth(Double.MAX_VALUE);
		hl.setAlignment(Pos.BASELINE_CENTER);
		hl.setPrefHeight(30);
		
		Button add = new Button("+" + m_f.getName());
		
		add.setOnAction( (evt) -> {
			int newIndex = m_f.getArraySize(m_mi);
			m_f.setArrayIndex(newIndex);
			m_f.arrayInsert(m_mi);
			FieldEditor fe = EditorCreator.createEditor(m_mi, m_f, newIndex, false);
			m_editors.add(fe.createUI());
			rebuild();
		});		
	
		m_box.getChildren().setAll(hl, buildGridPane(), add);
		m_box.setFillWidth(true);
	}
	
	private Button mkRemoveBtn(int idx)
	{
		Button rm = new Button("-");
		rm.setOnAction((v) -> {
			m_f.setArrayIndex(idx);
			m_f.arrayErase(m_mi);
			m_editors.remove(idx);
			rebuild();
		});
		return rm;
	}
	
	private GridPane buildGridPane()
	{
		GridPane gridpane = new GridPane();
		gridpane.setMaxWidth(Double.MAX_VALUE);
		
		ColumnConstraints column0 = new ColumnConstraints(-1,-1,Double.MAX_VALUE);
		ColumnConstraints column1 = new ColumnConstraints(100,100,Double.MAX_VALUE);
		
		if (((m_f.getType() == 3 && m_f.isAuxPtr()) || m_f.getType() == 5))
			column1.setHgrow(Priority.ALWAYS);
		
	    gridpane.getColumnConstraints().setAll(column0, column1);
	    for (int i=0;i<m_editors.size();i++)
	    {
			Label lbl = new Label(" " + i);
			lbl.setMaxHeight(Double.MAX_VALUE);
			lbl.setAlignment(Pos.CENTER);
			
			gridpane.add(lbl,  0,  i);
			GridPane.setValignment(lbl, VPos.TOP);
			gridpane.add(m_editors.get(i),  1,  i);
			
			Button rm_btn = mkRemoveBtn(i);
			gridpane.add(rm_btn, 2, i);		
			GridPane.setValignment(rm_btn,  VPos.TOP);
		}
		
		return gridpane;
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
