package idare.metanode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class Model {

	private Method ListOfSpecies;
	private Method ListOfReactions;
	private Object SBMLModel;
	
	public Model(Object o)
	{
		SBMLModel = o;
		try{
		ListOfReactions = o.getClass().getMethod("getListOfReactions");
		ListOfSpecies= o.getClass().getMethod("getListOfSpecies");
		}
		catch(NoSuchMethodException e)
		{
			
		}
	}
	
	
	public List<Species> getListOfSpecies()
	{
		try{
			List specieslist = (List)ListOfSpecies.invoke(SBMLModel);
			List<Species> result = new LinkedList<Species>();
				for(Object o : specieslist)
				{
					result.add(new Species(o));
				}
				return result;
			}
			catch(InvocationTargetException | IllegalAccessException e)
			{
				return null;
			}
		}
		
	
	
	public List<Reaction> getListOfReactions()
	{
		try{
			List specieslist = (List)ListOfReactions.invoke(SBMLModel);
			List<Reaction> result = new LinkedList<Reaction>();
				for(Object o : specieslist)
				{
					result.add(new Reaction(o));
				}
				return result;
			}
			catch(InvocationTargetException | IllegalAccessException e)
			{
				return null;
			}
		}
}
