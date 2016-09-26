package idare.sbmlannotator.internal.Tasks;


import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;
import idare.ThirdParty.CobraUtil;
import idare.ThirdParty.DelayedVizProp;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.Services.JSBML.*;
import idare.sbmlannotator.internal.gpr.FBCGPRParser;
import idare.sbmlannotator.internal.gpr.GPRAssociation;
import idare.sbmlannotator.internal.gpr.GPRListTokenizer;
import idare.sbmlannotator.internal.gpr.GPRManager;
import idare.sbmlannotator.internal.gpr.GPRTokenizer;
import idare.sbmlannotator.internal.gpr.Gene;
import idare.sbmlannotator.internal.gpr.Protein;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.solr.common.util.Hash;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.RequestsUIHelper;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.ListSingleSelection;
import org.identifiers.registry.RegistryUtilities;



/**
 * A Task that annotates an network with data from an SBML file that is gene and protein specific.
 * There are three expected ways how GPRS can be annotated:
 * 1. By FBC GeneProductAssociations and the respective GeneProduct fields
 * 2. by COBRA notes fields.
 * 3. By ModifierSpecies annotated to reactions.
 * We expect these methods to be mutually exclusive and not used simultaneously in one SBML!
 * Furthermore, we expect, that GeneProducts or Species EITHER represent Genes OR Represent Proteins.
 * Thus the protein/gene annotation will likely fail if a mixed annotation is done.
 * If annotation is done via FBC, then COBRA Fields Are ignored for any reaction that is annotated by FBC GeneProductAssociations.
 * Any formerly loaded fbc AND or OR nodes (e.g. by cy3sbml) will eb removed and a new logic will be calculated.
 * GeneProduct Nodes will be retained but will get their sbml-type adjusted to the type they represent (either Gene or Protein).
 * The same applies for modifierspecies, which will also be reannotated as gene or protein, if their annotation fields contain the information.
 * FBC GeneProducts without annotation wil be considered as Genes.
 * @author Thomas Pfau
 *
 */
public class SBMLAnnotatorTask extends AbstractTask  implements RequestsUIHelper {



	@Tunable(description="Do you want to generate gene and Protein Nodes?", dependsOn="doc!=null")
	public boolean generateGeneNodes = true;


	//@Tunable
	boolean hasProteinAnnotation = false;

	@Tunable(description="Select protein database for names")
	public ListSingleSelection<String> proteinAnnotationDataBase;

	//	@Tunable()
	boolean hasGeneAnnotation = false;

	@Tunable(description="Select gene database for names")
	public ListSingleSelection<String> geneAnnotationDataBase;


	//	@Tunable
	boolean sbmlTypeColSet = false;

	@Tunable(description="Select Column for SBML Type")
	public ListSingleSelection<String> sbmltypecolsel;


	//	@Tunable
	boolean sbmlIDColSet = false;

	@Tunable(description="Select Column for SBML ID")//, dependsOn="sbmlIDColSet=true")
	public ListSingleSelection<String> sbmlidcolsel;

	private String sbmlIDcol;
	private String sbmlTypecol;

	@Tunable
	public SBMLDocument doc;
	
	private CyNetwork network;
	GPRManager gm;
	IDARESettingsManager ism;	
	CyEventHelper eventHelper;
	CyNetworkView networkView;
	private boolean cysbmlNetwork;



	private HashMap<SBase,HashMap<String,Vector<String>>> proteinToGeneAnnotation = new HashMap<SBase, HashMap<String,Vector<String>>>();
	private HashMap<SBase,HashMap<String,String>> geneIDAnnotation = new HashMap<SBase, HashMap<String,String>>();
	private HashMap<SBase,HashMap<String,String>> proteinIDAnnotation = new HashMap<SBase, HashMap<String,String>>();

	private HashMap<String,HashMap<String,Gene>> Genes;

	HashSet<String> proteinAnnotationDatabases = new HashSet<String>();
	HashSet<String> geneAnnotationDatabases = new HashSet<String>();

	HashMap<CyNode,Set<GPRAssociation>> AssociatedGPRs = new HashMap<CyNode, Set<GPRAssociation>>();	
	HashMap<String,SBase> SBMLObjectIDs = new HashMap<String, SBase>();
	HashMap<SBase,CyRow> matchingNodes = new HashMap<SBase, CyRow>();

	HashMap<SBase, Protein> proteinMap = new HashMap<SBase, Protein>();
	HashMap<SBase, Gene> geneMap = new HashMap<SBase, Gene>();

	HashMap<Protein, CyNode> proteinNodes = new HashMap<Protein, CyNode>();
	HashMap<Gene, CyNode> geneNodes = new HashMap<Gene, CyNode>();


	HashMap<CyNode, Set<CyRow>> nodesToMerge = new HashMap<CyNode, Set<CyRow>>();

	HashMap<Protein,Set<Gene>> CodingGenes = new HashMap<Protein, Set<Gene>>();

	HashMap<CyNode,Set<CyNode>> proteinNodeSources = new HashMap<CyNode, Set<CyNode>>();  
	HashMap<CyNode,Set<CyNode>> reacNodeSources = new HashMap<CyNode, Set<CyNode>>();
	HashMap<CyNode,Set<CyNode>> proteinComplexNodeSources = new HashMap<CyNode, Set<CyNode>>();


	HashMap<CyNode,Set<CyNode>> proteinNodeTargets = new HashMap<CyNode, Set<CyNode>>();  	
	HashMap<CyNode,Set<CyNode>> geneNodeTargets = new HashMap<CyNode, Set<CyNode>>();
	HashSet<CyNode> placedNodes = new HashSet<CyNode>();
	Vector<Reaction> CobraReactions = new Vector<Reaction>();

	boolean skipNodes = false;
	String ErrorMessage = "";

