package idare.imagenode.internal;


import idare.imagenode.IDAREImageNodeAppService;
import idare.imagenode.internal.DataManagement.DataSetProvider;
import idare.imagenode.internal.DataSetReaders.DataSetReaderProvider;
import idare.imagenode.internal.GUI.DataSetAddition.DataSetParametersGUIHandlerFactory;
import idare.imagenode.internal.GUI.DataSetController.DataSetControlPanel;
import idare.imagenode.internal.GUI.Legend.IDARELegend;
import idare.imagenode.internal.GUI.Legend.LegendUpdater;
import idare.imagenode.internal.GUI.NetworkSetup.Tasks.NetworkSetupGUIHandlerFactory;
import idare.imagenode.internal.ImageManagement.DefaultLayoutProvider;
import idare.imagenode.internal.Services.JSBML.SBMLManagerHolder;
import idare.internal.IDAREApp;
import idare.sbmlannotator.internal.Tasks.SBMLAnnotatorTaskFactory;
import idare.subnetwork.internal.NetworkViewSwitcher;
import idare.subnetwork.internal.SubnetworkSessionManager;
import idare.subnetwork.internal.Tasks.SubsystemGeneration.SubnetworkCreationGUIHandlerFactory;
import idare.subnetwork.internal.Tasks.SubsystemGeneration.SubnetworkCreatorTaskFactory;
import idare.subnetwork.internal.Tasks.propertySelection.SubnetworkPropertyColumnGUIHandlerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.ActionEnableSupport;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionSavedListener;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;



public class CyActivator extends AbstractCyActivator {

	IDAREApp app;
	CyServiceRegistrar reg;
	BundleContext appcontext;
	//SBMLServiceRegistrar SBMLReg;

