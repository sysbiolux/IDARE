package idare.imagenode.Interfaces.DataSetReaders;


import idare.imagenode.internal.exceptions.io.WrongFormat;

import java.io.IOException;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;


public abstract class IDARETask extends AbstractTask implements ObservableTask{

	public enum Status{
		NOT_STARTED,
		SUCCESS,
		ERROR
	}	
	
	
	public static String NOT_RUN = "Not called yet";
	public static String EXECUTION_SUCCESS = "Executed Successfully";
	private Status state = Status.NOT_STARTED;
	private String statusMessage = "NOT_STARTED";	
	protected Object result;
	public abstract void execute(TaskMonitor taskMonitor) throws Exception;
	
	@Override
	public final void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		try{
			execute(taskMonitor);
			state = Status.SUCCESS;
			statusMessage = EXECUTION_SUCCESS;
		}
		catch(Exception e)
		{
			state =	Status.ERROR;
			statusMessage = e.getMessage();
			
		}
	}

	public final Status getStatus()
	{
		return state;
	}
	public final String getStatusString()
	{
		return statusMessage;
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		// TODO Auto-generated method stub
		if(type.equals(String.class))
		{
			return (R)statusMessage;
		}
		if(type.equals(Status.class))
		{
			return (R)state;
		}
		return (R)result;
	}
	
}
