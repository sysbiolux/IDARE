package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSetReaders.IDARETask.Status;
import idare.imagenode.Interfaces.DataSetReaders.IDAREWorkbook;
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
	IDAREWorkbook wb;
	boolean datasetadded = false;
	private Iterator<IDAREDatasetReader> readeriter;
	public DataSetAdderTask(DataSetManager dsm) {
		this.dsm = dsm;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Setting up DataSet Addition");
		readerTasks = new TaskIterator();		
		readeriter = dsm.getAvailableReaders().iterator();
		if(!addReaderTask())
		{
			readerTasks.append(new FailureTask(errors));
		}
		insertTasksAfterCurrentTask(readerTasks);
	}


	private boolean addReaderTask()
	{
		boolean readeradded = false;
		while(readeriter.hasNext())
		{
			
			IDAREDatasetReader reader = readeriter.next();
			System.out.println("Looping over readers. Currentreader is " + reader.getClass().getSimpleName());
			if(reader.fileTypeAccepted(params.inputFile))
			{
				System.out.println("Adding Reader task for reader " + reader.getClass().getSimpleName());
				readerTasks.append(new IDAREReaderTask(reader,params.useTwoColumns,params.inputFile));
				readeradded = true;
				break;
			}
			else
			{
				errors.add(reader.getClass().getSimpleName() + ": File extension not accepted"); 
			}
		}		
		return readeradded;
	}

	@Override
	public void setUIHelper(TunableUIHelper arg0) {
		// TODO Auto-generated method stub
		PrintFDebugger.Debugging(this,"Setting UIHelper");
	}
	@Override
	public void taskFinished(ObservableTask task) {
		// TODO Auto-generated method stub
		System.out.println("Yay, we heard of a finished task!!");
		if(!task.getResults(Status.class).equals(Status.SUCCESS))
		{
			//remove all current tasks from the queue and add a new reader task if possible.
			errors.add(task.getResults(String.class));
			//clear any remainig tasks, they belong to an old reader.
			while(readerTasks.hasNext())
			{
				readerTasks.next();
			}
			//if we don't have a valid additional reader, this will fail.			
			if(!addReaderTask())
			{
				readerTasks.append(new FailureTask(errors));
			}
		}
		else
		{
			if (task instanceof ReadWorkBookTask) {
				System.out.println("Found a Workbook Task");
				wb = task.getResults(IDAREWorkbook.class);
				readerTasks.append(new AddDataSetToManagerTask(wb,dsm,params.DataSetType,params.SetDescription,params.useTwoColumns));
			}
			if(task instanceof AddDataSetToManagerTask)
			{
				datasetadded = true;				
				while(readerTasks.hasNext())
				{
					readerTasks.next();
				}
			}
		}
	}
	@Override
	public void allFinished(FinishStatus finishStatus) {
		System.out.println("Everything is finished.");
		for(IDAREDatasetReader reader : dsm.getAvailableReaders())
		{
			reader.reset();
		}
	}
}

