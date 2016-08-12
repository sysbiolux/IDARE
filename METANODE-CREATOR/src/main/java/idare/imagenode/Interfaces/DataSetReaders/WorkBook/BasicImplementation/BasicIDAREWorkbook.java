package idare.imagenode.Interfaces.DataSetReaders.WorkBook.BasicImplementation;

import java.util.HashMap;
import java.util.Vector;

import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARESheet;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook;

/**
 * A simple implementation of an {@link IDAREWorkbook}
 * @author Thomas Pfau
 *
 */
public class BasicIDAREWorkbook implements IDAREWorkbook {

		
	HashMap<String,BasicIDARESheet> sheetNames = new HashMap<String,BasicIDARESheet>();
	Vector<BasicIDARESheet> sheets = new Vector<BasicIDARESheet>();
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook#getNumberOfSheets()
	 */
	@Override
	public int getNumberOfSheets() {
		// TODO Auto-generated method stub
		return sheets.size();
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook#getSheet(java.lang.String)
	 */
	@Override
	public IDARESheet getSheet(String arg0) {
				
		return sheetNames.get(arg0);
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook#getSheetAt(int)
	 */
	@Override
	public IDARESheet getSheetAt(int arg0) {
		// TODO Auto-generated method stub
		return sheets.get(arg0);
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook#getSheetIndex(java.lang.String)
	 */
	@Override
	public int getSheetIndex(String arg0) {
		// TODO Auto-generated method stub		
		return sheets.indexOf(sheetNames.get(arg0));
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook#getSheetIndex(idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARESheet)
	 */
	@Override
	public int getSheetIndex(IDARESheet arg0) {
		// TODO Auto-generated method stub
		return sheets.indexOf(arg0);
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDAREWorkbook#getSheetName(int)
	 */
	@Override
	public String getSheetName(int arg0) {
		return sheets.get(arg0).getSheetName();
	}
	
	/**
	 *Create a new Sheet in this Workbook.
	 * If the sheetname already exists it wont be generated but the existing sheet will be returned.
	 * @param sheetname The name of the sheet.
	 * @return The generated {@link BasicIDARESheet} or an existing Sheet if the name already exists.
	 */
	public BasicIDARESheet createSheet(String sheetname)
	{
		if(sheetNames.containsKey(sheetname))
		{
			return sheetNames.get(sheetname);
		}
		else
		{
			BasicIDARESheet newSheet = new BasicIDARESheet(sheetname);
			sheetNames.put(sheetname, newSheet);
			sheets.add(newSheet);
			return newSheet;
		}
		 
	}

}
