package idare.imagenode.internal.DataSetReaders.POIReader;

import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARESheet;

import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class POISheet implements IDARESheet {

	Sheet sheet;
	
	public POISheet(Sheet sheet) {
		super();
		this.sheet = sheet;
	}

	@Override
	public Iterator<IDARERow> iterator() {
		// TODO Auto-generated method stub
		return new RowIter(sheet.iterator());
	}

	@Override
	public int getLastRowNum() {
		// TODO Auto-generated method stub
		return sheet.getLastRowNum();
	}

	@Override
	public int getPhysicalNumberOfRows() {
		// TODO Auto-generated method stub
		return sheet.getPhysicalNumberOfRows();
	}

	@Override
	public IDARERow getRow(int arg0) {
		// TODO Auto-generated method stub
		return new POIRow(sheet.getRow(arg0));	
		}

	@Override
	public String getSheetName() {
		// TODO Auto-generated method stub
		return sheet.getSheetName();
	}

	@Override
	public Iterator<IDARERow> rowIterator() {
		// TODO Auto-generated method stub
		return new RowIter(sheet.rowIterator());
	}

	@Override
	public boolean equals(Object o)
	{
		try
		{
			return this.sheet.equals(((POISheet)o).sheet);
		}
		catch(ClassCastException e){
			return false;
		}
	}
	
	@Override
	public int hashCode()
	{
		return sheet.hashCode();
	}
	
	/**
	 * Row iterator implementation, to return the cells in the order of appearance.
	 * @author Thomas Pfau
	 *
	 */
	private class RowIter implements Iterator<IDARERow>
	{

		Iterator<Row> iter;
		public RowIter(Iterator<Row> iter)
		{
			this.iter = iter;			
		}
		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public IDARERow next() {
			return new POIRow(iter.next());
		}
		
	}
}
