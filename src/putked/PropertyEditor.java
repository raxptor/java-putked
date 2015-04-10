package putked;

import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
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

		Button save = new Button("Save");
		save.setMaxWidth(Double.MAX_VALUE);
	
		StructEditor se = new StructEditor(object, null);
		
		VBox k = new VBox(se.createUI());
		k.setPadding(new Insets(5));
		
		ScrollPane sp = new ScrollPane(k);	
		VBox.setVgrow(sp,  Priority.ALWAYS);
				
		box.getChildren().add(sp);
		return box;
	}
}

