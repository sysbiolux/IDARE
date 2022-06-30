package idare.imagenode.internal.DataSetReaders.CSVReader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
/**
 * An implementation of a Row in a CSV Sheet.
 * @author Thomas Pfau
 *
 */
public class CSVRow implements Row {

	HashMap<Integer,Cell> data;
	HashMap<Cell,Integer> datapos;
	int lastCellposition = -1;
	Vector<cellPosition> cellpositions;
	CSVSheet sheet;
	/**
	 * Basic constructor with the enclosing sheet.
	 * @param sheet the Sheet containing this row
	 */
	public CSVRow(CSVSheet sheet)
	{
		this.data = new HashMap<Integer,Cell>();
		this.sheet = sheet;
		cellpositions = new Vector<cellPosition>();
		datapos = new HashMap<Cell, Integer>();
	}
	
	
	/**
	 * Add a Cell with a specified value.
	 * @param numeric whether the added cell is numeric
	 * @param value the value of the cell
	 */	
	public void addCell(boolean numeric, String value)
	{
		lastCellposition++;
		Cell current = createCell(lastCellposition);
		data.put(lastCellposition,current);		
		if(numeric)
		{
			current.setCellValue(Double.parseDouble(value));
		}
		else
		{
			current.setCellValue(value);
		}
		
	}
	
	/**
	 * Get the Column number of a specific cell.
	 * @param cell the cell to get the column for
	 * @return the position of the requested cell in this row.
	 */
	public int getColumnNumber(Cell cell)
	{
		return datapos.get(cell);
	}
	
	
	@Override
	public Iterator<Cell> iterator() {
		// TODO Auto-generated method stub		
		return new Celliter(data);
	}

	@Override
	public Iterator<Cell> cellIterator() {
		// TODO Auto-generated method stub
		return new Celliter(data);
	}

	@Override
	public Cell createCell(int arg0) {

		return createCell(arg0,CellType.BLANK);
	}
	

	@Override
	public Cell createCell(int column, CellType type) {		
		Cell newcell = new CSVCell(type,this);
		if(!data.containsKey(column))
		{
			cellpositions.add(new cellPosition(column));
		}
		else
		{
			//remove an old cell.
			datapos.remove(data.get(column));
		}
		datapos.put(newcell, column);
		data.put(column, newcell);
		return newcell;			
	}


	@Override
	public int getOutlineLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Cell getCell(int arg0) {
		// TODO Auto-generated method stub
		return data.get(arg0);
	}

	@Override
	public Cell getCell(int arg0, MissingCellPolicy arg1) {
		if(arg1 == Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
		{
			if(data.get(arg0).toString().equals(""))
			{
				return null;
			}
		}
		if(arg1 == Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
		{
			if(!data.containsKey(arg0))
			{
				return createCell(arg0);
			}
		}		
			
		return data.get(arg0);
	}

	@Override
	public short getFirstCellNum() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getHeightInPoints() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getLastCellNum() {
		// TODO Auto-generated method stub
		Collections.sort(cellpositions);
		//Return the Last Cell num + 1 according to specification.
		return (short)(cellpositions.lastElement().position + 1);
	}

	@Override
	public int getPhysicalNumberOfCells() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public int getRowNum() {
		// TODO Auto-generated method stub
		return sheet.getRowNumber(this);
	}

	@Override
	public CellStyle getRowStyle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Sheet getSheet() {
		// TODO Auto-generated method stub
		return sheet;
	}

	@Override
	public boolean getZeroHeight() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFormatted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeCell(Cell arg0) {
		

	}

	@Override
	public void setHeight(short arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHeightInPoints(float arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRowNum(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRowStyle(CellStyle arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setZeroHeight(boolean arg0) {
		// TODO Auto-generated method stub

	}
	
	public String toString()
	{
		  
		String secondline = "";
		Iterator<Cell> iter = cellIterator();
		String firstline = "";
		
		while(iter.hasNext())
		{
			Cell cell = iter.next();
			if(cell!= null)
			{
				secondline += "" + cell.toString();
				firstline += "" + cell.getColumnIndex();
			}
			secondline += "\t";
			firstline += "\t";
		}
		firstline += "\n";				
		return firstline + secondline + "\n";
	}
	
	/**
	 * Cell iterator implementation, to return the cells in the order of appearance.
	 * @author Thomas Pfau
	 *
	 */
	private class Celliter implements Iterator<Cell>
	{

		private HashMap<Integer, Cell> map;
		private Vector<Integer> cellpositions;
		Iterator<Integer> iter;
		public Celliter(HashMap<Integer, Cell> entry)
		{
			this.cellpositions = new Vector<Integer>();
			cellpositions.addAll(entry.keySet());
			Collections.sort(cellpositions);
			iter = cellpositions.iterator();
			map = entry;
		}
		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public Cell next() {
			return map.get(iter.next());
		}
		
	}

	@Override
	public void shiftCellsRight(int firstShiftColumnIndex, int lastShiftColumnIndex, int step) {
		if(firstShiftColumnIndex + step < 0 )
		{
			// do nothing if we try to shift "out of range"
			return;
		}
		HashMap<Integer,Cell> cData = new HashMap<Integer, Cell>();
		HashMap<Cell,Integer> cDatapos = new HashMap<Cell, Integer>();		
		Vector<Integer> cCellpositions = new Vector<Integer>();
		for(int i=firstShiftColumnIndex; i < lastShiftColumnIndex; i ++)
		{
			if(cellpositions.contains(i))
			{
				cData.put(i+step, data.get(i));
				cDatapos.put( data.get(i),i+step);
				cCellpositions.add(i+step);
				datapos.remove(data.get(i));
				data.remove(i);
				cellpositions.remove(new cellPosition(i));
			}
			else
			{
				if(data.containsKey(i+step))
				{
					datapos.remove(data.get(i+step));				
					data.remove(i+step);
					cellpositions.remove(new cellPosition(i+step));
				}
			}
		}
		
	}


	@Override
	public void shiftCellsLeft(int firstShiftColumnIndex, int lastShiftColumnIndex, int step) {
		// a shift to the left is the same as a negative shift to the right...
		shiftCellsRight(firstShiftColumnIndex, lastShiftColumnIndex, -step);
		
	}
	private class cellPosition implements Comparable<cellPosition>
	{
		public Integer position;
		public cellPosition(Integer position)
		{
			this.position = position;
		}
		
		public boolean equals(Object o)
		{
			if(o instanceof cellPosition)
			{
				return ((cellPosition) o).position == this.position;
			}
			return false;
		}
		
		@Override
		public int compareTo(cellPosition o) {
			// TODO Auto-generated method stub
			return position.compareTo(o.position);
		}		
		
	}
}
