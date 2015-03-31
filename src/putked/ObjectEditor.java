package putked;

import putked.Interop.MemInstance;
import javafx.scene.*;
import javafx.scene.layout.GridPane;

public class ObjectEditor
{
	GridPane m_grid;
	MemInstance m_mi;
	
	public ObjectEditor(MemInstance mi)
	{
		m_grid = new GridPane();
		m_mi = mi;
		
		m_grid.setPrefWidth(300);
		
		Interop.Type t = m_mi.getType();
		for (int i=0;true;i++)
		{
			Interop.Field f = t.getField(i);
			if (f == null)
				break;
			System.out.println("Field[" + i + "] is " + f.getName());
		}
	}
	
	public void constructField(Interop.Field f)
	{
		
	}
	
	public Parent getRoot()
	{
		return m_grid;
	}

}
