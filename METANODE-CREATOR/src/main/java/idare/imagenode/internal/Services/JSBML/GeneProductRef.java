package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GeneProductRef extends Association{

	Method getGeneProduct;
	public GeneProductRef(Object o) {
		super(o);		
		try{
			getGeneProduct = o.getClass().getMethod("getGeneProduct");
		}
		catch(NoSuchMethodException e)
		{
			
		}
	}

	public String getGeneProduct()
	{
		try{
			Object res = getGeneProduct.invoke(SBMLAssoc);
			return res == null ? null : (String) res;							
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	
	}
}
