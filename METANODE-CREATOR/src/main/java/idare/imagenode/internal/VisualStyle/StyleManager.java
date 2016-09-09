package idare.imagenode.internal.VisualStyle;

import idare.Properties.IDAREProperties;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.Utilities.IOUtils;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.ImageManagement.GraphicsChangedEvent;
import idare.imagenode.internal.ImageManagement.GraphicsChangedListener;
import idare.imagenode.internal.ImageManagement.ImageStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
/**
 * The StyleManager handles the availability of Images in a given Style.
 * It will save properties of the original style and restore them, if the nodes are removed.
 * During save/load only earlier mappings used by the style will not be recovered and only the default properties will be used.
 * @author Thomas Pfau
 *
 */
public class StyleManager implements SessionAboutToBeSavedListener,
SessionLoadedListener, GraphicsChangedListener {

	private Vector<VisualProperty> storedProperties = new Vector<VisualProperty>();
	private HashSet<String> VisualStyleTitles = new HashSet<String>();
	private HashMap<VisualStyle,HashMap<VisualProperty,VisualMappingFunction>> origstylemappings = new HashMap<VisualStyle, HashMap<VisualProperty,VisualMappingFunction>>();
	private HashMap<VisualStyle,HashMap<VisualProperty,Object>> origstyleproperties = new HashMap<VisualStyle, HashMap<VisualProperty,Object>>();
	private HashMap<String,Boolean> lockedenabled = new HashMap<String, Boolean>();  
	private VisualMappingManager vmmServiceRef;
	private CyNetworkViewManager cyNetViewMgr;
	private CyEventHelper eventHelper;	
	private CyApplicationManager cyAppMgr;
	private NodeManager nm;
	private ImageStorage imf;

	/**
	 * Default Constructor. 
	 * @param imf
	 * @param vmmServiceRef
	 * @param cyNetViewMgr
	 * @param eventHelper
	 * @param nm
	 * @param cyAppMgr
	 */
	public StyleManager(ImageStorage imf, VisualMappingManager vmmServiceRef, CyNetworkViewManager cyNetViewMgr,
			CyEventHelper eventHelper, NodeManager nm, CyApplicationManager cyAppMgr)
	{		
		this.imf = imf;
		this.vmmServiceRef = vmmServiceRef;
		this.cyNetViewMgr = cyNetViewMgr;
		this.eventHelper = eventHelper;
		this.nm = nm;
		this.cyAppMgr = cyAppMgr;
		storedProperties.addElement(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY);
		storedProperties.addElement(BasicVisualLexicon.NODE_WIDTH);
		storedProperties.addElement(BasicVisualLexicon.NODE_HEIGHT);
		storedProperties.addElement(imf.getVisualProperty());
	}
	/**
	 * Update all Views that could need an update due to a change in nodes.
	 */
	private void updateRelevantViews()
	{
		//Generate all Node Images, if they don't exist.
		imf.getAll();
		
		HashMap<CyNetworkView,VisualStyle> views = new HashMap<CyNetworkView, VisualStyle>();
		for(CyNetworkView networkView : cyNetViewMgr.getNetworkViewSet())
		{			
			VisualStyle cvs = vmmServiceRef.getVisualStyle(networkView);			
			if(VisualStyleTitles.contains(cvs.getTitle()))
			{
				views.put(networkView,cvs);
			}
		}
		eventHelper.flushPayloadEvents();
		for(CyNetworkView networkView : views.keySet())
		{
			views.get(networkView).apply(networkView);
			vmmServiceRef.setVisualStyle(views.get(networkView), networkView);
			networkView.updateView();
		}		
	}	
	/**
	 * Add nodes to the currently used Style, and update the TaskMonitor to indicate the current activity.
	 * @param monitor
	 */
	public synchronized void addNodes(TaskMonitor monitor)
	{
		CyNetworkView CurrentView = cyAppMgr.getCurrentNetworkView();
		VisualStyle currentstyle = vmmServiceRef.getVisualStyle(CurrentView);
		String StyleTitle = currentstyle.getTitle();
		monitor.setStatusMessage("Adding Nodes to Style " + StyleTitle);
		if(!VisualStyleTitles.contains(StyleTitle) && !StyleTitle.equalsIgnoreCase(IDAREVisualStyle.IDARE_STYLE_TITLE))
		{
			monitor.setStatusMessage("Saving old Properties for " + StyleTitle);
			saveProperties(currentstyle, false);
			setMappings(currentstyle);
			VisualStyleTitles.add(StyleTitle);			
		}	
		monitor.setProgress(0.1);
		monitor.setStatusMessage("Updating Views");
		updateRelevantViews();
		monitor.setProgress(1.0);

	}


	/**
	 * Set the Mappings for IDARE to the current style.
	 */
	private void setMappings(VisualStyle currentstyle)
	{
		IDAREDependentMapper<Integer> LabelTransparency = new IDAREDependentMapper<Integer>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_LABEL_TRANSPARENCY,nm,0);		
		IDAREDependentMapper<Double> imagenodeHeight = new IDAREDependentMapper<Double>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_HEIGHT,nm,IMAGENODEPROPERTIES.IDARE_NODE_DISPLAY_HEIGHT-2);
		IDAREDependentMapper<Double> imagenodeWidth = new IDAREDependentMapper<Double>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_WIDTH,nm,IMAGENODEPROPERTIES.IDARE_NODE_DISPLAY_WIDTH-2);
		currentstyle.removeVisualMappingFunction(LabelTransparency.getVisualProperty());
		currentstyle.addVisualMappingFunction(LabelTransparency);
		currentstyle.removeVisualMappingFunction(imagenodeHeight.getVisualProperty());
		currentstyle.addVisualMappingFunction(imagenodeHeight);
		currentstyle.removeVisualMappingFunction(imagenodeWidth.getVisualProperty());
		currentstyle.addVisualMappingFunction(imagenodeWidth);
		currentstyle.removeVisualMappingFunction(imf.getVisualProperty());
		currentstyle.addVisualMappingFunction(imf);
	}
	/**
	 * Remove the IDARE Ndoes from the current Visualstyle (if they were added) and restore default values.
	 * @param monitor
	 */
	public synchronized void removeNodes(TaskMonitor monitor)
	{
		CyNetworkView CurrentView = cyAppMgr.getCurrentNetworkView();
		VisualStyle currentstyle = vmmServiceRef.getVisualStyle(CurrentView);		
		String StyleTitle = currentstyle.getTitle();
		monitor.setStatusMessage("Removing Nodes from Style " + StyleTitle);
		if(VisualStyleTitles.contains(StyleTitle))
		{
			monitor.setStatusMessage("Restoring Properties of " + StyleTitle);
			restoreProperties(currentstyle);
			eventHelper.flushPayloadEvents();
			monitor.setProgress(0.2);
			monitor.setStatusMessage("Updating Views");
			for(CyNetworkView networkView : cyNetViewMgr.getNetworkViewSet())
			{			
				VisualStyle cvs = vmmServiceRef.getVisualStyle(networkView);			
				if(StyleTitle.equalsIgnoreCase(cvs.getTitle()))
				{
					currentstyle.apply(networkView);
					vmmServiceRef.setVisualStyle(currentstyle, networkView);
					networkView.updateView();	
				}
			}
			eventHelper.flushPayloadEvents();
			VisualStyleTitles.remove(StyleTitle);	
			monitor.setProgress(0.9);
		}
	}

	/**
	 * Restore the stored values to all styles.
	 */
	public synchronized void shutDown()
	{
		Iterator it = vmmServiceRef.getAllVisualStyles().iterator();
		while (it.hasNext()){
			VisualStyle curVS = (VisualStyle)it.next();
			if (VisualStyleTitles.contains(curVS.getTitle()))
			{
				restoreProperties(curVS);
			}
		}
	}
	@Override
	public void handleEvent(SessionLoadedEvent e) {
		// TODO Auto-generated method stub
		List<File> StyleNameFiles = e.getLoadedSession().getAppFileListMap().get(IMAGENODEPROPERTIES.STYLE_MAPPINGS_SAVE_NAME);

		if(StyleNameFiles != null)
		{
			ObjectInputStream oi;
			File CFile = StyleNameFiles.get(0);
			try{
				oi = new ObjectInputStream(new FileInputStream(CFile));
				VisualStyleTitles = (HashSet<String>) (oi.readObject());
				lockedenabled = (HashMap<String,Boolean>) oi.readObject();
				oi.close();
			}
			catch(IOException ex)
			{
				//Should not happen
			}
			catch(ClassNotFoundException ex)
			{
				//Should not happen
			}
			
			Iterator it = vmmServiceRef.getAllVisualStyles().iterator();

			while (it.hasNext()){
				VisualStyle curVS = (VisualStyle)it.next();
				IDAREDependentMapper<Integer> LabelTransparency = new IDAREDependentMapper<Integer>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_LABEL_TRANSPARENCY,nm,0);
				IDAREDependentMapper<Double> imagenodeHeight = new IDAREDependentMapper<Double>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_HEIGHT,nm,IMAGENODEPROPERTIES.IDARE_NODE_DISPLAY_HEIGHT-2);
				IDAREDependentMapper<Double> imagenodeWidth = new IDAREDependentMapper<Double>(IDAREProperties.IDARE_NODE_NAME, BasicVisualLexicon.NODE_WIDTH,nm,IMAGENODEPROPERTIES.IDARE_NODE_DISPLAY_WIDTH-2);


				if (VisualStyleTitles.contains(curVS.getTitle()))
				{
					saveProperties(curVS,true);
					curVS.removeVisualMappingFunction(LabelTransparency.getVisualProperty());
					curVS.addVisualMappingFunction(LabelTransparency);
					curVS.removeVisualMappingFunction(imagenodeHeight.getVisualProperty());
					curVS.addVisualMappingFunction(imagenodeHeight);
					curVS.removeVisualMappingFunction(imagenodeWidth.getVisualProperty());
					curVS.addVisualMappingFunction(imagenodeWidth);
					curVS.removeVisualMappingFunction(imf.getVisualProperty());
					curVS.addVisualMappingFunction(imf);
				}
			}
		}

	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		// TODO Auto-generated method stub
		//Map<String,List<File>> FileList = e.getAppFileListMap();
		LinkedList<File> MappingStyles = new LinkedList<>();

		//Create A Temporary Zip File

		File TempFile = IOUtils.getTemporaryFile(IMAGENODEPROPERTIES.STYLE_MAPPINGS_SAVE_FILE,"");
		try{
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(TempFile));
			os.writeObject(VisualStyleTitles);
			os.writeObject(lockedenabled);
			os.close();
			MappingStyles.add(TempFile);
			e.addAppFiles(IMAGENODEPROPERTIES.STYLE_MAPPINGS_SAVE_NAME, MappingStyles);
		}
		catch(Exception ex)
		{
			PrintFDebugger.Debugging(this, "Could not save Style properties.\n ");
			ex.printStackTrace(System.out);
		}			
	}
	
	/**
	 * Save the Properties of the given Visualstyle. 
	 * If savedefautls is selected only default values and no mappings will be stored.
	 * @param style
	 * @param savedefaults
	 */
	private void saveProperties(VisualStyle style, boolean savedefaults)
	{
		for(VisualProperty prop : storedProperties)
		{
			VisualMappingFunction map = null;
			if(!savedefaults)
			{
				map = style.getVisualMappingFunction(prop);
			}
			if(map == null)
			{
				Object property = style.getDefaultValue(prop);
				if(!origstyleproperties.containsKey(style))
				{
					origstyleproperties.put(style, new HashMap<VisualProperty, Object>());
				}
				origstyleproperties.get(style).put(prop,property);
			}
			else
			{
				if(!origstylemappings.containsKey(style))
				{
					origstylemappings.put(style, new HashMap<VisualProperty, VisualMappingFunction>());
				}
				origstylemappings.get(style).put(prop,map);
			}
		}	

		/*
		 * this is ugly, but since there is no other way to obtain this "default" dependency, we have to do it like this. 
		 */
		if(!savedefaults)
		{
			for(VisualPropertyDependency<?> dependency : style.getAllVisualPropertyDependencies()) {
				if(dependency.getIdString().equals("nodeSizeLocked")) {
					lockedenabled.put(style.getTitle(), dependency.isDependencyEnabled());
					dependency.setDependency(false);
					break;
				}
			}
		}
	}
	/**
	 * Restore the properties saved for a given Style to that style.
	 * @param style
	 */
	private void restoreProperties(VisualStyle style)
	{
		if(origstyleproperties.containsKey(style))
		{			
			for(VisualProperty cprop : origstyleproperties.get(style).keySet())
			{
				style.removeVisualMappingFunction(cprop);
				style.setDefaultValue(cprop, origstyleproperties.get(style).get(cprop));
			}
		}
		if(origstylemappings.containsKey(style))
		{
			for(VisualProperty cprop : origstylemappings.get(style).keySet())
			{
				style.removeVisualMappingFunction(cprop);
				style.addVisualMappingFunction(origstylemappings.get(style).get(cprop));
			}

		}
		// Again, this is ugly, but we can't do anything else.
		if(lockedenabled.containsKey(style))
		{
			for(VisualPropertyDependency<?> dependency : style.getAllVisualPropertyDependencies()) {
				if(dependency.getIdString().equals("nodeSizeLocked")) {					 
					dependency.setDependency(lockedenabled.get(style));
					break;
				}
			}
		}
	}
	/**
	 * Test whether a given Style uses IDARE Image Nodes.
	 * @param StyleName
	 * @return whether the provided StyleName is one of the names of styles using image nodes
	 */
	public boolean styleUsed(String StyleName)
	{
		if(VisualStyleTitles.contains(StyleName))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Test whether a given NetworkView uses a Style containing ImageNodes. 
	 * @param view - the view to check 
	 * @return whether the provided {@link CyNetworkView} uses a style that contains image nodes. 
	 */
	public boolean viewUsesStyleWithNodes(CyNetworkView view)
	{
		if(VisualStyleTitles.contains(vmmServiceRef.getVisualStyle(view).getTitle()))
		{
			return true;
		}
		return false;
	}
	
	@Override
	public void imageUpdated(GraphicsChangedEvent e) {

		if(!e.getIDs().isEmpty())
		{
			updateRelevantViews();
		}
	}
		

}
