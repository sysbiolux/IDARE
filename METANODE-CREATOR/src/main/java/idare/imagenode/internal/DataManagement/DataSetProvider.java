package idare.imagenode.internal.DataManagement;

import idare.imagenode.Data.BasicDataTypes.ValueSetData.ValueSetDataSet;
import idare.imagenode.Data.BasicDataTypes.itemizedData.ItemDataSet;
import idare.imagenode.Interfaces.Plugin.IDAREPlugin;
import idare.imagenode.Interfaces.Plugin.IDAREService;
import idare.imagenode.internal.Data.ValueSetData.GraphData.GraphDataSetProperties;
import idare.imagenode.internal.Data.ValueSetData.ScatterData.LargeScatterProperties;
import idare.imagenode.internal.Data.ValueSetData.ScatterData.SmallScatterProperties;
import idare.imagenode.internal.Data.itemizedData.CircleData.CircleDataSetProperties;
import idare.imagenode.internal.Data.itemizedData.CircleGridData.CircleGridProperties;
import idare.imagenode.internal.Data.itemizedData.RectangleData.RectangleDataSetProperties;
import idare.imagenode.internal.Data.itemizedData.TimeSeriesData.TimeSeriesDataSetProperties;

import java.util.Vector;

public class DataSetProvider implements IDAREPlugin {

	@Override
	public Vector<IDAREService> getServices() {
			Vector<IDAREService> datasetservices = new Vector<IDAREService>();
			datasetservices.add(new ItemDataSet());							
			datasetservices.add(new CircleDataSetProperties());
			datasetservices.add(new CircleGridProperties());
			datasetservices.add(new RectangleDataSetProperties());
			datasetservices.add(new TimeSeriesDataSetProperties());			
			datasetservices.add(new ValueSetDataSet());		
			datasetservices.add(new GraphDataSetProperties());
			datasetservices.add(new LargeScatterProperties());
			datasetservices.add(new SmallScatterProperties());
			return datasetservices;
		
	}
	

}
