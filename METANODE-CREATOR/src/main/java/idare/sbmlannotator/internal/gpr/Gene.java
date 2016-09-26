package idare.sbmlannotator.internal.gpr;

import java.util.HashMap;

public class Gene {

	private String name;
	private HashMap<String,String> databasenames = new HashMap<String, String>();
	
	public Gene(HashMap<String, String> altnames)
	{
		addAltNames(altnames);
	}
	
	public Gene(String name, String database)
	{
		this.name = name;
		databasenames.put(name, database);
	}
	
	public void addAltNames(HashMap<String, String> altnames)
	{		
		databasenames.putAll(altnames);
		if(name == null)
		{
			for(String cname : altnames.values())
			{
				name = cname;
				break;
			}
		}
	}
	public void addAltName(String name, String database)
	{
		databasenames.put(database, name);
	}
	
	public boolean hasName(String name, String database)
	{
		return name.equals(databasenames.get(database));
	}
	
	public String getName()
	{
		return name;
	}
	@Override
	public boolean equals(Object o)
	{
			if(o == null)
			{
				return false;
			}
			if(!(o instanceof Gene))
			{
				return false;
			}
			Gene other = (Gene)o;
			return name.equals(other.name);	
	}
	
	@Override
	public int hashCode()
	{
		return name.hashCode();
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	
	public boolean isSetName()
	{
		return name != null;
	}
	
	public void setName(String name, String Database)
	{
		this.name = name;
		databasenames.put(Database,name);
	}
	
	public String getDataBaseName(String databasename)
	{
		return databasenames.get(databasename);
	}
}
