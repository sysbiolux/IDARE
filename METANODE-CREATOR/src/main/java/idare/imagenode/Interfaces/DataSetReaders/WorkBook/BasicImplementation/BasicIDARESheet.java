package idare.imagenode.Interfaces.DataSetReaders.WorkBook.BasicImplementation;

import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARESheet;

import java.util.Iterator;
import java.util.Vector;

/**
 * This is a basic implementation of a {@link IDARESheet} for convenience.
 * @author Thomas Pfau
 *
 */
public class BasicIDARESheet implements IDARESheet {

	private String name; 
	Vector<BasicIDARERow> rows = new Vector<BasicIDARERow>();
	
	/**
	 * Default constructor using the name of the sheet.
	 * @param sheetname the name of this sheet.
	 */
	public BasicIDARESheet(String sheetname) {
		name = sheetname;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<IDARERow> iterator() {		
		return new IDARERowIterator(rows.iterator());
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARESheet#getLastRowNum()
	 */
	@Override
	public int getLastRowNum() {		
		return rows.size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARESheet#getPhysicalNumberOfRows()
	 */
	@Override
	public int getPhysicalNumberOfRows() {
		return rows.size();
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARESheet#getRow(int)
	 */
	@Override
	public IDARERow getRow(int arg0) {
		return rows.get(arg0);
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARESheet#getSheetName()
	 */
	@Override
	public String getSheetName() {		
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARESheet#rowIterator()
	 */
	@Override
	public Iterator<IDARERow> rowIterator() {
		return new IDARERowIterator(rows.iterator());
	}

	/**
	 * Create a row at the end of this sheet
	 * @return the created Row.
	 */
	public BasicIDARERow createRow()
	{
		rows.addElement(new BasicIDARERow(this));
		return rows.lastElement();
	}
	
	/**
	 * Get the index of a given Row
	 * @param row The {@link BasicIDARERow} to get the index for.
	 * @return the index of the given row.
	 */
	public int getRowIndex(BasicIDARERow row)
	{
		return rows.indexOf(row);
	}
	
}
