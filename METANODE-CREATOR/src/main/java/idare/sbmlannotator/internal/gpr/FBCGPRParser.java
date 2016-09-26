package idare.sbmlannotator.internal.gpr;

import idare.imagenode.internal.Services.JSBML.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;


public class FBCGPRParser {

	GPRManager gm;
	Set<GPRAssociation> AssociationSet;
	Model model;
	HashMap<String,GeneProduct> productids;
	HashMap<SBase, Gene> geneMap;
	HashMap<SBase, Protein> proteinMap;
	public FBCGPRParser(Association assoc, Model model, HashMap<SBase,Gene> genes, HashMap<SBase,Protein> proteins) {
		// TODO Auto-generated constructor stub		
		this.model = model;
		for(GeneProduct gp : model.getListOfGeneProducts())
		{
			productids.put(gp.getId(), gp);
		}
		AssociationSet = parseAssoc(assoc);
	}
	
	public Set<GPRAssociation> getGPRAssociation()
	{
		return AssociationSet;
	}
	
	private Set<GPRAssociation> parseAssoc(Association assoc)
	{		
		
		if(assoc instanceof Or)
		{
			Or orNode = (Or) assoc;
			Set<GPRAssociation> currentassociations = new HashSet<>();
			for(Association cassoc : orNode.getListOfAssociations())
			{
				currentassociations.addAll(parseAssoc(cassoc));				
			}			
			return currentassociations;
		}
		if(assoc instanceof And)
		{
			And andNode = (And) assoc;
			Vector<Set<GPRAssociation>> currentassociations = new Vector<>();
			
			for(Association cassoc : andNode.getListOfAssociations())
			{
				currentassociations.add(parseAssoc(cassoc));
			}
			Set<GPRAssociation> assocset = new HashSet<>();
			assocset.addAll(permuteSets(currentassociations));
			return assocset;
		}
		if(assoc instanceof GeneProductRef)
		{
			GeneProductRef gpref = (GeneProductRef) assoc;
			String prod = gpref.getGeneProduct();
			Set<GPRAssociation> currentassociations = new HashSet<>();
			if(productids.containsKey(prod))
			{
				GeneProduct product = productids.get(prod);
				//if the gene Product is a protein, we will add all its Genes to its  
				if(proteinMap.containsKey(product))
				{
					GPRAssociation newassoc = new GPRAssociation();
					newassoc.addGenesByProtein(proteinMap.get(product), proteinMap.get(product).getCodingGenes());
					currentassociations.add(newassoc);
					return currentassociations;					
				}
				if(geneMap.containsKey(product))
				{
					GPRAssociation newassoc = new GPRAssociation();
					newassoc.addGene(geneMap.get(product));
					currentassociations.add(newassoc);
					return currentassociations;
				}
			}
			else
			{			
				return new HashSet<GPRAssociation>();
			}
			
		}
		else
		{
			
			return new HashSet<GPRAssociation>();
		}
		return new HashSet<GPRAssociation>();
	}
	
	Vector<GPRAssociation> permuteSets(Vector<Set<GPRAssociation>> sets)
	{
		Vector<GPRAssociation> Resultingsets = new Vector<GPRAssociation>();
		for(Set<GPRAssociation> cset : sets)
		{
			if(Resultingsets.isEmpty())
			{
				Resultingsets.addAll(cset);
			}
			else
			{
				Vector<GPRAssociation> newsets = new Vector<GPRAssociation>();
				for(GPRAssociation assoc :  cset)
				{
					for(GPRAssociation cassoc : Resultingsets)
					{
						newsets.add(new GPRAssociation(assoc,cassoc));
					}
				}
				Resultingsets = newsets;
			}
		}
		return Resultingsets;
	}
}
