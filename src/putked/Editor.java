package putked;

import javafx.scene.Parent;

public interface Editor
{
	int getPriority();
	String getDescription();
	Parent createEditor(Interop.MemInstance object);
}