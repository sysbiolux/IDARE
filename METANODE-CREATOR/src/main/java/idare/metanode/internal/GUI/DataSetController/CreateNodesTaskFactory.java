package idare.metanode.internal.GUI.DataSetController;

import idare.metanode.internal.DataManagement.NodeManager;
import idare.metanode.internal.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.metanode.internal.exceptions.layout.DimensionMismatchException;
import idare.metanode.internal.exceptions.layout.TooManyItemsException;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.swing.DialogTaskManager;
/**
 * A {@link TaskFactory} that initializes the generation of Imagenodes.
 * @author Thomas Pfau
 *
 */
public class CreateNodesTaskFactory extends AbstractTaskFactory{

	
	NodeManager nodeManager;
	DataSetControlPanel builder;
	DialogTaskManager dtm;
	/**
	 * Default constructor with a {@link TaskManager} that can handle the created tasks, the {@link NodeManager} to generate the Layouts and 
	 * the {@link DataSetControlPanel} to obtain the layoutinformation from.
	 * @param nodeManager
	 * @param builder
	 * @param dtm
	 */
	public CreateNodesTaskFactory(NodeManager nodeManager,DataSetControlPanel builder, DialogTaskManager dtm) {
		this.builder = builder;
		this.nodeManager = nodeManager;
		this.dtm = dtm;
	}
	
	/**
	 * Execute the Task generated by this TaskFactory.
	 */
	public void run()
	{
		dtm.execute(createTaskIterator());
	}
	@Override
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return new TaskIterator(new CreateNodesTask(nodeManager, builder));
	}
	
	/**
	 * The Task to use for Creating Nodes
	 * @author Thomas Pfau
	 *
	 */
	private class CreateNodesTask extends AbstractTask
	{
		NodeManager nodeManager;
		DataSetControlPanel builder;
		/**
		 * Default constructor for the task with {@link NodeManager} and {@link DataSetControlPanel}.
		 * @param nodeManager
		 * @param builder
		 */
		public CreateNodesTask(NodeManager nodeManager,DataSetControlPanel builder) {
			this.builder = builder;
			this.nodeManager = nodeManager;
		}
		@Override
		public void run(TaskMonitor arg0) throws Exception {
			// TODO Auto-generated method stub
			try{
				arg0.setTitle("Creating Image Nodes");
				arg0.setProgress(0.);
				arg0.setStatusMessage("Setting DataSetProperties");
				builder.dssm.setDataSetProperties();				
				arg0.setProgress(0.1);
				arg0.setStatusMessage("Creating Layouts");
				nodeManager.generateLayoutsForNodes(builder.dssm.getSelectedDataSets(),arg0);
				arg0.setStatusMessage("Layouts Created");
				arg0.setProgress(1.0);
				if(builder.PreviewFrame != null)
				{
					builder.PreviewFrame.dispose();
				}
				if(builder.PreviewLegendFrame != null)
				{
					builder.PreviewLegendFrame.dispose();
				}							

			}
			catch(TooManyItemsException ex)
			{
				arg0.showMessage(Level.ERROR, ex.getMessage());
			}
			catch(ContainerUnplaceableExcpetion ex)
			{
				arg0.showMessage(Level.ERROR, ex.getMessage());
			}
			catch(DimensionMismatchException ex)
			{
				arg0.showMessage(Level.ERROR, ex.getMessage());
			}
		}
		
	}
}	
