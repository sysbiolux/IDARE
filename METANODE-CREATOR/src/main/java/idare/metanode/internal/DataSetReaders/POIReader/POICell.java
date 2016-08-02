package idare.metanode.internal.DataSetReaders.POIReader;

import idare.metanode.internal.DataSetReaders.IDARECell;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

public class POICell implements IDARECell {
	Cell cell;
	
	public POICell(Cell cell) {
		super();
		this.cell = cell;
	}

	@Override
	public int getCellType() {
		return cell.getCellType();
	}

	@Override
	public int getColumnIndex() {
		// TODO Auto-generated method stub
		return cell.getColumnIndex();
	}

	@Override
	public double getNumericCellValue() {
		// TODO Auto-generated method stub
		return cell.getNumericCellValue();
	}


	@Override
	public int getRowIndex() {
		// TODO Auto-generated method stub
		return cell.getRowIndex();
	}

	@Override
	public String getStringCellValue() {
		// TODO Auto-generated method stub
		return cell.getStringCellValue();
	}
	
	@Override
	public boolean equals(Object o)
	{
		try
		{
			return this.cell.equals(((POICell)o).cell);
		}
		catch(ClassCastException e){
			return false;
		}
	}
	
	@Override
	public int hashCode()
	{
		return cell.hashCode();
	}

	@Override
	public String getFormattedCellValue() {
		// TODO Auto-generated method stub
		DataFormatter df = new DataFormatter();
		return df.formatCellValue(cell);
	}
	

}
