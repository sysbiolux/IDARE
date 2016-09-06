package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook;
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
		taskMonitor.setStatusMessage("Trying to convert Workbook to Dataset");
		PrintFDebugger.Debugging(this, "Running");
		if(wb == null)
		{
			PrintFDebugger.Debugging(this, "Workbook was null");
			taskMonitor.setStatusMessage("Failed because Workook was null");
			dsri.addErrorMessage("When trying to add Dataset: WorkBook was null");
		}
		else
		{			
			try{
				
				PrintFDebugger.Debugging(this, "Creating Dataset");
				dsri.getDataSetManager().createDataSet(dsri.doUseTwoColumns(), dsri.getDataSetType(), dsri.getDataSetDescription(), wb);
				taskMonitor.setStatusMessage("Added DataSet to IDARE");
				PrintFDebugger.Debugging(this, "Updating dsri");
				dsri.setDataSetAdded();
			}
			catch(Exception e)
			{
				taskMonitor.setStatusMessage("DataSet addition failed, trying further readers");
				PrintFDebugger.Debugging(this, "Caught an error");
				dsri.addErrorMessage(dsri.getDataSetType() + ": " + e.getMessage() );
				e.printStackTrace(System.out);
			}
		}
	}

}
