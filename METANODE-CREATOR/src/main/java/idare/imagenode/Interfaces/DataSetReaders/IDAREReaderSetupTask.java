package idare.imagenode.Interfaces.DataSetReaders;


import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/**
 * An Abstract Basis Class that defines a Task that is necessary to set up a reader for dataset parsing.
 * The reader can use this Task to obtain a set of {@link Tunable}s that can be defined in this Task if the corresponding TunableHandler is defined.
 * This Task should NOT add additional Tasks
 * @author Thomas Pfau
 *
 */
public abstract class IDAREReaderSetupTask implements Task{

	/**
	 * The reader used in this Task.
	 */
	final IDAREDatasetReader reader;
	
	/**
	 * Default constructor using the reader this Task is supposed to set up.
	 * @param reader
	 */
	public IDAREReaderSetupTask(IDAREDatasetReader reader) {
		this.reader = reader;
	}
	/**
	 * If true, we should stop execution as soon as possible.
	 */
	private volatile boolean cancelled = false;

	/**
	 * Execute the Task.
	 * @param taskMonitor the TaskMonitor this Task can use.
	 * @throws Exception 
	 */
	public abstract void execute(TaskMonitor taskMonitor) throws Exception;


	
	/**
	 * Runs the Task specified by the execute method and catches any exception, converting the exception into a status message.
	 * Also updates the statusMessage to indicate the reader is ready if the Task completes without errors.  
	 */
	@Override
	public final void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		if(!cancelled)
		{
			try{
				execute(taskMonitor);
				if(cancelled)
				{
					reader.setStatusMessage("Execution Cancelled");
					return;
				}
				reader.setStatusMessage(IDAREDatasetReader.IS_SET_UP);				
			}
			catch(Exception e)
			{
				reader.setStatusMessage(e.getMessage());				
			}
		}
		else
		{
			reader.setStatusMessage("Execution Cancelled");
			return;
		}
	}

	@Override
	public void cancel()
	{
		cancelled = true;
	}

}
