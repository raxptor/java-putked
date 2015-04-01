package putked;

import java.util.HashMap;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class Interop 
{
	public interface NI extends Library
	{
		public void MED_Initialize(String dllPath, String dataPath);

		public Pointer MED_TypeByIndex(int i);
		
		// mem objects
		public Pointer MED_DiskLoad(String path);
		public Pointer MED_TypeOf(Pointer obj);
		
		// types
		public String MED_Type_GetName(Pointer p);
		public String MED_Type_GetModuleName(Pointer p);	
		public Pointer MED_Type_GetField(Pointer type, int index);
		
		// fields
		public String MED_Field_GetName(Pointer p);
		public String MED_Field_GetRefType(Pointer p);
		public int MED_Field_GetType(Pointer p);
		public boolean MED_Field_IsArray(Pointer p);
		public boolean MED_Field_IsAuxPtr(Pointer p);
		public boolean MED_Field_ShowInEditor(Pointer p);
		
		public int MED_Field_GetArraySize(Pointer field, Pointer mi);
		public void MED_Field_SetArrayIndex(Pointer field, int index);

		public String MED_Field_GetString(Pointer field, Pointer mi);
		public String MED_Field_GetPointer(Pointer field, Pointer mi);
		public Pointer MED_Field_GetStructInstance(Pointer field, Pointer mi);
	}
	
	public static class Field
	{
		Pointer _p;
		
		public Field(Pointer p)
		{
			_p = p;
		}
		
		public String getName()
		{
			return s_ni.MED_Field_GetName(_p);
		}
		
		public void setArrayIndex(int i)
		{
			s_ni.MED_Field_SetArrayIndex(_p,  i);
		}
		
		public boolean isArray()
		{
			return s_ni.MED_Field_IsArray(_p);
		}
		
		public boolean isAuxPtr()
		{
			return s_ni.MED_Field_IsAuxPtr(_p);
		}	
		
		public int getArraySize(MemInstance mi)
		{
			return s_ni.MED_Field_GetArraySize(_p, mi._p);
		}
		
		public String getString(MemInstance mi)
		{
			return s_ni.MED_Field_GetString(_p, mi._p);
		}
		
		public String getRefType()
		{
			return s_ni.MED_Field_GetRefType(_p);
		}
		
		public String getPointer(MemInstance mi)
		{
			return s_ni.MED_Field_GetPointer(_p, mi._p);
		}	
		
		public boolean showInEditor()
		{
			return s_ni.MED_Field_ShowInEditor(_p);
		}
		
		public MemInstance getStructInstance(MemInstance mi)
		{
			return new MemInstance(s_ni.MED_Field_GetStructInstance(_p, mi._p));
		}
		
		public int getType()
		{
			return s_ni.MED_Field_GetType(_p);
		}
	}
	
	public static class Type 
	{
		Pointer _p;
		
		public Type(Pointer p)
		{
			_p = p;
		}
		
		String getName()
		{
			return s_ni.MED_Type_GetName(_p); 
		}
		
		String getModule()
		{
			return s_ni.MED_Type_GetModuleName(_p);
		}
		
		Field getField(int i)
		{
			return s_wrap.getFieldWrapper(s_ni.MED_Type_GetField(_p,  i));
		}
	}
		
	public static class MemInstance
	{
		public Pointer _p;
		public Type _type;
		
		public MemInstance(Pointer p)
		{
			_p = p;
			_type = s_wrap.getTypeWrapper(s_ni.MED_TypeOf(p));
		}
		
		public Type getType()
		{
			return _type;
		}
	}

	public static class NIWrap 
	{
		private NI _i;
		private HashMap<Pointer, Field> s_fields = new HashMap<>();
		private HashMap<Pointer, Type> s_types = new HashMap<>();
		
		public NIWrap(NI i)
		{
			_i = i;
		}
		
		public Field getFieldWrapper(Pointer p)
		{
			if (p == Pointer.NULL)
				return null;
			
			Field f = s_fields.get(p);
			if (f == null)
			{
				f = new Field(p);
				s_fields.put(p, f);
			}
			return f;
		}
		
		public Type getTypeWrapper(Pointer p)
		{
			if (p == Pointer.NULL)
				return null;
			
			Type f = s_types.get(p);
			if (f == null)
			{
				f = new Type(p);
				s_types.put(p, f);
			}
			return f;
		}		
	}
	
	public static NI s_ni;
	public static NIWrap s_wrap;
	
	public static NI Load(String file)
	{
		s_ni = (NI) Native.loadLibrary(file, NI.class);
		s_wrap = new NIWrap(s_ni);
		return s_ni;
	}
}
