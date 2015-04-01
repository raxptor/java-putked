package putked;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.*;
import javafx.beans.*;
import javafx.util.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import com.sun.jna.Pointer;

import java.beans.EventHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ObjectLibrary 
{
	class ObjEntry
	{
		public String name;
		public String path;
		public Pointer object;
	};
	
	class DirEntry
	{
		public String name;
		public String path;
		private ArrayList<ObjEntry> entries;
	};
	
	private HBox m_root;
	private TreeView<String> m_dirView;
	private TableView<ObjEntry> m_filesView;
	private HashMap<TreeItem<String>, DirEntry> m_dirMap = new HashMap<>();
	private ObservableList<ObjEntry> m_allObjects = FXCollections.observableArrayList();
	
	FilteredList<ObjEntry> m_filteredData;	
	
	ObjectLibrary()
	{
		m_root = new HBox();
		m_root.setFillHeight(true);
		m_root.setMaxWidth(100000.0);
		m_root.setMaxHeight(100000.0);
		
        final TreeItem<String> root = new TreeItem<>("/");
        root.setExpanded(true);
        
        scanDirectory(root, new File("/Users/dannilsson/git/claw-putki/data/objs"), "/");
    
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
		        // p.getValue() returns the Person instance for a particular TableView row
		    	if (p.getValue().object != Pointer.NULL)
		    	{
		    		Pointer type = Main.s_interop.MED_TypeOf(p.getValue().object);
			        return new ReadOnlyStringWrapper(Main.s_interop.MED_Type_GetName(type));
		    	}
		        return new ReadOnlyStringWrapper("<NULL>");
		    }
		});

		m_filesView = new TableView<ObjEntry>();
		m_filesView.getColumns().setAll(col_fn, col_type);
		
		m_root.getChildren().add(m_filesView);
		
		HBox.setHgrow(m_filesView, Priority.ALWAYS);
		
		m_filteredData = new FilteredList<ObjEntry>(m_allObjects, p -> true);
		m_filesView.setItems(m_filteredData);
		
        m_dirView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem>() {
			@Override
			public void changed( ObservableValue<? extends TreeItem> paramObservableValue, TreeItem paramT1, TreeItem selectedItem) {
				DirEntry de = m_dirMap.get(selectedItem);
				filterOnPath(de.path);
				System.out.println("I got " + de.entries.size() + " for " + de.name);				
			}
        });
                
        m_root.getChildren().add(new ObjectEditor(new Interop.MemInstance(Interop.s_ni.MED_DiskLoad("ui/mainmenu/rootwidget"))).getRoot());
	}
	
	private void filterOnPath(String start)
	{
		System.out.println("Filtering on [" + start + "]");
        m_filteredData.setPredicate(obj -> {
        	if (obj.path.startsWith(start))
        		return true;
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
				oe.object = Main.s_interop.MED_DiskLoad(oe.path);
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
