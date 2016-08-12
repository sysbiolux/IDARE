package idare.imagenode.internal.Services.POI;

import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell.CellType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class POIToIDARETypes {
    public static final Map<Integer, CellType> POIToIDARE;
    static {
        Map<Integer, CellType> aMap = new HashMap<Integer, CellType>();
        aMap.put(Cell.CELL_TYPE_NUMERIC, CellType.NUMERIC);
        aMap.put(Cell.CELL_TYPE_STRING, CellType.STRING);
        aMap.put(Cell.CELL_TYPE_FORMULA, CellType.FORMULA);
        aMap.put(Cell.CELL_TYPE_BLANK, CellType.BLANK);
        aMap.put(Cell.CELL_TYPE_ERROR, CellType.UNKNOWN);
        aMap.put(Cell.CELL_TYPE_BOOLEAN, CellType.UNKNOWN);
        POIToIDARE = Collections.unmodifiableMap(aMap);
    }
    public static final Map<Integer,Row.MissingCellPolicy> POIToIDARE_ACCESS_TYPES;
    static {
        Map<Integer,Row.MissingCellPolicy> aMap = new HashMap< Integer, Row.MissingCellPolicy>();
        aMap.put(IDARERow.CREATE_NULL_AS_BLANK,Row.CREATE_NULL_AS_BLANK);
        aMap.put(IDARERow.RETURN_BLANK_AS_NULL,Row.RETURN_BLANK_AS_NULL);
        aMap.put(IDARERow.RETURN_NULL_AND_BLANK,Row.RETURN_NULL_AND_BLANK);
        POIToIDARE_ACCESS_TYPES = Collections.unmodifiableMap(aMap);
    }
}



