package idare.imagenode.internal.DataSetReaders.POIReader;

import idare.imagenode.Interfaces.DataSetReaders.IDARESheet;
import idare.imagenode.Interfaces.DataSetReaders.IDAREWorkbook;

import java.util.HashMap;

import org.apache.poi.ss.usermodel.Workbook;

public class POIWorkBook implements IDAREWorkbook {

	Workbook wb;	
	HashMap<String,IDARESheet> sheetNames = new HashMap<String, IDARESheet>();
	HashMap<Integer,IDARESheet> sheetPositions = new HashMap<Integer, IDARESheet>();
	HashMap<IDARESheet,Integer> sheetsToPositions = new HashMap<IDARESheet,Integer >();		
	public POIWorkBook(Workbook wb) {
		// TODO Auto-generated constructor stub
		this.wb = wb;
		for(int i = 0; i < wb.getNumberOfSheets(); i++)
		{
			sheetPositions.put(i, new POISheet(wb.getSheetAt(i)));
			String sheetname = wb.getSheetName(i);
			sheetNames.put(sheetname, sheetPositions.get(i));
			sheetsToPositions.put(sheetPositions.get(i), i);
		}
	}
	
	
	@Override
	public int getNumberOfSheets() {
		// TODO Auto-generated method stub
		return wb.getNumberOfSheets();
	}

	@Override
	public IDARESheet getSheet(String arg0) {
		return sheetNames.get(arg0);
	}

	@Override
	public IDARESheet getSheetAt(int arg0) {
		return sheetPositions.get(arg0);
	}

	@Override
	public int getSheetIndex(String arg0) {
		// TODO Auto-generated method stub
		return sheetsToPositions.get(sheetNames.get(arg0));
	}

	@Override
	public int getSheetIndex(IDARESheet arg0) {
		// TODO Auto-generated method stub
		return sheetsToPositions.get(arg0);
	}

	@Override
	public String getSheetName(int arg0) {
		// TODO Auto-generated method stub
		return sheetPositions.get(arg0).getSheetName();
	}

}