	public SBMLAnnotatorTask(SBMLManagerHolder holder, CyNetwork network, IDARESettingsManager ism,
			CyNetworkView networkView, CyEventHelper eventHelper) {
		// TODO Auto-generated constructor stub
		this.eventHelper = eventHelper;
		this.networkView = networkView;		

		this.ism = ism;
		gm = new GPRManager(ism);

		cysbmlNetwork = holder.isSBMLManagerPresent();
		try{
			doc = holder.readSBML(network);
		}
		catch(Exception e)
		{
			ErrorMessage = e.getMessage();
			e.printStackTrace(System.out);
			return;
		}
		if(doc == null)
		{			
			ErrorMessage = "SBML Could not be read or the network is not associated with an SBML.";
			return;
		}
		this.network = network;
		setupAnnotationDBs();
		if(!proteinAnnotationDatabases.isEmpty())
		{
			PrintFDebugger.Debugging(this, "Setting up the protein AnnotationDatabase IDs");
			ArrayList<String> al = new ArrayList<String>();
			al.addAll(proteinAnnotationDatabases);
			proteinAnnotationDataBase = new ListSingleSelection<String>(al);			
			hasProteinAnnotation = true;
		}
		if(!geneAnnotationDatabases.isEmpty())
		{
			PrintFDebugger.Debugging(this, "Setting up the gene AnnotationDatabase IDs with " + geneAnnotationDatabases.size() + " Options");
			ArrayList<String> al = new ArrayList<String>();
			al.addAll(geneAnnotationDatabases);
			for(String temp : geneAnnotationDatabases)
			{
				PrintFDebugger.Debugging(this, "Gene Database Option: " + temp);
			}
			geneAnnotationDataBase = new ListSingleSelection<String>(al);
			hasGeneAnnotation = true;
		}
		List<String> columnchoices = new ArrayList<String>();
		for(CyColumn col : network.getDefaultNodeTable().getColumns())
		{
			columnchoices.add(col.getName());
		}
		CyColumn sbmlidcol = null;
		sbmlidcol = network.getDefaultNodeTable().getColumn("sbml id");
		if(sbmlidcol != null)
		{
			sbmlIDColSet = true;
			sbmlIDcol = "sbml id";
		}
		else
		{
			sbmlidcolsel = new ListSingleSelection<String>(columnchoices);
		}		
		CyColumn sbmltypecol = null;
		sbmltypecol = network.getDefaultNodeTable().getColumn("sbml type");
		if(sbmltypecol != null)
		{
			sbmlTypeColSet = true;
			sbmlTypecol = "sbml type";
		}
		else
		{
			sbmltypecolsel = new ListSingleSelection<String>(columnchoices);
		}
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		if(doc == null)
		{
			throw new Exception(ErrorMessage);
		}

		taskMonitor.setTitle("Adding SBML Annotations to the Network");	
		if(!sbmlIDColSet)
		{
			sbmlIDcol = sbmlidcolsel.getSelectedValue();
		}
		if(!sbmlTypeColSet)
		{
			sbmlTypecol = sbmltypecolsel.getSelectedValue();
		}
		PrintFDebugger.Debugging(this, "The SBML Columns are: ID - " + sbmlIDcol + " ; Type - " + sbmlTypecol);


		taskMonitor.setStatusMessage("Obtaining SBML Document");
		Model sbmlModel = doc.getModel();
		PrintFDebugger.Debugging(this, "Found the document ");

		taskMonitor.setStatusMessage("Finding matching nodes");
		//set up the matching rows
		getMatchingNodes();
		taskMonitor.setStatusMessage("Annotating Cobra Fields");
		if(!cysbmlNetwork)
		{
			annotateCobraFields();
		}		
		else
		{
			getCobraReactions();
		}
		//if(cysbmlNetwork & generateGeneNodes)
		//{
		//	removeFBCLogic();			
		//}
		if(!generateGeneNodes)
		{			
			taskMonitor.setStatusMessage("No Gene Nodes to be generated - Finishing");
			return;
		}
		IDARESettingsManager.initNetwork(network);
		if(hasProteinAnnotation)
		{
			taskMonitor.setStatusMessage("Annotating Proteins");
		}
		createSpeciesProteins();
		if(hasGeneAnnotation)
		{
			taskMonitor.setStatusMessage("Annotating Genes");
		}
		createSpeciesGenes();
		//FBCLogic >> COBRA Logic
		taskMonitor.setStatusMessage("Parsing GPRs");
		if(doc.getModel().isFBCPackageEnabled());
		{
			parseFBCLogic();
		}
		parseCOBRALogic();
		//removeModifierGeneEdges();	
		if(!skipNodes)
		{
			taskMonitor.setStatusMessage("Creating and connecting nodes for GPRs");
			buildGPRs(taskMonitor);
			buildNonLogicGenes();
			assignPositions();
		}
		else
		{
			throw new Exception(ErrorMessage);
		}
	}

	private void buildNonLogicGenes()
	{
		if(generateGeneNodes)
		{
			for(Protein p : proteinNodes.keySet())
			{
				if(!AssociatedGPRs.containsKey(proteinNodes.get(p)))
				{					
					assignProteinTargets(p);
					String proteinName = hasProteinAnnotation ? p.getDataBaseName(proteinAnnotationDataBase.getSelectedValue()) : p.getName();
					setupRowAs(network.getDefaultNodeTable(), network.getRow(proteinNodes.get(p)), proteinName, IDAREProperties.NodeType.IDARE_PROTEIN);
					setupProteinNode(p, getProteinNode(p));					
				}
			}
		}
	}

	private void assignProteinTargets( Protein p)
	{
		CyNode proteinNode = getProteinNode(p);
		for(CyEdge edge : network.getAdjacentEdgeList(proteinNode, CyEdge.Type.ANY))
		{
			CyNode target = proteinNode.equals(edge.getSource()) ? edge.getTarget() : edge.getSource();
			if(proteinComplexNodeSources.containsKey(target) || reacNodeSources.containsKey(target) || IDAREProperties.SBML_REACTION_STRING.equals(network.getRow(target).get(sbmlTypecol, String.class)))
			{
				proteinNodeTargets.get(proteinNode).add(target);
			}
		}
	}

	private void buildGPRs(TaskMonitor taskMonitor)
	{
		if(generateGeneNodes)
		{
			taskMonitor.setStatusMessage("Setting up Gene Nodes");

			for(CyNode reacNode : AssociatedGPRs.keySet())
			{				
				Set<GPRAssociation> reacGPRs = AssociatedGPRs.get(reacNode);
				if(!reacNodeSources.containsKey(reacNode)){
					reacNodeSources.put(reacNode, new HashSet<CyNode>());
				}
				for(GPRAssociation currentGPR : reacGPRs)
				{
					setupGPRAssociation(currentGPR, reacNode);								
				}
			}

		}
	}

	private void assignPositions()
	{
		//make sure, views exist for all nodes!
		eventHelper.flushPayloadEvents();
		//then assign their position
		assignPositionsSurroundingReacs();
		assignPositionsSouroundingComplexes();
		assignProteinPositions();
		assignPositionsSurroundingProteins();
		assignGenePositions();
		networkView.updateView();
	}


	private void assignGenePositions()
	{		
		Set<DelayedVizProp> VizProps = new HashSet<DelayedVizProp>(); 
		Set<CyNode> unplacedgeneNodes = new HashSet<CyNode>();
		unplacedgeneNodes.addAll(geneNodes.values());
		unplacedgeneNodes.removeAll(placedNodes);

		for(CyNode geneNode : unplacedgeneNodes)
		{
			double xpos = 0;
			double ypos = 0;
			if(geneNodeTargets.containsKey(geneNode))
			{
				for(CyNode node : geneNodeTargets.get(geneNode))
				{
					View<CyNode> nodeView = networkView.getNodeView(node);
					xpos += nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
					ypos += nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				}

				if(geneNodeTargets.get(geneNode).size() != 0)
				{
					xpos /= geneNodeTargets.get(geneNode).size();
					ypos /= geneNodeTargets.get(geneNode).size();
				}
				else
				{
					View<CyNode> nodeView = networkView.getNodeView(geneNode);
					if(nodeView != null)
					{
						xpos = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
						ypos = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
					}
				}
				VizProps.add(new DelayedVizProp(geneNode, BasicVisualLexicon.NODE_X_LOCATION, xpos, false));
				VizProps.add(new DelayedVizProp(geneNode, BasicVisualLexicon.NODE_Y_LOCATION, ypos, false));

			}

		}
		eventHelper.flushPayloadEvents();			
		DelayedVizProp.applyAll(networkView, VizProps);		
		eventHelper.flushPayloadEvents();			

	}

