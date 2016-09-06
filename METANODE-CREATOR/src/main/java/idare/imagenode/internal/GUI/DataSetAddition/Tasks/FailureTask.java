package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class FailureTask extends AbstractTask {

	DataSetReadingInfo dsri;
	
	public FailureTask(DataSetReadingInfo dsri) {
		// TODO Auto-generated constructor stub
		this.dsri = dsri;
		
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
//		System.out.println("Finishing Tasks");
		if(!dsri.isDataSetAdded())
		{
			taskMonitor.setStatusMessage("Dataset addition failed");
			String ErrorMessage = "";
			for(String error : dsri.getErrorMessages())
			{
				ErrorMessage += error + "\n";
			}
			throw new Exception(ErrorMessage);

		}

	}

}
