package idare.imagenode.Interfaces.DataSetReaders.WorkBook.BasicImplementation;

import java.util.Iterator;
import java.util.Vector;

import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell.CellType;
/**
 * A simple implementation of an {@link IDARERow}
 * @author Thomas Pfau
 *
 */
public class BasicIDARERow implements IDARERow {

	Vector<BasicIDARECell> cells = new Vector<BasicIDARECell>(); 
	int nullcells = 0;
	BasicIDARESheet parentSheet;
	
	/**
	 * Simple constructor providing the parent sheet.
	 * @param parent The {@link BasicIDARESheet} containing this Row.
	 */
	public BasicIDARERow(BasicIDARESheet parent) {
		parentSheet = parent;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<IDARECell> iterator() {		
		return new IDARECellIterator(cells.iterator());
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow#cellIterator()
	 */
	@Override
	public Iterator<IDARECell> cellIterator() {
		return new IDARECellIterator(cells.iterator());
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow#getCell(int)
	 */
	@Override
	public IDARECell getCell(int arg0) {		
		return getCell(arg0,IDARERow.RETURN_NULL_AND_BLANK);
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow#getCell(int, int)
	 */
	@Override
	public IDARECell getCell(int arg0, int arg1) {
		if(arg0 >= cells.size())
		{
			if(arg1 == IDARERow.RETURN_NULL_AND_BLANK || arg1 == IDARERow.RETURN_BLANK_AS_NULL)
			{
				return null;
			}
			if(arg1 == IDARERow.CREATE_NULL_AS_BLANK)
			{
				for(int i = cells.size(); i < arg1; i++ )
				{
					createNullCell();
					nullcells++;
				}
				createCell(CellType.BLANK);
			}
		}
		else
		{
			BasicIDARECell currentcell = cells.get(arg0);
			if(currentcell == null && arg1 == CREATE_NULL_AS_BLANK)
			{
				currentcell = createCell(CellType.BLANK,arg0);
			}
			return currentcell;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow#getLastCellNum()
	 */
	@Override
	public short getLastCellNum() {
		// TODO Auto-generated method stub
		return (short)cells.size();
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow#getRowNum()
	 */
	@Override
	public int getRowNum() {
		// TODO Auto-generated method stub
		return parentSheet.getRowIndex(this);
	}
	
	/**
	 * Create a Null cell at the end of this row.
	 */
	public void createNullCell()
	{
		cells.add(null);
	}
	
	/**
	 * Create a Blank Cell at the end of this row.
	 * This is the same as createCell(getLastCellNum()-1)
	 * @return the created cell.
	 */
	public BasicIDARECell createCell()
	{
		return createCell(cells.size());
	}
	
	/**
	 * Create a Cell with a specified type at the end of this row
	 * This is the same as a call to createCell(type,getLastCellNum()-1)
	 * @param type the type of the created cell
	 * @return the created cell
	 */
	public BasicIDARECell createCell(CellType type)
	{
		return createCell(type, cells.size());
	}
	
	/**
	 * Create a blank cell at a specified position, overwriting any cell existing at the provided position
	 * This is the same as the Call createCell(CellType.BLANK,pos) 
	 * @param pos the 0-based position at which to place the cell.
	 * @return the created cell
	 */
	public BasicIDARECell createCell(int pos)
	{
		return createCell(CellType.BLANK, pos); 
	}
	
	/**
	 * Create a Cell of the specified type at the specified position.
	 * This extends the row accordingly.
	 * @param CellType The {@link CellType} of the created cell
	 * @param pos the 0-based position of the cell
	 * @return the created {@link BasicIDARECell}
	 */
	public BasicIDARECell createCell(CellType CellType, int pos)
	{
		//fill the rows
		while(cells.size() <= pos)
		{
			cells.add(null);
			nullcells++;
		}		
		if(cells.get(pos) == null)
		{
			nullcells--;
		}		
		BasicIDARECell newCell = new BasicIDARECell(CellType,this);
		cells.set(pos, newCell);				
		return newCell;
	}
	
	
	/**
	 * Get the position/index of a given Cell
	 * @param cell the cell for which the index is requested
	 * @return the 0-based index of the cell.
	 */
	public int getCellIndex(BasicIDARECell cell)
	{
		return cells.indexOf(cell);
	}
	

}