	@Override
	public void start(BundleContext context) throws Exception {		
		appcontext = context;		
		app = new IDAREApp();
		registerAllServices(context, app, new Properties());

		// Set the default logging position for the App.
		
		//The following lines allow access to the yFiles layouts for subnetwork generation tasks.
		final NetworkViewTaskFactory organic = getService(context,NetworkViewTaskFactory.class, "(title=Organic)");
		final NetworkViewTaskFactory orthogonal = getService(context,NetworkViewTaskFactory.class,
				"(title=Orthogonal)");
		final NetworkViewTaskFactory hierarchic = getService(context,NetworkViewTaskFactory.class,
				"(title=Hierarchic)");
		final NetworkViewTaskFactory circular = getService(context,NetworkViewTaskFactory.class, "(title=Circular)");

		final DummyLayoutWrapper wrapped1 = new DummyLayoutWrapper(organic, "organic", "yFiles Organic Layout",
				getService(context,UndoSupport.class));
		final DummyLayoutWrapper wrapped2 = new DummyLayoutWrapper(orthogonal, "orthogonal", "yFiles Orthogonal Layout",
				getService(context,UndoSupport.class));
		final DummyLayoutWrapper wrapped3 = new DummyLayoutWrapper(hierarchic, "hierarchic", "yFiles Hierarchic Layout",
				getService(context,UndoSupport.class));
		final DummyLayoutWrapper wrapped4 = new DummyLayoutWrapper(circular, "circular", "yFiles Circular Layout",
				getService(context,UndoSupport.class));
		final Collection<CyLayoutAlgorithm> yFilesLayouts = new HashSet<CyLayoutAlgorithm>();
		Properties yproperties = new Properties();
		yFilesLayouts.add(wrapped1);
		yFilesLayouts.add(wrapped2);
		yFilesLayouts.add(wrapped3);
		yFilesLayouts.add(wrapped4);
		registerService(context,wrapped1,CyLayoutAlgorithm.class, yproperties);
		registerService(context,wrapped2,CyLayoutAlgorithm.class, yproperties);
		registerService(context,wrapped3,CyLayoutAlgorithm.class, yproperties);
		registerService(context,wrapped4,CyLayoutAlgorithm.class, yproperties);		
		reg = getService(context, CyServiceRegistrar.class);
		setupimagenodeApp(context);
		setupNetworkCreatorApp(context);
		registerSBMLAnnotator(context);
		
	}
	@SuppressWarnings("rawtypes")
	private void setupimagenodeApp(BundleContext context)
	{
		//Obtain all services required for the app.
		CyApplicationManager cyAppMgr = getService(context, CyApplicationManager.class);	
		VisualMappingManager vmm = getService(context, VisualMappingManager.class);
		CySwingApplication cySwingApp = getService(context, CySwingApplication.class);	

		FileUtil util = getService(context, FileUtil.class);
		

		//initialize and register the app components.
		IDAREImageNodeApp imageapp = new IDAREImageNodeApp(reg, app.getSettingsManager());
		app.setImageApp(imageapp);
		imageapp.registerPlugin(new DataSetReaderProvider());
		imageapp.registerPlugin(new DataSetProvider());				
		imageapp.registerPlugin(new DefaultLayoutProvider());
		//Generate the Externally available sErvice
		IDAREImageNodeAppService appService = new IDAREImageNodeAppService(imageapp);
			
		//Set up the Legend Panel
		IDARELegend pan = imageapp.getLegend();
		LegendUpdater up = new LegendUpdater(pan, imageapp.getNodeManager(), cyAppMgr,vmm, imageapp.getStyleManager());		
		up.activate();
		DataSetControlPanel dcp = imageapp.getDataSetPanel();	
		
		//Generate he TunableHandlers
		DataSetParametersGUIHandlerFactory dsctf = new DataSetParametersGUIHandlerFactory(util,imageapp.getDatasetManager(),cySwingApp);		
		NetworkSetupGUIHandlerFactory nsghf = new NetworkSetupGUIHandlerFactory(imageapp.getNodeManager(), imageapp.getSettingsManager(), cyAppMgr);
		
		//Register the Actions of the App.
		for(CyAction cyAct : imageapp.getActions())
		{
			registerAllServices(context, cyAct, new Properties());
		}
		// Register the TaskFactories of the App.
		HashMap<AbstractTaskFactory, Vector<Properties>> facs = imageapp.getFactories();
		for(AbstractTaskFactory fac : facs.keySet())
		{
			for(Properties prop : facs.get(fac))
			{
				if(prop.containsKey("USE_CLASS"))
				{
					registerService(context, fac, (Class)prop.get("USE_CLASS"), prop);
				}
				else
				{
					registerAllServices(context, fac, prop);	
				}
			}
		}
		registerService(context, imageapp.getStyleManager(), SessionAboutToBeSavedListener.class, new Properties());
		registerAllServices(context, pan, new Properties());
		registerAllServices(context, up, new Properties());
		//Register the tunable Handlers
		registerService(context, nsghf, GUITunableHandlerFactory.class, new Properties());
		registerService(context, dsctf, GUITunableHandlerFactory.class, new Properties());

		//Register the app as its own service and load and save listener.
		registerService(context, imageapp, SessionAboutToBeSavedListener.class, new Properties());
		registerService(context, appService, IDAREImageNodeAppService.class, new Properties());

		//Register the visual style
		registerAllServices(context, imageapp.getVisualStyle(), new Properties());
		//Register the DatasetControlPanel
		registerAllServices(context, dcp, new Properties());

	}	
	private void registerSBMLAnnotator(BundleContext context)
	{
		CyApplicationManager cyApplicationManager = getService(context, CyApplicationManager.class);
		FileUtil FileUtilService = getService(context,FileUtil.class);		
		CyEventHelper eventHelper = getService(context, CyEventHelper.class);
		CySwingApplication cySwingApp = getService(context, CySwingApplication.class);
		

		//This is a holder for the cy3sbml SBMLManager class, which can provide that class to the SBMLAnnotationFactory if it is available. 
		//SBMLReg = new SBMLServiceRegistrar(context,FileUtilService, cySwingApp);
		//context.addServiceListener(SBMLReg);
		
		
		//Set up properties for the Context and menu items for SBML annotation.
		Properties addAnnotationPropertiesMenu = new Properties();
		addAnnotationPropertiesMenu.setProperty(ServiceProperties.PREFERRED_ACTION, "NEW");
		addAnnotationPropertiesMenu.setProperty(ServiceProperties.PREFERRED_MENU, "Apps.IDARE");
		addAnnotationPropertiesMenu.setProperty(ServiceProperties.IN_MENU_BAR, "true");
		addAnnotationPropertiesMenu.setProperty(ServiceProperties.IN_CONTEXT_MENU, "false");
		addAnnotationPropertiesMenu.setProperty(ServiceProperties.TITLE, "Add SBML Annotations");		
		addAnnotationPropertiesMenu.setProperty(ServiceProperties.ENABLE_FOR, ActionEnableSupport.ENABLE_FOR_ALWAYS);

		Properties addAnnotationPropertiesTask = new Properties();
		addAnnotationPropertiesTask.setProperty(ServiceProperties.PREFERRED_ACTION, "NEW");
		addAnnotationPropertiesTask.setProperty(ServiceProperties.PREFERRED_MENU, ServiceProperties.NETWORK_APPS_MENU);
		addAnnotationPropertiesTask.setProperty(ServiceProperties.IN_TOOL_BAR, "false");
		addAnnotationPropertiesTask.setProperty(ServiceProperties.IN_MENU_BAR, "false");
		addAnnotationPropertiesTask.setProperty(ServiceProperties.IN_CONTEXT_MENU, "true");
		addAnnotationPropertiesTask.setProperty(ServiceProperties.TITLE, "Add SBML Annotations");		
		addAnnotationPropertiesTask.setProperty(ServiceProperties.ENABLE_FOR, ActionEnableSupport.ENABLE_FOR_NETWORK_AND_VIEW);	

		//Create and register the SBMLAnnotationFactory.
//		SBMLAnnotationTaskFactory Annotator = new SBMLAnnotationTaskFactory(cyApplicationManager, eventHelper, FileUtilService, cySwingApp, SBMLReg.getHolder(), app.getImageNodeApp());
//		registerService(context, Annotator, NetworkViewTaskFactory.class, addAnnotationPropertiesMenu);
//		registerService(context, Annotator, NetworkViewTaskFactory.class, addAnnotationPropertiesTask);		
	
		SBMLAnnotatorTaskFactory Annotator2 = new SBMLAnnotatorTaskFactory(cyApplicationManager, eventHelper, FileUtilService, cySwingApp, new SBMLManagerHolder(FileUtilService, cySwingApp, context), app.getImageNodeApp());
		registerService(context, Annotator2, NetworkViewTaskFactory.class, addAnnotationPropertiesMenu);
		registerService(context, Annotator2, NetworkViewTaskFactory.class, addAnnotationPropertiesTask);
		
	}

