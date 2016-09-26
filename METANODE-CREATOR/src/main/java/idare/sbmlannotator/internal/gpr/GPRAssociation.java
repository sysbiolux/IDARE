package idare.sbmlannotator.internal.gpr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GPRAssociation {

	Set<Gene> geneset;
	HashMap<Protein, Set<Gene>> genesPresentByProteins;
	
	public GPRAssociation(GPRAssociation a, GPRAssociation b)
	{
		genesPresentByProteins = new HashMap<Protein, Set<Gene>>();
		genesPresentByProteins.putAll(a.genesPresentByProteins);
		genesPresentByProteins.putAll(b.genesPresentByProteins);
		geneset = new HashSet<Gene>();
		geneset.addAll(a.geneset);
		geneset.addAll(b.geneset);
	}
			
	public GPRAssociation()
	{
		geneset = new HashSet<>();
		genesPresentByProteins = new HashMap<Protein, Set<Gene>>();
	}
	
	
	public boolean isProteinComplex()
	{
		return !genesPresentByProteins.isEmpty();
	}
	public Set<Protein> getProteins()
	{
		return genesPresentByProteins.keySet();
	}
	
	public Set<Gene> getGenesWithoutProteins()
	{
		HashSet<Gene> result = new HashSet<Gene>();
		result.addAll(geneset);
		for(Set<Gene> assoc : genesPresentByProteins.values())
		{
			result.removeAll(assoc);
		}
		return result;
	}
	
	public Set<Gene> getGenes()
	{
		HashSet<Gene> result = new HashSet<Gene>();
		result.addAll(geneset);
		return result;
	}

	public void addAllGenes(Set<Gene> g)
	{
		geneset.addAll(g);
	}
	
	public void addGene(Gene g)
	{
		geneset.add(g);
	}
	public void addGenesByProtein(Protein prot ,Set<Gene> genes)
	{		
		geneset.addAll(genes);
		genesPresentByProteins.put(prot, genes);
	}
		
	@Override
	public boolean equals(Object o)
	{
		if(o == null)
		{
			return false;
		}
		if(!(o instanceof GPRAssociation))
		{
			return false;
		}
		GPRAssociation other = (GPRAssociation)o;
		if(other.geneset.containsAll(geneset) & geneset.containsAll(other.geneset))
		{
			return true;			
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public int hashCode()
	{
		int hash = 0;
		for(Gene g: geneset)
		{
			hash+= g.hashCode();
		}			
		return hash;
	}

	@Override
	public String toString()
	{
		String res = "";
		Iterator<Gene> iter = geneset.iterator();
		if(iter.hasNext())
		{
			res += iter.next().toString();
		}
		while(iter.hasNext())
		{
			res += " and " +iter.next().toString();
		}
		return res;
	}
}
