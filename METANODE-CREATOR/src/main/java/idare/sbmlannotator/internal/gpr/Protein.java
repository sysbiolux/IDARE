package idare.sbmlannotator.internal.gpr;

import idare.imagenode.internal.Debug.PrintFDebugger;

import java.util.HashMap;
import java.util.Set;

public class Protein {

	private String name;
	private HashMap<String,String> databasenames = new HashMap<String, String>();
	private Set<Gene> CodingGenes;
	public Protein(HashMap<String,String> databaseids)	
	{
		addAltNames(databaseids);
//		PrintFDebugger.Debugging(this, "Created a new Protein with name " + name + " from a set of databases with size " + databaseids.keySet().size());
	}
	
	public Protein(String name, String database)
	{
		this.name = name;
		databasenames.put(database,name);
//		PrintFDebugger.Debugging(this, "Created a new Protein with name " + name);
	}
	
	public Set<Gene> getCodingGenes()
	{
		return CodingGenes;
	}
	
	public void addAltNames(HashMap<String, String> altnames)
	{
		if(name == null)
		{
//			PrintFDebugger.Debugging(this, "The Current protein has no name");
			for(String protName : altnames.values())
			{
//				PrintFDebugger.Debugging(this, "Setting " + protName  + " as the protein name");
				name = protName;
				break;
			}
		}
		databasenames.putAll(altnames);
	}
	public void addAltName(String name, String database)
	{
		databasenames.put(database, name);
	}
	
	public boolean hasName(String name, String database)
	{
		return name.equals(databasenames.get(database));
	}
	
	public GPRAssociation setCodingGenes(Set<Gene> genes, GPRManager gm)
	{
		this.CodingGenes = genes;
		return gm.associateGenesToProtein(this, genes);
	}
	
	public String getName(String Database)
	{
		if(databasenames.containsKey(Database))
		{
			return databasenames.get(Database);
		}
		else
		{
			return name;
		}
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
			if(!(o instanceof Protein))
			{
				return false;
			}
			Protein other = (Protein)o;
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