	private void setupNetworkCreatorApp(BundleContext context)
	{

		CyApplicationManager cyApplicationManager = getService(context, CyApplicationManager.class);	
		CyLayoutAlgorithmManager LayoutManager =  getService(context, CyLayoutAlgorithmManager.class);
		CyServiceRegistrar reg = getService(context, CyServiceRegistrar.class);

		//initialize the basic components of the Subnetwork generator part of this app.
		NetworkViewSwitcher nvs = new NetworkViewSwitcher(reg, app.getSettingsManager());
		SubnetworkSessionManager SubSysSave = new SubnetworkSessionManager(nvs, app.getSettingsManager());
		Properties doubleClickProperties = new Properties();
		doubleClickProperties.setProperty(ServiceProperties.PREFERRED_ACTION, NetworkViewSwitcher.PREFERRED_OPTION);
		doubleClickProperties.setProperty(ServiceProperties.TITLE, "Switch To Network");

		//We need our own synchronization to load the different parts of the App when a new Session is loaded, so we don't register 
		//these parts with Cytoscape but with the IDAREapp which handles the order of loading.
		app.setSubsysManager(SubSysSave);
		
		//Register the diverse tasks of the NetworkViewSwitcher.
		registerService(context,nvs,NodeViewTaskFactory.class, doubleClickProperties);
		registerService(context,nvs,RowsSetListener.class, new Properties());
		registerService(context,nvs,NetworkAboutToBeDestroyedListener.class, doubleClickProperties);
		registerService(context,nvs,NetworkAddedListener.class, new Properties());
		registerService(context,nvs,NetworkViewAboutToBeDestroyedListener.class, doubleClickProperties);
		registerService(context,nvs,NetworkViewAddedListener.class, doubleClickProperties);
		registerService(context,nvs,SessionAboutToBeSavedListener.class, new Properties());
		
		
		//Create and register the Tunable Handler Factories for the dialogs
		SubnetworkCreationGUIHandlerFactory sncghf = new SubnetworkCreationGUIHandlerFactory(nvs, cyApplicationManager,LayoutManager);
		SubnetworkPropertyColumnGUIHandlerFactory snpcghf = new SubnetworkPropertyColumnGUIHandlerFactory(cyApplicationManager);
		registerService(context, sncghf, GUITunableHandlerFactory.class, new Properties());
		registerService(context, snpcghf, GUITunableHandlerFactory.class, new Properties());		

		

		//create and register the subnetworkgeneratorTaskFactory, with the appropriate properties.
		Properties createSubnetworkPropertiesMenu = new Properties();
		createSubnetworkPropertiesMenu.setProperty(ServiceProperties.PREFERRED_ACTION, "NEW");
		createSubnetworkPropertiesMenu.setProperty(ServiceProperties.PREFERRED_MENU, "Apps.IDARE");
		createSubnetworkPropertiesMenu.setProperty(ServiceProperties.IN_MENU_BAR, "true");
		createSubnetworkPropertiesMenu.setProperty(ServiceProperties.IN_CONTEXT_MENU, "false");
		createSubnetworkPropertiesMenu.setProperty(ServiceProperties.TITLE, "Create Subnetworks");		
		createSubnetworkPropertiesMenu.setProperty(ServiceProperties.ENABLE_FOR, ActionEnableSupport.ENABLE_FOR_ALWAYS);

		Properties createSubnetworkPropertiesTask = new Properties();
		createSubnetworkPropertiesTask.setProperty(ServiceProperties.PREFERRED_ACTION, "NEW");
		createSubnetworkPropertiesTask.setProperty(ServiceProperties.PREFERRED_MENU, ServiceProperties.NETWORK_APPS_MENU);
		createSubnetworkPropertiesTask.setProperty(ServiceProperties.IN_TOOL_BAR, "false");
		createSubnetworkPropertiesTask.setProperty(ServiceProperties.IN_MENU_BAR, "false");
		createSubnetworkPropertiesTask.setProperty(ServiceProperties.IN_CONTEXT_MENU, "true");
		createSubnetworkPropertiesTask.setProperty(ServiceProperties.TITLE, "Create Subnetworks");		
		createSubnetworkPropertiesTask.setProperty(ServiceProperties.ENABLE_FOR, ActionEnableSupport.ENABLE_FOR_NETWORK_AND_VIEW);

		
		SubnetworkCreatorTaskFactory snctf = new SubnetworkCreatorTaskFactory(reg,nvs,app.getSettingsManager(),sncghf);
		registerService(context, snctf, NetworkViewTaskFactory.class, createSubnetworkPropertiesMenu);
		registerService(context, snctf, NetworkViewTaskFactory.class, createSubnetworkPropertiesTask);
		
		
	}

	@Override
	public void shutDown()
	{
		super.shutDown();
		app.getImageNodeApp().unregisterAll();		
		try{
			reg.unregisterService(app, IDAREImageNodeApp.class);
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}

	}
	/**
	 * This Class is a means to obtain the y-Files layout and to make them available as possible layouts in the 
	 * IDARE app. 
	 * @author Thomas Pfau
	 *
	 */
	private static final class DummyLayoutWrapper extends AbstractLayoutAlgorithm {

		private final NetworkViewTaskFactory tf;

		public DummyLayoutWrapper(final NetworkViewTaskFactory tf, String computerName, String humanName,
				UndoSupport undoSupport) {
			super(computerName, humanName, undoSupport);
			this.tf = tf;

		}

		@Override
		public TaskIterator createTaskIterator(CyNetworkView networkView, Object arg1,
				Set<View<CyNode>> arg2, String arg3) {
			return tf.createTaskIterator(networkView);
		}

	}

}