	private void assignPositionsSurroundingProteins()
	{		
		Set<DelayedVizProp> VizProps = new HashSet<DelayedVizProp>(); 

		for(CyNode proteinNode : proteinNodeSources.keySet())
		{
			Set<CyNode> surroundingNodes = new HashSet<CyNode>();		

			for(CyNode sourceNode : proteinNodeSources.get(proteinNode) )
			{
				if(geneNodeTargets.containsKey(sourceNode) && geneNodeTargets.get(sourceNode).size() <=1)
				{
					surroundingNodes.add(sourceNode);
					placedNodes.add(sourceNode);
				}
			}

			int sourroundingNodeCount = surroundingNodes.size(); 
			int i = 0;	
			//place them in a distance of 2*Node Width starting with the "Top" position and then circling around.		
			View<CyNode> proteinNodeView = networkView.getNodeView(proteinNode);

			Double width = IMAGENODEPROPERTIES.IDARE_NODE_DISPLAY_HEIGHT;
			for(CyNode neighbourNode : surroundingNodes)
			{
				if(neighbourNode == null)
				{
					continue;
				}
				Double x = proteinNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				Double y = proteinNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				Double NewNodeX = GetXPosition(i, sourroundingNodeCount, x, width);
				Double NewNodeY = GetYPosition(i, sourroundingNodeCount, y, width);

				i+=1;
				VizProps.add(new DelayedVizProp(neighbourNode, BasicVisualLexicon.NODE_X_LOCATION, NewNodeX, false));
				VizProps.add(new DelayedVizProp(neighbourNode, BasicVisualLexicon.NODE_Y_LOCATION, NewNodeY, false));
			}

		}
		eventHelper.flushPayloadEvents();			
		DelayedVizProp.applyAll(networkView, VizProps);		
		eventHelper.flushPayloadEvents();			

	}

	private void assignProteinPositions()
	{		
		Set<DelayedVizProp> VizProps = new HashSet<DelayedVizProp>(); 
		Set<CyNode> unplacedProteinNodes = new HashSet<CyNode>();
		unplacedProteinNodes.addAll(proteinNodes.values());
		unplacedProteinNodes.removeAll(placedNodes);

		for(CyNode proteinNode : unplacedProteinNodes)
		{
			double xpos = 0;
			double ypos = 0;
			if(proteinNodeTargets.containsKey(proteinNode))
			{
				for(CyNode node : proteinNodeTargets.get(proteinNode))
				{
					View<CyNode> nodeView = networkView.getNodeView(node);
					xpos += nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
					ypos += nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				}
				if(proteinNodeTargets.get(proteinNode).size() != 0)
				{				
					xpos /= proteinNodeTargets.get(proteinNode).size();
					ypos /= proteinNodeTargets.get(proteinNode).size();
				}
				else
				{
					
					View<CyNode> nodeView = networkView.getNodeView(proteinNode);
					if(nodeView != null)
					{
						xpos = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
						ypos = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
					}
				}				
				VizProps.add(new DelayedVizProp(proteinNode, BasicVisualLexicon.NODE_X_LOCATION, xpos, false));
				VizProps.add(new DelayedVizProp(proteinNode, BasicVisualLexicon.NODE_Y_LOCATION, ypos, false));

			}

		}
		eventHelper.flushPayloadEvents();			
		DelayedVizProp.applyAll(networkView, VizProps);		
		eventHelper.flushPayloadEvents();			

	}


	private void assignPositionsSouroundingComplexes()
	{		
		Set<DelayedVizProp> VizProps = new HashSet<DelayedVizProp>(); 

		for(CyNode proteinComplexNode : proteinComplexNodeSources.keySet())
		{
			Set<CyNode> surroundingNodes = new HashSet<CyNode>();		

			for(CyNode sourceNode : proteinComplexNodeSources.get(proteinComplexNode) )
			{
				if(proteinNodeTargets.containsKey(sourceNode) && proteinNodeTargets.get(sourceNode).size() <=1)
				{
					surroundingNodes.add(sourceNode);
					placedNodes.add(sourceNode);
				}
			}

			int sourroundingNodeCount = surroundingNodes.size(); 
			int i = 0;	
			//place them in a distance of 2*Node Width starting with the "Top" position and then circling around.		
			View<CyNode> ReacNodeView = networkView.getNodeView(proteinComplexNode);

			Double width = IMAGENODEPROPERTIES.IDARE_NODE_DISPLAY_HEIGHT;
			for(CyNode neighbourNode : surroundingNodes)
			{
				if(neighbourNode == null)
				{
					continue;
				}
				Double x = ReacNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				Double y = ReacNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				Double NewNodeX = GetXPosition(i, sourroundingNodeCount, x, width);
				Double NewNodeY = GetYPosition(i, sourroundingNodeCount, y, width);

				i+=1;
				VizProps.add(new DelayedVizProp(neighbourNode, BasicVisualLexicon.NODE_X_LOCATION, NewNodeX, false));
				VizProps.add(new DelayedVizProp(neighbourNode, BasicVisualLexicon.NODE_Y_LOCATION, NewNodeY, false));
			}

		}
		eventHelper.flushPayloadEvents();			
		DelayedVizProp.applyAll(networkView, VizProps);		
		eventHelper.flushPayloadEvents();			

	}



	private void assignPositionsSurroundingReacs()
	{		
		Set<DelayedVizProp> VizProps = new HashSet<DelayedVizProp>(); 

		for(CyNode reacNode : reacNodeSources.keySet())
		{
			Set<CyNode> surroundingNodes = new HashSet<CyNode>();		
			for(CyNode sourceNode : reacNodeSources.get(reacNode) )
			{
				if(proteinComplexNodeSources.containsKey(sourceNode))
				{
					surroundingNodes.add(sourceNode);
					placedNodes.add(sourceNode);
				}
				if(proteinNodeTargets.containsKey(sourceNode) && proteinNodeTargets.get(sourceNode).size() <=1)
				{
					surroundingNodes.add(sourceNode);
					placedNodes.add(sourceNode);
				}
			}

			int sourroundingNodeCount = surroundingNodes.size(); 
			int i = 0;	
			//place them in a distance of 2*Node Width starting with the "Top" position and then circling around.		
			View<CyNode> ReacNodeView = networkView.getNodeView(reacNode);

			Double width = IMAGENODEPROPERTIES.IDARE_NODE_DISPLAY_HEIGHT;
			for(CyNode neighbourNode : surroundingNodes)
			{
				if(neighbourNode == null)
				{
					continue;
				}
				Double x = ReacNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				Double y = ReacNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				Double NewNodeX = GetXPosition(i, sourroundingNodeCount, x, width);
				Double NewNodeY = GetYPosition(i, sourroundingNodeCount, y, width);
//				System.out.println("Assigning position for node " + network.getRow(neighbourNode).get(sbmlIDcol, String.class) + " surrounding node " + network.getRow(reacNode).get(sbmlIDcol, String.class) + " at " + x +"/"+y + " to " + NewNodeX + "/" + NewNodeY);
				i+=1;
				VizProps.add(new DelayedVizProp(neighbourNode, BasicVisualLexicon.NODE_X_LOCATION, NewNodeX, false));
				VizProps.add(new DelayedVizProp(neighbourNode, BasicVisualLexicon.NODE_Y_LOCATION, NewNodeY, false));
			}

		}
		eventHelper.flushPayloadEvents();			
		DelayedVizProp.applyAll(networkView, VizProps);		

	}


