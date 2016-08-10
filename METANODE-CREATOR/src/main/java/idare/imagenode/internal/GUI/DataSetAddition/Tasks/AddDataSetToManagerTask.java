package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import idare.imagenode.Interfaces.DataSetReaders.IDARETask;
import idare.imagenode.Interfaces.DataSetReaders.IDAREWorkbook;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.exceptions.io.WrongFormat;

import org.cytoscape.work.TaskMonitor;

public class AddDataSetToManagerTask extends IDARETask {


	IDAREWorkbook wb;	
	DataSetManager dsm;
	String dataSetType;
	String dataSetDescription;
	boolean twocolumns;
	
	public AddDataSetToManagerTask(IDAREWorkbook wb, DataSetManager dsm, String dataSetType, String dataSetDescription,
			boolean twocolumns) {
		super();
		this.wb = wb;
		this.dataSetType = dataSetType;
		this.dataSetDescription = dataSetDescription;
		this.twocolumns = twocolumns;
	}
	
	@Override
	public void execute(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Trying to Add DataSet");
		if(wb == null)
		{
			throw new WrongFormat("Workbook was null");
		}
		else
		{			
			dsm.createDataSet(twocolumns, dataSetType, dataSetDescription, wb);			
		}
	}

}
