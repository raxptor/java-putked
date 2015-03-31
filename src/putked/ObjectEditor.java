package putked;

import java.util.ArrayList;

import putked.Interop.*;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
				return new StructEditor(field.getStructInstance(mi));
			default:
				return new StringEditor(mi, field, index);
		}
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
	public void createUI(ArrayList<Node> output) 
	{
		m_f.setArrayIndex(m_index);
		Label lbl = new Label(m_f.getName());
		Label txt = new Label(m_f.getString(m_mi));
		
		lbl.setMinWidth(300);
		lbl.setMinHeight(24);
		
		HBox root = new HBox();
		root.getChildren().setAll(lbl, txt);
		output.add(root);
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
	public void createUI(ArrayList<Node> output) 
	{
		int size = m_f.getArraySize(m_mi);
		for (int i=0;i<size;i++)
		{
			FieldEditor fe = EditorCreator.createEditor(m_mi, m_f, i, false);
			
			ArrayList<Node> tmp = new ArrayList<>();
			
			Label p = new Label(m_f.getName() + "[" + i + "]");
			tmp.add(p);
			fe.createUI(tmp);
			
			VBox row = new VBox();
			
			VBox entry = new VBox();
			entry.getChildren().setAll(tmp);
			entry.setPadding(new Insets(0, 0, 0, 20));
			row.getChildren().setAll(p, entry);
			
			output.add(row);
		}
	}
}

class StructEditor implements FieldEditor
{
	MemInstance m_mi;
	public StructEditor(MemInstance mi)
	{
		m_mi = mi;
	}
	
	@Override
	public void createUI(ArrayList<Node> output) 
	{
		for (int i=0;true;i++)
		{
			Interop.Field f = m_mi.getType().getField(i);
			if (f == null)
				break;
			FieldEditor fe = EditorCreator.createEditor(m_mi, f, 0, f.isArray());
			fe.createUI(output);
		}
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
		
		m_root = new StructEditor(mi);
		
		ArrayList<Node> items = new ArrayList<Node>();
		m_root.createUI(items);
		m_props.getChildren().setAll(items);
	}
	
	public void constructField(Interop.Field f)
	{
		
	}
	
	public Parent getRoot()
	{
		return m_props;
	}

}
