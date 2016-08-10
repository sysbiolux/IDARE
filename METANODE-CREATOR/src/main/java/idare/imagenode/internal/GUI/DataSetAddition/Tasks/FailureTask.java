package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import java.util.Vector;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class FailureTask extends AbstractTask {

	String 	ErrorMessage;
	
	public FailureTask(Vector<String> errors) {
		// TODO Auto-generated constructor stub
		for(String error : errors)
		{
			ErrorMessage += error + "\n";			
		}
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		throw new Exception(ErrorMessage);
	}

}
