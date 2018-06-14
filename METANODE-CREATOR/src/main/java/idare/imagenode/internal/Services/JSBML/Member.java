package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Member {
	
	private Method getIdRef;
	//private Method getListOfGeneProducts;
	private Object SBMLMember;

	public Member(Object o)
	{
		SBMLMember = o;
		try{
			//getListOfGeneProducts = o.getClass().getMethod("getListOfGeneProducts");
			getIdRef = o.getClass().getMethod("getIdRef");							
		}
		catch(NoSuchMethodException e)
		{
			e.printStackTrace();
//			PrintFDebugger.Debugging(this,"Available Methods");
			for(Method m : o.getClass().getMethods())
			{
				System.out.println(m.getName());
			}
			
		}
	}
	
	public String getIdRef()
	{
		try{
			return (String) getIdRef.invoke(SBMLMember);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return "";
		}
	}
}
