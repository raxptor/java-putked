package putked;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;

import java.io.File;
import java.util.ArrayList;

public class ObjectLibrary 
{
	private HBox m_root;
	private TreeView<String> m_dirView;
	
	ObjectLibrary()
	{
		m_root = new HBox();
		m_root.fillHeightProperty().set(true);
		
        //Creating tree item
        final TreeItem<String> childNode1 = new TreeItem<>("Child Node 1");
        final TreeItem<String> childNode2 = new TreeItem<>("Child Node 2");
        final TreeItem<String> childNode3 = new TreeItem<>("Child Node 3");
        
        //Creating the root element
        final TreeItem<String> root = new TreeItem<>("Data");
        root.setExpanded(true);   
        
        scanDirectory(root, new File("/Users/dannilsson/git/claw-putki/data/objs"));
     
        //Creating a column
        TreeTableColumn<String,String> column = new TreeTableColumn<>("Column");
        column.setPrefWidth(400);   
     
        //Defining cell content
        column.setCellValueFactory((CellDataFeatures<String, String> p) -> 
            new ReadOnlyStringWrapper(p.getValue().getValue()));  

        //Creating a tree table view
        m_dirView = new TreeView<>(root);
        m_dirView.setPrefWidth(400);
		m_root.getChildren().add(m_dirView);
	}
	
	private void scanDirectory(TreeItem n, File f)
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
					scanDirectory(ni, files[i]);
				}
			}
		}
		n.getChildren().setAll(out);
	}
	
	public Node getRoot()
	{
		return m_root;
	}
}
