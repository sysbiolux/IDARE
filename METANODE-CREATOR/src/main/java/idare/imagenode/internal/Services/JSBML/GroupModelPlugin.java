package idare.imagenode.internal.Services.JSBML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class GroupModelPlugin {
	
	Object SBMLGroupModel;
	Method ListOfGroups;
	
	public GroupModelPlugin(Object o)
	{
		SBMLGroupModel = o;
		try{
			//getListOfGeneProducts = o.getClass().getMethod("getListOfGeneProducts");
			ListOfGroups = o.getClass().getMethod("getListOfGroups");
					
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
	
	public List<Group> getListOfGroups()
	{
		try{
			List specieslist = (List)ListOfGroups.invoke(SBMLGroupModel);
			List<Group> result = new LinkedList<Group>();
			for(Object o : specieslist)
			{
				if(o != null)
				{
					result.add(new Group(o));
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
