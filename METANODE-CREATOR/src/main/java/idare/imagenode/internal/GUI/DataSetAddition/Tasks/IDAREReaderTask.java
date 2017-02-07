package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSetReaders.IDAREReaderSetupTask;
import idare.imagenode.internal.Debug.PrintFDebugger;

import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class IDAREReaderTask extends ObservableIDARETask {

	IDAREDatasetReader reader;
	DataSetReadingInfo dsri;
	
	public IDAREReaderTask(IDAREDatasetReader reader, DataSetReadingInfo dsri) {
		super();
		this.reader = reader;
		this.dsri = dsri;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		if(dsri.isDataSetAdded())
		{
//			PrintFDebugger.Debugging(this, "DataSet was already added. Returning");
			return;
		}
		if(reader.getStatusMessage() == IDAREDatasetReader.IS_READY)
		{
			taskMonitor.setStatusMessage("Initializing " + reader.getClass().getSimpleName());

			IDAREReaderSetupTask setupTask = null;
			try{
//				PrintFDebugger.Debugging(this, "Trying to obtain SetupTask");
				setupTask = reader.getSetupTask(dsri.getInputFile(), dsri.doUseTwoColumns());
			}
			catch(Exception e)
			{
//				PrintFDebugger.Debugging(this, "Couldn't obtain SetupTask");
				dsri.addErrorMessage(reader.getClass().getSimpleName() + ": " + e.getMessage());
				return;
			}
			if(setupTask != null)
			{				
//				PrintFDebugger.Debugging(this, "Adding a new SetupTask before the ReadWorkBookTask");
				this.insertTasksAfterCurrentTask(new TaskIterator(setupTask, new ReadWorkBookTask(reader,dsri)));
			}
			else
			{
				//if we do not have a Setup Task, it is obviously not necessary, so the reader is set up and ready.
//				PrintFDebugger.Debugging(this, "No SetupTask provided adding ReadWorkbookTask");
				reader.setStatusMessage(IDAREDatasetReader.IS_SET_UP);
				this.insertTasksAfterCurrentTask(new ReadWorkBookTask( reader,dsri));				
			}
			
		}
		else
		{
			throw new RuntimeException(reader.getClass().getSimpleName() + ": not ready. Skipping");
		}
	}




}
