package idare.imagenode.internal.Services.POI;

import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell.CellType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class POIToIDARETypes {
    public static final Map<org.apache.poi.ss.usermodel.CellType, CellType> POIToIDARE;
    static {
        Map<org.apache.poi.ss.usermodel.CellType, CellType> aMap = new HashMap<org.apache.poi.ss.usermodel.CellType, CellType>();
        aMap.put(org.apache.poi.ss.usermodel.CellType.NUMERIC, CellType.NUMERIC);
        aMap.put(org.apache.poi.ss.usermodel.CellType.STRING, CellType.STRING);
        aMap.put(org.apache.poi.ss.usermodel.CellType.FORMULA, CellType.FORMULA);
        aMap.put(org.apache.poi.ss.usermodel.CellType.BLANK, CellType.BLANK);
        aMap.put(org.apache.poi.ss.usermodel.CellType.ERROR, CellType.UNKNOWN);
        aMap.put(org.apache.poi.ss.usermodel.CellType.BOOLEAN, CellType.UNKNOWN);
        POIToIDARE = Collections.unmodifiableMap(aMap);
    }
    public static final Map<Integer,Row.MissingCellPolicy> POIToIDARE_ACCESS_TYPES;
    static {
        Map<Integer,Row.MissingCellPolicy> aMap = new HashMap< Integer, Row.MissingCellPolicy>();
        aMap.put(IDARERow.CREATE_NULL_AS_BLANK,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        aMap.put(IDARERow.RETURN_BLANK_AS_NULL,Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        aMap.put(IDARERow.RETURN_NULL_AND_BLANK,Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
        POIToIDARE_ACCESS_TYPES = Collections.unmodifiableMap(aMap);
    }
}



