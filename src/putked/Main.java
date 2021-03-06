package putked;

import java.util.ArrayList;

import putked.Interop.MemInstance;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

class OpenEditors
{
	public Editor _editor;
	public MemInstance _mi;
	public Tab _tab;
	public String _contentHash;
}

public class Main extends Application 
{
	public static Main s_instance;
		
	private TabPane m_pane;
	public ObjectLibrary m_objectLibrary;
	private static ArrayList<Editor> m_editors = new ArrayList<>();
	private static ArrayList<DataImporter> m_importers = new ArrayList<DataImporter>();
	private static ArrayList<OpenEditors> m_openEditors = new ArrayList<OpenEditors>();
	private static Editor m_defaultEditor =  new PropertyEditor();
	
    public static void main(String[] args) 
    {
    	initEditor();
        Application.launch(args);
    }
    
    public static void initEditor()
    {
    	addEditor(new PropertyEditor()); 	
    }
    
    public static void addImporter(DataImporter importer)
    {
    	m_importers.add(importer);
    }
    
    public static void addEditor(Editor e)
    {
    	m_editors.add(e);
    }
    
    public static ArrayList<DataImporter> getImporters()
    {
    	return m_importers;
    }
    
    public static ArrayList<Editor> getEditors()
    {
    	return m_editors;
    }
    
    public void startEditing(String path)
    {   	
    	startEditing(path, m_defaultEditor);
    }
    
    public void startEditing(String path, Editor editor)
    {
    	Interop.MemInstance mi = Interop.s_wrap.load(path);
    	if (mi == null)
    		return;
    	if (!editor.canEdit(mi.getType()))
    		return;
    	
    	for (int i=0;i<m_openEditors.size();i++)
    	{
    		OpenEditors oe = m_openEditors.get(i);
    		if (oe._mi.getPath().equals(mi.getPath())) // && oe._editor == editor)
    		{
    		   	m_pane.getSelectionModel().select(oe._tab);
    		   	return;
    		}
    	}
   
    	Tab t = addTab(mi, editor.createUI(mi), path);
    	
    	OpenEditors oe = new OpenEditors();
    	oe._editor = editor;
    	oe._mi = mi;
    	oe._tab = t;
    	oe._contentHash = mi.getContentHash();
    	m_openEditors.add(oe);
    }
    
    public Stage makeDialogStage(javafx.scene.Scene scene)
    {
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());       
 	
    	Stage s = new Stage();
    	s.initModality(Modality.APPLICATION_MODAL);
    	s.setTitle("Question");
    	s.setScene(scene);
    	return s;    	
    }
    
    public String[] askForResources()
    {
    	return null;
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
    			return to.name.contains(newval);
    		});
    	});

    	searchField.setText("");
    	
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
    
    public static class ImportFinalizationQuestion
    {
    	public String proposedPath;
    	public String proposedResPath;
    	public boolean accepted;
    }
    
    public void askImportFinalization(ImportFinalizationQuestion question, Node aux)
    {    	
    	VBox contents = new VBox();
    	contents.getStyleClass().add("vbox-dialog");
    	contents.getStyleClass().add("root");
    	
    	TextField propPath = new TextField();
    	if (question.proposedPath != null)
    	{
        	Label L = new Label("Object path");
        	propPath.setText(question.proposedPath);
    		contents.getChildren().add(L);
    		contents.getChildren().add(propPath);
    	}
    	
    	TextField propResPath = new TextField();
    	if (question.proposedResPath != null)
    	{
        	Label L = new Label("Resource path");
        	propResPath.setText(question.proposedResPath);
        	contents.getChildren().add(L);
        	contents.getChildren().add(propResPath);
    	}
    	
    	if (aux != null)
    		contents.getChildren().add(aux);

    	contents.setMaxHeight(Double.MAX_VALUE);

    	VBox box = new VBox();
    	Button ok = new Button("OK");
    	Button cancel = new Button("Cancel");
    	ok.setMaxWidth(Double.MAX_VALUE);
    	cancel.setMaxWidth(Double.MAX_VALUE);
    	
    	HBox btns = new HBox();
    	btns.getChildren().setAll(cancel, ok);
    	btns.setAlignment(Pos.BOTTOM_CENTER);
    	
    	box.getChildren().add(contents);
    	box.getChildren().add(btns);

    	Scene scene = new Scene(box, 300, 400);
    	Stage stage = makeDialogStage(scene);
   	
    	ok.setOnAction((evt) -> {
    		question.accepted = true;
    		question.proposedResPath = propResPath.getText();
    		question.proposedPath = propPath.getText();
    		stage.hide();
    	});

    	cancel.setOnAction( (evt) -> {
    		question.accepted = false;
    		stage.hide();
    	});

    	stage.showAndWait();
    }    
    
    public Tab addTab(MemInstance mi, Node n, String title)
    {
    	Tab t = new Tab(title);
    	t.setContent(n);
    	m_pane.getTabs().add(t);
    	m_pane.getSelectionModel().select(t);
    	return t;
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

    	String base = "/Users/dannilsson/git/lilwiz";
    	Interop.Initialize(base + "/build/liblilwiz-data-dll.dylib", base);

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
