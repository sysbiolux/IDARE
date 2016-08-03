package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class Annotation {
	
	private Object SBMLAnnotation;
	private Method ListOfCVTerms;
	public Annotation(Object o)
	{
		SBMLAnnotation = o;
		try{
			ListOfCVTerms = o.getClass().getMethod("getListOfCVTerms");
		}
		catch(NoSuchMethodException e)
		{
			
		}
		
	}
	
	public List<CVTerm> getListOfCVTerms()
	{
		try{
			List cvtermlist = (List)ListOfCVTerms.invoke(SBMLAnnotation);
			List<CVTerm> result = new LinkedList<CVTerm>();
			for(Object o : cvtermlist)
			{
				result.add(new CVTerm(o));
			}
			return result;
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	}
}
