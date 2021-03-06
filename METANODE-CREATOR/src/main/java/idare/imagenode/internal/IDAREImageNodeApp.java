package idare.imagenode.internal;

import idare.Properties.IDARESettingsManager;
import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.Interfaces.Plugin.IDAREPlugin;
import idare.imagenode.exceptions.io.DuplicateIDException;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.GUI.DataSetAddition.Tasks.DataSetAdderTaskFactory;
import idare.imagenode.internal.GUI.DataSetController.CreateNodesTaskFactory;
import idare.imagenode.internal.GUI.DataSetController.DataSetControlPanel;
import idare.imagenode.internal.GUI.Legend.IDARELegend;
import idare.imagenode.internal.GUI.Legend.Tasks.CreateNodeImagesTaskFactory;
import idare.imagenode.internal.GUI.NetworkSetup.Tasks.NetworkSetupTaskFactory;
import idare.imagenode.internal.ImageManagement.ActiveNodeManager;
import idare.imagenode.internal.ImageManagement.ImageStorage;
import idare.imagenode.internal.Layout.ImageNodeLayout;
import idare.imagenode.internal.Layout.io.LayoutIOManager;
import idare.imagenode.internal.VisualStyle.IDAREVisualStyle;
import idare.imagenode.internal.VisualStyle.StyleManager;
import idare.imagenode.internal.VisualStyle.Tasks.AddNodesToStyleTaskFactory;
import idare.imagenode.internal.VisualStyle.Tasks.RemoveNodesFromStyleTaskFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.ActionEnableSupport;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * The core class of the app, generates necessary objects for the class and provides them to external requests.
 * @author Thomas Pfau
 *
 */
public class IDAREImageNodeApp implements SessionAboutToBeSavedListener{
	
