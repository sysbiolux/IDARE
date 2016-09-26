package idare.sbmlannotator.internal.gpr;

import idare.Properties.IDARESettingsManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GPRManager {

	public static String PROTEIN_BASE = "Protein";
	public static String GENE_BASE = "Protein";
	
	HashMap<String, HashMap<String, Protein>> proteinIdentifierToProtein = new HashMap<String, HashMap<String,Protein>>();
	HashMap<Protein, HashMap<String, String>> proteinIdentifiers = new HashMap<Protein, HashMap<String,String>>();
	HashMap<String, HashMap<String, Gene>> geneIdentifierToGene = new HashMap<String, HashMap<String,Gene>>();
	HashMap<Gene, HashMap<String, String>> geneIdentifiers = new HashMap<Gene, HashMap<String,String>>();
	HashMap<GPRAssociation,Protein> GPRToProt = new HashMap<GPRAssociation, Protein>();
	HashMap<Protein,GPRAssociation> ProtToGPR = new HashMap<Protein, GPRAssociation>();
	HashMap<String, Gene> geneIDsWithoutDB = new HashMap<String, Gene>();
	HashMap<String, Protein> proteinIDsWithoutDB = new HashMap<String, Protein>();
	HashMap<Gene,Protein> singleGeneProteins = new HashMap<Gene, Protein>();
	HashMap<Protein,Set<Gene>> protToGene = new HashMap<Protein,Set<Gene>>();
	IDARESettingsManager ism;
	
	public GPRManager(IDARESettingsManager ism) {
		this.ism = ism;
	}
	
	public GPRAssociation associateGenesToProtein(Protein p, Set<Gene> genes)
	{		
		GPRAssociation gpr = null;
		protToGene.put(p, genes);
		if(genes.size() > 0)
		{
			gpr = new GPRAssociation();
			gpr.addAllGenes(genes);
			GPRToProt.put(gpr, p);
			ProtToGPR.put(p, gpr);
		}
		if(genes.size() == 1)
		{
			for(Gene g : genes)
			{
				singleGeneProteins.put(g, p);
			}
		}
		else
		{
			for(Gene g : genes)
			{				
				if(singleGeneProteins.containsKey(g) && singleGeneProteins.get(g).equals(p))
				{
					singleGeneProteins.remove(g);
				}
			}
		}
		return gpr;
	}
	
	public Protein getSingleGeneProtein(Gene g)
	{
		return singleGeneProteins.get(g);
	}
	
	public Set<Gene> getProteinBuildingGenes( Protein p)
	{		
		return protToGene.get(p);	
	}
	
	
	public Gene getGene(String genename)
	{
		if(!geneIDsWithoutDB.containsKey(genename))
		{
			geneIDsWithoutDB.put(genename,getUnassociatedGene(genename));
			
		}
		return geneIDsWithoutDB.get(genename);
	}
	
	public GPRAssociation getGPRAssoc(Protein p)
	{
		return ProtToGPR.get(p);
	}
	
	public Protein getProtein(GPRAssociation assoc)
	{
		if(!GPRToProt.containsKey(assoc))
		{
			String proteinName = PROTEIN_BASE + ism.getNextProteinID();
			Protein p = new Protein(proteinName,null);			 
			//p.setName(proteinName, null);
			proteinIDsWithoutDB.put(proteinName,p);
			GPRToProt.put(assoc, p);
			ProtToGPR.put(p, assoc);
			p.setCodingGenes(assoc.geneset, this);
		}
		return GPRToProt.get(assoc);
	}
	
	public Set<Protein> getProteins(Collection<GPRAssociation> assocs)
	{
		Set<Protein> prots = new HashSet<Protein>();
		for(GPRAssociation gpr : assocs)
		{
			prots.add(getProtein(gpr));
		}
		return prots;
	}
	
	public Protein getProtein(String Proteinname)
	{
		if(!proteinIDsWithoutDB.containsKey(Proteinname))
		{
			getUnassociatedProtein(Proteinname); 
		}
		return proteinIDsWithoutDB.get(Proteinname);
	}
	
	public Gene getGene(HashMap<String,String> databaseids)
	{
		Gene result = null;
		for(String db : databaseids.keySet())
		{
			if(geneIdentifierToGene.containsKey(db))
			{
				if(geneIdentifierToGene.get(db).containsKey(databaseids.get(db)))
				{
					result = geneIdentifierToGene.get(db).get(databaseids.get(db));
				}
			}
		}
		if(result == null)
		{
			result = new Gene(databaseids);			
		}
		if(!geneIdentifiers.containsKey(result))
		{
			geneIdentifiers.put(result, databaseids);
			result.addAltNames(databaseids);
			for(String genename : databaseids.values())
			{
				geneIDsWithoutDB.put(genename, result);
			}
		}
		else
		{
			geneIdentifiers.get(result).putAll(databaseids);
			for(String genename : databaseids.values())
			{
				geneIDsWithoutDB.put(genename, result);
			}
		}
		//now add the alternative ids		
		for(String db : databaseids.keySet())
		{

			if(!geneIdentifierToGene.containsKey(db))
			{
				geneIdentifierToGene.put(db, new HashMap<String, Gene>());
			}
			if(!geneIdentifierToGene.get(db).containsKey(databaseids.get(db)))
			{
				geneIdentifierToGene.get(db).put(databaseids.get(db), result);	
			}			

		}
		return result;
	}
	
	public Protein getUnassociatedProtein(String name)
	{
		HashMap<String, String> proteinName = new HashMap<String, String>();
		proteinName.put(null,name);		
		Protein p = getProtein(proteinName);
		HashMap<String, String> geneName = new HashMap<String, String>();
		geneName.put(null,GENE_BASE + ism.getNextGeneID());				
		Gene g = getGene(geneName);
		HashSet<Gene> genes = new HashSet<Gene>(); 
		genes.add(g);
		p.setCodingGenes(genes,this);
		protToGene.put(p, genes);
		return p;
	}
	
	public Gene getUnassociatedGene(String name)
	{
		
		Gene g = new Gene(name, null);
		return g;
	}
	
	public Protein getProtein(HashMap<String,String> databaseids)
	{
		Protein result = null;
		for(String db : databaseids.keySet())
		{
			if(proteinIdentifierToProtein.containsKey(db))
			{
				if(proteinIdentifierToProtein.get(db).containsKey(databaseids.get(db)))
				{
					result = proteinIdentifierToProtein.get(db).get(databaseids.get(db));
					break;
				}
			}
		}
		if(result == null)
		{
			result = new Protein(databaseids);			
		}		
		if(!proteinIdentifiers.containsKey(result))
		{
			proteinIdentifiers.put(result, databaseids);			
			for(String proteinName : databaseids.values())
			{
				proteinIDsWithoutDB.put(proteinName, result);
			}
		}
		else
		{
			proteinIdentifiers.get(result).putAll(databaseids);
			for(String proteinName : databaseids.values())
			{
				proteinIDsWithoutDB.put(proteinName, result);
			}
		}
		//now add the alternative ids		
		for(String db : databaseids.keySet())
		{

			if(!proteinIdentifierToProtein.containsKey(db))
			{
				proteinIdentifierToProtein.put(db, new HashMap<String, Protein>());
			}
			if(!proteinIdentifierToProtein.get(db).containsKey(databaseids.get(db)))
			{
				proteinIdentifierToProtein.get(db).put(databaseids.get(db), result);	
			}			

		}
		return result;
	}

	public void updateGenedbs(Gene g, HashMap<String,String> databaseids )
	{
		geneIdentifiers.get(g).putAll(databaseids);
	
		//now add the alternative ids		
		for(String db : databaseids.keySet())
		{

			if(!geneIdentifierToGene.containsKey(db))
			{
				geneIdentifierToGene.put(db, new HashMap<String, Gene>());
			}
			if(!geneIdentifierToGene.get(db).containsKey(databaseids.get(db)))
			{
				geneIdentifierToGene.get(db).put(databaseids.get(db), g);	
			}			

		}
	}
	
	
	public void updateProteindbs(Protein p, HashMap<String,String> databaseids )
	{
		proteinIdentifiers.get(p).putAll(databaseids);

		//now add the alternative ids		
		for(String db : databaseids.keySet())
		{

			if(!proteinIdentifierToProtein.containsKey(db))
			{
				proteinIdentifierToProtein.put(db, new HashMap<String, Protein>());
			}
			if(!proteinIdentifierToProtein.get(db).containsKey(databaseids.get(db)))
			{
				proteinIdentifierToProtein.get(db).put(databaseids.get(db), p);	
			}			

		}
	}

	
}