	private void removeModifierGeneEdges()
	{
		Set<CyEdge> edgesToRemove = new HashSet<CyEdge>();
		for(SBase base : geneMap.keySet())
		{
			if(base instanceof Species)
			{
				edgesToRemove.addAll(network.getAdjacentEdgeList(network.getNode(matchingNodes.get(base).get(CyNode.SUID,Long.class)),CyEdge.Type.ANY));			
			}
		}
	}


	private void setupGPRAssociation(GPRAssociation assoc, CyNode reacNode)
	{
		CyTable Tab = network.getDefaultNodeTable(); 
		if(!assoc.getProteins().isEmpty())
		{						
			//This is possibly a Protein Complex
			if(assoc.getProteins().size() > 1 || !assoc.getGenesWithoutProteins().isEmpty())
			{
				//Yes this IS a protein complex
				CyNode proteincomplexNode = network.addNode();
				proteinComplexNodeSources.put(proteincomplexNode, new HashSet<CyNode>());
				reacNodeSources.get(reacNode).add(proteincomplexNode);				
				//Add a directed edge from the complex to the reaction.
				network.addEdge(proteincomplexNode, reacNode, true);
				setupRowAs(Tab,network.getRow(proteincomplexNode),GPRManager.PROTEIN_BASE+ism.getNextProteinID(),IDAREProperties.NodeType.IDARE_PROTEINCOMPLEX);
				for(Protein p : assoc.getProteins())
				{						
					CyNode proteinNode = getProteinNode(p);											
					network.addEdge(proteinNode, proteincomplexNode, true);
					proteinComplexNodeSources.get(proteincomplexNode).add(proteinNode);
					proteinNodeTargets.get(proteinNode).add(proteincomplexNode);
					for(Gene g : p.getCodingGenes())
					{
						CyNode geneNode = getGeneNode(g);
						proteinNodeSources.get(proteinNode).add(geneNode);
						geneNodeTargets.get(geneNode).add(proteinNode);
						//If there is no edge pointing from the gene to the protein, create it.
						//this can lead to a second directed edge, if for whatever reason there was a directed link between the protein and the gene
						if(!network.containsEdge(geneNode, proteinNode))
						{
							network.addEdge(geneNode,proteinNode,true);
						}							
					}
				}

				Set<Gene> genes = assoc.getGenesWithoutProteins();
				if(genes.size() > 0)
				{
					GPRAssociation cassoc = new GPRAssociation();
					cassoc.addAllGenes(genes);
					Protein p = gm.getProtein(cassoc);
					CyNode proteinNode = getProteinNode(p);
					proteinComplexNodeSources.get(proteincomplexNode).add(proteinNode);
					proteinNodeTargets.get(proteinNode).add(proteincomplexNode);
					if(!network.containsEdge(proteinNode, proteincomplexNode))
					{
						network.addEdge(proteinNode, proteincomplexNode, true);
					}
					setupProteinNode(p,proteinNode);
				}
			}
			else
			{
				//this GPR has a single Protein, so we will treat it as a single protein.
				Protein p = null;
				//there is only one item but this is the only way to obtain it...
				for(Protein cp : assoc.getProteins())
				{
					p = cp;
				}
				CyNode proteinNode = getProteinNode(p);
				setupProteinNode(p,proteinNode);
				reacNodeSources.get(reacNode).add(proteinNode);
				proteinNodeTargets.get(proteinNode).add(reacNode);
				if(!network.containsEdge(proteinNode, reacNode))
				{
					network.addEdge(proteinNode, reacNode, true);
				}
			}
		}		
		else
		{
			//This is a simple GPR with Genes in the association
			Protein p = gm.getProtein(assoc);
			CyNode proteinNode = getProteinNode(p);
			setupProteinNode(p,proteinNode);
			reacNodeSources.get(reacNode).add(proteinNode);
			proteinNodeTargets.get(proteinNode).add(reacNode);
			if(!network.containsEdge(proteinNode, reacNode))
			{
				network.addEdge(proteinNode, reacNode, true);
			}
		}

	}

	private void setupProteinNode(Protein p, CyNode proteinNode)
	{
//		PrintFDebugger.Debugging(this, "Setting up protein Node for protein  " + p.getName() + " which has " + p.getCodingGenes().size() + " Genes");

		for(Gene g : p.getCodingGenes())
		{
			CyNode geneNode = getGeneNode(g);
			proteinNodeSources.get(proteinNode).add(geneNode);
			geneNodeTargets.get(geneNode).add(proteinNode);
			if(!network.containsEdge(geneNode, proteinNode))
			{
				network.addEdge(geneNode, proteinNode, true);
			}
		}	
	}


	private void setupRowAs(CyTable Tab, CyRow row, String id, String type)
	{

		if(Tab.getColumn(CyNetwork.NAME) != null)
		{
			row.set(CyNetwork.NAME, id);
		}		
		if(Tab.getColumn(IDAREProperties.IDARE_NODE_NAME) != null)
		{
			row.set(IDAREProperties.IDARE_NODE_NAME, id);
		}

		if(Tab.getColumn(IDAREProperties.IDARE_NODE_TYPE) != null)
		{
			row.set(IDAREProperties.IDARE_NODE_TYPE, type);
		}

		if(Tab.getColumn(IDAREProperties.IDARE_NODE_UID) != null)
		{
			if(!row.isSet(IDAREProperties.IDARE_NODE_UID) || row.getTable().getColumn(IDAREProperties.IDARE_NODE_UID).equals(row.get(IDAREProperties.IDARE_NODE_UID, Long.class)))
			{
				row.set(IDAREProperties.IDARE_NODE_UID, ism.getNextNodeID());
			}
		}

		row.set(sbmlTypecol, type);
		if(Tab.getColumn(IDAREProperties.IDARE_NODE_TYPE) != null)
		{
			row.set(IDAREProperties.IDARE_NODE_TYPE, type);
		}
		row.set(sbmlIDcol, id);
	}

	private CyNode getGeneNode(Gene gene)
	{

		if(!geneNodes.containsKey(gene))
		{			
			PrintFDebugger.Debugging(this, "Adding a new Node for Gene " + gene.toString());
			CyNode geneNode = network.addNode();
			geneNodes.put(gene,geneNode);
			geneNodeTargets.put(geneNode, new HashSet<CyNode>());
			CyRow geneRow = network.getRow(geneNode);
			String geneName = hasGeneAnnotation ? gene.getDataBaseName(geneAnnotationDataBase.getSelectedValue()) : gene.getName();
			setupRowAs(network.getDefaultNodeTable(), geneRow, geneName, IDAREProperties.NodeType.IDARE_GENE);
		}		
		return geneNodes.get(gene);
	}


	private CyNode getProteinNode(Protein prot)
	{
		if(!proteinNodes.containsKey(prot))
		{
			CyNode proteinNode = network.addNode();							
			proteinNodes.put(prot, proteinNode);
			proteinNodeSources.put(proteinNode, new HashSet<CyNode>());
			proteinNodeTargets.put(proteinNode, new HashSet<CyNode>());
			CyRow proteinRow = network.getRow(proteinNode);
			String proteinName = hasProteinAnnotation ? prot.getDataBaseName(proteinAnnotationDataBase.getSelectedValue()) : prot.getName();
			setupRowAs(network.getDefaultNodeTable(), proteinRow, proteinName, IDAREProperties.NodeType.IDARE_PROTEIN);
		}		
		return proteinNodes.get(prot);
	}

