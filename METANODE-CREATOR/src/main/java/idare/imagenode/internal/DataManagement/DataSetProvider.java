package idare.imagenode.internal.DataManagement;

import idare.imagenode.Data.BasicDataTypes.ArrayData.ArrayDataSet;
import idare.imagenode.Data.BasicDataTypes.MultiArrayData.MultiArrayDataSet;
import idare.imagenode.Interfaces.Plugin.IDAREPlugin;
import idare.imagenode.internal.IDAREService;
import idare.imagenode.internal.Data.Array.CircleData.CircleDataSetProperties;
import idare.imagenode.internal.Data.Array.CircleGridData.CircleGridProperties;
import idare.imagenode.internal.Data.Array.RectangleData.RectangleDataSetProperties;
import idare.imagenode.internal.Data.Array.TimeSeriesData.TimeSeriesDataSetProperties;
import idare.imagenode.internal.Data.MultiArray.GraphData.GraphDataSetProperties;
import idare.imagenode.internal.Data.MultiArray.ScatterData.LargeScatterProperties;
import idare.imagenode.internal.Data.MultiArray.ScatterData.SmallScatterProperties;

import java.util.Vector;

/**
 * A Simple Plugin that adds the Default properties and DataSets to IDARE
 * @author Thomas Pfau
 *
 */
public class DataSetProvider implements IDAREPlugin {

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Plugin.IDAREPlugin#getServices()
	 */
	@Override
	public Vector<IDAREService> getServices() {
			Vector<IDAREService> datasetservices = new Vector<IDAREService>();
			datasetservices.add(new ArrayDataSet());							
			datasetservices.add(new CircleDataSetProperties());
			datasetservices.add(new CircleGridProperties());
			datasetservices.add(new RectangleDataSetProperties());
			datasetservices.add(new TimeSeriesDataSetProperties());			
			datasetservices.add(new MultiArrayDataSet());		
			datasetservices.add(new GraphDataSetProperties());
			datasetservices.add(new LargeScatterProperties());
			datasetservices.add(new SmallScatterProperties());
			return datasetservices;
		
	}
	

}
