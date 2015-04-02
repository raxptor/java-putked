package putked;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
 
public class Main extends Application 
{
	public static Interop.NI s_interop;
	public static Main s_instance;
	
    public static void main(String[] args) 
    {
        Application.launch(args);
    }
    
    public void startEditing(String path)
    {
    	Interop.MemInstance mi = Interop.s_wrap.load(path);
    	if (mi == null)
    		return;
   
    	PropertyEditor e = new PropertyEditor();
    	Stage stage = new Stage();
    	Scene scene = new Scene(e.createEditor(mi), 800, 600);
    	stage.setTitle(path);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());       
        stage.setScene(scene);
        stage.show();
    }
    
    @Override
    public void start(Stage stage) 
    {
    	s_instance = this;
    	s_interop = Interop.Load("/tmp/libputked-java-interop.dylib");
    	String base = "/Users/dannilsson/git/claw-putki/";
    	s_interop.MED_Initialize(base + "/build/libclaw-data-dll.dylib", base);
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
    	
    	SplitPane pane = new SplitPane();
    	Pane top = new Pane();
    	
    	pane.orientationProperty().set(Orientation.VERTICAL);
    	pane.getItems().add(top);
    	pane.getItems().add(new ObjectLibrary().getRoot());
    	
        final Scene scene = new Scene(pane, 800, 400);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());       
        stage.setScene(scene);
        stage.show();
    }     
}