	private void getCobraReactions()
	{

		CyColumn assoccol = null;
		CyColumn listcol = null;
		assoccol = network.getDefaultNodeTable().getColumn("GENE_ASSOCIATION");
		if(assoccol == null)
		{
			assoccol = network.getDefaultNodeTable().getColumn("GENE ASSOCIATION");
		}
		listcol = network.getDefaultNodeTable().getColumn("GENE_LIST");
		if(listcol == null)
		{
			listcol = network.getDefaultNodeTable().getColumn("GENE LIST");
		}
		if(assoccol != null || listcol != null)
		{
			for(SBase sbmlitem : matchingNodes.keySet())
			{
				if(sbmlitem instanceof Reaction)
				{
					CobraReactions.add((Reaction) sbmlitem );
				}
			}
		}
	}

	private void createSpeciesProteins()
	{
		//At this point, we KNOW the proteinAnnotationDatabase
		//If we have them we use the selected Identifier to generate the Proteins.
		if(hasProteinAnnotation)
		{
			for(SBase gprod: proteinIDAnnotation.keySet())
			{

				matchingNodes.get(gprod).set(IDAREProperties.SBML_NAME_STRING,proteinIDAnnotation.get(gprod).get(proteinAnnotationDataBase.getSelectedValue()));
				matchingNodes.get(gprod).set(IDAREProperties.IDARE_NODE_NAME,proteinIDAnnotation.get(gprod).get(proteinAnnotationDataBase.getSelectedValue()));
				matchingNodes.get(gprod).set(IDAREProperties.SBML_TYPE_STRING,IDAREProperties.NodeType.IDARE_PROTEIN);
				matchingNodes.get(gprod).set(IDAREProperties.IDARE_NODE_TYPE,IDAREProperties.NodeType.IDARE_PROTEIN);
			}
		}
		//otherwise, we use the available label/name
		else
		{
			for(SBase gprod: proteinIDAnnotation.keySet())
			{

				matchingNodes.get(gprod).set(IDAREProperties.SBML_NAME_STRING,gprod.getName());
				matchingNodes.get(gprod).set(IDAREProperties.IDARE_NODE_NAME,gprod.getName());
				matchingNodes.get(gprod).set(IDAREProperties.SBML_TYPE_STRING,IDAREProperties.NodeType.IDARE_PROTEIN);
				matchingNodes.get(gprod).set(IDAREProperties.IDARE_NODE_TYPE,IDAREProperties.NodeType.IDARE_PROTEIN);
			}

		}
	}


	private void createSpeciesGenes()
	{
		//At this point, we KNOW the geneAnnotationDatabase 
		//If we have them we use the selected Identifier to generate the gene.
		if(hasGeneAnnotation)
		{
			for(SBase gprod: geneIDAnnotation.keySet())
			{
				matchingNodes.get(gprod).set(IDAREProperties.SBML_NAME_STRING,geneIDAnnotation.get(gprod).get(geneAnnotationDataBase.getSelectedValue()));
				matchingNodes.get(gprod).set(IDAREProperties.IDARE_NODE_NAME,geneIDAnnotation.get(gprod).get(geneAnnotationDataBase.getSelectedValue()));
				matchingNodes.get(gprod).set(IDAREProperties.SBML_TYPE_STRING,IDAREProperties.NodeType.IDARE_GENE);
				matchingNodes.get(gprod).set(IDAREProperties.IDARE_NODE_TYPE,IDAREProperties.NodeType.IDARE_GENE);
			}			
		}
		//otherwise, we use the available label/name
		else
		{
			for(SBase gprod: geneIDAnnotation.keySet())
			{
				matchingNodes.get(gprod).set(IDAREProperties.SBML_NAME_STRING,gprod.getName());
				matchingNodes.get(gprod).set(IDAREProperties.IDARE_NODE_NAME,gprod.getName());
				matchingNodes.get(gprod).set(IDAREProperties.SBML_TYPE_STRING,IDAREProperties.NodeType.IDARE_GENE);
				matchingNodes.get(gprod).set(IDAREProperties.IDARE_NODE_TYPE,IDAREProperties.NodeType.IDARE_GENE);
			}

		}
	}

	private void parseFBCLogic()
	{

		for(Reaction reac : doc.getModel().getListOfReactions())
		{
			Association assoc = reac.getAssociation();
			if(assoc == null)
			{
				continue;
			}
			FBCGPRParser parser = new FBCGPRParser(assoc, doc.getModel() ,geneMap,proteinMap);
			if(matchingNodes.containsKey(reac))
			{
				CyNode reacNode = network.getNode(matchingNodes.get(reac).get(CyNode.SUID, Long.class));
				if(!AssociatedGPRs.containsKey(reacNode))
				{
					AssociatedGPRs.put(reacNode, parser.getGPRAssociation());
				}
				else
				{
					AssociatedGPRs.put(reacNode, combineGPRAnnotations(parser.getGPRAssociation(),AssociatedGPRs.get(reacNode)));
				}
			}


		}
	}

	private void parseCOBRALogic()
	{
		CyColumn assoccol = null;
		CyColumn listcol = null;
		assoccol = network.getDefaultNodeTable().getColumn("GENE_ASSOCIATION");
		if(assoccol == null)
		{
			assoccol = network.getDefaultNodeTable().getColumn("GENE ASSOCIATION");
		}
		listcol = network.getDefaultNodeTable().getColumn("GENE_LIST");
		if(listcol == null)
		{
			listcol = network.getDefaultNodeTable().getColumn("GENE LIST");
		}
		if(assoccol != null || listcol != null)
		{
			for(Reaction reac : CobraReactions)
			{
				boolean annotationparsed = false;
				CyRow row = matchingNodes.get(reac);
				//We got the info from FBC which supersedes COBRA note annotations!!
				if(row == null || AssociatedGPRs.containsKey(network.getNode(row.get(CyNode.SUID,Long.class))))
				{
					continue;
				}				
				if(assoccol != null)
				{
					String geneAssoc =  row.get(assoccol.getName(),String.class);
					if(geneAssoc != null)
					{
						geneAssoc = geneAssoc.trim();
						System.out.println("Setting up the GPR for " + row.get(sbmlIDcol, String.class) + " as " + geneAssoc + " of size " + geneAssoc.length());
					}				
					if(geneAssoc != null && !geneAssoc.trim().equals("") && !geneAssoc.equals(assoccol.getDefaultValue()))
					{					
						annotationparsed = true;
						System.out.println("Parsing GPR: " + geneAssoc);						
						GPRTokenizer gpr = new GPRTokenizer(geneAssoc,gm);										
						AssociatedGPRs.put(network.getNode(row.get(CyNode.SUID,Long.class)), gpr.getGPRAssociations());
					}
				}	
				if(!annotationparsed)					
				{
					if(listcol != null)
					{					
						String geneList =  row.get(listcol.getName(),String.class);
						if(geneList != null)
						{
							geneList = geneList.trim();
							System.out.println("Setting up the GPR for " + row.get(sbmlIDcol, String.class) + " as " + geneList+ " of size " + geneList.length());
						}
						
						if(geneList != null && geneList.trim().equals("") && !geneList.equals(listcol.getDefaultValue()))
						{
							System.out.println("Parsing GPR: " + geneList);						
							GPRListTokenizer gpr = new GPRListTokenizer(geneList,gm);					
							AssociatedGPRs.put(network.getNode(row.get(CyNode.SUID,Long.class)), gpr.getGPRAssociations());
						}
					}
				}
			}
		}
	}


