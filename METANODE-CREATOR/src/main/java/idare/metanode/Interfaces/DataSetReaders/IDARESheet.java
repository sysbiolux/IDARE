package idare.metanode.Interfaces.DataSetReaders;

import java.util.Iterator;

public interface IDARESheet extends Iterable<IDARERow>{

	public int getLastRowNum();
	public int getPhysicalNumberOfRows();
	public IDARERow getRow(int arg0);
	public String getSheetName();	
	public Iterator<IDARERow> rowIterator();	

}
