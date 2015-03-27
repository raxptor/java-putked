package putked;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;


public class Interop 
{
	public interface NI extends Library
	{
		public void MED_Initialize(String dllPath, String dataPath);

		public Pointer MED_TypeByIndex(int i);
		public String MED_Type_GetName(Pointer p);
		public String MED_Type_GetModuleName(Pointer p);
	}
	
	public static NI Load(String file)
	{
		return (NI) Native.loadLibrary(file, NI.class);
	}
}
