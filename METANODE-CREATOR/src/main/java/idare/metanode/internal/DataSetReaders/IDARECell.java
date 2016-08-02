package idare.metanode.internal.DataSetReaders;


public interface IDARECell {
	
	public int getCellType();	
	public int getColumnIndex();
	public double getNumericCellValue();
	public int getRowIndex();	
	public String getStringCellValue();
	public String getFormattedCellValue();
}
