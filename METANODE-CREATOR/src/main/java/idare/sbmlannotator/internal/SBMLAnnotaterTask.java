package idare.sbmlannotator.internal;

import idare.Properties.IDAREProperties;
import idare.ThirdParty.DelayedVizProp;
import idare.imagenode.internal.Services.JSBML.Annotation;
import idare.imagenode.internal.Services.JSBML.CVTerm;
import idare.imagenode.internal.Services.JSBML.Model;
import idare.imagenode.internal.Services.JSBML.Reaction;
import idare.imagenode.internal.Services.JSBML.SBMLDocument;
import idare.imagenode.internal.Services.JSBML.Species;
import idare.imagenode.internal.Services.JSBML.CVTerm.Qualifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
//import org.sbml.jsbml.CVTerm.Qualifier;

/*import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
//import org.sbml.jsbml.SBMLReader;
//import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
*/

public class SBMLAnnotaterTask extends AbstractTask{

	private CyApplicationManager cymanager;
	private CyEventHelper eventHelper;
	private String ProteinAnnotationDatabase = null;
	private String geneAnnotationDatabase = null;
    private Map<String,Set<CVTerm>> ProteinAnnotations;
	private SBMLDocument SBMLDoc;
	private boolean GenerateGeneNodes;
	private CySwingApplication cySwingApp;
	private Model model;
	private String sbmlTypeCol;
	private String sbmlIDCol;
	private String sbmlCompCol;
	private String sbmlInteractionCol;
	/**
	 * A Task reading in an SBML File and parsing its Notes fields adding the information to the Nodes of the current network. 
	 * @param cymanager 
	 * @param FileName  - The SBML FileName that should be parsed.
	 * @param GenerateGeneNodes - Whether to generate additional nodes based on the GENE_ASSOCIATION or GENE_LIST fields in the Notes. - This can potentially be replaced by the fbc fields at some point.
	 * @param eventHelper
	 */
	public SBMLAnnotaterTask(CyApplicationManager cymanager,SBMLDocument SBMLDoc, boolean GenerateGeneNodes,	CyEventHelper eventHelper, CySwingApplication cySwingApp, String GeneAnnotDB,
			String ProtAnnotDB,Map<String,Set<CVTerm>> ProtAnnot, Model model,String sbmlTypeCol,String sbmlIDCol, String sbmlCompCol, String SBMLInteractionCol)
	{
		this.geneAnnotationDatabase = GeneAnnotDB;
		this.ProteinAnnotationDatabase = ProtAnnotDB;
		this.ProteinAnnotations = ProtAnnot;
		this.cySwingApp = cySwingApp;
		this.cymanager = cymanager;
		this.SBMLDoc = SBMLDoc;
		this.GenerateGeneNodes = GenerateGeneNodes;
		this.eventHelper = eventHelper;
		this.model = model;
		this.sbmlTypeCol= sbmlTypeCol;
		this.sbmlIDCol = sbmlIDCol;
		this.sbmlCompCol = sbmlCompCol;
		sbmlInteractionCol = SBMLInteractionCol;
	}

