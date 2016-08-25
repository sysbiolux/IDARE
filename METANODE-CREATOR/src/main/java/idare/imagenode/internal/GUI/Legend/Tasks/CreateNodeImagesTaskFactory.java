package idare.imagenode.internal.GUI.Legend.Tasks;

import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.GUI.Legend.IDARELegend;

import java.io.File;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class CreateNodeImagesTaskFactory extends AbstractTaskFactory implements NetworkViewTaskFactory{

	IDARELegend legend;
	FileUtil util;
	NodeManager manager;
	CySwingApplication cySwingApp;
	
	public CreateNodeImagesTaskFactory(FileUtil util, IDARELegend legend, NodeManager manager, CySwingApplication cySwingApp) {
		// TODO Auto-generated constructor stub
		this.manager = manager;
		this.legend = legend;
		this.util = util;		
		this.cySwingApp = cySwingApp; 
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		Vector<FileChooserFilter> filter = new Vector<FileChooserFilter>();
		filter.add(new FileChooserFilter("Zip Files", "zip"));		
		Object[] options = new Options[]{new Options("svg","SVG Images"), new Options("png", "PNG Images")};
		//get the selected option usestring.
		//taskMonitor.setStatusMessage("Selecting Options );
		String selection = ((Options)(JOptionPane.showInputDialog(null, "Select the File Format to save in.",
				"File Format selection", JOptionPane.PLAIN_MESSAGE, null, options, options[0]))).getUseString();
		File f = util.getFile(cySwingApp.getJFrame(),"Select File to save the Nodes:",util.SAVE,filter);
		
		
		return new TaskIterator(new CreateNodeImageTask(legend, f, manager, selection));
	}

	@Override
	public boolean isReady() {			
		return legend.isActive();
	}
	
	/**
	 *  A Small Helper Class to combine Display and usage Strings.
	 * @author Thomas Pfau
	 *
	 */
	private class Options
	{
		String Displaystring;
		String usestring;
		
		Options(String useString, String DisplayString)
		{
			this.Displaystring = DisplayString;
			this.usestring = useString;
		}
		/**
		 * Get the string to use  for this option.
		 * @return the string used to generate the displaystring
		 */
		public String getUseString()
		{
			return usestring;
		}
		/**
		 * Get the Display String for this option
		 * @return The Displaystring associated with the useString
		 */
		public String toString()
		{
			return Displaystring;
		}
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView arg0) {
		return createTaskIterator();
	}

	@Override
	public boolean isReady(CyNetworkView arg0) {
		return isReady();
	}
	
}
