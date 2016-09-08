package idare.subnetwork.internal;

import idare.Properties.IDARESettingsManager;

import java.util.Set;
import java.util.Vector;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;


/**
 * Task Factory for Subnetwork Creation
 * @author Thomas Pfau
 *
 */
public class SubNetworkCreatorTaskFactory extends AbstractTaskFactory{

	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkViewFactory networkViewFactory;
	private final CyEventHelper eventHelper;
	private final CyApplicationManager applicationManager;
	private final CyNetworkManager networkManager;
	private CyLayoutAlgorithm layout;
	private final VisualMappingManager vmm;
	private final CyRootNetworkManager rootManager;
	
	private final IDARESettingsManager ism;
	private final NetworkViewSwitcher nvs;	
	
	private String subNetworkIDColumnName;
	private final Vector<Object> subSystemsToDo;
	private final Set<CyNode> ignoredNodes;
	private final Set<CyNode> noBranchNodes;

	/**
	 * Default Constructor for the Task Factory
	 * @param networkViewManager
	 * @param networkViewFactory
	 * @param eventHelper
	 * @param applicationManager
	 * @param networkManager
	 * @param layout
	 * @param ColumnName
	 * @param vmm
	 * @param nvs
	 * @param subSystemsToDo
	 * @param ignoredNodes
	 * @param noBranchNodes
	 */
	public SubNetworkCreatorTaskFactory(
			CyRootNetworkManager rootManager, CyNetworkViewManager networkViewManager,
			CyNetworkViewFactory networkViewFactory, CyEventHelper eventHelper,
			CyApplicationManager applicationManager, CyNetworkManager networkManager, CyLayoutAlgorithm layout,
			String ColumnName, VisualMappingManager vmm, NetworkViewSwitcher nvs, Vector<Object> subSystemsToDo,
			Set<CyNode> ignoredNodes,Set<CyNode> noBranchNodes, IDARESettingsManager ism) {
		super();
		this.networkViewManager = networkViewManager;
		this.networkViewFactory = networkViewFactory;
		this.eventHelper = eventHelper;
		this.applicationManager = applicationManager;
		this.networkManager = networkManager;
		this.layout = layout;
		this.subNetworkIDColumnName = ColumnName;
		this.vmm = vmm;
		this.nvs = nvs;
		this.subSystemsToDo = subSystemsToDo;
		this.ignoredNodes = ignoredNodes;
		this.noBranchNodes = noBranchNodes;
		this.rootManager = rootManager;
		this.ism = ism;
	}

	@Override
	public TaskIterator createTaskIterator() {
		SubNetworkCreationTask task = new SubNetworkCreationTask(rootManager,networkViewManager,networkViewFactory,eventHelper
				,applicationManager,networkManager, layout,subNetworkIDColumnName,vmm,nvs,subSystemsToDo,ignoredNodes,noBranchNodes, ism);
		return new TaskIterator(task);
	}
	/**
	 * Set the layout to use for all {@link SubNetworkCreationTask}s
	 * @param Layout - the {@link CyLayoutAlgorithm} to use for subsystem layout
	 */
	public void setLayout(CyLayoutAlgorithm Layout)
	{
		layout = Layout;
	}
	/**
	 * Set the column to use as Subnetwork Defining Column String
	 * @param col - The Column Identifier of the column containing the subnetwork IDs
	 */
	public void setColumnName(String col)
	{
		subNetworkIDColumnName = col;
	}

}
