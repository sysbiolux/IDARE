package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import idare.imagenode.Interfaces.DataSetReaders.IDAREDatasetReader;
import idare.imagenode.Interfaces.DataSetReaders.IDARETask;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.exceptions.io.WrongFormat;

import java.io.File;
import java.util.Stack;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ReadWorkBookTask extends IDARETask{

	File inputFile;
	IDAREDatasetReader reader;
	
	public ReadWorkBookTask(File inputFile, IDAREDatasetReader reader) {
		super();
		this.inputFile = inputFile;
		this.reader = reader;
	}

	@Override
	public void execute(TaskMonitor taskMonitor) throws Exception {
		System.out.println("Trying to read the Workbook");
		if(reader.getStatus() == IDAREDatasetReader.IS_SET_UP)
		{
			result = reader.readData(inputFile);
			System.out.println("Success");
		}
		else
		{
			System.out.println("Failed");
			throw new WrongFormat(reader.getClass().getSimpleName() + ": " + reader.getStatus());
		}
	}

}
