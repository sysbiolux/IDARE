package idare.sbmlannotator.internal.gpr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class GPRListTokenizer{

	
	String orregexp = "^or |^OR |^\\| | OR | or | \\| ";
	String andregexp = "^and |^AND |^& | AND | and | & ";
	HashMap<String,String> tokens;
	GPRManager gm;
	Set<GPRAssociation> AssociationSet;
	
	public GPRListTokenizer(String input,GPRManager gm)
	{
		this.gm = gm;
		String[] GeneIDs = input.trim().split(" +");
		AssociationSet = new HashSet<GPRAssociation>();
		for(String gid : GeneIDs)
		{
			GPRAssociation assoc = new GPRAssociation();
			assoc.addGene(gm.getGene(gid.trim()));
			AssociationSet.add(assoc);
		}
	}	
	public Set<GPRAssociation> getGPRAssociations()
	{
		return AssociationSet;
	}

}
