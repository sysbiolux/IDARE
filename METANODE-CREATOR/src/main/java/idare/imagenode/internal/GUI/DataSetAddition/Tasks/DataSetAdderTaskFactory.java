package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import java.util.Stack;

import idare.imagenode.internal.DataManagement.DataSetManager;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * A Class to provide a factory method to create a Dataset
 * @author Thomas Pfau
 *
 */
public class DataSetAdderTaskFactory extends AbstractTaskFactory {

	//get the properties.
	
	private DataSetManager dsm;
	DialogTaskManager dtm;
	DataSetAdderTask dat;
	public DataSetAdderTaskFactory(DataSetManager dsm, DialogTaskManager dtm,CySwingApplication cySwingApp) {
		// TODO Auto-generated constructor stub
		this.dsm = dsm;
		this.dtm = dtm;
	}
	@Override
	public TaskIterator createTaskIterator() {		
		return new TaskIterator(dat);								
		
	}
	/**
	 * Add A Dataset
	 * @param f - the file to read from
	 * @param useTwoColHeaders - whether to use two column headers
	 * @param Description - Description of the Dataset
	 * @param DataTypeString - Type String of the Dataset.
	 */
	public void addDataset()
	{		
		dat = new DataSetAdderTask(dsm);
		dtm.execute(createTaskIterator(),dat);
	}
	
	
}
