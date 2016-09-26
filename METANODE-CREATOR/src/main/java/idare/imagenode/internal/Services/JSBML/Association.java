package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class Association {

	Object SBMLAssoc;
	Method getListOfAssociations;
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
				if(o.getClass().getSimpleName().equals("And"))
				{
					result.add(new And(o));
				}
				if(o.getClass().getSimpleName().equals("Or"))
				{
					result.add(new Or(o));
				}
				if(o.getClass().getSimpleName().equals("GeneProductRef"))
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
