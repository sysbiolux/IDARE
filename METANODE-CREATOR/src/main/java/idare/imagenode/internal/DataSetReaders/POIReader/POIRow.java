package idare.imagenode.internal.DataSetReaders.POIReader;

import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow;
import idare.imagenode.internal.Services.POI.POIToIDARETypes;

import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class POIRow implements IDARERow {
	Row row;
	
	public POIRow(Row row) {
		super();
		this.row = row;
	}

	@Override
	public Iterator<IDARECell> iterator() {
		// TODO Auto-generated method stub
		return new CellIter(row.iterator());
	}

	@Override
	public Iterator<IDARECell> cellIterator() {
		// TODO Auto-generated method stub
		return new CellIter(row.cellIterator());
	}

	@Override
	public IDARECell getCell(int arg0) {
		// TODO Auto-generated method stub
		Cell current = row.getCell(arg0);		
		return current == null ? null : new POICell(current);
	}

	@Override
	public IDARECell getCell(int arg0, int arg1) {
		Cell current = row.getCell(arg0,POIToIDARETypes.POIToIDARE_ACCESS_TYPES.get(arg1));
		
		return current == null ? null : new POICell(current);
	}

	@Override
	public short getLastCellNum() {
		// TODO Auto-generated method stub
		return row.getLastCellNum();
	}

	@Override
	public int getRowNum() {
		// TODO Auto-generated method stub
		return row.getRowNum();
	}

	@Override
	public boolean equals(Object o)
	{
		try
		{
			return this.row.equals(((POIRow)o).row);
		}
		catch(ClassCastException e){
			return false;
		}
	}
	
	@Override
	public int hashCode()
	{
		return row.hashCode();
	}
	
	/**
	 * Cell iterator implementation, to return the cells in the order of appearance.
	 * @author Thomas Pfau
	 *
	 */
	private class CellIter implements Iterator<IDARECell>
	{

		Iterator<Cell> iter;
		public CellIter(Iterator<Cell> iter)
		{
			this.iter = iter;			
		}
		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public IDARECell next() {
			return new POICell(iter.next());
		}
		
	}
}
