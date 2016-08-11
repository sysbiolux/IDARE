package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import idare.imagenode.Interfaces.DataSetReaders.IDAREWorkbook;
import idare.imagenode.internal.Debug.PrintFDebugger;

import org.cytoscape.work.TaskMonitor;

public class AddDataSetToManagerTask extends ObservableIDARETask {


	IDAREWorkbook wb;	
	DataSetReadingInfo dsri;
	
	public AddDataSetToManagerTask(IDAREWorkbook wb,DataSetReadingInfo dsri) {
		super();
		this.wb = wb;
		this.dsri = dsri;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Trying to Add DataSet");
		if(wb == null)
		{
			dsri.addErrorMessage("When trying to add Dataset: WorkBook was null");
		}
		else
		{			
			try{
				dsri.getDataSetManager().createDataSet(dsri.doUseTwoColumns(), dsri.getDataSetType(), dsri.getDataSetDescription(), wb);
				dsri.setDataSetAdded();
			}
			catch(Exception e)
			{
				dsri.addErrorMessage(dsri.getDataSetType() + ": " + e.getMessage() );
				e.printStackTrace(System.out);
			}
		}
	}

}
