package putked;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.*;
import javafx.util.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.input.*;

public class ObjectLibrary 
{
	public class ObjEntry
	{
		public String name;
		public String path;
		public Interop.Type type;
		public Interop.MemInstance mi;
	};
	
	class DirEntry
	{
		public String name;
		public String path;
		private ArrayList<ObjEntry> entries;
	};
	
	private HBox m_root;
	private TextField m_search;
	private TreeView<String> m_dirView;
	private TableView<ObjEntry> m_filesView;
	private HashMap<TreeItem<String>, DirEntry> m_dirMap = new HashMap<>();
	private ObservableList<ObjEntry> m_allObjects = FXCollections.observableArrayList();
	
	FilteredList<ObjEntry> m_filteredData;	
	private String m_dirFilterString = "";
	
	ObjectLibrary()
	{
		m_root = new HBox();
		m_root.setFillHeight(true);
		m_root.setMaxWidth(100000.0);
		m_root.setMaxHeight(100000.0);
		
        final TreeItem<String> root = new TreeItem<>("/");
        root.setExpanded(true);
        
        scanDirectory(root, new File("/Users/dannilsson/git/claw-putki/data/objs/"), "");
    
        //Creating a tree table view
        m_dirView = new TreeView<String>(root);

        m_root.getChildren().add(m_dirView);
		
		TableColumn<ObjEntry, String> col_fn = new TableColumn<>("Name");
		TableColumn<ObjEntry, String> col_type = new TableColumn<>("Type");
		
		col_fn.setPrefWidth(300);
		
		col_fn.setCellValueFactory(new Callback<CellDataFeatures<ObjEntry, String>, ObservableValue<String>>() {
		    public ObservableValue<String> call(CellDataFeatures<ObjEntry, String> p) {
		        // p.getValue() returns the Person instance for a particular TableView row
		        return new ReadOnlyStringWrapper(p.getValue().path);
		    }
		});
		 
		col_type.setCellValueFactory(new Callback<CellDataFeatures<ObjEntry, String>, ObservableValue<String>>() {
		    public ObservableValue<String> call(CellDataFeatures<ObjEntry, String> p) {
		    	Interop.Type t = p.getValue().type;
		    	if (t != null)
			        return new ReadOnlyStringWrapper(t.getName());
		        return new ReadOnlyStringWrapper("<NULL>");
		    }
		});

		m_filesView = new TableView<ObjEntry>();
		m_filesView.getColumns().add(0, col_fn);
		m_filesView.getColumns().add(1, col_type);
		
		VBox fbox = new VBox();
		m_search = new TextField();
		m_search.setMaxWidth(Double.MAX_VALUE);
		m_search.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
		        updateFilter();
		    }			
		});
		fbox.getChildren().setAll(m_search, m_filesView);

		m_root.getChildren().add(fbox);		
		HBox.setHgrow(fbox, Priority.ALWAYS);
		
		m_filteredData = new FilteredList<ObjEntry>(m_allObjects, p -> true);
		m_filesView.setItems(m_filteredData);
		
        m_dirView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
			@Override
			public void changed( ObservableValue<? extends TreeItem<String>> paramObservableValue, TreeItem<String> paramT1, TreeItem<String> selectedItem) {
				DirEntry de = m_dirMap.get(selectedItem);
				m_dirFilterString = de.path;
				updateFilter();
				System.out.println("I got " + de.entries.size() + " for " + de.name);				
			}
        });
        
        m_filesView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override 
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                	Main.s_instance.startEditing(m_filesView.getSelectionModel().getSelectedItem().path);
                }
            }
        });
	}
	
	public ObservableList<ObjEntry> getAllObjects()
	{
		return m_allObjects;
	}
	
	private void updateFilter()
	{
        m_filteredData.setPredicate(obj -> {
        	String s = m_search.getText();
        	if (obj.path.startsWith(m_dirFilterString))
        		return s.isEmpty() || obj.path.contains(s) || obj.type.getName().contains(s);
        	return false;
        });   
	}
	
	private void scanDirectory(TreeItem n, File f, String path)
	{
		if (!f.exists())
		{
			System.out.println("File " + f.getName() + " does not exist!");
			return;
		}
		
		File[] files = f.listFiles();
		if (files == null)
		{
			System.out.println("File " + f.getName() + " is not a directory!");
			return;
		}
		
		// this directoyr.
		DirEntry de = new DirEntry();
		de.entries = new ArrayList<>();
		de.path = path;
			
		ArrayList<TreeItem> out = new ArrayList<>();
		for (int i=0;i<files.length;i++)
		{
			if (files[i].isDirectory())
			{
				if (!files[i].getName().equals(".") && !files[i].getName().equals(".."))
				{
					TreeItem<String> ni = new TreeItem<>(files[i].getName());
					out.add(ni);
					System.out.println("Adding [" + files[i].getName() + "]");
					scanDirectory(ni, files[i], path + files[i].getName() + "/");
				}
			}
			else
			{
				String ending = ".json";
				String name = files[i].getName();
				if (name.length() < ending.length())
					continue;
				if (!name.endsWith(ending))
					continue;

				ObjEntry oe = new ObjEntry();
				oe.name = name.substring(0, name.length() - ending.length());
				oe.path = path + oe.name;
				oe.mi = Interop.s_wrap.load(oe.path);
				if (oe.mi != null)
					oe.type = oe.mi.getType();
				de.entries.add(oe);
				m_allObjects.add(oe);
			}
		}
		
		m_dirMap.put(n, de);
		
		n.getChildren().setAll(out);
	}
	
	public Parent getRoot()
	{
		return m_root;
	}
}
