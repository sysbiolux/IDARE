package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.sbml.jsbml.JSBML;
/**
 * Wrapper for the Association Object from the fbc package of JSBML
 * @author Thomas Pfau
 *
 */
public class Association {

	Object SBMLAssoc;
	Method getListOfAssociations;
	/**
	 * A Wrapper that takes a hopefully) Association object
	 * @param o Association object to initialize this object.
	 */
	public Association(Object o)
	{
		SBMLAssoc = o;		
		try{
			getListOfAssociations = o.getClass().getMethod("getListOfAssociations");
		}
		catch(NullPointerException | NoSuchMethodException e)
		{
			
		}
	}
	
	public List<Association> getListOfAssociations()
	{
		try{
			List assoclist = (List)getListOfAssociations.invoke(SBMLAssoc);
			List<Association> result = new LinkedList<Association>();
			for(Object o : assoclist)
			{
				if(o.getClass().getSimpleName().equals(org.sbml.jsbml.ext.fbc.And.class.getSimpleName()))
				{
					result.add(new And(o));
				}
				if(o.getClass().getSimpleName().equals(org.sbml.jsbml.ext.fbc.Or.class.getSimpleName()))
				{
					result.add(new Or(o));
				}
				if(o.getClass().getSimpleName().equals(org.sbml.jsbml.ext.fbc.GeneProductRef.class.getSimpleName()))
				{
					result.add(new GeneProductRef(o));
				}
			}
			return result;
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	}
	
	public boolean isNull()
	{
		return SBMLAssoc == null;
	}
}
