package idare.imagenode.Data.BasicDataTypes.ValueSetData;

import idare.imagenode.Interfaces.DataSets.NodeValue;
import idare.imagenode.Properties.NodeValueType;

import java.util.Vector;


/**
 * A DataSetValue for ValueSetDataSets
 * @author Thomas Pfau
 *
 */
public class ValueSetDataValue extends NodeValue {

	private Vector<Double> graphlocations = new Vector<Double>();	
	
	public ValueSetDataValue(Comparable ColorValue) {
		super(false);
		type = NodeValueType.vector; 
		// TODO Auto-generated constructor stub
		value = ColorValue;
	}
	/**
	 * Set the Line Data for this {@link NodeValue}.
	 * This assumes, that this is containing Double Data.
	 * @param yValues
	 */
	public void setEntryData(Vector<Double> yValues)
	{
		graphlocations.clear();
		graphlocations.addAll(yValues);
	}
	/**
	 * Get the Line Data stored in this value
	 * @return A Vector with the (Double) LineData for this Value.
	 */
	public Vector<Double> getEntryData()
	{
		Vector<Double> yValues = new Vector<Double>();
		yValues.addAll(graphlocations);
		return yValues;
	}
	
	}
