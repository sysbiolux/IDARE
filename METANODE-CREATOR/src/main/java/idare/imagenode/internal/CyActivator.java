package idare.imagenode.internal;


import idare.Properties.IDARESettingsManager;
import idare.imagenode.IDAREImageNodeAppService;
import idare.imagenode.internal.DataManagement.DataSetProvider;
import idare.imagenode.internal.DataSetReaders.DataSetReaderProvider;
import idare.imagenode.internal.GUI.DataSetAddition.DataSetParametersGUIHandlerFactory;
import idare.imagenode.internal.GUI.DataSetController.DataSetControlPanel;
import idare.imagenode.internal.GUI.Legend.IDARELegend;
import idare.imagenode.internal.GUI.Legend.LegendUpdater;
import idare.imagenode.internal.GUI.NetworkSetup.Tasks.NetworkSetupGUIHandlerFactory;
import idare.imagenode.internal.Services.JSBML.SBMLServiceRegistrar;
import idare.sbmlannotator.internal.SBMLAnnotationTaskFactory;
import idare.subsystems.internal.NetworkViewSwitcher;
import idare.subsystems.internal.SubNetworkCreator;
import idare.subsystems.internal.SubSystemsSaver;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.ActionEnableSupport;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	IDAREImageNodeApp app;
	CyServiceRegistrar reg;
	BundleContext appcontext;
	SBMLServiceRegistrar SBMLReg;
	
	@Override
	public void start(BundleContext context) throws Exception {
		
		appcontext = context;
		CyApplicationConfiguration configuration = getService(context, CyApplicationConfiguration.class);
		File cyDirectory = configuration.getConfigurationDirectoryLocation();

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

	private void setupimagenodeApp(BundleContext context)
	{
		//Obtain all services required for the app.
		CyApplicationManager cyAppMgr = getService(context, CyApplicationManager.class);	
		CyNetworkViewManager nvm = getService(context, CyNetworkViewManager.class);
		CyEventHelper eventHelper = getService(context, CyEventHelper.class);
		VisualStyleFactory vSFSR = getService(context,VisualStyleFactory.class);
		VisualMappingManager vmm = getService(context, VisualMappingManager.class);
		FileUtil util = getService(context, FileUtil.class);
		VisualMappingFunctionFactory vmfFactoryD = getService(context,VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		VisualMappingFunctionFactory vmfFactoryP = getService(context,VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
		CySwingApplication cySwingApp = getService(context, CySwingApplication.class);
		VisualLexicon currentLexicon = getService(context,RenderingEngineManager.class).getDefaultVisualLexicon();
		CyNetworkManager networkManager = getService(context, CyNetworkManager.class);
		DialogTaskManager dialogTaskManager = getService(context, DialogTaskManager.class);    
		

		//initialize and register the app components.
		app = new IDAREImageNodeApp(cySwingApp,currentLexicon,util,vSFSR,vmm,vmfFactoryD,vmfFactoryP,eventHelper,nvm,cyAppMgr,networkManager,dialogTaskManager);		
		app.registerPlugin(new DataSetReaderProvider());
		app.registerPlugin(new DataSetProvider());				
		
		//Generate the Externally available sErvice
		IDAREImageNodeAppService appService = new IDAREImageNodeAppService(app);
		
		//NetworkSetup nsa = new NetworkSetup(cyAppMgr, cySwingApp, app.getSettingsManager(),app.getNodeManager());
		
		//Set up the Legend Panel
		IDARELegend pan = app.getLegend();
		LegendUpdater up = new LegendUpdater(pan, app.getNodeManager(), cyAppMgr,vmm, app.getStyleManager());		
		up.activate();
		DataSetControlPanel dcp = app.getDataSetPanel();	
		
		//Generate he TunableHandlers
		DataSetParametersGUIHandlerFactory dsctf = new DataSetParametersGUIHandlerFactory(util,app.getDatasetManager());		
		NetworkSetupGUIHandlerFactory nsghf = new NetworkSetupGUIHandlerFactory(app.getNodeManager(), app.getSettingsManager(), cyAppMgr);
		
		//Register the Actions of the App.
		for(CyAction cyAct : app.getActions())
		{
			registerAllServices(context, cyAct, new Properties());
		}
		// Register the TaskFactories of the App.
		HashMap<AbstractTaskFactory, Vector<Properties>> facs = app.getFactories();
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
		registerAllServices(context, app.getStyleManager(), new Properties());
		registerAllServices(context, pan, new Properties());
		registerAllServices(context, up, new Properties());
		//Register the tunable Handlers
		registerService(context, nsghf, GUITunableHandlerFactory.class, new Properties());
		registerService(context, dsctf, GUITunableHandlerFactory.class, new Properties());

		//Register the app as its own service and load and save listener.
		registerService(context, app, SessionAboutToBeSavedListener.class, new Properties());
		registerService(context, app, SessionLoadedListener.class, new Properties());
		registerService(context, appService, IDAREImageNodeAppService.class, new Properties());

		//Register the visual style
		registerAllServices(context, app.getVisualStyle(), new Properties());
		//Register the DatasetControlPanel
		registerAllServices(context, dcp, new Properties());
		//Register the Network Setup Menu item.
		//registerAllServices(context, nsa, new Properties());
	}	
	private void registerSBMLAnnotator(BundleContext context)
	{
		CyApplicationManager cyApplicationManager = getService(context, CyApplicationManager.class);
		FileUtil FileUtilService = getService(context,FileUtil.class);		
		CyEventHelper eventHelper = getService(context, CyEventHelper.class);
		CySwingApplication cySwingApp = getService(context, CySwingApplication.class);
		
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


		//SBMLAnnotationReader action2 = new SBMLAnnotationReader(cyApplicationManager, "Add SBML Notes",dialogTaskManager,
		//		cytoscapePropertiesServiceRef,eventHelper,FileUtilService,cySwingApp);
		SBMLReg = new SBMLServiceRegistrar(context,FileUtilService, cySwingApp);
		context.addServiceListener(SBMLReg);
		SBMLAnnotationTaskFactory Annotator = new SBMLAnnotationTaskFactory(cyApplicationManager, eventHelper, FileUtilService, cySwingApp, SBMLReg.getHolder(), app);
		registerService(context, Annotator, NetworkViewTaskFactory.class, addAnnotationPropertiesMenu);
		registerService(context, Annotator, NetworkViewTaskFactory.class, addAnnotationPropertiesTask);
	
//		try{
//			SBMLManager mgr = getService(context, SBMLManager.class);
//			Annotator.setSBMLManager(mgr);
//		}
//		catch(NoClassDefFoundError|RuntimeException e)
//		{
//		}		
//
		//SBMLAnnotationTaskFactory Annotator = new SBMLAnnotationTaskFactory(cyApplicationManager, eventHelper, FileUtilService, cySwingApp);
		//registerService(context, Annotator, NetworkViewTaskFactory.class, addAnnotationPropertiesMenu);
		//registerService(context, Annotator, NetworkViewTaskFactory.class, addAnnotationPropertiesTask);
		//Properties properties = new Properties();					
		//registerAllServices(context, action2, properties);
		//registerServiceListener(context, cySBMLlistener, "serviceRegistered", "serviceUnRegistered", Object.class);
	}

	private void setupNetworkCreatorApp(BundleContext context)
	{

		//Obtain all services required for the app.
		DialogTaskManager dialogTaskManager = getService(context, DialogTaskManager.class);    
		CyApplicationManager cyApplicationManager = getService(context, CyApplicationManager.class);	
		CyNetworkViewFactory networkViewFactory = getService(context, CyNetworkViewFactory.class);
		CyNetworkViewManager networkViewManager = getService(context, CyNetworkViewManager.class);
		CyEventHelper eventHelper = getService(context, CyEventHelper.class);
		CyNetworkFactory networkFactory = getService(context, CyNetworkFactory.class);
		CyNetworkManager networkManager = getService(context, CyNetworkManager.class);
		CyLayoutAlgorithmManager LayoutManager =  getService(context, CyLayoutAlgorithmManager.class);
		VisualMappingManager vmm = getService(context, VisualMappingManager.class);
		CySwingApplication cySwingApp = getService(context, CySwingApplication.class);
		CyRootNetworkManager rootManager = getService(context, CyRootNetworkManager.class);
		CyServiceRegistrar reg = getService(context, CyServiceRegistrar.class);

		//initialize and Register the App Components
		IDARESettingsManager iDAREIDMgr= app.getSettingsManager();
		NetworkViewSwitcher nvs = new NetworkViewSwitcher(reg);
		SubNetworkCreator snc = new SubNetworkCreator(rootManager, cyApplicationManager, "SubNetworkCreator", networkViewManager,
				networkViewFactory, eventHelper, networkFactory, networkManager,LayoutManager,dialogTaskManager,vmm, nvs,iDAREIDMgr,cySwingApp);
		SubSystemsSaver SubSysSave = new SubSystemsSaver(nvs, iDAREIDMgr);
		Properties properties = new Properties();
		Properties doubleClickProperties = new Properties();
		doubleClickProperties.setProperty(ServiceProperties.PREFERRED_ACTION, NetworkViewSwitcher.PREFERRED_OPTION);
		doubleClickProperties.setProperty(ServiceProperties.TITLE, "Switch To Network");
		//registerService(context,SubSysSave,SessionLoadedListener.class, new Properties());		
		registerAllServices(context, SubSysSave, new Properties());
		registerAllServices(context, snc, properties);

		registerService(context,nvs,NodeViewTaskFactory.class, doubleClickProperties);
		registerService(context,nvs,RowsSetListener.class, new Properties());
		registerService(context,nvs,NetworkAboutToBeDestroyedListener.class, doubleClickProperties);
		registerService(context,nvs,NetworkAddedListener.class, new Properties());
		registerService(context,nvs,NetworkViewAboutToBeDestroyedListener.class, doubleClickProperties);
		registerService(context,nvs,NetworkViewAddedListener.class, doubleClickProperties);
	}

	@Override
	public void shutDown()
	{
		super.shutDown();
		app.unregisterAll();		
		if(SBMLReg != null)
		{
			appcontext.removeServiceListener(SBMLReg);
		}
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
