package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

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
	 * Creates a new DataSetAdderTask.
	 */
	public void addDataset()
	{		
		dat = new DataSetAdderTask(dsm);
		dtm.execute(createTaskIterator(),dat);
	}
	
	
}
