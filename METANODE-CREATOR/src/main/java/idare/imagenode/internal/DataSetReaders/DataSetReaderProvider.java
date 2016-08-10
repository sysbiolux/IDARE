package idare.imagenode.internal.DataSetReaders;

import idare.imagenode.Interfaces.Plugin.IDAREPlugin;
import idare.imagenode.Interfaces.Plugin.IDAREService;
import idare.imagenode.internal.DataSetReaders.CSVReader.CSVReader;
import idare.imagenode.internal.DataSetReaders.CSVReader.TSVReader;
import idare.imagenode.internal.DataSetReaders.POIReader.POIReader;

import java.util.Vector;

public class DataSetReaderProvider implements IDAREPlugin {

	@Override
	public Vector<IDAREService> getServices() {
		Vector<IDAREService> readers = new Vector<IDAREService>();
		readers.add(new CSVReader());
		readers.add(new TSVReader());
		readers.add(new POIReader());		
		return readers;
	}

}
