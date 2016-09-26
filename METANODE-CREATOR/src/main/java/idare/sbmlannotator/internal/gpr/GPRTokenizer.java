package idare.sbmlannotator.internal.gpr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GPRTokenizer {

	
	String orregexp = "^or |^OR |^\\| |^Or | Or | OR | or | \\| ";
	String andregexp = "^and |^AND |^& |^And | And | AND | and | & ";
	HashMap<String,String> tokens;
	GPRManager gm;
	Set<GPRAssociation> AssociationSet;
	
	public GPRTokenizer(String input,GPRManager gm)
	{
		this.gm = gm;
		tokens = new HashMap<>();
		int tokencount = 0;
		Pattern matchregexp = Pattern.compile("\\(([^" + Pattern.quote("(")+ "]+?)\\)");
		Matcher match = matchregexp.matcher(input);
		String modstring = input;
		String headtoken = input;
		if(match.find())
		{
			String currenttoken = "$" + tokencount++;
			headtoken = currenttoken;
			tokens.put(currenttoken, match.group(1));
			modstring = modstring.replace("(" + match.group(1) + ")", currenttoken);		
		}
		while(modstring != input)
		{
			input = modstring;
			match = matchregexp.matcher(input);
			if(match.find())
			{
				String currenttoken = "$" + tokencount++;
				headtoken = currenttoken;
				tokens.put(currenttoken, match.group(1));
				modstring = modstring.replace("(" + match.group(1) + ")", currenttoken );
			}
//			System.out.println(modstring);
		}
//		System.out.println("There are " + tokens.size() + " tokens");
		
//		for(int val = tokencount-1 ; val >= 0; val--)
//		{
//			System.out.println("$" + val + ": " + tokens.get("$" + val));
//		}		
		AssociationSet = generateGPRAssociations(headtoken);
	}
	
	
	private Set<GPRAssociation> generateGPRAssociations(String Head)
	{
		String headtoken = Head;
		if(tokens.containsKey(Head))
		{
			headtoken = tokens.get(Head);
		}
		String[] proteincoders = headtoken.trim().split(orregexp);
		HashSet<GPRAssociation> parsedAssociations = new HashSet<>();
		for(String token : proteincoders)
		{
//			System.out.println("Parsing OR token : " + token.trim());
			Vector<GPRAssociation> currentassociations = new Vector<>();
			String[] andtokens = token.trim().split(andregexp);			
			for(String andtoken :andtokens)
			{	
//				System.out.println("Parsing AND token : " + andtoken.trim());
				if(tokens.containsKey(andtoken.trim()))
				{
					Set<GPRAssociation> tokenSet = generateGPRAssociations(andtoken.trim());
					Vector<GPRAssociation> newGPRs = new Vector<>();
					if(currentassociations.isEmpty())
					{
						currentassociations.addAll(tokenSet);
					}
					else
					{
						for(GPRAssociation assoc : tokenSet)
						{
							for(GPRAssociation existingassoc : currentassociations)
							{
								
								GPRAssociation temp = new GPRAssociation(assoc,existingassoc);
								String GenesInA = "";
								String GenesInB = "";
								for(Gene g : assoc.geneset)
								{
									GenesInA += g + ";";
								}
								for(Gene g : existingassoc.geneset)
								{
									GenesInB += g  + ";";
								}
//								System.out.println("Generated new GPR for GPR with genes: (" + GenesInA + ") and (" + GenesInB + ")");
								newGPRs.add(temp);
							}
						}
						currentassociations = newGPRs;
					}
				}
				else
				{
					if(!currentassociations.isEmpty())
					{
					for(GPRAssociation assoc: currentassociations)
					{
						assoc.addGene(gm.getGene(andtoken.trim()));
					}
					}
					else
					{
						
						GPRAssociation singleGeneAssoc = new GPRAssociation();
						Gene g = gm.getGene(andtoken.trim());
//						System.out.println("Generated new GPR for gene " + g );
						singleGeneAssoc.addGene(g);
						currentassociations.add(singleGeneAssoc);
					}
				}
			}
			parsedAssociations.addAll(currentassociations);
		}
		
//		for(GPRAssociation gpr : parsedAssociations)
//		{
//			System.out.println("Current GPRs: " + gpr.toString());
//		}
		return parsedAssociations;
		
	}

	
	public Set<GPRAssociation> getGPRAssociations()
	{
		return AssociationSet;
	}

}
