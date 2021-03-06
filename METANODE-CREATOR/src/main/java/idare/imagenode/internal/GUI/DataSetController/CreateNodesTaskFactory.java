package idare.imagenode.internal.GUI.DataSetController;

import java.util.Set;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.swing.DialogTaskManager;

import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.imagenode.exceptions.layout.DimensionMismatchException;
import idare.imagenode.exceptions.layout.TooManyItemsException;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Layout.ImageNodeLayout;
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
	 * @param nodeManager the nodeManager for this factory
	 * @param builder the control panel builder to use
	 * @param dtm the Dialaogtaskmanager to use
	 */
	public CreateNodesTaskFactory(NodeManager nodeManager,DataSetControlPanel builder, DialogTaskManager dtm) {
		this.builder = builder;
		this.nodeManager = nodeManager;
		this.dtm = dtm;
	}
	
	/**
	 * Run the task factory.
	 * @param DataSetsToLayout The Datasets to layout
	 * @param layout The node layout.
	 */
	public void run(Set<DataSet> DataSetsToLayout, ImageNodeLayout layout)
	{
		dtm.execute(new TaskIterator(new CreateNodesTask(nodeManager, DataSetsToLayout, layout)));
	}
	@Override
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return new TaskIterator();
	}
	
	/**
	 * The Task to use for Creating Nodes
	 * @author Thomas Pfau
	 *
	 */
	private class CreateNodesTask extends AbstractTask
	{
		NodeManager nodeManager;
		ImageNodeLayout layout;
		Set<DataSet> setsToLayout;
		/**
		 * A Default constructor that adds the given {@link ImageNodeLayout} to all Nodes in the network also present in the provided {@link DataSet}s according to the {@link NodeManager}.
		 * @param nodeManager
		 * @param SetsToLayout
		 * @param layout
		 */
		  
		public CreateNodesTask(NodeManager nodeManager,Set<DataSet> SetsToLayout, ImageNodeLayout layout ) {
			this.nodeManager = nodeManager;
			this.setsToLayout = SetsToLayout;
			this.layout = layout;			
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
				if(builder.dssm.getSelectedDataSets().size() > 0)
				{
					nodeManager.generateLayoutsForNodes(setsToLayout,layout,arg0);
				}
				else
				{
						arg0.showMessage(Level.WARN, "Please select at least one Dataset for layouting");
						arg0.setStatusMessage("No Datasets selected for layout generation");
						arg0.setProgress(1.0);
						return;
				}

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
