package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Species extends SBase{
	
	private Object SBMLSpecies;
	private Method notesSet;
	private Method id;
	private Method notesString;
	private Method annotation;
	private Method compartment;
	private Method setAnnotation;
	private Method SBOTerm;
	public Species(Object o) 
	{
		SBMLSpecies = o;
		try{
			notesSet = o.getClass().getMethod("isSetNotes");
			id = o.getClass().getMethod("getId");
			notesString = o.getClass().getMethod("getNotesString");
			annotation = o.getClass().getMethod("getAnnotation");
			compartment = o.getClass().getMethod("getCompartment");
			setAnnotation = o.getClass().getMethod("isSetAnnotation");
			SBOTerm = o.getClass().getMethod("getSBOTerm");
		}
		catch(NoSuchMethodException e)
		{
			
		}
	}
	
	public int getSBOTerm()
	{
		try{
			return (Integer) SBOTerm.invoke(SBMLSpecies);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return 0;
		}
	}
	
	public boolean isSetNotes()
	{		
		try{
			return (Boolean) notesSet.invoke(SBMLSpecies);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return false;
		}
	}
	
	public String getId()
	{
		try{
			return (String) id.invoke(SBMLSpecies);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return "";
		}
	}
	
	public String getNotesString()
	{
		try{
			return (String) notesString.invoke(SBMLSpecies);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return "";
		}
	}
	
	public Annotation getAnnotation()
	{
		try{
			return new Annotation(annotation.invoke(SBMLSpecies));
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	}
	
	public boolean isSetAnnotation()
	{
		try{
			return (Boolean) setAnnotation.invoke(SBMLSpecies);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return false;
		}
	}
	
	public String getCompartment()
	{
		{
			try{
				return compartment.invoke(SBMLSpecies).toString();
			}
			catch(InvocationTargetException | IllegalAccessException e)
			{
				return "";
			}
		}
	}
	
}
