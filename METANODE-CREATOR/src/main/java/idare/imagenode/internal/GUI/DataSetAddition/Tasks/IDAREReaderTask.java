package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import java.io.File;
import java.util.Stack;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSetReaders.IDARETask;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.Debug.PrintFDebugger;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class IDAREReaderTask extends IDARETask {

	IDAREDatasetReader reader;
	boolean twocolumn; 
	File inputFile;

	
	public IDAREReaderTask(IDAREDatasetReader reader,
			boolean twocolumn, File inputFile) {
		super();
		this.reader = reader;
		this.twocolumn = twocolumn;
		this.inputFile = inputFile;
	}

	@Override
	public void execute(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		if(reader.getStatus() == IDAREDatasetReader.IS_READY)
		{
			IDARETask setupTask = null;
			setupTask = reader.getSetupTask(inputFile, twocolumn);
			if(setupTask != null)
			{
				PrintFDebugger.Debugging(this, "Adding a new SetupTask before the ReadWorkBookTask");
				this.insertTasksAfterCurrentTask(new TaskIterator(setupTask, new ReadWorkBookTask(inputFile,reader)));
			}
			else
			{
				this.insertTasksAfterCurrentTask(new ReadWorkBookTask(inputFile, reader));				
			}
			
		}
		else
		{
			throw new RuntimeException(reader.getClass().getSimpleName() + ": not ready. Skipping");
		}
	}




}
