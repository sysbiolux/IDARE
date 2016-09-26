package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class Model {

	private Method ListOfSpecies;
	private Method ListOfReactions;
	private Method hasFBCExtension;
	private Method getListOfGeneProducts;
	private Object SBMLModel;

	public Model(Object o)
	{
		SBMLModel = o;
		try{
			getListOfGeneProducts = o.getClass().getMethod("getListOfGeneProducts");
			ListOfReactions = o.getClass().getMethod("getListOfReactions");
			ListOfSpecies= o.getClass().getMethod("getListOfSpecies");
			Class[] cArg = new Class[1];
			cArg[0] = String.class;
			hasFBCExtension = o.getClass().getMethod("isPackageEnabled", cArg);				
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
				if(o != null)
				{
					result.add(new Species(o));
				}
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
				if(o != null)
				{
					result.add(new Reaction(o));
				}
			}
			return result;
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	}

	public boolean isFBCPackageEnabled()
	{
		try{
			return (Boolean) hasFBCExtension.invoke(SBMLModel, "fbc");
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	public List<GeneProduct> getListOfGeneProducts()
	{
		try{
			List specieslist = (List)getListOfGeneProducts.invoke(SBMLModel);
			List<GeneProduct> result = new LinkedList<GeneProduct>();
			for(Object o : specieslist)
			{
				if(o != null)
				{
					result.add(new GeneProduct(o));
				}
			}
			return result;
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	}
}
	

	