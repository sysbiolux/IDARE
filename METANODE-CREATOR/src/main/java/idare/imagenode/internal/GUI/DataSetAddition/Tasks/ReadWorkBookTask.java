package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook;
import idare.imagenode.exceptions.io.WrongFormat;
import idare.imagenode.internal.Debug.PrintFDebugger;

import org.cytoscape.work.TaskMonitor;

public class ReadWorkBookTask extends ObservableIDARETask{


	IDAREDatasetReader reader;
	DataSetReadingInfo dsri;
	
	public ReadWorkBookTask(IDAREDatasetReader reader, DataSetReadingInfo dsri) {
		this.reader = reader;
		this.dsri = dsri;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Reading DataSet into Workbook Structure with " + reader.getClass().getSimpleName());
		try{
			if(reader.getStatusMessage() == IDAREDatasetReader.IS_SET_UP)
			{				
				PrintFDebugger.Debugging(this, "Reading Workbook");
				IDAREWorkbook wb = reader.readData(dsri.getInputFile());				
				//We add a success message to the Error Stack for this reader, since now its just the DataSetParsing that can go wrong.
				dsri.addErrorMessage(reader.getClass().getSimpleName() + ": Workbook read successfully");
				PrintFDebugger.Debugging(this, "Adding AddDataSetToManagerTask");
				insertTasksAfterCurrentTask(new AddDataSetToManagerTask(wb, dsri));
				
//				System.out.println("Success");
			}
			else
			{
//				System.out.println("Failed");
				PrintFDebugger.Debugging(this, "Reader not ready");
				throw new WrongFormat(reader.getClass().getSimpleName() + ": " + reader.getStatusMessage());
			}
		}
		catch(Exception e)
		{
			PrintFDebugger.Debugging(this, "Caught an error");
			e.printStackTrace(System.out);
			dsri.addErrorMessage(reader.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

}
