package idare.imagenode.internal.Services.JSBML;

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
			Class[] cArg = new Class[2];
			cArg[0] = String.class;
			cArg[1] = String.class;
			getChildCount = o.getClass().getMethod("getChildCount");
			getChildElement = o.getClass().getMethod("getChildElement", cArg);
			getChildElements= o.getClass().getMethod("getChildElements", cArg);
			Class[] cArg2 = new Class[1];
			cArg2[0] = Integer.class;			
			getChild = o.getClass().getMethod("getChild",cArg2);
			getCharacters= o.getClass().getMethod("getCharacters");
		}
		catch(NoSuchMethodException e)
		{

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
