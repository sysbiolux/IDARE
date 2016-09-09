package idare.imagenode.ColorManagement.ColorMapTypes;

import idare.imagenode.ColorManagement.ColorScale;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

public class ContinousZeroBalancedMap extends ContinousColorMap{
	private static final long serialVersionUID = 1001;
	boolean zerocentered = true;	
	HashMap<Double,String> translate;

	/**
	 * A Constructor using a minimum and maximum value along with a given colorscale
	 * @param maxvalue The minimum value to represent
	 * @param minvalue The maximum value to represent
	 * @param cs The {@link ColorScale} to use
	 */
	public ContinousZeroBalancedMap(double maxvalue, double minvalue,
			ColorScale cs) {
		super(maxvalue, minvalue, cs);		
		// Check whether zero is "approximately in the middle of min and maxvalue.

	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorMapTypes.ContinousColorMap#setup()
	 */
	@Override
	protected void setup()
	{
		//LEts test, whether we can use a zero-centered map. (i.e. if the maxvalue and minvalue are "around" 0.
		zerocentered = (minval < 0 && maxval > 0) && Math.abs( Math.log10(maxval) - Math.log10(Math.abs(minval)) ) < 0.5;

		HashMap<Double,Double> fractionToValue = new HashMap<Double, Double>();

		//Simply use min and maxval if its an odd range.
		if(!oddrange)
		{
			fractionToValue.put(0.,minval);
			fractionToValue.put(getCenterPosition(),getCenterValue());
			fractionToValue.put(1.,maxval);
		}
		else
		{
			fractionToValue.put(0.,minval);
			//fractionToValue.put((float)getCenterPosition(),getCenterValue());
			fractionToValue.put(1.,maxval);

		}				
		//Get labels associated with the Higher and lower value.
		translate = GetLabelForNumbers(fractionToValue);

		//IF it is zerocentered, we have to move the colorscale to be zero-centered
		if(zerocentered)
		{
			cs.movePointOnScale(0.5f, (float)getCenterPosition());
		}

	}	
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorMapTypes.ContinousColorMap#getColor(java.lang.Comparable)
	 */
	@Override
	@SuppressWarnings("rawtypes") 
	public Color getColor(Comparable value)
	{
		if(value == null)
		{
			return new Color(0.9f,0.9f,0.9f); 
		}

		return super.getColor(value);

	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorMapTypes.ContinousColorMap#getCenterPosition()
	 */
	@Override
	public double getCenterPosition()
	{
		if(zerocentered)
		{	//if its zerocentered, this is shifted.
			return Math.abs(minval) / (maxval -minval);		
		}
		else
		{
			return super.getCenterPosition();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorMapTypes.ContinousColorMap#getCenterValue()
	 */
	@Override
	public double getCenterValue()
	{
		//if its zerocentered, this is shifted.
		if(zerocentered)
		{
			return 0;
		}
		else
		{
			return super.getCenterValue();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorMap#setColorScale(idare.imagenode.internal.ColorManagement.ColorScale)
	 */
	@Override
	public void setColorScale(ColorScale scale) {
		resetColorData();
		cs = scale;		
		setup();
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorMap#getColorMapComponent()
	 */
	@Override
	public JComponent getColorMapComponent(JScrollPane Legend) {
		return buildColorMapVisualisation(translate,Legend);
	}

}
