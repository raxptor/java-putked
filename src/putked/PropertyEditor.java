package putked;

import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class PropertyEditor implements Editor
{
	public PropertyEditor()
	{

	}
	
	public int getPriority()
	{
		return 0;
	}
	
	public String getDescription()
	{
		return "Property Editor";
	}
	
	public Parent createEditor(Interop.MemInstance object)
	{
		VBox box = new VBox();

		Label desc = new Label("Property Editor");
		Button save = new Button("Save");
	
		HBox header = new HBox();
		header.getChildren().add(desc);
		header.getChildren().add(save);
		
		box.getChildren().add(header);
		
		StructEditor se = new StructEditor(object, null);
		box.getChildren().add(se.createUI());
		return box;
	}
}

