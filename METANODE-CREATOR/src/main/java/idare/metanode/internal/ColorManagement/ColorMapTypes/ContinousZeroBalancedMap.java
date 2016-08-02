package idare.metanode.internal.ColorManagement.ColorMapTypes;

import idare.metanode.internal.ColorManagement.ColorScale;
import idare.metanode.internal.Debug.PrintFDebugger;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.JComponent;

public class ContinousZeroBalancedMap extends ContinousColorMap{
	private static final long serialVersionUID = 1001;
	boolean zerocentered = true;	
	HashMap<Double,String> translate;
	public ContinousZeroBalancedMap(double maxvalue, double minvalue,
			ColorScale cs) {
		super(maxvalue, minvalue, cs);		
		// Check whether zero is "approximately in the middle of min and maxvalue.
	
	}
	
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

		
/*		//get the fractoins used.
		Vector<Double> fracs = new Vector<Double>();
		fracs.addAll(translate.keySet());
		Collections.sort(fracs);
		//and initialize a ColorScaleLegendsLabel with a specific Layout
		JPanel ColorScaleLegendLabels = new JPanel();
		ColorScaleLegendLabels.setBackground(this.getBackground());		
		ColorScaleLegendLabels.setLayout(new ColorScaleLegendLayout(fracs.toArray(new Double[fracs.size()])));
		

		ColorScaleLegendLabels.setLayout(new ColorScaleLegendLayout(fracs.toArray(new Double[fracs.size()])));
		DescriptionPanes = new Vector<JLabel>();
		for(double val : fracs)
		{
			if(translate.containsKey(val)){
				JLabel clab = new JLabel(translate.get(val));
				DescriptionPanes.add(clab);
				ColorScaleLegendLabels.add(clab);
				clab.setBackground(this.getBackground());
			}
		}
		ColorPane = cs.getColorScalePane();
		this.setLayout(new GridLayout(2,1));
		add(ColorPane);
		add(ColorScaleLegendLabels);*/
	}	
	@Override
	public Color getColor(Comparable value)
	{
		if(value == null)
		{
			PrintFDebugger.Debugging(this, "Trying to get Color for null value");
			return new Color(0.9f,0.9f,0.9f); 
		}
		PrintFDebugger.Debugging(this, "Trying to get Color for " + value.toString());

		return super.getColor(value);
	
	}
	
	
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

	@Override
	public void setColorScale(ColorScale scale) {
		// TODO Auto-generated method stub
		resetColorData();
		cs = scale;		
		setup();
	}

	@Override
	public JComponent getColorMapComponent() {
		// TODO Auto-generated method stub
		return buildColorMapVisualisation(translate);
	}
	
}
