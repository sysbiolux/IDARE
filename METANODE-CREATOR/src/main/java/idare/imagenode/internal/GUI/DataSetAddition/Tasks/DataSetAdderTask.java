package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.GUI.DataSetAddition.DataSetGenerationParameters;

import java.util.Iterator;
import java.util.Vector;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.RequestsUIHelper;
import org.cytoscape.work.swing.TunableUIHelper;

public class DataSetAdderTask extends AbstractTask implements RequestsUIHelper, TaskObserver
{
	@Tunable
	public DataSetGenerationParameters params;
	
	TaskIterator readerTasks;
	Vector<String> errors = new Vector<String>();
	private DataSetManager dsm;
	DataSetReadingInfo dsri;
	boolean datasetadded = false;
	private Iterator<IDAREDatasetReader> readeriter;
	public DataSetAdderTask(DataSetManager dsm) {
		this.dsm = dsm;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Setting up DataSet Addition");
		readerTasks = new TaskIterator();
		dsri = new DataSetReadingInfo(dsm,params);
		readeriter = dsm.getAvailableReaders().iterator();
		addReaderTask();
		readerTasks.append(new FailureTask(dsri));		
		insertTasksAfterCurrentTask(readerTasks);
	}


	private void addReaderTask()
	{
		while(readeriter.hasNext())
		{
			
			IDAREDatasetReader reader = readeriter.next();
//			System.out.println("Looping over readers. Currentreader is " + reader.getClass().getSimpleName());
			if(reader.fileTypeAccepted(params.inputFile))
			{
				System.out.println("Adding Reader task for reader " + reader.getClass().getSimpleName());
				readerTasks.append(new IDAREReaderTask(reader,dsri));
			}
			else
			{
				dsri.addErrorMessage(reader.getClass().getSimpleName() + ": File extension not accepted"); 
			}
		}		
		
	}

	@Override
	public void setUIHelper(TunableUIHelper arg0) {
		// TODO Auto-generated method stub
		PrintFDebugger.Debugging(this,"Setting UIHelper");
	}
	@Override
	public void taskFinished(ObservableTask task) {
		// TODO Auto-generated method stub
		System.out.println("Finshed a " + task.getClass().getSimpleName() + " !!");
	}
	@Override
	public void allFinished(FinishStatus finishStatus) {
//		System.out.println("Everything is finished.");
		for(IDAREDatasetReader reader : dsm.getAvailableReaders())
		{
			reader.reset();
		}
	}
}

