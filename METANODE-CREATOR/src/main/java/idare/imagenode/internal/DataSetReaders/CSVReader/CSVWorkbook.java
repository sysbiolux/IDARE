package idare.imagenode.internal.DataSetReaders.CSVReader;

import idare.imagenode.Utilities.StringUtils;
import idare.imagenode.exceptions.io.WrongFormat;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * This class provides functionality to read text delimited files.
 * It assumes, that there is no "text" delimiter, and that the delimiting character is never used at any other position.
 * There will only ever be one {@link Sheet} in this object, and any sheet manipulations will fail or return <code>null</code>. 
 * @author Thomas Pfau
 *
 */
public class CSVWorkbook implements Workbook{

	/**
	 * There will only ever be one sheet in a text delimited file.
	 */
	CSVSheet currentSheet;

	/**
	 * Create a new Workbook using a specific file and a defined separator.
	 * @param F - The file to read from.
	 * @param separator the separator used in the Workbook
	 * @param idColumnCount how many ID columns there are
	 * @throws IOException if something goes wrong during IO
	 * @throws WrongFormat if the format of the workbook is not proper CSV
	 */
	public CSVWorkbook(File F, String separator, int idColumnCount ) throws IOException, WrongFormat
	{

		//create the sheet
		currentSheet = new CSVSheet(this,F.getName());
		int currentRowNumber = 0;
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(F));
		String currentline = br.readLine();	
		boolean started = false;
		boolean emptyrows = false;
		int columncount = 0; 
		while(currentline != null)
		{
			//read each row (i.e. line)
//			System.out.println("Creating new row for line:\n " + currentline);
			Row currentRow = currentSheet.createRow(currentRowNumber);
			//and create cells for each entry.
			String[] Cells = currentline.split(separator);
			String StringArr = "";
			for(String str : Cells)
			{
				StringArr += str + "\t$\t";
			}
			PrintFDebugger.Debugging(this, currentline + "\n" + StringArr + " with idColCount " + idColumnCount);
			//if a line contains less than the id columns than this can't work.
			if(Cells.length < idColumnCount + 1)
			{
				if(started)
				{
					emptyrows = true;
					currentline = br.readLine();
					continue;
				}
				else
				{
					br.close();
					throw new WrongFormat("All rows in a value separated file must have the same number of separators! Started with rows that indicate no data.");
				}

			}
			if(emptyrows)
			{
				// We can only reach this point if there are too small rows somewhere in the file.
				br.close();
				throw new WrongFormat("All rows in a value separated file must have the same number of separators! No empty rows allowed in between non empty rows.");
			}
			if(!started)
			{
				started = true;
				columncount = Cells.length;
			}
			else
			{
				if(columncount != Cells.length)
				{
					br.close();
					throw new WrongFormat("All rows in a value separated file must have the same number of separators! " + currentRowNumber + " had " + (Cells.length-1) + " separators, while earlier rows had " + (columncount-1) + " separators");	
				} 
			}
			int cellpos = 0;
			for(String cell : Cells)
			{
//				System.out.println("Creating new Cell for value:" + cell);
				//Create the cells either as numeric or as string, depending on whether the value is numeric or string.
				Cell currentcell = currentRow.createCell(cellpos);
				if(StringUtils.isNumeric(cell))
				{
//					System.out.println("Creating numeric cell for value: " + cell);
					currentcell.setCellType(Cell.CELL_TYPE_NUMERIC);
					currentcell.setCellValue(Double.parseDouble(cell));
				}
				else if(cell.equals(""))
				{
//					System.out.println("Creating blank cell for value: " + cell);
					currentcell.setCellType(Cell.CELL_TYPE_BLANK);
				}
				else 
				{
//					System.out.println("Creating string cell for value: " + cell);
					currentcell.setCellType(Cell.CELL_TYPE_STRING);
					currentcell.setCellValue(cell);
				}
				cellpos++;
			}				
//			System.out.println("The created row is: \n" + currentRow.toString());
			//currentSheet.addRow(currentRow);
			currentline = br.readLine();
			currentRowNumber++;
		}
		br.close();	
//		System.out.println(toString());
	}	

	@Override
	public int addPicture(byte[] arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addToolPack(UDFFinder arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Sheet cloneSheet(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CellStyle createCellStyle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataFormat createDataFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Font createFont() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Name createName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Sheet createSheet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Sheet createSheet(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int getActiveSheetIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<? extends PictureData> getAllPictures() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CreationHelper getCreationHelper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getFirstVisibleTab() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Font getFontAt(short arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getForceFormulaRecalculation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MissingCellPolicy getMissingCellPolicy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Name getName(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Name getNameAt(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNameIndex(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getNumberOfFonts() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfNames() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfSheets() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public String getPrintArea(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Sheet getSheet(String arg0) {
		// TODO Auto-generated method stub
		return currentSheet;
	}

	@Override
	public Sheet getSheetAt(int arg0) {
		// TODO Auto-generated method stub
		return currentSheet;
	}

	@Override
	public int getSheetIndex(String arg0) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public int getSheetIndex(Sheet arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getSheetName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isHidden() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSheetHidden(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSheetVeryHidden(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeName(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeName(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePrintArea(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeSheetAt(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setActiveSheet(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFirstVisibleTab(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setForceFormulaRecalculation(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHidden(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMissingCellPolicy(MissingCellPolicy arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPrintArea(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPrintArea(int arg0, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub

	}
	@Override
	public void setSheetHidden(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSheetName(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSheetOrder(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(OutputStream arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	public String toString()
	{
		StringBuffer res = new StringBuffer();

		try{
//			System.out.println("Generating the WorkBook Representation String");

			for(Row row : currentSheet)
			{
				for(int i = 0; i < row.getLastCellNum(); i++)
				{
					Cell current = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					DataFormatter df = new DataFormatter();
					res.append(df.formatCellValue(current) + "\t");
				}
				res.append("\n");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
		return res.toString();
	}

	@Override
	public Iterator<Sheet> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Sheet> sheetIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Font findFont(boolean bold, short color, short fontHeight, String name, boolean italic, boolean strikeout,
			short typeOffset, byte underline) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CellStyle getCellStyleAt(int idx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<? extends Name> getNames(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends Name> getAllNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeName(Name name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int linkExternalWorkbook(String name, Workbook workbook) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SheetVisibility getSheetVisibility(int sheetIx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSheetVisibility(int sheetIx, SheetVisibility visibility) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SpreadsheetVersion getSpreadsheetVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int addOlePackage(byte[] oleData, String label, String fileName, String command) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSelectedTab(int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNumCellStyles() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSheetHidden(int sheetIx, boolean hidden) {
		// TODO Auto-generated method stub
		
	}

}
