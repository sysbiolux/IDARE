package idare.metanode.internal;


import idare.Properties.IDARESettingsManager;
import idare.metanode.IDAREMetaNodeAppService;
import idare.metanode.internal.DataManagement.DataSetProvider;
import idare.metanode.internal.DataSetReaders.DataSetReaderProvider;
import idare.metanode.internal.Debug.PrintFDebugger;
import idare.metanode.internal.GUI.DataSetController.DataSetControlPanel;
import idare.metanode.internal.GUI.Legend.IDARELegend;
import idare.metanode.internal.GUI.Legend.LegendUpdater;
import idare.metanode.internal.GUI.NetworkSetup.NetworkSetup;
import idare.metanode.internal.Services.JSBML.SBMLServiceRegistrar;
import idare.sbmlannotator.internal.SBMLAnnotationTaskFactory;
import idare.subsystems.internal.NetworkViewSwitcher;
import idare.subsystems.internal.SubNetworkCreator;
import idare.subsystems.internal.SubSystemsSaver;

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
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	IDAREMetaNodeApp app;
	CyServiceRegistrar reg;
	BundleContext appcontext;
	SBMLServiceRegistrar SBMLReg;
	
	@Override
	public void start(BundleContext context) throws Exception {		
		//System.out.println("-----------------------------------------------------------------------------------");
		//Calendar cal = Calendar.getInstance();
		//SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		//System.out.println( sdf.format(cal.getTime()) );
		appcontext = context;
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
		setupMetanodeApp(context);
		setupNetworkCreatorApp(context);
		registerSBMLAnnotator(context);
		
	}

	private void setupMetanodeApp(BundleContext context)
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
		app = new IDAREMetaNodeApp(cySwingApp,currentLexicon,util,vSFSR,vmm,vmfFactoryD,vmfFactoryP,eventHelper,nvm,cyAppMgr,networkManager,dialogTaskManager);		
		app.registerPlugin(new DataSetReaderProvider());
		app.registerPlugin(new DataSetProvider());
		IDAREMetaNodeAppService appService = new IDAREMetaNodeAppService(app);
		NetworkSetup nsa = new NetworkSetup(cyAppMgr, cySwingApp, app.getSettingsManager(),app.getNodeManager());
		IDARELegend pan = app.getLegend();
		LegendUpdater up = new LegendUpdater(pan, app.getNodeManager(), cyAppMgr,vmm, app.getStyleManager());		
		up.activate();
		DataSetControlPanel dcp = app.getDataSetPanel();
		//MenuAction action = new MenuAction(cyAppMgr, "Manage datasets for image nodes",app,cySwingApp,util,pan);
		//ImageCreator painter = new ImageCreator(app.getLegend(), cyAppMgr, util, cySwingApp, app.getNodeManager());
		//AddNodesToStyleAction addnodes = new AddNodesToStyleAction(cyAppMgr, app.getStyleManager());
		//RemoveNodesFromStyleAction removenodes = new RemoveNodesFromStyleAction(cyAppMgr, app.getStyleManager());
		//registerAllServices(context, painter, new Properties());
		//Start up the Menuaction...
		//action.actionPerformed(new ActionEvent(this, 1, "1"));

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
					PrintFDebugger.Debugging(this, "Registering Service for class " + ((Class)prop.get("USE_CLASS")).getName());					
					registerService(context, fac, (Class)prop.get("USE_CLASS"), prop);
				}
				else
				{
					registerAllServices(context, fac, prop);	
				}
			}
		}
		//registerAllServices(context, addnodes, new Properties());
		//registerAllServices(context, removenodes, new Properties());
		registerAllServices(context, app.getStyleManager(), new Properties());
		//DataSetManager.clearTemporaryFolder();
		//Register the Legend Panel and its updater.
		registerAllServices(context, pan, new Properties());
		registerAllServices(context, up, new Properties());
		//Register the app as its own service and load and save listener.
		registerService(context, app, SessionAboutToBeSavedListener.class, new Properties());
		registerService(context, app, SessionLoadedListener.class, new Properties());
		registerService(context, appService, IDAREMetaNodeAppService.class, new Properties());

		//Register the visual style
		registerAllServices(context, app.getVisualStyle(), new Properties());
		//Register the DatasetControlPanel
		registerAllServices(context, dcp, new Properties());
		//Register the Network Setup Menu item.
		registerAllServices(context, nsa, new Properties());
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
		SBMLAnnotationTaskFactory Annotator = new SBMLAnnotationTaskFactory(cyApplicationManager, eventHelper, FileUtilService, cySwingApp, SBMLReg.getHolder());
		registerService(context, Annotator, NetworkViewTaskFactory.class, addAnnotationPropertiesMenu);
		registerService(context, Annotator, NetworkViewTaskFactory.class, addAnnotationPropertiesTask);
	
//		try{
//			SBMLManager mgr = getService(context, SBMLManager.class);
//			Annotator.setSBMLManager(mgr);
//		}
//		catch(NoClassDefFoundError|RuntimeException e)
//		{
//			PrintFDebugger.Debugging(this, "CySBML not available, using non cysbml version");				
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
			reg.unregisterService(app, IDAREMetaNodeApp.class);
		}
		catch(Exception e)
		{
			PrintFDebugger.Debugging(this, "Error during shutdown\n" + e.getMessage());
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
