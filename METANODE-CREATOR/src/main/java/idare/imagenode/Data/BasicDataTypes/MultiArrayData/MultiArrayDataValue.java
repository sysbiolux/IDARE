package idare.imagenode.Data.BasicDataTypes.MultiArrayData;

import idare.imagenode.Interfaces.DataSets.NodeValue;
import idare.imagenode.Properties.NodeValueType;

import java.util.Vector;


/**
 * A {@link NodeValue} for {@link MultiArrayDataSet}
 * The Value of a {@link NodeValue} is always the String representing the Sheet it is contained in.
 * @author Thomas Pfau
 *
 */
public class MultiArrayDataValue extends NodeValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Vector<Double> yvalues = new Vector<Double>();	
	
	/**
	 * Generate a new NodeValue based on a specific ColorValue (likely the sheet name).
	 * @param ColorValue The Colorvalues (strings or doubles).
	 */
	public MultiArrayDataValue(Comparable ColorValue) {
		super(false);
		type = NodeValueType.vector; 
		// TODO Auto-generated constructor stub
		value = ColorValue;
	}
	/**
	 * Set the Line Data for this {@link NodeValue}.
	 * This assumes, that this is containing Double Data.
	 * @param yValues the y Values being used.
	 */
	public void setEntryData(Vector<Double> yValues)
	{
		yvalues.clear();
		yvalues.addAll(yValues);
	}
	/**
	 * Get the Line Data stored in this value
	 * @return A Vector with the (Double) LineData for this Value.
	 */
	public Vector<Double> getEntryData()
	{
		Vector<Double> yValues = new Vector<Double>();
		yValues.addAll(yvalues);
		return yValues;
	}
	
	}
