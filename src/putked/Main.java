package putked;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
        final Scene scene = new Scene(new ObjectLibrary().getRoot(), 800, 400);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());       
        stage.setScene(scene);
        stage.show();
    }     
}