	ImageStorage storage;
	NodeManager nm;
	DataSetManager dsm;
	IDARESettingsManager Settings;
	IDAREVisualStyle ids;
	IDARELegend legend;
	DataSetControlPanel dcp; 
	StyleManager styleManager;
	ActiveNodeManager anm;
	HashMap<AbstractTaskFactory,Vector<Properties>> taskFactories = new HashMap<AbstractTaskFactory, Vector<Properties>>();	
	Vector<AbstractCyAction> cyActions = new  Vector<AbstractCyAction>();
	HashMap<IDAREPlugin,Vector<IDAREService>> plugins = new HashMap<IDAREPlugin, Vector<IDAREService>>();	
	DialogTaskManager dtm;
	Logger logger;
	LayoutIOManager layoutIOmanager;
	/**
	 * Basic constructor that gets the IDareSettingsManager along with a CyServiceRegistrar to obtain all necessary services.
	 * 
	 * @param reg The {@link CyServiceRegistrar} for the current bundle
	 * @param ism The {@link IDARESettingsManager} used in this app
	 */
	public IDAREImageNodeApp(CyServiceRegistrar reg, IDARESettingsManager ism)
	{		
		this.dtm = reg.getService(DialogTaskManager.class);
		this.logger = LoggerFactory.getLogger(IDAREImageNodeApp.class);
		VisualLexicon currentLexicon = reg.getService(VisualLexicon.class);
		VisualProperty<CyCustomGraphics<CustomGraphicLayer>>  VisualcustomGraphiVP = (VisualProperty<CyCustomGraphics<CustomGraphicLayer>>)currentLexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");		
		storage = new ImageStorage(VisualcustomGraphiVP);
		dsm = new DataSetManager();		
		anm = new ActiveNodeManager(reg.getService(CyNetworkViewManager.class), reg.getService(VisualMappingManager.class));
		nm = new NodeManager(reg.getService(CyNetworkManager.class),anm);
		anm.setNodeManager(nm);
		storage.setNodeManager(nm);
		nm.setDataSetManager(dsm);
		nm.addNodeChangeListener(storage);
		dsm.addDataSetChangeListener(nm);		
		ids = new IDAREVisualStyle(reg.getService(VisualStyleFactory.class), reg.getService(VisualMappingManager.class),
								   reg.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)"),
								   reg.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)"),				
								   reg.getService(CyEventHelper.class), storage, reg.getService(CyNetworkViewManager.class),nm,anm);		
		storage.setVisualStyle(ids);
		Settings = ism;
		legend = new IDARELegend(new JPanel(),nm);
		nm.addNodeChangeListener(legend);
		nm.updateNetworkNodes();
		storage.addImageLayoutChangedListener(ids);
		styleManager = new StyleManager(storage, reg.getService(VisualMappingManager.class), reg.getService(CyNetworkViewManager.class),
										reg.getService(CyEventHelper.class), nm, reg.getService(CyApplicationManager.class),anm);
		storage.addImageLayoutChangedListener(styleManager);
		dcp = new DataSetControlPanel(reg.getService(CySwingApplication.class), this);
		layoutIOmanager = new LayoutIOManager();
		createActions(dtm, reg.getService(CyApplicationManager.class));		
		createTaskFactories(dtm,reg.getService(FileUtil.class),reg.getService(CySwingApplication.class),reg.getService(CyApplicationManager.class));

	}
	
	
	/**
	 * Obtain the {@link StyleManager} generated by this instance of IDARE.
	 * @return The {@link StyleManager} used by this instance
	 */
	public StyleManager getStyleManager()
	{
		return styleManager;
	}
	/**
	 * Obtain the {@link DataSetControlPanel} generated by this instance of IDARE.
	 * @return The {@link DataSetControlPanel} used by this instance
	 */
	public DataSetControlPanel getDataSetPanel()
	{
		return dcp;
	}
	/**
	 * Get the {@link IDARELegend} generated by this instance of IDARE.
	 * @return The {@link IDARELegend} used by this instance
	 */
	public IDARELegend getLegend()
	{
		return legend;
	}
	
	/**
	 * Get the {@link IDAREVisualStyle} created by this App
	 * @return The {@link IDAREVisualStyle} used by this instance
	 */
	public IDAREVisualStyle getVisualStyle()
	{
		return ids;
	}
	
	
	/**
	 * Get the {@link ImageStorage} created by this App
	 * @return The {@link ImageStorage} used by this instance
	 */
	public ImageStorage getImageStorage() {
		return storage;
	}
	/**
	 * Get the {@link NodeManager} created by this App
	 * @return The {@link NodeManager} used by this instance
	 */
	public NodeManager getNodeManager() {
		return nm;
	}
	/**
	 * Get the {@link DataSetManager} created by this App
	 * @return The {@link DataSetManager} used by this instance
	 */
	public DataSetManager getDatasetManager() {
		return dsm;
	}
	
	/**
	 * Get the {@link ActiveNodeManager} created by this App
	 * @return The {@link ActiveNodeManager} used by this instance
	 */
	public ActiveNodeManager getActiveNodeManager() {
		return anm;
	}
	/**
	 * Register a the type of a dataset provided to the set of avilable {@link DataSet} classes
	 * @param ds An instance of a {@link DataSet} which is of the class to register.
	 * @throws DuplicateIDException if there are two datasets with the same type name
	 */
	public void registerDataSetType(DataSet ds) throws DuplicateIDException 
	{		
		dsm.registerDataSetType(ds.getDataSetTypeName(), ds.getClass());			
	}
	
	/**
	 * Deregister a the an available {@link DataSet} class based on an instance of the class.
	 * @param ds An instance of a {@link DataSet} object that belongs t the class that is supposed to be registered
	 */
	public void deregisterDataSetType(DataSet ds)  
	{				
		dsm.deRegisterDataSetType(ds.getDataSetTypeName(), ds.getClass());			
	}
	
	/**
	 * Register DataSetProperties for a DataSet of the given Type.
	 * @param dataSetClass the classname of the {@link DataSet}, for which to add the properties
	 * @param props The DataSetproperties to make available.
	 * @return whether the dataset type could be added
	 */
	public boolean registerDataSetProperties(Class<? extends DataSet> dataSetClass, DataSetLayoutProperties props)
	{
		return dsm.registerPropertiesForDataSet(dataSetClass, props);		
	}
	
	/**
	 * Register DataSetProperties for a DataSet of the given Type.
	 * @param dataSetClass the classname of the {@link DataSet}, for which to add the properties
 	 * @param props The collection of DataSetproperties to make available to this type of dataset.
	 * @return A set of Properties that could not be registered
	 */
	public Collection<DataSetLayoutProperties> registerDataSetProperties(Class<? extends DataSet> dataSetClass, Collection<DataSetLayoutProperties> props)
	{
		return dsm.registerPropertiesForDataSet(dataSetClass, props);		
	}
	
	/**
	 * Deregister DataSetProperties from a DataSet of the given Type.
	 * @param dataSetClass the classname of the {@link DataSet} to deregister the properties from
	 * @param props The properties to deregister.
	 */
	public void deRegisterDataSetProperties(Class<? extends DataSet> dataSetClass, DataSetLayoutProperties props)
	{
		dsm.deregisterPropertiesForDataSet(dataSetClass, props);		
	}
	
	
	/**
	 * Deregister an {@link IDAREDatasetReader} from being available in the app.
	 * @param reader the {@link IDAREDatasetReader} to deregister.
	 */
	public void deRegisterDataSetReader(IDAREDatasetReader reader)
	{
		dsm.deregisterDataSetReader(reader);		
	}
	
	/**
	 * Register an {@link IDAREDatasetReader} to be available in the app.
	 * @param reader the {@link IDAREDatasetReader} to register.
	 */
	public void registerDataSetReader(IDAREDatasetReader reader)
	{
		dsm.registerDataSetReader(reader);		
	}
	
	/**
	 * Unregister all Types of {@link DataSet}, that were before registered. 
	 * This will reset the {@link NodeManager}, as all Sets have been disabled and will also reset the {@link ImageStorage}. 
	 */
	public void unregisterAll()
	{
		Vector<IDAREPlugin> currentplugins = new Vector<IDAREPlugin>();
		currentplugins.addAll(plugins.keySet());
		for(IDAREPlugin plugin : currentplugins)
		{
			deRegisterPlugin(plugin);
		}
		dsm.clearDataSets();
		nm.reset();
		storage.reset();
		ids.shutdown();
		styleManager.shutDown();		
	}
	/**
	 * Get the {@link IDARESettingsManager} created by this App
	 * @return The {@link IDARESettingsManager} used by this instance
	 */
	public IDARESettingsManager getSettingsManager()
	{
		return Settings;
	}
	
	/**
	 * Handle a session load event. This class does not implement the {@link SessionLoadedListener} itself, as the order of loading is relevant.
	 * @param arg0 The {@link SessionLoadedEvent} that provides information on the state of the app in the loaded session.
	 */
	public void handleSessionLoadedEvent(SessionLoadedEvent arg0) {
		//first, see, whether we have datasets. If, we have to reset them, otherwise we will simply load the new ones.
		if(!dsm.getDataSets().isEmpty())
		{
			dsm.reset();
			nm.reset();
			storage.reset();			
		}
		//first initialize the Dataset manager (i.e. get the datasets set up)		
		dsm.handleEvent(arg0);				
		//then init the nodemanager (i.e. assign the appropriate layouts) 
		nm.handleEvent(arg0,this);
		//and finally redo the styles.
		styleManager.handleEvent(arg0);
		
		
	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent arg0) {
		//Save in the correct order.
		dsm.handleEvent(arg0);
		nm.handleEvent(arg0,this);
	}
	
	
	/**
	 * Generate the Taskfactories used in the App and register them with the appropriate objects.
	 * @param dtm The dialogTaskmanager uses
	 * @param util A File Util to use in the Factories
	 * @param cySwingApp A reference to the CySwingApp to be used in Factories
	 */
	private void createTaskFactories(DialogTaskManager dtm, FileUtil util,CySwingApplication cySwingApp, CyApplicationManager cyAppMgr)
	{
		CreateNodesTaskFactory nodeFactory = new CreateNodesTaskFactory(nm, dcp, dtm);
		Vector<Properties> props = new Vector<Properties>();
		props.add(new Properties());
		taskFactories.put(nodeFactory, props);
		dcp.setNodeFactory(nodeFactory);
		DataSetAdderTaskFactory dsatf = new DataSetAdderTaskFactory(dsm, dtm, cySwingApp);
		taskFactories.put(dsatf, props);
		dcp.setDatasetAdderFactory(dsatf);	
		
		//The Network Setup Task Factory 
		NetworkSetupTaskFactory nstf = new NetworkSetupTaskFactory(cyAppMgr);
		Properties setupNetworkMenuProperties = new Properties();
		setupNetworkMenuProperties.setProperty(ServiceProperties.PREFERRED_ACTION, "NEW");
		setupNetworkMenuProperties.setProperty(ServiceProperties.PREFERRED_MENU, "Apps.IDARE");
		setupNetworkMenuProperties.setProperty(ServiceProperties.IN_MENU_BAR, "true");
		setupNetworkMenuProperties.setProperty(ServiceProperties.IN_CONTEXT_MENU, "false");		
		setupNetworkMenuProperties.setProperty(ServiceProperties.TITLE, "Setup Network For IDARE");
		setupNetworkMenuProperties.setProperty(ServiceProperties.ENABLE_FOR, ActionEnableSupport.ENABLE_FOR_ALWAYS);
		setupNetworkMenuProperties.put("USE_CLASS",NetworkViewTaskFactory.class);			
		Properties setupNetworkContextProperties = new Properties();
		setupNetworkContextProperties.setProperty(ServiceProperties.PREFERRED_ACTION, "NEW");
		setupNetworkContextProperties.setProperty(ServiceProperties.PREFERRED_MENU, ServiceProperties.APPS_MENU);
		setupNetworkContextProperties.setProperty(ServiceProperties.IN_TOOL_BAR, "false");
		setupNetworkContextProperties.setProperty(ServiceProperties.IN_MENU_BAR, "false");
		setupNetworkContextProperties.setProperty(ServiceProperties.IN_CONTEXT_MENU, "true");
		setupNetworkContextProperties.setProperty(ServiceProperties.TITLE, "Setup Network For IDARE");		
		setupNetworkContextProperties.setProperty(ServiceProperties.ENABLE_FOR, ActionEnableSupport.ENABLE_FOR_ALWAYS);
		setupNetworkContextProperties.put("USE_CLASS", NetworkViewTaskFactory.class);
		Vector<Properties> networkSetupProps = new Vector<Properties>();		
		networkSetupProps.add(setupNetworkContextProperties);
		networkSetupProps.add(setupNetworkMenuProperties);
		taskFactories.put(nstf, networkSetupProps);
		
		

		
		CreateNodeImagesTaskFactory nodeImageFactory = new CreateNodeImagesTaskFactory(util, legend, nm, cySwingApp);
		Properties createNodesImageMenuProperties = new Properties();
		createNodesImageMenuProperties.setProperty(ServiceProperties.PREFERRED_ACTION, "NEW");
		createNodesImageMenuProperties.setProperty(ServiceProperties.PREFERRED_MENU, "Apps.IDARE");
		createNodesImageMenuProperties.setProperty(ServiceProperties.IN_MENU_BAR, "true");
		createNodesImageMenuProperties.setProperty(ServiceProperties.IN_CONTEXT_MENU, "false");		
		createNodesImageMenuProperties.setProperty(ServiceProperties.TITLE, "Create Images for current Legend");
		createNodesImageMenuProperties.setProperty(ServiceProperties.ENABLE_FOR, ActionEnableSupport.ENABLE_FOR_ALWAYS);
		createNodesImageMenuProperties.put("USE_CLASS",NetworkViewTaskFactory.class);			
		Properties createNodesImageContextProperties = new Properties();
		createNodesImageContextProperties.setProperty(ServiceProperties.PREFERRED_ACTION, "NEW");
		createNodesImageContextProperties.setProperty(ServiceProperties.PREFERRED_MENU, ServiceProperties.APPS_MENU);
		createNodesImageContextProperties.setProperty(ServiceProperties.IN_TOOL_BAR, "false");
		createNodesImageContextProperties.setProperty(ServiceProperties.IN_MENU_BAR, "false");
		createNodesImageContextProperties.setProperty(ServiceProperties.IN_CONTEXT_MENU, "true");
		createNodesImageContextProperties.setProperty(ServiceProperties.TITLE, "Create Node Images for current Legend");		
		createNodesImageContextProperties.setProperty(ServiceProperties.ENABLE_FOR, ActionEnableSupport.ENABLE_FOR_ALWAYS);
		createNodesImageContextProperties.put("USE_CLASS", NetworkViewTaskFactory.class);
		Vector<Properties> nodeImageProps = new Vector<Properties>();		
		nodeImageProps.add(createNodesImageMenuProperties);
		nodeImageProps.add(createNodesImageContextProperties);
		
		taskFactories.put(nodeImageFactory, nodeImageProps);
		
	}
	/**
	 * Create All Actions, and their associated factories if necessary.
	 * @param dtm A DialogTaskmanager for the tasks
	 * @param cyAppMgr the CyAppMgr to set the Actions.
	 */
	private void createActions(DialogTaskManager dtm, CyApplicationManager cyAppMgr)
	{
		AddNodesToStyleTaskFactory addFactory = new AddNodesToStyleTaskFactory(styleManager, dtm);
		styleManager.setAddNodesTaskFactory(addFactory);
		//AddNodesToStyleAction addAction = new AddNodesToStyleAction(cyAppMgr, addFactory);
		Vector<Properties> props = new Vector<Properties>();
		Properties addNodesToStylePropertiesMenu = new Properties();
		addNodesToStylePropertiesMenu.setProperty(ServiceProperties.PREFERRED_ACTION, "NEW");
		addNodesToStylePropertiesMenu.setProperty(ServiceProperties.PREFERRED_MENU, "Apps.IDARE");
		addNodesToStylePropertiesMenu.setProperty(ServiceProperties.IN_MENU_BAR, "true");
		addNodesToStylePropertiesMenu.setProperty(ServiceProperties.IN_CONTEXT_MENU, "false");
		addNodesToStylePropertiesMenu.setProperty(ServiceProperties.TITLE, "Add IDARE Images to Style");
		addNodesToStylePropertiesMenu.setProperty(ServiceProperties.TOOLTIP, "Add IDARE Images");
		addNodesToStylePropertiesMenu.setProperty(ServiceProperties.ENABLE_FOR, ActionEnableSupport.ENABLE_FOR_ALWAYS);
		addNodesToStylePropertiesMenu.put("USE_CLASS",NetworkViewTaskFactory.class);
		props.add(addNodesToStylePropertiesMenu);
		Properties addNodesToStylePropertiesTask = new Properties();
		addNodesToStylePropertiesTask.setProperty(ServiceProperties.PREFERRED_ACTION, "NEW");
		addNodesToStylePropertiesTask.setProperty(ServiceProperties.PREFERRED_MENU, ServiceProperties.NETWORK_APPS_MENU);
		addNodesToStylePropertiesTask.setProperty(ServiceProperties.IN_TOOL_BAR, "false");
		addNodesToStylePropertiesTask.setProperty(ServiceProperties.IN_MENU_BAR, "false");
		addNodesToStylePropertiesTask.setProperty(ServiceProperties.IN_CONTEXT_MENU, "true");
		addNodesToStylePropertiesTask.setProperty(ServiceProperties.TITLE, "Add IDARE Images");
		addNodesToStylePropertiesTask.setProperty(ServiceProperties.TOOLTIP, "Add IDARE Images to Style");
		addNodesToStylePropertiesTask.setProperty(ServiceProperties.ENABLE_FOR, ActionEnableSupport.ENABLE_FOR_NETWORK_AND_VIEW);
		addNodesToStylePropertiesTask.put("USE_CLASS", NetworkViewTaskFactory.class);
		props.add(addNodesToStylePropertiesTask);
		taskFactories.put(addFactory,props);
		//cyActions.add(addAction);
		Vector<Properties> props2 = new Vector<Properties>();
		RemoveNodesFromStyleTaskFactory remFactory = new RemoveNodesFromStyleTaskFactory(styleManager, dtm); 
		//RemoveNodesFromStyleAction remAction = new RemoveNodesFromStyleAction(cyAppMgr, remFactory);
		Properties removeNodesFromStyleProperties = new Properties();
		removeNodesFromStyleProperties.setProperty(ServiceProperties.PREFERRED_ACTION, "NEW");
		removeNodesFromStyleProperties.setProperty(ServiceProperties.PREFERRED_MENU, "Apps.IDARE");
		removeNodesFromStyleProperties.setProperty(ServiceProperties.TITLE, "Remove IDARE Images from Style");
		removeNodesFromStyleProperties.setProperty(ServiceProperties.IN_TOOL_BAR, "false");		
		removeNodesFromStyleProperties.setProperty(ServiceProperties.IN_MENU_BAR, "true");
		removeNodesFromStyleProperties.setProperty(ServiceProperties.IN_CONTEXT_MENU, "false");
		removeNodesFromStyleProperties.setProperty(ServiceProperties.TOOLTIP, "Remove IDARE Images");
		removeNodesFromStyleProperties.setProperty(ServiceProperties.ENABLE_FOR, ActionEnableSupport.ENABLE_FOR_ALWAYS);
		removeNodesFromStyleProperties.put("USE_CLASS",NetworkViewTaskFactory.class);
		Properties removeNodesFromStyleProperties2 = new Properties();
		removeNodesFromStyleProperties2.setProperty(ServiceProperties.PREFERRED_ACTION, "NEW");
		removeNodesFromStyleProperties2.setProperty(ServiceProperties.PREFERRED_MENU,ServiceProperties.NETWORK_APPS_MENU);
		removeNodesFromStyleProperties2.setProperty(ServiceProperties.TITLE, "Remove IDARE Images ");
		removeNodesFromStyleProperties2.setProperty(ServiceProperties.IN_TOOL_BAR, "false");
		removeNodesFromStyleProperties2.setProperty(ServiceProperties.IN_MENU_BAR, "false");
		removeNodesFromStyleProperties2.setProperty(ServiceProperties.IN_CONTEXT_MENU, "true");
		removeNodesFromStyleProperties2.setProperty(ServiceProperties.TOOLTIP, "Remove IDARE Images from Style");
		removeNodesFromStyleProperties2.setProperty(ServiceProperties.ENABLE_FOR, ActionEnableSupport.ENABLE_FOR_NETWORK_AND_VIEW);
		removeNodesFromStyleProperties2.put("USE_CLASS",NetworkViewTaskFactory.class);
		props2.add(removeNodesFromStyleProperties);
		props2.add(removeNodesFromStyleProperties2);
		taskFactories.put(remFactory,props2);		
	}
	/**
	 * Get the Task Factories used in the App.
	 * @return the TaskFactories produced by the IDARE App matched to all properties for which they should eb registered
	 */
	public HashMap<AbstractTaskFactory,Vector<Properties>> getFactories()
	{
		HashMap<AbstractTaskFactory,Vector<Properties>> factories = new HashMap<AbstractTaskFactory, Vector<Properties>>();
		factories.putAll(taskFactories);
		return factories;
	}
	
	/**
	 * Get The LayoutIOManager
	 * @return The Layoutmanager of this app
	 */
	public LayoutIOManager getLayoutIOManager()
	{
		return layoutIOmanager;
	}
	/**
	 * Get the actions used in this app
	 * @return The Vector of AbstractCyActions generated by this App.
	 */
	public Vector<AbstractCyAction> getActions()
	{
		Vector<AbstractCyAction> actions = new Vector<AbstractCyAction>();
		actions.addAll(cyActions);
		return actions;
	}
	
	/**
	 * 
	 * Register a plugin.	 
	 * @param plugin the plugin to register
	 */
	public void registerPlugin(IDAREPlugin plugin)
	{
		
		dtm.execute(new TaskIterator(new UpdateTask(plugin,plugin.getServices(),true)));			
		
	}
	
	/**
	 * UnRegister a plugin.
	 * @param plugin the plugin to unregister.
	 */
	public void deRegisterPlugin(IDAREPlugin plugin)
	{
		if(plugins.containsKey(plugin))
		{			
			dtm.execute(new TaskIterator(new UpdateTask(plugin,null,false)));			
		}
				
	}
	
	protected void updateServices(IDAREPlugin plugin, Vector<IDAREService> services, boolean registered, TaskMonitor monitor) throws IllegalAccessException,InstantiationException
	{		
		Vector<IDAREService> changedservices = new Vector<IDAREService>();
		
		if(registered)
		{
			monitor.setTitle("Installing plugin");								
		}
		else
		{
			monitor.setTitle("Uninstalling plugin");
			services = plugins.get(plugin);
		}
		//this should only happen if there are no services registered for a given plugin.
		if(services == null)
		{
			return;
		}
		double service = 0.;
		for(IDAREService serv : services)
		{		
		
			if( serv instanceof DataSetLayoutProperties)
			{
				DataSetLayoutProperties props = (DataSetLayoutProperties)serv;
				for(Class clazz : props.getWorkingClassTypes())
				{
					if(registered)
					{
						if(registerDataSetProperties(clazz, props))
						{							
							changedservices.add(props);
							monitor.setStatusMessage(props.getTypeName() +  " registered for class " + clazz.getSimpleName());
						}
						else
						{
							monitor.setStatusMessage("Could not register " + props.getTypeName());						
						}
					}
					else
					{
						deRegisterDataSetProperties(clazz, props);
						monitor.setStatusMessage(props.getTypeName() +  " deregistered");
					}
				}
			}
			if( serv instanceof IDAREDatasetReader)
			{
				if(registered)
				{					
					registerDataSetReader((IDAREDatasetReader) serv);
					monitor.setStatusMessage(serv.getClass().getTypeName()+  " registered");
					changedservices.add(serv);
				}
				else
				{
					deRegisterDataSetReader((IDAREDatasetReader) serv);
					monitor.setStatusMessage(serv.getClass().getTypeName()+  " unregistered");
				}
				
			}
			if( serv instanceof DataSet)
			{
				if(registered)
				{
					try{
						registerDataSetType((DataSet)serv);
						monitor.setStatusMessage(((DataSet) serv).getDataSetTypeName()+  " registered");
					}
					catch( DuplicateIDException e)
					{
						monitor.setStatusMessage("Could not register " + ((DataSet) serv).getDataSetTypeName()+  " due to the following error: \n " + e.getMessage());					
					}
				}
				else
				{
					deregisterDataSetType((DataSet)serv);
					monitor.setStatusMessage(((DataSet) serv).getDataSetTypeName()+  " deregistered");					
				}
			}
			if( serv instanceof ImageNodeLayout)
			{
				if(registered)
				{
					layoutIOmanager.registerLayout((ImageNodeLayout)serv);
				}
				else
				{
					layoutIOmanager.deRegisterLayout((ImageNodeLayout)serv);
				}
			}
			service++;
			monitor.setProgress(service/services.size());
		}
		if(registered)
		{
			plugins.put(plugin,changedservices);
		}
		else
		{
			plugins.remove(plugin);
		}		
	}
	/**
	 * A small Task that updates a service for IDARE.
	 * @author Thomas Pfau
	 *
	 */
	private class UpdateTask implements Task
	{
		IDAREPlugin plugin;
		boolean registered;
		Vector<IDAREService> services;
		public UpdateTask( IDAREPlugin plugin, Vector<IDAREService> services,boolean registered) {
			// TODO Auto-generated constructor stub
			this.plugin = plugin;
			this.services = services;
			this.registered = registered;
		}
		@Override
		public void cancel() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void run(TaskMonitor arg0) throws Exception {
			// TODO Auto-generated method stub
			updateServices(plugin, services,registered,arg0);
		}
		
	}
	
}
