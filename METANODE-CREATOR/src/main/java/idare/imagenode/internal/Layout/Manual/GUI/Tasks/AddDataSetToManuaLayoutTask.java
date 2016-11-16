package idare.imagenode.internal.Layout.Manual.GUI.Tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;
import idare.imagenode.internal.Layout.Manual.LayoutGUI;

public class AddDataSetToManuaLayoutTask extends AbstractTask{

	@Tunable
	DataSetLayoutInfoBundle layoutinfo;

	LayoutGUI gui;
	
	public AddDataSetToManuaLayoutTask(LayoutGUI gui) {
		// TODO Auto-generated constructor stub
		this.gui = gui;
	}
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		//gui.createFrame(layoutinfo.dataset, layoutinfo.props, layoutinfo.map);
	}
	
	
	
}
