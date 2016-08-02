package idare.metanode.internal.GUI.DataSetAddition.Tasks;

import idare.metanode.internal.DataManagement.DataSetManager;
import idare.metanode.internal.exceptions.io.DuplicateIDException;
import idare.metanode.internal.exceptions.io.WrongFormat;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * A Class to provide a factory method to create a Dataset
 * @author Thomas Pfau
 *
 */
public class DataSetAdderTaskFactory extends AbstractTaskFactory {

	//get the properties.
	File f ;
	boolean useTwoColHeaders ;
	String Description ;
	String DatasetType;
	private DataSetManager dsm;
	DialogTaskManager dtm;
	public DataSetAdderTaskFactory(DataSetManager dsm, DialogTaskManager dtm) {
		// TODO Auto-generated constructor stub
		this.dsm = dsm;
		this.dtm = dtm;
	}
	@Override
	public TaskIterator createTaskIterator() {
		
		return new TaskIterator(new DataSetAdderTask(f, useTwoColHeaders, Description, DatasetType, dsm));
	}
	/**
	 * Add A Dataset
	 * @param f - the file to read from
	 * @param useTwoColHeaders - whether to use two column headers
	 * @param Description - Description of the Dataset
	 * @param DataTypeString - Type String of the Dataset.
	 */
	public void addDataset(File f,boolean useTwoColHeaders,String Description, String DataTypeString)
	{
		this.f = f;
		this.useTwoColHeaders = useTwoColHeaders;
		this.Description = Description;
		this.DatasetType = DataTypeString;
		dtm.execute(createTaskIterator());
	}
	
	private class DataSetAdderTask extends AbstractTask
	{
		File f ;
		boolean useTwoColHeaders ;
		String Description ;
		String DatasetType;
		private DataSetManager dsm;
		
		public DataSetAdderTask(File f, boolean useTwoColHeaders,String Description,	String DatasetType, DataSetManager dsm) {
			// TODO Auto-generated constructor stub
			this.dsm = dsm;
			this.f = f;
			this.useTwoColHeaders = useTwoColHeaders;
			this.Description = Description;
			this.DatasetType = DatasetType;
		}
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			// TODO Auto-generated method stub
			try{
				dsm.createDataSet(useTwoColHeaders, DatasetType, Description, f);
			} catch(IOException ex){
				taskMonitor.showMessage(Level.ERROR, "Could not read File " + f.getCanonicalPath());
//				JOptionPane.showMessageDialog(null, "Could not read File " + f.getCanonicalPath(), "IO Error on selected File", JOptionPane.ERROR_MESSAGE);				
			} catch (WrongFormat e1) {
				taskMonitor.showMessage(Level.ERROR, "Invalid Format: "  + e1.getMessage());				
			} catch (InvalidFormatException e1) {
				taskMonitor.showMessage(Level.ERROR, "Invalid Format: "  + e1.getMessage());
			} catch (DuplicateIDException e1) {
				taskMonitor.showMessage(Level.ERROR, "Duplicate ID: "  + e1.getMessage());
			}
			catch(ClassNotFoundException e1)
			{
				taskMonitor.showMessage(Level.ERROR, "Class Not found: "  + e1.getMessage());
			}
		}
		
	}

}
