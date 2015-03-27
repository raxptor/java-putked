package putked;

import com.sun.jna.Pointer;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;
 
public class Main extends Application 
{
	public static Interop.NI s_interop;
	
    public static void main(String[] args) 
    {
        Application.launch(args);
    }
    
    
    @Override
    public void start(Stage stage) 
    {
    	s_interop = Interop.Load("/tmp/libputked-java-interop.dylib");
    	String base = "/Users/dannilsson/git/claw-putki/";
    	s_interop.MED_Initialize(base + "/build/libclaw-data-dll.dylib", base + "/data");
    	/*
    	for (int i=0;true;i++)
    	{
    		Pointer p = s_interop.MED_TypeByIndex(i);
    		if (p == Pointer.NULL)
    			break;
    		
    		System.out.println("Loaded type [" + s_interop.MED_Type_GetName(p) + "] [" + s_interop.MED_Type_GetModuleName(p) + "]"); 	
    	}
    	*/
    	stage.setTitle("PutkEd");
        final Scene scene = new Scene(new Group(), 200, 400);
        Group sceneRoot = (Group)scene.getRoot();  
      
        sceneRoot.getChildren().add(new ObjectLibrary().getRoot());
        stage.setScene(scene);
        stage.show();
    }     
}
