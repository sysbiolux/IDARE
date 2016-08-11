package idare.imagenode.internal.GUI.DataSetAddition.Tasks;

import java.io.File;
import java.util.Vector;

import idare.imagenode.Interfaces.DataSetReaders.IDAREWorkbook;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.GUI.DataSetAddition.DataSetGenerationParameters;

public class DataSetReadingInfo {	
	IDAREWorkbook workbook;
	boolean datasetAdded = false;
	private DataSetManager dsm;
	private DataSetGenerationParameters params;
	public DataSetReadingInfo(DataSetManager dsm, DataSetGenerationParameters params)
	{
		this.params = params;
		this.dsm = dsm;
	}
	Vector<String> errorMessages = new Vector<String>();
	
	public IDAREWorkbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(IDAREWorkbook workbook) {
		this.workbook = workbook;
	}
	
	
	public void addErrorMessage(String message)
	{
		System.out.println("Adding Error Message" + message);
		errorMessages.add(message);
	}
	
	public Vector<String> getErrorMessages()
	{
		Vector<String> errors = new Vector<String>();
		errors.addAll(errorMessages);
		return errors;
	}
	
	public boolean isDataSetAdded()
	{
		return datasetAdded;
	}
	
	public void setDataSetAdded()
	{
		datasetAdded = true;
	}
	
	public DataSetManager getDataSetManager()
	{
		return dsm;
	}
	
	public String getDataSetType()
	{
		return params.DataSetType;
	}
	
	public String getDataSetDescription()
	{
		return params.SetDescription;
	}
	public boolean doUseTwoColumns()
	{		
		return params.useTwoColumns;	
	}
	public File getInputFile()
	{
		return params.inputFile;
	}

}