	private HashSet<GPRAssociation> combineGPRAnnotations(Set<GPRAssociation> gprset1, Set<GPRAssociation> gprset2)
	{
		HashSet<GPRAssociation> resultingSet = new HashSet<GPRAssociation>();
		resultingSet.addAll(gprset1);
		resultingSet.addAll(gprset2);
		//resultingSet.addAll(gpr1);
		for(GPRAssociation gpr1 : gprset1)
		{
			boolean found = false;
			GPRAssociation foundItem = null;			
			for(GPRAssociation gpr2 : gprset2)
			{
				if(gpr2.equals(gpr1))
				{
					foundItem = gpr2;
					found = true;
				}
			}
			if(found)
			{
				resultingSet.remove(foundItem);
				resultingSet.add(new GPRAssociation(gpr1, foundItem));
			}			
		}
		return resultingSet;
	}


	private void annotateCobraFields()
	{
		HashSet<String> AddedCols = new HashSet<String>();
		CyTable nodeTable = network.getDefaultNodeTable();
		for(SBase sbmlitem : matchingNodes.keySet())
		{
			if(sbmlitem.isSetNotes())
			{
				try
				{
					Properties props = CobraUtil.parseCobraNotes(sbmlitem);
					for(Object propertyname : props.keySet())
					{
						if(!AddedCols.contains(propertyname.toString()))
						{
							try{
								nodeTable.createColumn(propertyname.toString(), String.class, false);
								AddedCols.add(propertyname.toString());
							}
							catch(IllegalArgumentException e)
							{
								//This should only happen if the annotation was done twice (for whatever reason)
							}
						}						
						matchingNodes.get(sbmlitem).set(propertyname.toString(), props.get(propertyname));
						
					}
				}
				catch(Exception e)
				{
					e.printStackTrace(System.out);
				}
			}
		}
		getCobraReactions();
	}

	private boolean isGeneInformation(String NoteType)
	{
		return NoteType.equals("GENE_ASSOCIATION") || NoteType.equals("GENE ASSOCIATION") || NoteType.equals("GENE_LIST") || NoteType.equals("GENE LIST");						
	}

	private void getMatchingNodes()
	{
		PrintFDebugger.Debugging(this, "Setting up SBMLObjectIDs");
		setupSBMLObjectIDs();
		PrintFDebugger.Debugging(this, "Matching Row-IDs to sbml IDs.");
		CyTable tab = network.getDefaultNodeTable();
		for(CyRow row : tab.getAllRows())
		{
			String rowSBMLId = row.get(sbmlIDcol,String.class);
			PrintFDebugger.Debugging(this, "Looking up an sbml item with id " + rowSBMLId);
			if(SBMLObjectIDs.containsKey(rowSBMLId))
			{
				SBase sbmlitem = SBMLObjectIDs.get(rowSBMLId); 
				matchingNodes.put(sbmlitem, row);
				if(proteinMap.containsKey(sbmlitem))
				{
					if(proteinNodes.containsKey(proteinMap.get(sbmlitem)))
					{
						if(!nodesToMerge.containsKey(proteinNodes.get(proteinMap.get(sbmlitem))))
						{
							nodesToMerge.put(proteinNodes.get(proteinMap.get(sbmlitem)), new HashSet<CyRow>());
						}
//						System.out.println("A: " + proteinMap.get(sbmlitem) );
//						System.out.println("B: " + proteinNodes.get(proteinMap.get(sbmlitem)));
//						System.out.println("C: " + nodesToMerge.get(proteinNodes.get(proteinMap.get(sbmlitem))));
						nodesToMerge.get(proteinNodes.get(proteinMap.get(sbmlitem))).add(row);
					}
					else
					{
						proteinNodes.put(proteinMap.get(sbmlitem), network.getNode(row.get(CyNode.SUID, Long.class)));
						proteinNodeSources.put(proteinNodes.get(proteinMap.get(sbmlitem)), new HashSet<CyNode>());
						proteinNodeTargets.put(proteinNodes.get(proteinMap.get(sbmlitem)), new HashSet<CyNode>());
					}
				}
				if(geneMap.containsKey(sbmlitem))
				{
					if(geneNodes.containsKey(geneMap.get(sbmlitem)))
					{
						if(!nodesToMerge.containsKey(geneNodes.get(geneMap.get(sbmlitem))))
						{
							nodesToMerge.put(geneNodes.get(geneMap.get(sbmlitem)), new HashSet<CyRow>());
						}
						nodesToMerge.get(geneNodes.get(geneMap.get(sbmlitem))).add(row);
					}
					else
					{
						geneNodes.put(geneMap.get(sbmlitem), network.getNode(row.get(CyNode.SUID, Long.class)));
						geneNodeTargets.put(geneNodes.get(geneMap.get(sbmlitem)),new HashSet<CyNode>());
					}
				}
			}
		}
		PrintFDebugger.Debugging(this, "Finished Matching Row-IDs to sbml IDs.");

	}

	public void setupSBMLObjectIDs()
	{
		for(Species spec : doc.getModel().getListOfSpecies())
		{
			SBMLObjectIDs.put(spec.getId(), spec);
//			PrintFDebugger.Debugging(this, "Found a species with id " + spec.getId());

		}
		for(Reaction reac : doc.getModel().getListOfReactions())
		{
			SBMLObjectIDs.put(reac.getId(), reac);
//			PrintFDebugger.Debugging(this, "Found a reaction with id " + reac.getId());
		}
		PrintFDebugger.Debugging(this, "Finished reading Reactions ");
		if(doc.getModel().isFBCPackageEnabled())
		{
//			PrintFDebugger.Debugging(this, "Checking the model for FBC");
			for(GeneProduct gprod : doc.getModel().getListOfGeneProducts())
			{
				SBMLObjectIDs.put(gprod.getId(), gprod);
			}
		}
//		PrintFDebugger.Debugging(this, "Finished reading SBMLIDs");
	}

	public void addCobraInformationAnnotation()
	{

	}


