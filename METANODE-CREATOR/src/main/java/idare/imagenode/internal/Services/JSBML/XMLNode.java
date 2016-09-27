package idare.imagenode.internal.Services.JSBML;

import idare.imagenode.internal.Debug.PrintFDebugger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class XMLNode {

	Object xmlnode;
	Method getChildCount;
	Method getChildElement;
	Method getChildElements;
	Method getChild;
	Method getCharacters;
	public XMLNode(Object o)
	{
		xmlnode = o;
		try{
			getChildCount = o.getClass().getMethod("getChildCount");
			getChildElement = o.getClass().getMethod("getChildElement", String.class, String.class);
			getChildElements= o.getClass().getMethod("getChildElements", String.class, String.class);
			getChild = o.getClass().getMethod("getChild",int.class);
			getCharacters= o.getClass().getMethod("getCharacters");			
		}
		catch(NoSuchMethodException e)
		{
			e.printStackTrace(System.out);
			PrintFDebugger.Debugging(this,"Available Methods");
			for(Method m : o.getClass().getMethods())
			{
				System.out.println(m.getName());
			}			
		}
	}
	
	public int getChildCount()
	{
		try{
			return (Integer)getChildCount.invoke(xmlnode);
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return 0;
		}
	}
	
	public XMLNode getChildElement(String elementName, String elemntURI)
	{
		try{
			return new XMLNode(getChildElement.invoke(xmlnode,elementName,elemntURI));
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	}
	
	public List<XMLNode> getChildElements(String elementName, String elemntURI)
	{
		try{
			List specieslist = (List)getChildElements.invoke(xmlnode,elementName,elemntURI);
			List<XMLNode> result = new LinkedList<XMLNode>();
			for(Object o : specieslist)
			{
				if(o != null)
				{
					result.add(new XMLNode(o));
				}
			}
			return result;
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	}

	
	public String getCharacters()
	{
		try{
			return getCharacters.invoke(xmlnode).toString();
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	}
	
	public XMLNode getChild(int childIndex)
	{
		try{
			return new XMLNode(getChild.invoke(xmlnode,childIndex));
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	}
	
	
	
}
