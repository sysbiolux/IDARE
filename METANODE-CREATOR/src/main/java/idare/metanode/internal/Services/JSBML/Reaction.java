package idare.metanode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Reaction {

	private Object SBMLReaction;
	private Method notesSet;
	private Method id;
	private Method notesString;
	
	private Method reversible;
	public Reaction(Object o) 
	{
		SBMLReaction = o;
		try{
			notesSet = o.getClass().getMethod("isSetNotes");
			id = o.getClass().getMethod("getId");
			notesString = o.getClass().getMethod("getNotesString");
			reversible = o.getClass().getMethod("isReversible");
		}
		catch(NoSuchMethodException e)
		{
			
		}
	}
	
	public boolean isSetNotes()
	{		
		try{
			return (Boolean) notesSet.invoke(SBMLReaction);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return false;
		}
	}
	
	public String getId()
	{
		try{
			return (String) id.invoke(SBMLReaction);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return "";
		}
	}
	
	public String getNotesString()
	{
		try{
			return (String) notesString.invoke(SBMLReaction);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return "";
		}
	}


	public boolean isReversible()
	{
		try{
			return (Boolean) reversible.invoke(SBMLReaction);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return false;
		}
	}
}