	/**
	 * Before anything is done we have to extract the annotation information.
	 */
	private void setupAnnotationDBs()
	{
		proteinAnnotationDatabases = new HashSet<String>();
		geneAnnotationDatabases = new HashSet<String>();
		boolean nodesAnnotedAsProtein = false;
		boolean nodesAnnotedAsGene= false;
		boolean fbcnodesAnnotedAsProtein = false;
		boolean fbcnodesAnnotedAsGene= false;
		Model sbmlModel = doc.getModel();
		for(Species cspec : sbmlModel.getListOfSpecies())
		{

			SBMLObjectIDs.put(cspec.getId(),cspec);
			if(!cspec.isSetSBOTerm())
			{
				//if the SBO Term is not set, we can't determine, what this is..				
				continue;
			}
			//This is a Protein.
			if(cspec.getSBOTerm() == 14 || cspec.getSBOTerm() == 252)
			{
//				PrintFDebugger.Debugging(this, "Found a Protein " + cspec.getName() );
				nodesAnnotedAsProtein = true;
				Set<Gene> GeneList = new HashSet<Gene>();
				if(cspec.isSetAnnotation())
				{
					Annotation cspecAnnotation = cspec.getAnnotation();
					List<CVTerm> CVTerms = cspecAnnotation.getListOfCVTerms();
					//Checking whether it is annotated as polypeptide chain or as enzyme
					boolean proteinCreated = false;
					// There are two allowable options, how isEncodedBy can be used
					// either for each database we have all encoding genes in one CVTerm
					// This assumes, that identifiers are present in all databases annotated for all genes.
					//the other option is, all terms for one database in one CVTerm, which assumes, that all databases do have 
					//that CVTerm.
					boolean genesPresent = false;
					for(CVTerm cv : CVTerms)
					{						
						//Ok, We have an enzyme (otherwise a cspec should not be 'Encoded' by anything)							
						if(cv.getBiologicalQualifierType() == CVTerm.Qualifier.BQB_IS_ENCODED_BY)
						{

							//isProtein = true;
							for(String ressource : cv.getResources())
							{
								//We assume, that we have a URI of the form: http://authority/database/Entry
								String dataCollection = RegistryUtilities.getDataCollectionPartFromURI(ressource);
								String identifier = RegistryUtilities.getIdentifierFromURI(ressource);
								if(!proteinToGeneAnnotation.containsKey(cspec))
								{
									proteinToGeneAnnotation.put(cspec, new HashMap<String, Vector<String>>());
								}
								if(!proteinToGeneAnnotation.get(cspec).containsKey(dataCollection))
								{
									proteinToGeneAnnotation.get(cspec).put(dataCollection, new Vector<String>());
								}
//								PrintFDebugger.Debugging(this, "Adding Gene " + identifier + " as coding for protein " + cspec.getName());
								proteinToGeneAnnotation.get(cspec).get(dataCollection).add(identifier);								
								geneAnnotationDatabases.add(dataCollection);
								genesPresent = true;
							}
						}

						if(cv.getBiologicalQualifierType() == CVTerm.Qualifier.BQB_IS)
						{
							//isProtein = true;
							HashMap<String,String> dbIDs = new HashMap<String, String>(); 
							for(String ressource : cv.getResources())
							{
								//We assume, that we have a URI of the form: http://authority/database/Entry
								String dataCollection = RegistryUtilities.getDataCollectionPartFromURI(ressource);
								String identifier = RegistryUtilities.getIdentifierFromURI(ressource);
								if(!proteinIDAnnotation.containsKey(cspec))
								{
									proteinIDAnnotation.put(cspec, new HashMap<String, String>());
								}
								proteinIDAnnotation.get(cspec).put(dataCollection, identifier);
								proteinAnnotationDatabases.add(dataCollection);
//								PrintFDebugger.Debugging(this, "Adding Protein " + identifier + " as protein ID for protein " + cspec.getName());
								dbIDs.put(dataCollection, identifier);
							}
							if(!proteinMap.containsKey(cspec))
							{
								proteinMap.put(cspec,gm.getProtein(dbIDs));
							}
							else
							{
								gm.updateProteindbs(proteinMap.get(cspec),dbIDs);
							}

							proteinCreated = true;
						}
					}
					if(!proteinCreated)
					{
						HashMap<String,String> proteinName = new HashMap<String, String>();
						proteinName.put(null, cspec.getName());						
						proteinMap.put(cspec,gm.getProtein(proteinName));
					}
					if(genesPresent)
					{
						try{
							//First, determine the number of genes
							int genecount = 0; 
							for(String DataCollection : proteinToGeneAnnotation.get(cspec).keySet())
							{
								//We will require that 
								genecount = Math.max(genecount,proteinToGeneAnnotation.get(cspec).get(DataCollection).size());
							}
							for(int i = 0; i < genecount; i++)
							{
								HashMap<String,String> databaseIDs = new HashMap<String, String>();
								for(String datacollection : proteinToGeneAnnotation.get(cspec).keySet())
								{
									databaseIDs.put(datacollection, proteinToGeneAnnotation.get(cspec).get(datacollection).get(i));
								}
								GeneList.add(gm.getGene(databaseIDs));

							}
						}
						catch(ArrayIndexOutOfBoundsException e)
						{
							//If we get an array index out of bounds exception, the annotation has different length for different databases (which doesn't make any sense)
							//And we can't determine how the genes looks like. Thus we will simply create an unassociated protein. 
							GeneList = new HashSet<Gene>();								
						}
					}				

					proteinMap.get(cspec).setCodingGenes(GeneList,gm);
				}
				else
				{
					proteinIDAnnotation.put(cspec,new HashMap<String, String>());
					proteinToGeneAnnotation.put(cspec,new HashMap<String, Vector<String>>());
					proteinMap.put(cspec, gm.getUnassociatedProtein(cspec.getName()));
				}
			}
			//Gene or DNA Segment or gene coding region is all considered as gene.
			if(cspec.getSBOTerm() == 243 || cspec.getSBOTerm() == 634 || cspec.getSBOTerm() == 335 )
			{	
				nodesAnnotedAsGene = true;
				if(cspec.isSetAnnotation())
				{
					Annotation cspecAnnotation = cspec.getAnnotation();
					List<CVTerm> CVTerms = cspecAnnotation.getListOfCVTerms();
					boolean genecreated = false;
					for(CVTerm cv : CVTerms)
					{			

						if(cv.getBiologicalQualifierType() == CVTerm.Qualifier.BQB_IS)
						{

							HashMap<String,String> dbIDs = new HashMap<String, String>();
							for(String ressource : cv.getResources())
							{
								//We assume, that we have a URI of the form: http://authority/database/Entry
								String dataCollection = RegistryUtilities.getDataCollectionPartFromURI(ressource);
								String identifier = RegistryUtilities.getIdentifierFromURI(ressource);
								if(!geneIDAnnotation.containsKey(cspec))
								{
									geneIDAnnotation.put(cspec, new HashMap<String, String>());
								}
								geneIDAnnotation.get(cspec).put(dataCollection, identifier);
								geneAnnotationDatabases.add(dataCollection);
								dbIDs.put(dataCollection, identifier);
							}
							if(!geneMap.containsKey(cspec))
							{
								geneMap.put(cspec,gm.getGene(dbIDs));
							}
							else
							{
								gm.updateGenedbs(geneMap.get(cspec),dbIDs);
							}
							genecreated = true;
						}
					}
					if(!genecreated)
					{
						HashMap<String,String> geneName = new HashMap<String, String>();
						geneName.put(null, cspec.getId());
						geneMap.put(cspec,gm.getGene(geneName));
					}
				}
				else
				{
					geneIDAnnotation.put(cspec, new HashMap<String, String>());
					geneMap.put(cspec, gm.getUnassociatedGene(cspec.getName()));
				}

			}

		}


		if(doc.getModel().isFBCPackageEnabled())
		{
				
			for(GeneProduct cspec : doc.getModel().getListOfGeneProducts())
			{
				SBMLObjectIDs.put(cspec.getId(),cspec);
				if(!cspec.isSetSBOTerm())
				{
					//if the SBO Term is not set, we can't determine, what this is..				
					continue;
				}
				if(cspec.getSBOTerm() == 14 || cspec.getSBOTerm() == 252)
				{
					fbcnodesAnnotedAsProtein = true;

					Set<Gene> GeneList = new HashSet<Gene>();

					if(cspec.isSetAnnotation())
					{
						Annotation cspecAnnotation = cspec.getAnnotation();
						List<CVTerm> CVTerms = cspecAnnotation.getListOfCVTerms();
						//Checking whether it is annotated as polypeptide chain or as enzyme
						boolean proteinCreated = false;
						boolean genesPresent = false;
						for(CVTerm cv : CVTerms)
						{						


							//Ok, We have an enzyme (otherwise a cspec should not be 'Encoded' by anything)							
							if(cv.getBiologicalQualifierType() == CVTerm.Qualifier.BQB_IS_ENCODED_BY)
							{
								//isProtein = true;
								for(String ressource : cv.getResources())
								{
									//We assume, that we have a URI of the form: http://authority/database/Entry
									String dataCollection = RegistryUtilities.getDataCollectionPartFromURI(ressource);
									String identifier = RegistryUtilities.getIdentifierFromURI(ressource);
									if(!proteinToGeneAnnotation.containsKey(cspec))
									{
										proteinToGeneAnnotation.put(cspec, new HashMap<String, Vector<String>>());
									}
									if(!proteinToGeneAnnotation.get(cspec).containsKey(dataCollection))
									{
										proteinToGeneAnnotation.get(cspec).put(dataCollection, new Vector<String>());
									}
									proteinToGeneAnnotation.get(cspec).get(dataCollection).add(identifier);								
									geneAnnotationDatabases.add(dataCollection);
									genesPresent = true;
								}

							}

							if(cv.getBiologicalQualifierType() == CVTerm.Qualifier.BQB_IS)
							{
								//isProtein = true;
								HashMap<String,String> dbIDs = new HashMap<String, String>(); 
								for(String ressource : cv.getResources())
								{
									//We assume, that we have a URI of the form: http://authority/database/Entry
									String dataCollection = RegistryUtilities.getDataCollectionPartFromURI(ressource);
									String identifier = RegistryUtilities.getIdentifierFromURI(ressource);
									if(!proteinIDAnnotation.containsKey(cspec))
									{
										proteinIDAnnotation.put(cspec, new HashMap<String, String>());
									}
									proteinIDAnnotation.get(cspec).put(dataCollection, identifier);
									proteinAnnotationDatabases.add(dataCollection);
									dbIDs.put(dataCollection, identifier);
								}
								if(!proteinMap.containsKey(cspec))
								{
									proteinMap.put(cspec,gm.getProtein(dbIDs));
								}
								else
								{
									gm.updateProteindbs(proteinMap.get(cspec),dbIDs);
								}

								proteinCreated = true;
							}
						}
						if(!proteinCreated)
						{
							HashMap<String,String> proteinName = new HashMap<String, String>();
							proteinName.put(null, cspec.getLabel());
							proteinMap.put(cspec,gm.getProtein(proteinName));
						}

						if(genesPresent)
						{
							try{
								//First, determine the number of genes
								int genecount = 0; 
								for(String DataCollection : proteinToGeneAnnotation.get(cspec).keySet())
								{
									//We will require that 
									genecount = Math.max(genecount,proteinToGeneAnnotation.get(cspec).get(DataCollection).size());
								}
								for(int i = 0; i < genecount; i++)
								{
									HashMap<String,String> databaseIDs = new HashMap<String, String>();
									for(String datacollection : proteinToGeneAnnotation.get(cspec).keySet())
									{
										databaseIDs.put(datacollection, proteinToGeneAnnotation.get(cspec).get(datacollection).get(i));
									}
									GeneList.add(gm.getGene(databaseIDs));

								}
							}
							catch(ArrayIndexOutOfBoundsException e)
							{
								//If we get an array index out of bounds exception, the annotation has different length for different databases (which doesn't make any sense)
								//And we can't determine how the genes looks like. Thus we will simply create an unassociated protein. 
								GeneList = new HashSet<Gene>();								
							}

						}


						proteinMap.get(cspec).setCodingGenes(GeneList,gm);						
					}
					else
					{
						proteinIDAnnotation.put(cspec,new HashMap<String, String>());
						proteinToGeneAnnotation.put(cspec,new HashMap<String, Vector<String>>());
						proteinMap.put(cspec, gm.getUnassociatedProtein(cspec.getLabel()));
					}
				}
				//Gene or DNA Segment or gene coding region is all considered as gene.
				if(cspec.getSBOTerm() == 243 || cspec.getSBOTerm() == 634 || cspec.getSBOTerm() == 335 )
				{	
					fbcnodesAnnotedAsGene = true;
					if(cspec.isSetAnnotation())
					{
						Annotation cspecAnnotation = cspec.getAnnotation();
						List<CVTerm> CVTerms = cspecAnnotation.getListOfCVTerms();
						boolean genecreated = false;
						for(CVTerm cv : CVTerms)
						{			

							if(cv.getBiologicalQualifierType() == CVTerm.Qualifier.BQB_IS)
							{

								HashMap<String,String> dbIDs = new HashMap<String, String>();
								for(String ressource : cv.getResources())
								{
									//We assume, that we have a URI of the form: http://authority/database/Entry
									String dataCollection = RegistryUtilities.getDataCollectionPartFromURI(ressource);
									String identifier = RegistryUtilities.getIdentifierFromURI(ressource);
									if(!geneIDAnnotation.containsKey(cspec))
									{
										geneIDAnnotation.put(cspec, new HashMap<String, String>());
									}
									geneIDAnnotation.get(cspec).put(dataCollection, identifier);
									geneAnnotationDatabases.add(dataCollection);
									dbIDs.put(dataCollection,identifier);
								}
								if(!geneMap.containsKey(cspec))
								{
									geneMap.put(cspec,gm.getGene(dbIDs));
								}
								else
								{
									gm.updateGenedbs(geneMap.get(cspec),dbIDs);
								}
								genecreated = true;
							}
						}
						if(!genecreated)
						{
							HashMap<String,String> geneName = new HashMap<String, String>();
							geneName.put(null, cspec.getId());
							geneMap.put(cspec,gm.getGene(geneName));
						}
					}
					else
					{
						geneIDAnnotation.put(cspec, new HashMap<String, String>());
						geneMap.put(cspec, gm.getUnassociatedGene(cspec.getLabel()));
					}

				}

			}

		}
		if((fbcnodesAnnotedAsGene && fbcnodesAnnotedAsProtein))
		{
			skipNodes = true;
			ErrorMessage += "FBC GeneProducts annotated as both Genes and Proteins. Cannot use this annotation\n";
		}
		if(nodesAnnotedAsGene && nodesAnnotedAsProtein)
		{
			skipNodes = true;
			ErrorMessage+= "Species annotated as both Genes and Proteins, cannot use this annotation";
		}


	}

	@Override
	public void setUIHelper(TunableUIHelper arg0) {
		// TODO Auto-generated method stub

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
		double xchange = width*Math.cos(currentangle);
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
		double singleangle = 2*Math.PI / count;
		double currentangle = number * singleangle;		
		double ychange = width*Math.sin(currentangle);
		return (y+ychange);
	}

}
