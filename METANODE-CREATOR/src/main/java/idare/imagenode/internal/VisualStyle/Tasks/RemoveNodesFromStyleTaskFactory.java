package idare.imagenode.internal.VisualStyle.Tasks;

import idare.imagenode.internal.VisualStyle.StyleManager;

import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
/**
 * A TaskFactory that adds nodes to Styles.
 * @author Thomas Pfau
 *
 */
public class RemoveNodesFromStyleTaskFactory extends AbstractTaskFactory implements NetworkViewTaskFactory{

	private StyleManager mgr;
	private DialogTaskManager dtm;
	
	public RemoveNodesFromStyleTaskFactory(StyleManager mgr, DialogTaskManager dtm) {
		// TODO Auto-generated constructor stub
		this.mgr = mgr;
		this.dtm = dtm;
	}
	@Override
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return new TaskIterator(new RemoveNodesToStyleTask(mgr));
	}
	
	/**
	 * Add Nodes to the current style
	 */
	public void removeNodes()
	{
		dtm.execute(createTaskIterator());
	}
	/**
	 * The Actual Task to remove the nodes.
	 * @author Thomas Pfau
	 *
	 */
	private class RemoveNodesToStyleTask extends AbstractTask
	{
		private StyleManager mgr;
		
		public RemoveNodesToStyleTask(StyleManager mgr) {
			// TODO Auto-generated constructor stub
			this.mgr = mgr;
		}
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			// TODO Auto-generated method stub
			taskMonitor.setTitle("Removing Images from Style");
			taskMonitor.setProgress(0.0);			
			mgr.removeNodes(taskMonitor);
			taskMonitor.setProgress(1.0);
		
			
		}
		
	}
	@Override
	public TaskIterator createTaskIterator(CyNetworkView arg0) {		
		return createTaskIterator();
	}
	@Override
	public boolean isReady(CyNetworkView arg0) {

		return mgr.viewUsesStyleWithNodes(arg0);
	}

}
