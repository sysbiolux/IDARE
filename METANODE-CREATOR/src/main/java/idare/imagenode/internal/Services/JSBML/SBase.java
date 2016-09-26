package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class SBase {

	Object SBMLBase; 
	Method getId;
	private Method notesSet;
	private Method notesString;
	private Method SBOTerm;
	private Method SBOTermSet;
	private Method annotation;
	private Method setAnnotation;
	private Method getNotes;
	public SBase(Object o)
	{
		SBMLBase = o;
		try{
			getId = o.getClass().getMethod("getId");
			SBOTerm = o.getClass().getMethod("getSBOTerm");
			SBOTermSet = o.getClass().getMethod("isSetSBOTerm");
			notesSet = o.getClass().getMethod("isSetNotes");
			notesString = o.getClass().getMethod("getNotesString");
			annotation = o.getClass().getMethod("getAnnotation");
			setAnnotation = o.getClass().getMethod("isSetAnnotation");
			setAnnotation = o.getClass().getMethod("getNotes");
		}
		catch(NoSuchMethodException e)
		{
			
		}
	}
	
	public boolean isSetNotes()
	{		
		try{
			return (Boolean) notesSet.invoke(SBMLBase);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return false;
		}
	}

	
	public String getNotesString()
	{
		try{
			return (String) notesString.invoke(SBMLBase);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return "";
		}
	}
	
	public String getId()
	{
		try{
			Object res = getId.invoke(SBMLBase);
			return res == null ? null : (String) res;							
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	
	}
	
	public int getSBOTerm()
	{
		try{
			Object SBO = SBOTerm.invoke(SBMLBase);
			return (Integer) SBO;
			
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return 0;
		}
	}
	public boolean isSetSBOTerm()
	{
		try{
			Object SBO = SBOTermSet.invoke(SBMLBase);
			return (Boolean) SBO;
			
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return false;
		}
	}
	
	
	public Annotation getAnnotation()
	{
		try{
			Object annot = annotation.invoke(SBMLBase);
			if(annot != null)
			{
			return new Annotation(annot);
			}
			else
			{
				return null;
			}
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	}
	
	public boolean isSetAnnotation()
	{
		try{
			return (Boolean) setAnnotation.invoke(SBMLBase);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return false;
		}
	}
	
	
	public boolean isNull()
	{
		return SBMLBase == null;
	}
	
	public XMLNode getNotes()
	{
		try{
			return new XMLNode(getNotes.invoke(SBMLBase));
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	}
	
	
	public abstract String getName();	
}
