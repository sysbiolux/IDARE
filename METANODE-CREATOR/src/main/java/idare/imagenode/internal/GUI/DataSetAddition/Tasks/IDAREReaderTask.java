package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import java.io.File;
import java.util.Stack;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSetReaders.IDAREReaderSetupTask;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.Debug.PrintFDebugger;

import org.cytoscape.work.AbstractTask;
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
			return;
		}
		if(reader.getStatusMessage() == IDAREDatasetReader.IS_READY)
		{
			IDAREReaderSetupTask setupTask = null;
			try{
				setupTask = reader.getSetupTask(dsri.getInputFile(), dsri.doUseTwoColumns());
			}
			catch(Exception e)
			{
				dsri.addErrorMessage(reader.getClass().getSimpleName() + ": " + e.getMessage());
				return;
			}
			if(setupTask != null)
			{
				PrintFDebugger.Debugging(this, "Adding a new SetupTask before the ReadWorkBookTask");
				this.insertTasksAfterCurrentTask(new TaskIterator(setupTask, new ReadWorkBookTask(reader,dsri)));
			}
			else
			{
				//if we do not have a Setup Task, it is obviously not necessary, so the reader is set up and ready.
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
