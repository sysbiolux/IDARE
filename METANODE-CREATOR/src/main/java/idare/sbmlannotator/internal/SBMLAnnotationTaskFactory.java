package idare.sbmlannotator.internal;

import idare.Properties.IDAREProperties;
import idare.imagenode.internal.Services.JSBML.Annotation;
import idare.imagenode.internal.Services.JSBML.CVTerm;
import idare.imagenode.internal.Services.JSBML.Model;
import idare.imagenode.internal.Services.JSBML.SBMLDocument;
import idare.imagenode.internal.Services.JSBML.SBMLManagerHolder;
import idare.imagenode.internal.Services.JSBML.Species;
import idare.imagenode.internal.Services.JSBML.CVTerm.Qualifier;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
//import org.sbml.jsbml.Annotation;
//import org.sbml.jsbml.CVTerm;
//import org.sbml.jsbml.Model;
//import org.sbml.jsbml.SBMLDocument;
//import org.sbml.jsbml.SBMLReader;
//import org.sbml.jsbml.Species;
//import org.sbml.jsbml.CVTerm.Qualifier;

public class SBMLAnnotationTaskFactory extends AbstractTaskFactory implements
		NetworkViewTaskFactory {

	protected CyApplicationManager cyAppMgr;
	protected CyEventHelper eventHelper;
	protected final FileUtil fileutil;	
	protected CySwingApplication cySwingApp;
	private SBMLManagerHolder SBMLListener;
//	private String GeneAnnotDB;
//	private String ProtAnnotDB;
//	private Map<String,Set<CVTerm>> ProtAnnot;
	
	public SBMLAnnotationTaskFactory(final CyApplicationManager applicationManager,
				CyEventHelper eventHelper, FileUtil fileutil, CySwingApplication cySwingApp,SBMLManagerHolder SBMLListener) {
		this.cyAppMgr = applicationManager;
		this.fileutil = fileutil;
		this.eventHelper = eventHelper;
		this.cySwingApp = cySwingApp;
		this.SBMLListener = SBMLListener;
	}
		
	@Override
	public TaskIterator createTaskIterator() {
		return createTask();
	}

	@Override
	public boolean isReady()
	{
		return cyAppMgr.getCurrentNetwork() != null;
	}
	@Override
	public TaskIterator createTaskIterator(CyNetworkView arg0) {
		return createTask();
	}

	@Override
	public boolean isReady(CyNetworkView arg0) {		
		return true;
	}

	private TaskIterator createTask()
	{
	String[] extensions = {"sbml","xml"};		
	HashSet<String> geneAnnotationURIs = new HashSet<String>();
	HashSet<String> proteinAnnotationURIs = new HashSet<String>();
	Map<String,Set<CVTerm>> ProtAnnot  = new HashMap<String, Set<CVTerm>>();
	String ProtAnnotDB = null;
	String GeneAnnotDB = null;
	Model model = null;		
	String SBMLTypeCol = null;
	String SBMLIDCol = null;
	String SBMLCompCol = null;
	String SBMLInteractionCol = null;
	File SBMLFile = null;
	SBMLDocument doc = null;
	CyNetwork network = cyAppMgr.getCurrentNetwork();
	if(network == null)
	{
		JOptionPane.showMessageDialog(null, "Please select a network to apply the SBML annotation to");
		return null;
	}
	
	doc = SBMLListener.readSBML(network);						

	if(doc != null) 
	{
		//Select the current Network
		//If we want to read Gene nodes check whether there is a specific annotation.
		Vector<String> diffvals = new Vector<String>(); 
		for(CyColumn col : network.getDefaultNodeTable().getColumns())
		{
			diffvals.add(col.getName());
		}
		diffvals.remove(null);
		diffvals.remove("");
		if(diffvals.contains("sbml type"))
		{
			// we are in file parsed with the default sbml parser
			SBMLTypeCol = "sbml type";
			SBMLIDCol = "sbml id";
			SBMLCompCol = "sbml compartment";
			SBMLInteractionCol = "interaction type";
		}
		else if(diffvals.contains("sbml-type"))
		{
			//This is a cysbml parsed file.
			SBMLTypeCol = "sbml-type";
			SBMLIDCol = "id";
			SBMLCompCol = "compartment";
			SBMLInteractionCol = "sbml-interaction";
		}
		if(SBMLTypeCol == null)
		{
			//Otherwise: ask
			Object sbmlTypeCol = JOptionPane.showInputDialog(cySwingApp.getJFrame(), "Select a column for the SBML Type",
					"SBML Type Column Selection", JOptionPane.QUESTION_MESSAGE, null, diffvals.toArray(), diffvals.get(0));
			if(sbmlTypeCol != null)
			{
				SBMLTypeCol = sbmlTypeCol.toString();
			}
		}
		if(SBMLIDCol == null)
		{
			Object sbmlIDCol = JOptionPane.showInputDialog(cySwingApp.getJFrame(), "Select a column for the SBML ID",
					"SBML ID Column Selection", JOptionPane.QUESTION_MESSAGE, null, diffvals.toArray(), diffvals.get(0));
			if(sbmlIDCol != null)
			{
				SBMLIDCol = sbmlIDCol.toString();
			}
		}
		if(SBMLCompCol == null)
		{
			Object sbmlCompCol = JOptionPane.showInputDialog(cySwingApp.getJFrame(), "Select a column for the SBML Compartment",
					"SBML Compartment Column Selection", JOptionPane.QUESTION_MESSAGE, null, diffvals.toArray(), diffvals.get(0));
			if(sbmlCompCol != null)
			{
				SBMLTypeCol = sbmlCompCol.toString();
			}
		}
		if(SBMLInteractionCol == null)
		{
			Vector<String> edgecols = new Vector<String>(); 
			for(CyColumn col : network.getDefaultEdgeTable().getColumns())
			{
				edgecols.add(col.getName());
			}
			edgecols.remove(null);
			edgecols.remove("");
			Object sbmlIntCol = JOptionPane.showInputDialog(cySwingApp.getJFrame(), "Select a column for the interaction Type",
					"SBML Interaction Column Selection", JOptionPane.QUESTION_MESSAGE, null, edgecols.toArray(), edgecols.get(0));
			if(sbmlIntCol != null)
			{
				SBMLInteractionCol = sbmlIntCol.toString();
			}
		}
		if(SBMLIDCol == null || SBMLTypeCol == null || SBMLCompCol == null)
		{
			return null;
		}
		
		int confirmation = JOptionPane.showConfirmDialog(cySwingApp.getJFrame(), "Would you like to add Gene Nodes?");
		if(confirmation == JOptionPane.YES_OPTION)			
		{
			try
			{
				
				model = doc.getModel();				
				for (Species species : model.getListOfSpecies())
				{
					if(species.isSetAnnotation())
					{
						Annotation speciesAnnotation = species.getAnnotation();
						List<CVTerm> CVTerms = speciesAnnotation.getListOfCVTerms();
						//Checking whether it is annotated as polypeptide chain or as enzyme
						if(species.getSBOTerm() == 14 || species.getSBOTerm() == 252)
						{
							if(!ProtAnnot.containsKey(species.getId()))
							{
								ProtAnnot.put(species.getId(), new HashSet<CVTerm>());
							}
						}
						for(CVTerm cv : CVTerms)
						{						
							
							//Ok, We have an enzyme (otherwise a species should not be 'Encoded' by anything)							
							if(cv.getBiologicalQualifierType() == Qualifier.BQB_IS_ENCODED_BY)
							{
								//isProtein = true;
								if(!ProtAnnot.containsKey(species.getId()))
								{
									ProtAnnot.put(species.getId(), new HashSet<CVTerm>());
								}
								ProtAnnot.get(species.getId()).add(cv);				
								for(String ressource : cv.getResources())
								{
									//We assume, that we have a URI of the form: http://authority/database/Entry
									readURIString(ressource, geneAnnotationURIs, true);

								}
							}
						}
						if(ProtAnnot.containsKey(species.getId()))
						{
							//if this is a protein
							for(CVTerm cv : CVTerms)
							{
								// check which databases are used for the reference.
								if(cv.getBiologicalQualifierType() == Qualifier.BQB_IS)
								{
									ProtAnnot.get(species.getId()).add(cv);
									for(String ressource : cv.getResources())
									{
										//We assume, that we have a URI of the form: http://authority/database/Entry
										//or urn:authority:database:entry
										readURIString(ressource, proteinAnnotationURIs, true);


									}
								}
							}
						}

					}
				}
				if(geneAnnotationURIs.size() > 0)
				{
					//this only happens if we actually obtain some protein species, so we need to setup the 
					//IDARE Node type row to assign the respective proteins.				
					//try
					//{
						Object GeneAnnotSelect = JOptionPane.showInputDialog(cySwingApp.getJFrame(), "Select a database for Gene Annotations",
								"Database selection", JOptionPane.QUESTION_MESSAGE, null, geneAnnotationURIs.toArray(), geneAnnotationURIs.toArray()[0]);
						if(GeneAnnotSelect != null)
						{
							GeneAnnotDB = GeneAnnotSelect.toString();
						}
					//}
					//catch(NullPointerException ex)
					//{
					//	ex.printStackTrace(System.out);
					//	return null;
					//}

					System.out.println("The selected Database is : " + GeneAnnotDB);
					if(GeneAnnotDB == null)
					{
						return null;
					}
				}

				if(proteinAnnotationURIs.size() > 0)
				{
					if(network.getDefaultNodeTable().getColumn(IDAREProperties.SBML_NAME_STRING) != null)
					{
						geneAnnotationURIs.add(IDAREProperties.SBML_NAME_STRING);
					}
					//this only happens if we actually obtain some protein species, so we need to setup the 
					//IDARE Node type row to assign the respective proteins.				
					//try
					//{
						Object ProtAnnotSelect = JOptionPane.showInputDialog(cySwingApp.getJFrame(), "Select a database for Protein Annotations",
								"Database selection", JOptionPane.QUESTION_MESSAGE, null, proteinAnnotationURIs.toArray(), proteinAnnotationURIs.toArray()[0]);
						if(ProtAnnotSelect != null)
						{
							ProtAnnotDB = ProtAnnotSelect.toString();
						}
					//}
//					catch(NullPointerException ex)
//					{
//						ex.printStackTrace(System.out);
//						return null;
//					}

					System.out.println("The selected Database is : " + ProtAnnotDB);
					if(ProtAnnotDB == null)
					{
						return null;
					}
				}
			}		
			catch(Exception ex)
			{
				JOptionPane.showMessageDialog(cySwingApp.getJFrame(), "Error while determining Gene and Protein Information. \nSkipping Genes and Proteins", "Could not parse Genes/Proteins", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		TaskIterator SATF;
		if(confirmation == JOptionPane.YES_OPTION)
		{	
			SATF = new TaskIterator(new SBMLAnnotaterTask(cyAppMgr, doc ,true,eventHelper, cySwingApp, GeneAnnotDB, ProtAnnotDB,ProtAnnot, model,SBMLTypeCol,SBMLIDCol,SBMLCompCol,SBMLInteractionCol));
		}
		else
		{
			SATF = new TaskIterator(new SBMLAnnotaterTask(cyAppMgr, doc ,false,eventHelper,cySwingApp, null, ProtAnnotDB,ProtAnnot, model,SBMLTypeCol,SBMLIDCol,SBMLCompCol,SBMLInteractionCol));
		}
		return SATF;
	}
	return new TaskIterator(new Task() {
		
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			// TODO Auto-generated method stub
			taskMonitor.showMessage(Level.WARN, "No SBML File Selected");
			taskMonitor.setTitle("SBML Annotation Unsuccessful");
			taskMonitor.setStatusMessage("No SBML File selected");
		}
		
		@Override
		public void cancel() {
			// TODO Auto-generated method stub
			
		}
	});
	}
	
	
	protected void readURIString(String URI, Set<String> URICollection, boolean database)
	{
		int position = 0;
		
		if(database)
		{
			position = 3;
		}
		else
		{
			position = 4;
		}
		
		if(URI.matches("http://.*/.*/.*"))
		{
			try
			{
				String entryname = URI.split("/")[position];
				URICollection.add(entryname);
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
				String entryname = URI.split(":")[position - 1];
				URICollection.add(entryname);			
				}
			
			catch(IndexOutOfBoundsException ex)
			{
				//Do Nothing
			}
		}
	}
}
