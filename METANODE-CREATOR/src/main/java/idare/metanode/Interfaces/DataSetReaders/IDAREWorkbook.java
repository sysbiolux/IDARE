package idare.metanode.Interfaces.DataSetReaders;

/**
 * 
 * @author Thomas Pfau
 *
 */
public interface IDAREWorkbook{	

	
		public int getNumberOfSheets();
					
		public IDARESheet getSheet(String arg0);

		public IDARESheet getSheetAt(int arg0);

		public int getSheetIndex(String arg0);
		
		public int getSheetIndex(IDARESheet arg0);
		
		public String getSheetName(int arg0);
						

}


