package putked;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
 
public class Main extends Application 
{
	public static Interop.NI s_interop;
	public static Main s_instance;
	
	private TabPane m_pane;
	
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
    	addTab(e.createEditor(mi), path);
    }
    
    public Stage makeDialogStage(javafx.scene.Scene scene)
    {
    	Stage s = new Stage();
    	s.initModality(Modality.WINDOW_MODAL);
    	s.setTitle("Question");
    	s.setScene(scene);
    	return s;    	
    }
    
    public Interop.Type askForSubType(Interop.Type p, boolean asAux)
    {
    	ArrayList<Interop.Type> all = Interop.s_wrap.getAllTypes();
    	ObservableList<String> out = FXCollections.observableArrayList();
    	for (Interop.Type t : all)
    	{
    		if (t.hasParent(p) && (!asAux || t.permitAsAuxInstance()))
    			out.add(t.getName());
    	}
    	
    	if (out.size() == 1)
    		return Interop.s_wrap.getTypeByName(out.get(0));
    	
    	ListView<String> opts = new ListView<>();
    	opts.setItems(out);
    	opts.getSelectionModel().select(0);

    	VBox box = new VBox();
    	Button ok = new Button("OK");
    	Button cancel = new Button("Cancel");
    	ok.setMaxWidth(Double.MAX_VALUE);
    	cancel.setMaxWidth(Double.MAX_VALUE);
    	
    	class Tmp {
    		Interop.Type out = null;
    	};
    	
    	final Tmp holder = new Tmp();

    	Scene scene = new Scene(box, 300, 400);
    	Stage stage = makeDialogStage(scene);
   	
    	ok.setOnAction((evt) -> {
    		holder.out = Interop.s_wrap.getTypeByName(opts.getSelectionModel().getSelectedItem());
    		stage.hide();
    	});

    	cancel.setOnAction( (evt) -> {
    		stage.hide();
    	});

    	box.getChildren().setAll(opts, ok, cancel);    	
    	stage.showAndWait();
 	
		return holder.out;
    }
    
    public void addTab(Node n, String title)
    {
    	Tab t = new Tab(title);
    	t.setContent(n);
    	m_pane.getTabs().add(t);
    }
    
    @Override
    public void start(Stage stage) 
    {
    	s_instance = this;
    	s_interop = Interop.Load("/tmp/libputked-java-interop.dylib");
    	String base = "/Users/dannilsson/git/claw-putki/";
    	s_interop.MED_Initialize(base + "/build/libclaw-data-dll.dylib", base);

    	stage.setTitle("PutkEd");
    	
    	SplitPane pane = new SplitPane();
    	m_pane = new TabPane();
    	    	
    	pane.orientationProperty().set(Orientation.VERTICAL);
    	pane.getItems().add(m_pane);
    	pane.getItems().add(new ObjectLibrary().getRoot());
    	
        final Scene scene = new Scene(pane, 800, 400);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());       
        stage.setScene(scene);
        stage.show();
    }     
}
