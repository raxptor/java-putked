package putked;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Main extends Application 
{
	public static Main s_instance;
	
	private TabPane m_pane;
	public ObjectLibrary m_objectLibrary;
	private static ArrayList<Editor> m_editors = new ArrayList<>();
		
    public static void main(String[] args) 
    {
    	initEditor();
        Application.launch(args);
    }
    
    public static void initEditor()
    {
    	addEditor(new PropertyEditor()); 	
    }
    
    public static void addEditor(Editor e)
    {
    	m_editors.add(e);
    }
    
    public static ArrayList<Editor> getEditors()
    {
    	return m_editors;
    }
    
    public void startEditing(String path)
    {
    	startEditing(path, new PropertyEditor());
    }
    
    public void startEditing(String path, Editor editor)
    {
    	Interop.MemInstance mi = Interop.s_wrap.load(path);
    	if (mi == null)
    		return;
    	if (!editor.canEdit(mi.getType()))
    		return;
   
    	addTab(editor.createUI(mi), path);
    }
    
    public Stage makeDialogStage(javafx.scene.Scene scene)
    {
    	Stage s = new Stage();
    	s.initModality(Modality.WINDOW_MODAL);
    	s.setTitle("Question");
    	s.setScene(scene);
    	return s;    	
    }
    
    public String askForInstancePath(Interop.Type p)
    {
    	ObservableList<String> all = FXCollections.observableArrayList();
    	ObservableList<ObjectLibrary.ObjEntry> objs = m_objectLibrary.getAllObjects();
    	for (ObjectLibrary.ObjEntry o : objs)
    	{
    		if (o.type != null && o.type.hasParent(p))
    			all.add(o.path);
    	}
    	
       	ListView<String> opts = new ListView<>();
    	opts.setItems(all);
    	opts.getSelectionModel().select(0);

    	VBox box = new VBox();
    	Button ok = new Button("OK");
    	Button cancel = new Button("Cancel");
    	ok.setMaxWidth(Double.MAX_VALUE);
    	cancel.setMaxWidth(Double.MAX_VALUE);
    	
    	class Tmp {
    		String out = null;
    	};
    	
    	final Tmp holder = new Tmp();

    	Scene scene = new Scene(box, 300, 400);
    	Stage stage = makeDialogStage(scene);
   	
    	ok.setOnAction((evt) -> {
    		holder.out = opts.getSelectionModel().getSelectedItem();
    		stage.hide();
    	});

    	cancel.setOnAction( (evt) -> {
    		stage.hide();
    	});

    	box.getChildren().setAll(opts, ok, cancel);    	
    	stage.showAndWait();
		return holder.out;
    }
    
    
    class TypeOption
    {
    	public String name;
    	Interop.Type type;
    };

    public Interop.Type askForType()
    {
    	return askForSubType(null, false);
    }

    public Interop.Type askForSubType(Interop.Type p, boolean asAux)
    {
    	ArrayList<Interop.Type> all = Interop.s_wrap.getAllTypes();
    	ObservableList<TypeOption> out = FXCollections.observableArrayList();
    	for (Interop.Type t : all)
    	{
    		if (p == null || t.hasParent(p) && (!asAux || t.permitAsAuxInstance()))
    		{
    			TypeOption to = new TypeOption();
    			to.name = "[" + t.getModule() + "] - " + t.getName();
    			to.type = t;
    			out.add(to);
    		}
    	}
    	
    	if (out.size() == 1)
    		return out.get(0).type;
    	
    	FilteredList<TypeOption> filter = new FilteredList<TypeOption>(out, (obj) -> true);
    	ListView<TypeOption> opts = new ListView<>();
    	opts.setItems(filter);
    	opts.getSelectionModel().select(0);

    	VBox box = new VBox();
    	Button ok = new Button("OK");
    	Button cancel = new Button("Cancel");
    	ok.setMaxWidth(Double.MAX_VALUE);
    	cancel.setMaxWidth(Double.MAX_VALUE);
    	
    	TextField searchField = new TextField();
    	searchField.textProperty().addListener( (obs, oldval, newval) -> {
    		filter.setPredicate( (to) -> {
    			return to.name.contains(searchField.textProperty().get());
    		});
    	});
    	
        opts.setCellFactory(new Callback<ListView<TypeOption>, ListCell<TypeOption>>(){
            @Override
            public ListCell<TypeOption> call(ListView<TypeOption> p) {
               ListCell<TypeOption> cell = new ListCell<TypeOption>(){
                    @Override
                    protected void updateItem(TypeOption t, boolean bln) {
                        super.updateItem(t, bln);
                        if (t != null) {
                            setText(t.name);
                        } else {
                        	setText("");
                        }
                    }
                };
                return cell;
            }
        });
        
    	class Tmp {
    		Interop.Type out = null;
    	};
    	
    	final Tmp holder = new Tmp();

    	Scene scene = new Scene(box, 300, 400);
    	Stage stage = makeDialogStage(scene);
   	
    	ok.setOnAction((evt) -> {
    		holder.out = opts.getSelectionModel().getSelectedItem().type;
    		stage.hide();
    	});

    	cancel.setOnAction( (evt) -> {
    		stage.hide();
    	});

    	box.getChildren().setAll(searchField, opts, ok, cancel);    	
    	stage.showAndWait();
 	
		return holder.out;
    }
    
    public void addTab(Node n, String title)
    {
    	Tab t = new Tab(title);
    	t.setContent(n);
    	m_pane.getTabs().add(t);
    	m_pane.getSelectionModel().select(t);
    }
    
    @Override
    public void start(Stage stage) 
    {
    	s_instance = this;
    	if (!Interop.Load("/tmp/libputked-java-interop.dylib"))
    	{
    		System.out.println("Could not load interop lib");
    		return;
    	}

    	String base = "/Users/dannilsson/git/claw-putki/";
    	Interop.Initialize(base + "/build/libclaw-data-dll.dylib", base);

    	stage.setTitle("PutkEd");
    	
    	SplitPane pane = new SplitPane();
    	m_pane = new TabPane();
    	
    	m_objectLibrary = new ObjectLibrary();
    	    	
    	pane.orientationProperty().set(Orientation.VERTICAL);
    	pane.getItems().add(m_pane);
    	pane.getItems().add(m_objectLibrary.getRoot());
    	
        final Scene scene = new Scene(pane, 800, 400);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());       
        stage.setScene(scene);
        stage.show();
    }     
}
