package idare.metanode.internal.DataSetReaders;

import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

public interface IDARERow extends Iterable<IDARECell> {
	
	public Iterator<IDARECell> cellIterator();
	public IDARECell getCell(int arg0);	
	public IDARECell getCell(int arg0, MissingCellPolicy arg1);
	public short getFirstCellNum();
	/**
	 * Gets the index of the last cell contained in this row PLUS ONE.!
	 * @return
	 */
	public short getLastCellNum();
	public int getPhysicalNumberOfCells();
	public int getRowNum();
	
}
