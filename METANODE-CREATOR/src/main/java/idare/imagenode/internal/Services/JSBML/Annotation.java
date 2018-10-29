package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
/**
 * Wrapper for the Annotation Object from JSBML
 * which provides only a few operations.
 * @author Thomas Pfau
 *
 */
public class Annotation {
	
	private Object SBMLAnnotation;
	private Method ListOfCVTerms;
	/**
	 * Wrap the provided Object (which should be an org.sbml.jsbml.Annotation object 
	 * @param o the Annotation object to initialize this object.
	 */
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

	/**
	 * Get the List of CVTerms
	 * @return a List of CVTerms. 
	 */
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
