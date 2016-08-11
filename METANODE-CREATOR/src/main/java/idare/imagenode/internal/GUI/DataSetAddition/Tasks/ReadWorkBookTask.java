package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSetReaders.IDAREWorkbook;
import idare.imagenode.internal.exceptions.io.WrongFormat;

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
		System.out.println("Trying to read the Workbook");
		try{
			if(reader.getStatusMessage() == IDAREDatasetReader.IS_SET_UP)
			{
				IDAREWorkbook wb = reader.readData(dsri.getInputFile());
				//We add a success message to the Error Stack for this reader, since now its just the DataSetParsing that can go wrong.
				dsri.addErrorMessage(reader.getClass().getSimpleName() + ": Workbook read successfully");
				insertTasksAfterCurrentTask(new AddDataSetToManagerTask(wb, dsri));
				
				System.out.println("Success");
			}
			else
			{
				System.out.println("Failed");
				throw new WrongFormat(reader.getClass().getSimpleName() + ": " + reader.getStatusMessage());
			}
		}
		catch(Exception e)
		{
			dsri.addErrorMessage(reader.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

}
