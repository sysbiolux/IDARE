package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class Group extends SBase{
	private Method ListOfMembers;	
	private Object SBMLGroup;

	public Group(Object o) 
	{
		super(o);
		SBMLGroup = o;
		try{
			//getListOfGeneProducts = o.getClass().getMethod("getListOfGeneProducts");
			ListOfMembers = o.getClass().getMethod("getListOfMembers");
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
	public List<Member> getListOfMembers()
	{
		try{
			List specieslist = (List)ListOfMembers.invoke(SBMLGroup);
			List<Member> result = new LinkedList<Member>();
			for(Object o : specieslist)
			{
				if(o != null)
				{
					result.add(new Member(o));
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
