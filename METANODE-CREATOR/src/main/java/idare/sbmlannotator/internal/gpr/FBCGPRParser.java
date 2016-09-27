package idare.sbmlannotator.internal.gpr;

import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.Services.JSBML.And;
import idare.imagenode.internal.Services.JSBML.Association;
import idare.imagenode.internal.Services.JSBML.GeneProduct;
import idare.imagenode.internal.Services.JSBML.GeneProductRef;
import idare.imagenode.internal.Services.JSBML.Model;
import idare.imagenode.internal.Services.JSBML.Or;
import idare.imagenode.internal.Services.JSBML.SBase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;


public class FBCGPRParser {

	//GPRManager gm;
	Set<GPRAssociation> AssociationSet = new HashSet<GPRAssociation>();
	Model model;
	HashMap<String,SBase> productids = new  HashMap<String, SBase>();
	HashMap<SBase, Gene> geneMap;
	HashMap<SBase, Protein> proteinMap;
	public FBCGPRParser(Association assoc, Model model, HashMap<SBase,Gene> genes, HashMap<SBase,Protein> proteins, HashMap<String,SBase> productids) {
		// TODO Auto-generated constructor stub		
		this.model = model;
		this.productids = productids;
		geneMap = genes;
		proteinMap = proteins;
		PrintFDebugger.Debugging(this, "Parsing GPR");
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
			PrintFDebugger.Debugging(this, "Parsing an OR node");
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
			PrintFDebugger.Debugging(this, "Parsing an AND node");
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
			PrintFDebugger.Debugging(this, "Parsing a Gene Product Reference with name " + gpref.getGeneProduct());		
			String prod = gpref.getGeneProduct();
			Set<GPRAssociation> currentassociations = new HashSet<>();
			if(productids.containsKey(prod))
			{
				SBase product = productids.get(prod);
				PrintFDebugger.Debugging(this, "The Gene Product was found, adding it");
				//if the gene Product is a protein, we will add all its Genes to its  
				if(proteinMap.containsKey(product))
				{
					PrintFDebugger.Debugging(this, "as a Protein");
					GPRAssociation newassoc = new GPRAssociation();
					newassoc.addGenesByProtein(proteinMap.get(product), proteinMap.get(product).getCodingGenes());
					currentassociations.add(newassoc);
					return currentassociations;					
				}
				if(geneMap.containsKey(product))
				{
					PrintFDebugger.Debugging(this, "as a Gene");
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
