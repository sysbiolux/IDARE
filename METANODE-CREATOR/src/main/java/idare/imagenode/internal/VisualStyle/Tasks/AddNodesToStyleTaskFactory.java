package idare.imagenode.internal.VisualStyle.Tasks;

import idare.imagenode.internal.VisualStyle.StyleManager;

import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
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
public class AddNodesToStyleTaskFactory extends AbstractTaskFactory implements NetworkViewTaskFactory {

	private StyleManager mgr;
	private DialogTaskManager dtm;
	/**
	 * Default Constructor 
	 * @param mgr
	 * @param dtm
	 */
	public AddNodesToStyleTaskFactory(StyleManager mgr, DialogTaskManager dtm) {
		// TODO Auto-generated constructor stub
		this.mgr = mgr;
		this.dtm = dtm;
	}
	@Override
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return new TaskIterator(new AddNodesToCurrentStyleTask(mgr));
	}
	
	/**
	 * Add Nodes to the current style
	 */
	public void addNodes()
	{
		dtm.execute(createTaskIterator());
	}
	
	public void addNodesToStyle(VisualStyle style)
	{
		dtm.execute(new TaskIterator(new RestoreNodesToStyleTask(mgr,style)));
	}
	
	/**
	 * The Actual Task to add the nodes.
	 * @author Thomas Pfau
	 *
	 */
	private class RestoreNodesToStyleTask extends AbstractTask
	{
		private StyleManager mgr;
		private VisualStyle style;
		
		public RestoreNodesToStyleTask(StyleManager mgr, VisualStyle style) {
			// TODO Auto-generated constructor stub
			this.mgr = mgr;
			this.style = style;
		}
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			// TODO Auto-generated method stub
			taskMonitor.setTitle("Restoring Images to style " + style.getTitle());
			taskMonitor.setProgress(0.0);
			//taskMonitor.setStatusMessage("Adding Nodes to Style");
			mgr.addNodesToStyleDuringLoad(taskMonitor, style);
		
			
		}
		
	}

	
	/**
	 * The Actual Task to add the nodes.
	 * @author Thomas Pfau
	 *
	 */
	private class AddNodesToCurrentStyleTask extends AbstractTask
	{
		private StyleManager mgr;
		
		public AddNodesToCurrentStyleTask(StyleManager mgr) {
			// TODO Auto-generated constructor stub
			this.mgr = mgr;
		}
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			// TODO Auto-generated method stub
			taskMonitor.setTitle("Adding Images to Style");
			taskMonitor.setProgress(0.0);
			//taskMonitor.setStatusMessage("Adding Nodes to Style");
			mgr.addNodes(taskMonitor);
		
			
		}
		
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView arg0) {
		if(isReady())
			return createTaskIterator();
		else
			return null;
	}
	@Override
	public boolean isReady(CyNetworkView arg0) {			
		return !mgr.viewUsesStyleWithNodes(arg0);
	}

}