	@Override
	public synchronized void run(TaskMonitor TaskMonitor) throws Exception {
		try{
		TaskMonitor.setTitle("Reading SBML File");
		
		CyNetwork network = cymanager.getCurrentNetwork();		
		if(network == null)
		{
			JOptionPane.showMessageDialog(null, "Please select a network to apply the SBML annotation to");
			return;
		}

		//create one column for the GeneEdge Info 
		CyTable EdgeTable = network.getDefaultEdgeTable();
		try{
			EdgeTable.createColumn(IDAREProperties.IDARE_EDGE_PROPERTY, String.class, false,"");
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);			
		}

		TaskMonitor.setProgress(0.01);
		//now read the sbml File


		SBMLDocument sbml = null;
		System.out.println("Reading SBML");
		model = SBMLDoc.getModel();
		
		
		System.out.println("Getting Annotation Fields");
		TaskMonitor.setProgress(0.2);
		TaskMonitor.setTitle("Getting Annotation Fields");
		
		CyTable Tab = cymanager.getCurrentNetwork().getDefaultNodeTable();				
		//first of all we will check which new columns we have to add for both Species and Reactions
		// Lets assume there are the standard fields:
		Set<String> AddedCols = new HashSet<String>();
		HashMap<Long,String> GeneLists = new HashMap<Long, String>();
		//possible Gene Annotations (we have to select one type...
		System.out.println("The current model has " + model.getListOfSpecies().size() + "Species");
		HashMap<String,Set<Long>> GeneAssoc = new HashMap<String, Set<Long>>(); 
		HashSet<String> GeneSet = new HashSet<String>();
						
		for (Species species : model.getListOfSpecies())
		{
			//boolean isProtein = false;
			boolean readnotes = true; 
			if(!species.isSetNotes())
			{
				System.out.println("Species " + species.getId() + " has no Note String");
				readnotes = false;
			}			
			if(readnotes)
			{ 
				System.out.println("Getting the NotesString");
				try
				{
					System.out.println("Reading Notes String of species " + species.getId());

					String NotesString = species.getNotesString();	
					


					NotesString = NotesString.replace("html:", "");
					String[] NoteSections = NotesString.split("<p>");					
					//by default the First Notes entry is the HTML Entry, so we skip it
					for(int i=1;i < NoteSections.length;i++)
					{
						if(!isCOBRANote(NoteSections[i]))
						{
							continue;
						}
						String NoteType = NoteSections[i].split(":", 2)[0].trim();

						if(!AddedCols.contains(NoteType))
						{

							AddedCols.add(NoteType);
							try
							{
								Tab.createListColumn("COBRA_" + NoteType, String.class, true);
							}
							catch(IllegalArgumentException e)
							{								
								System.out.println("Column " + NoteType  + " already exists");
							}
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace(System.out);
				}
			}
			if(species.isSetAnnotation() & ProteinAnnotationDatabase != null)
			{
				//Lets test whether this species is also annotated in the protein db
				for(CVTerm cv : species.getAnnotation().getListOfCVTerms())
				{
					if(cv.getBiologicalQualifierType() == CVTerm.Qualifier.BQB_IS)
					{
						for(String ressource : cv.getResources())
						{
							if(getDataBase(ressource).equals(ProteinAnnotationDatabase))
							{
								if(!ProteinAnnotations.containsKey(species.getId()))
								{									
									ProteinAnnotations.put(species.getId(), new HashSet<CVTerm>());
								}
								ProteinAnnotations.get(species.getId()).add(cv);
							}
						}
					}
				}
			}
		}
		

		/*try{
			network.getDefaultNodeTable().createColumn(IDAREProperties.IDARE_NODE_TYPE, String.class, false,"");
		}
		catch(IllegalArgumentException e)
		{
			System.out.println(e.getMessage());
		}
		try{
			network.getDefaultNodeTable().createColumn(IDAREProperties.IDARE_NODE_NAME, String.class, false,"");
		}
		catch(IllegalArgumentException e)
		{
			System.out.println(e.getMessage());
		}
		try{
			network.getDefaultNodeTable().createColumn(IDAREProperties.IDARE_NODE_TYPE, String.class, false,"");
		}
		catch(IllegalArgumentException e)
		{
			System.out.println(e.getMessage());
		}*/
		//JOptionPane.showMessageDialog(null, "Finished Checking Species. Added items contain: " + AddedCols.toString());
		for (Reaction reac : model.getListOfReactions())
		{
			String NotesString = reac.getNotesString();		
			if(NotesString == null)
			{
				continue;
			}

			NotesString = NotesString.replace("html:", "");

			String[] NoteSections = NotesString.split("<p>");					
			//by default the First Notes entry is the HTML Entry, so we skip it
			for(int i=1;i < NoteSections.length;i++)
			{
				if(isCOBRANote(NoteSections[i]))
				{
					String NoteType = NoteSections[i].split(":", 2)[0].trim();

					if(!AddedCols.contains(NoteType))
					{

						AddedCols.add(NoteType);
						try
						{
							CyColumn currentcol = Tab.getColumn("COBRA_" + NoteType);
							if(currentcol == null)
							{
								Tab.createListColumn("COBRA_" + NoteType, String.class, true);
							}
						}
						catch(IllegalArgumentException e)
						{
							System.out.println("Column " +  "COBRA_" + NoteType  +" already exists");
						}
					}
				}
			}
		}
		TaskMonitor.setProgress(0.35);

		TaskMonitor.setTitle("Adding Annotations to Metabolites");
		//		System.out.println("Adding Annotations to Metabolites");
		//Only alter rows if we actually added metabolite data
		for (Species species : model.getListOfSpecies()) 
		{
			if(ProteinAnnotations.containsKey(species.getId()))
			{
				//if this is a Protein, we need to add the according genes (if present) set the sbml type row 
				//to protein and also update the "sbml_id" row 
				Collection<CyRow> proteinrows = Tab.getMatchingRows(sbmlIDCol, species.getId());
				for(CyRow currentrow : proteinrows)
				{
					currentrow.set(sbmlTypeCol,IDAREProperties.NodeType.IDARE_PROTEIN);
					//currentrow.set(IDAREProperties.IDARE_NODE_TYPE, IDAREProperties.NodeType.IDARE_PROTEIN);
					if(ProteinAnnotationDatabase != null)
					{
						if(ProteinAnnotationDatabase == sbmlIDCol)
						{
						//	currentrow.set(IDAREProperties.IDARE_NODE_NAME, currentrow.get(ProteinAnnotationDatabase, String.class));
						}
						else
						{
							Annotation proteinannotation = species.getAnnotation();
							for(CVTerm cv : proteinannotation.getListOfCVTerms())
							{
								
								if(cv.getBiologicalQualifierType() == Qualifier.BQB_IS)
								{
									for(String ressource : cv.getResources())
									{
										if(ressource.contains(ProteinAnnotationDatabase))
										{
											//We assume, that a ressource either is something like http:// 
											if(ressource.matches("http://.*/.*/.*"))
											{											
												try
												{
													String entry = ressource.split("/")[4];
													currentrow.set(sbmlIDCol, entry);
												}
												catch(IndexOutOfBoundsException e)
												{
													//if this is out of bounds, we just ignore it.
													continue;
												}
											}

											if(ressource.matches(".*:.*:.*:.*"))
											{
												try
												{
													String entry = ressource.split(":")[3];
													currentrow.set(sbmlIDCol, entry);
												}
												catch(IndexOutOfBoundsException e)
												{
													//if this is out of bounds, we just ignore it.
													continue;
												}
											}										
										}
									}
								}
							}
						}
					}
					//and now evaluate the genes. i.e. add genes to the gene list associated with this protein.
					if(geneAnnotationDatabase != null)
					{						
						for(CVTerm cv : ProteinAnnotations.get(species.getId()))
						{
							if(cv == null)
							{
								continue;
							}
							if(cv.getBiologicalQualifierType() == Qualifier.BQB_IS_ENCODED_BY)
							{
								for(String ressource : cv.getResources())
								{
									if(getDataBase(ressource).equals(geneAnnotationDatabase))
									{
										String GeneName = getURIid(ressource);
										if(GeneName != null)
										{
											if(!GeneAssoc.containsKey(GeneName))
											{
												GeneAssoc.put(GeneName, new HashSet<Long>());
											}
											GeneAssoc.get(GeneName).add(currentrow.get(CyNetwork.SUID, Long.class));
											GeneSet.add(GeneName);
										}
									}
								}

							}								
						}
					}




				}
			}
			else
			{
				//otherwise this is a proper species, so we won't add further details
				//this can be changed at some point.
				Collection<CyRow> namerows = Tab.getMatchingRows(sbmlIDCol, species.getId());
				Collection<CyRow> compartmentrow = Tab.getMatchingRows(sbmlCompCol, species.getCompartment());
				Vector<CyRow> truematches = new Vector<CyRow>();
				for(CyRow row : namerows)
				{
					if(compartmentrow.contains(row)){
						truematches.add(row);
					}
				}

				//Lets test what we got
				String NotesString = species.getNotesString();		
				if(NotesString == null)
				{
					continue;
				}
				NotesString = NotesString.replace("html:", "");
				HashMap<String,List<String>> NoteMap = new HashMap<String, List<String>>();
				//Split this by line
				String[] NoteSections = NotesString.split("(<p>)|(<html:p>)");					
				//by default the First Notes entry is the HTML Entry, so we skip it
				for(int i=1;i < NoteSections.length;i++)
				{
					if(!isCOBRANote(NoteSections[i]))
					{
						//ignore anything that is not a COBRA Note.
						continue;
					}
					//System.out.println("Original: " + NoteSections[i]);
					String NoteType = NoteSections[i].split(":", 2)[0].trim();
					//System.out.println("Before Identifier: " + NoteSections[i].split(":", 2)[0]);
					//remove the trailing html stuff and any leading or trailing whitespaces
					if(!NoteMap.containsKey(NoteType))
					{
						NoteMap.put(NoteType, new LinkedList<String>());
					}					
					String NoteValue = NoteSections[i].split(":", 2)[1].replaceAll("</[a-zA-Z]+>", "").trim();
					NoteMap.get(NoteType).add(NoteValue);
					//System.out.println("After Identifier" + NoteSections[i].split(":", 2)[1].replaceAll("</[a-zA-Z]+>", "").trim());
					//Now we should have something to put into the rows.

				}

				for(CyRow row : truematches)
				{
					for(String NoteType : NoteMap.keySet())
						//System.out.println(row.getAllValues().toString());
					{
						row.set("COBRA_" + NoteType, NoteMap.get(NoteType));
					}
				}
			}
		}
		TaskMonitor.setProgress(0.6);

		TaskMonitor.setTitle("Adding Annotations to Reactions");
		//		System.out.println("Adding Annotations to Reactions");
		//JOptionPane.showMessageDialog(null, "Finished Adding Metabolite Data");
		for (Reaction reac: model.getListOfReactions()) 
		{
			Collection<CyRow> namerows = Tab.getMatchingRows(sbmlIDCol, reac.getId());
			Vector<CyRow> truematches = new Vector<CyRow>();
			for(CyRow row : namerows)
			{
				truematches.add(row);
				if(reac.isReversible())
				{
					//adjust all adjacent edges
					List<CyEdge> edges = network.getAdjacentEdgeList(network.getNode(row.get(CyNode.SUID, Long.class)),CyEdge.Type.ANY);
					for(CyEdge edge : edges)
					{
						network.getDefaultEdgeTable().getRow(edge.getSUID()).set(sbmlInteractionCol, IDAREProperties.EdgeType.REACTION_REVERSIBLE);
					}
				}
			}
			HashMap<String,List<String>> NoteMap = new HashMap<String, List<String>>();

			//Lets test what we got
			String NotesString = reac.getNotesString();	
			if(NotesString == null)
			{
				continue;
			}
			
			NotesString = NotesString.replace("html:", "");
			String[] NoteSections = NotesString.split("<p>");					
			//by default the First Notes entry is the HTML Entry, so we skip it
			for(int i=1;i < NoteSections.length;i++)
			{
	
				if(!isCOBRANote(NoteSections[i]))
				{
					//ignore anything that is not a COBRA Note.
					continue;
				}
				String NoteType = NoteSections[i].split(":", 2)[0].trim();
				//System.out.println("Before Identifier: " + NoteSections[i].split(":", 2)[0]);
				//remove the trailing html stuff and any leading or trailing whitespaces
				if(!NoteMap.containsKey(NoteType))
				{
					NoteMap.put(NoteType, new LinkedList<String>());
				}
				String NoteValue = NoteSections[i].split(":", 2)[1].replaceAll("</[a-zA-Z]+>", "").trim();
				NoteMap.get(NoteType).add(NoteValue);

			}
			//Now we should have something to put into the rows.

			for(CyRow row : truematches)
			{
				for(String NoteType : NoteMap.keySet())
					//System.out.println(row.getAllValues().toString());
				{
					row.set("COBRA_" + NoteType, NoteMap.get(NoteType));
					if(NoteType.equals("GENE_ASSOCIATION") || NoteType.equals("GENE_LIST"))
					{
						//it doesn't matter whether we overwrite things here so long as the model file is coherent...
						for(String NoteValue : NoteMap.get(NoteType))
						{
							GeneLists.put(row.get(CyNetwork.SUID, Long.class),NoteValue);
						}
					}
				}
			}

		}
		TaskMonitor.setProgress(0.8);

		if(GenerateGeneNodes)
		{
			Set<DelayedVizProp> VizProps = new HashSet<DelayedVizProp>(); 
			TaskMonitor.setTitle("Setting up Gene Nodes");
			//			System.out.println("Setting up Gene Nodes");
			//Take whatever we can get. Either GENE Associations or Gene Lists
			// now, we simply split at each and/AND/And and Or/OR/or and collect the genes without brackets
			
			for(Long key : GeneLists.keySet())
			{
				String GeneString = GeneLists.get(key);
				GeneString = GeneString.replaceAll("\\(|\\)", "");
				String[] GeneList = GeneString.split(" And | AND | and | Or | OR | or ");				
				//HashSet<String> localGeneSet = new HashSet<String>();
				for(String gene : GeneList)
				{
					String[] Genes = gene.trim().split(" +");
					for(String lgene : Genes)
					{
						if(lgene.trim().length() > 0)
						{							
							GeneSet.add(lgene.trim());
							if(!GeneAssoc.containsKey(lgene.trim())){
								GeneAssoc.put(lgene.trim(), new HashSet<Long>());
								//System.out.println("The next gene symbol is" + lgene.trim() + " !");
							}
							//add this reaction to the gene list.
							//by doing this we can later on create one CyNode per Gene and link it to every associated reaction.
							GeneAssoc.get(lgene.trim()).add(key);
						}
					}

				}								
			}			
			Set<String> singleGenes = new HashSet<String>();			
			for(String Gene : GeneAssoc.keySet())
			{				
				Set<Long> ReacSet = GeneAssoc.get(Gene);
				if(ReacSet.size() > 1)
				{
					//if there is more than one Reaction associated with the gene, we will have to handle it seperately
					continue;
				}
				else
				{
					singleGenes.add(Gene);
					System.out.println("Gene " + Gene + " has only one associated reaction.");
				}
			}
			//now we can create the new CyNodes
			CyTable nodetab = cymanager.getCurrentNetwork().getDefaultNodeTable();
			CyTable edgetab = cymanager.getCurrentNetwork().getDefaultEdgeTable();
			Collection<CyColumn> cols = edgetab.getColumns();
			//			for(CyColumn col : cols)
			//			{
			//				System.out.println(col.getName());
			//			}
//			try{
//				edgetab.createColumn(IDAREProperties.IDARE_EDGE_PROPERTY, String.class, false,"");
//			}
//			catch(IllegalArgumentException e)
//			{
//				System.out.println(e.getMessage());
//			}

			//CyNetwork network = cymanager.getCurrentNetwork();

			HashMap<CyNode, Set<CyNode>> ReacToGene = new HashMap<CyNode, Set<CyNode>>();
			for(String gene: GeneSet)
			{
				CyNode newNode = network.addNode();				
				CyRow cRow = nodetab.getRow(newNode.getSUID());
				//if this network is set up, we will also fill in the IDARE Fields 
//				if(Tab.getColumn(IDAREProperties.IDARE_NODE_NAME) != null)
//				{
//					cRow.set(IDAREProperties.IDARE_NODE_NAME, gene);
//				}
//				if(Tab.getColumn(IDAREProperties.IDARE_NODE_TYPE) != null)
//				{
//					cRow.set(IDAREProperties.IDARE_NODE_TYPE, IDAREProperties.NodeType.IDARE_GENE);
//				}
				cRow.set(sbmlTypeCol, IDAREProperties.SBML_GENE_STRING);
				cRow.set(sbmlIDCol, gene);
				// if it is a single Gene Association, add it to the ReacToGeneMap of the corresponding node. 
				if(singleGenes.contains(gene))
				{
					for(Long ReacSUID : GeneAssoc.get(gene))						
					{
						CyNode ReacNode = network.getNode(ReacSUID);
						CyEdge edge = network.addEdge(newNode, ReacNode,true);
						//edgetab.getRow(edge.getSUID()).set(IDAREProperties.IDARE_EDGE_PROPERTY, IDAREProperties.GENE_EDGE_ID);
						if(!ReacToGene.containsKey(ReacNode))
						{
							ReacToGene.put(ReacNode, new HashSet<CyNode>());
						}
						ReacToGene.get(ReacNode).add(newNode);					
					}					
				}
				else
				{
					Double currenty = 0.;
					Double currentx = 0.;
					for(Long ReacSUID : GeneAssoc.get(gene))
					{
						CyNode ReacNode = network.getNode(ReacSUID);
						View<CyNode> ReacNodeView = cymanager.getCurrentNetworkView().getNodeView(ReacNode);
						Double xpos = ReacNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
						Double ypos = ReacNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
						//adjust the position into the middle of all involved reactions.
						currenty = (currenty + ypos/GeneAssoc.get(gene).size());
						currentx = (currentx + xpos/GeneAssoc.get(gene).size());
						CyEdge edge = network.addEdge(newNode, ReacNode ,true);
						//edgetab.getRow(edge.getSUID()).set(IDAREProperties.IDARE_EDGE_PROPERTY, IDAREProperties.GENE_EDGE_ID);

					}
					VizProps.add(new DelayedVizProp(newNode, BasicVisualLexicon.NODE_X_LOCATION, currentx, false));
					VizProps.add(new DelayedVizProp(newNode, BasicVisualLexicon.NODE_Y_LOCATION, currenty, false));

				}
			}
			//position all Genes with multiple places
			for(CyNode ReacNode : ReacToGene.keySet())
			{
				int i = 0;	
				//place them in a distance of 2*Node Width starting with the "Top" position and then circling around.
				int count = ReacToGene.get(ReacNode).size();
				View<CyNode> ReacNodeView = cymanager.getCurrentNetworkView().getNodeView(ReacNode);

				Double width = ReacNodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
				for(CyNode GeneNode : ReacToGene.get(ReacNode))
				{
					if(GeneNode == null)
					{
						continue;
					}
					Double x = ReacNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
					Double y = ReacNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
					Double NewNodeX = GetXPosition(i, count, x, width);
					Double NewNodeY = GetYPosition(i, count, y, width);

					i+=1;
					VizProps.add(new DelayedVizProp(GeneNode, BasicVisualLexicon.NODE_X_LOCATION, NewNodeX, false));
					VizProps.add(new DelayedVizProp(GeneNode, BasicVisualLexicon.NODE_Y_LOCATION, NewNodeY, false));
				}

			}
			eventHelper.flushPayloadEvents();			
			DelayedVizProp.applyAll(cymanager.getCurrentNetworkView(), VizProps);
			eventHelper.flushPayloadEvents();
			cymanager.getCurrentNetworkView().updateView();			
		}
		}
		catch(Exception e)
		{
			//e.getCause().printStackTrace(System.out);
			e.printStackTrace(System.out);
		}
		TaskMonitor.setProgress(1.0);		
	}

	/**
	 * Get the X Position for the number-th  of code Nodes 
	 * @param number - the position of the node (should be between 1 and count) 
	 * @param count - the total count of nodes (this corresponds to the sections a circle is split into)
	 * @param x - the x position of the center of the circle
	 * @param width - the width of the circle.
	 * @return The X position on the circle for the number-th of count nodes
	 */
	private double GetXPosition(int number, int count, double x, double width)
	{
		double singleangle = 2*Math.PI / count;
		double currentangle = number * singleangle;
		double xchange = width*2*Math.cos(currentangle);
		return x+xchange;
	}
	/**
	 * Get the Y Position for the number-th  of code Nodes 
	 * @param number - the position of the node (should be between 1 and count) 
	 * @param count - the total count of nodes (this corresponds to the sections a circle is split into)
	 * @param y - the y position of the center of the circle
	 * @param width - the width of the circle.
	 * @return The Y position on the circle for the number-th of count nodes
	 */
	public double GetYPosition(int number, int count, double y, double width)
	{
		double singleangle = 360 / count;
		double currentangle = number * singleangle;		
		double ychange = width*2*Math.sin(currentangle);
		return y+ychange;
	}
	private boolean isCOBRANote(String note)
	{
		if(note.contains("</p>"))
		{
			//The COBRA Field has to end with a </p> on the same line 
			note = note.split("</p>")[0];
		}
		boolean isCobraField = note.matches("[(^GENE_ASSOCIATION)|(^SUBSYSTEM)|(^FORMULA)|(^CHARGE)|(^EC Number)|(^AUTHORS)|(^Confidence Level)].*");
/*		if(isCobraField)
		{
			System.out.println("This is a COBRA field: " + note);
		}		
		else
		{
			System.out.println("This is not a COBRA Field: \n" + note);
		}*/
		return isCobraField;

	}
	private String getDataBase(String URI)
	{
		String database = "";

		if(URI.matches("http://.*/.*/.*"))
		{
			try
			{
				database = URI.split("/")[3];					
			}
			catch(IndexOutOfBoundsException ex)
			{
				//Do Nothing
			}
		}
		if(URI.matches(".*:.*:.*:.*"))
		{
			try
			{
				database = URI.split(":")[2];
			}

			catch(IndexOutOfBoundsException ex)
			{
				//Do Nothing
			}
		}

		return database;
	}
	private String getURIid(String URI)
	{
		String entry = null;
		if(URI.matches("http://.*/.*/.*"))
		{
			try
			{

				entry = URI.split("/")[4];												
			}
			catch(IndexOutOfBoundsException e)
			{
				return entry;
			}

		}
		if(URI.matches(".*:.*:.*:.*"))
		{
			try
			{
				entry = URI.split(":")[3];																								
			}
			catch(IndexOutOfBoundsException e)
			{
				return entry;
			}

		}
		return entry;
	}	
}

