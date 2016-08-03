package idare.metanode.internal.ColorManagement.ColorMapTypes;

import idare.metanode.Properties.METANODEPROPERTIES;
import idare.metanode.internal.ColorManagement.ColorMap;
import idare.metanode.internal.ColorManagement.ColorScale;
import idare.metanode.internal.Debug.PrintFDebugger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
/**
 * An Abstract class representing general continous color maps and providing some functionality for such maps.
 * @author Thomas Pfau
 *
 */
public abstract class ContinousColorMap extends ColorMap{
	private static final long serialVersionUID = 1001;
	protected double maxval;
	protected double minval;
	protected double range;
	protected boolean oddrange;	
	protected JPanel ColorPane;
	protected Vector<JLabel> DescriptionPanes;
	protected float[] ColorScaleFractions;
	protected Color[] ColorScaleColors;	
	/**
	 * Generate a continous color map based on a color scale and a minimal and maximal value.
	 * @param maxvalue - the maximum value this map shall be able to map
	 * @param minvalue - the minimum this map shall be able to map
	 * @param cs - the colorscale this map should use for color generation.
	 */
	public ContinousColorMap(double maxvalue, double minvalue, ColorScale cs) {
		super(cs);
		minval = minvalue;
		maxval = maxvalue;
		range = maxval - minval;
		oddrange = ((maxval / range) > 1e3);
		setup();			
	}
	/**
	 * Clear all color related data i.e. ColorScale Colors, fRactions and Panes.
	 */
	protected void resetColorData()
	{
		ColorScaleFractions = null;
		ColorScaleColors = null;
		ColorPane = null;
		DescriptionPanes.clear();
	}
	/**
	 * Setup this Color Map. 
	 */
	protected void setup()
	{
		HashMap<Double,Double> fractionToValue = new HashMap<Double, Double>();
		float[] vals;
		//If this is a continous color map, than we will simlply use min and maxval if its an odd range.
		if(!oddrange)
		{
			//PrintFDebugger.Debugging(this,"Creating values for non odd range");
			fractionToValue.put(0.,minval);
			fractionToValue.put(getCenterPosition(),getCenterValue());
			fractionToValue.put(1.,maxval);
		}
		else
		{
			fractionToValue.put(0.,minval);
			fractionToValue.put(1.,maxval);

		}				
		//Get labels associated with the Higher and lower value.
		HashMap<Double,String> translate = GetLabelForNumbers(fractionToValue);
		
	}
	
	/**
	 * Generate the Color Scale Layout and visual representation based on the Labels used for the respective positions.
	 * @param translate
	 */
	protected JComponent buildColorMapVisualisation(HashMap<Double,String> translate)
	{
		PrintFDebugger.Debugging(this, "Initializing  Component");
		JComponent colordesc = new JComponent() {
		};
		//get the fractions used.
		Vector<Double> fracs = new Vector<Double>();
		fracs.addAll(translate.keySet());
		Collections.sort(fracs);
		//and initialize a ColorScaleLegendsLabel with a specific Layout
		PrintFDebugger.Debugging(this, "Generating Panel");
		JPanel ColorScaleLegendLabels = new JPanel();
		ColorScaleLegendLabels.setBackground(Color.WHITE);
		PrintFDebugger.Debugging(this, "Setting Panel Layout");
		ColorScaleLegendLabels.setLayout(new ColorScaleLegendLayout(fracs.toArray(new Double[fracs.size()])));
		
		//And set up the description panes.
		PrintFDebugger.Debugging(this, "Setting up DescriptionPanes");
		DescriptionPanes = new Vector<JLabel>();
		for(double val : fracs)
		{
			if(translate.containsKey(val)){
				JLabel clab = new JLabel(translate.get(val));
				DescriptionPanes.add(clab);
				ColorScaleLegendLabels.add(clab);
				clab.setBackground(Color.WHITE);
			}
		}
		
		//This pane now has a two row grid layout with the ColorScalePane of its colorscale and the Description. 
		ColorPane = cs.getColorScalePane();
		GridLayout gl = new GridLayout(2,1);
		gl.setHgap(0);
		gl.setVgap(0);
		colordesc.setLayout(gl);
		colordesc.setBorder(null);
		PrintFDebugger.Debugging(this, "Adding Colorpane");
		colordesc.add(ColorPane);
		PrintFDebugger.Debugging(this, "Adding Labels");
		colordesc.add(ColorScaleLegendLabels);
		return colordesc;
	
	}
	/**
	 * Get the center value (i.e. the value in the middle between the minimum and maximum values mapped by this map.
	 * @return the center value
	 */
	protected double getCenterValue()
	{
		return minval + (maxval - minval)/2;
	}
	/**
	 * Create Strings Representations for the values in a Map which are Doubles. 
	 * This is a helper function to allow the generation of sensible numbers for legends. 
	 * @param fractions - A Translation between double keys to double values. The values will be used to generate strings.
	 * 	@return a Map, that maps the keys of the input to the string representation of the values of the input.
	 */
	protected HashMap<Double,String> GetLabelForNumbers(HashMap<Double, Double> fractions)	
	{
		HashMap<Double,String> rv = new HashMap<Double, String>();
		//generate a Vector with all fractions.
		Vector<Double> fractionValueArray = new Vector<>();
		fractionValueArray.addAll(fractions.values());			
		Collections.sort(fractionValueArray);
		//Get the strings to the fractions
		HashMap<Double,String> dispvals = ColorMap.getDisplayStrings(fractionValueArray.toArray(new Double[fractionValueArray.size()]));		
		for(Double f: fractions.keySet())
		{
			rv.put(f, dispvals.get(fractions.get(f)));
		}
		return rv;
	}
	@Override
	public Color getColor(Comparable Value)
	{
		//In a continous color map this value ALWAYS has to be a Double.

		Double DoubleValue = (Double)Value;
		if(DoubleValue == null)
		{
			return new Color(0.9f,0.9f,0.9f); 
		}

		double entry = (DoubleValue - minval) / (maxval - minval);
		return cs.getColor(entry);
	}


	/**
	 * Get the preferred size to be used by this color scale.
	 * Commonly this is 60 px heigh and 300 px wide, but, if we have an odd range of values, 
	 * the map might need more space to display the appropriate labels, thus it will demand more room (80px height)
	 * @return the preferred Dimension for visualisation.
	 */
	public Dimension getPreferredColorScaleSize()
	{
		if(oddrange)
		{
			return new Dimension(METANODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH,60);

		}
		else{
			return new Dimension(METANODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH,80);
		}
	}

	/**
	 * Get the central position of the color map (on a 0-1 range). This is the position that returns the value of the {@link ColorScale}
	 *  when 0.5 is requested from a basic colorscale used for this map. 
	 * @return the central position of the {@link ColorMap}
	 */
	public double getCenterPosition()
	{
//		PrintFDebugger.Debugging(this,"Getting Center position in Parent class");
		return 0.5 ;
	}	
	
	/**
	 * A Class that provides layout functionality for ColorScale Legends.
	 * This layout will distribute the labels for multiple values along the colorscale 
	 * @author Thomas Pfau
	 *
	 */
	protected class ColorScaleLegendLayout implements LayoutManager,Serializable
	{

		Double[] xpositions;		
		/**
		 * Generate a layout based on an array of fractions (these fractions have to rage from 0.0 to 1.0 in ascending order!
		 * @param fractions
		 */
		public ColorScaleLegendLayout(Double[] fractions) {
			// TODO Auto-generated constructor stub
			xpositions = fractions;
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
			// TODO Auto-generated method stub

		}
		@Override
		public void layoutContainer(Container parent) {
			//Determine the maximal dimensions and the number of components present in the container.
			Insets insets = parent.getInsets();
			int maxWidth = parent.getWidth()
					- (insets.left + insets.right);
			int maxHeight = parent.getHeight()
					- (insets.top + insets.bottom);
			int nComps = parent.getComponentCount();
			
			//at most 1 row if we have only 2 components, otherwise there can be more rows.
			int nRows = Math.max(Math.min(nComps/2,Math.min(2, maxHeight/20)),1) ;
			int fontSize = Math.min(maxHeight, 20);

			int[] xpos = new int[nRows];
			int[] xmaxpos = new int[nRows];
			for(int i = 0; i < xmaxpos.length; i++)
			{
				xmaxpos[i] = maxWidth;
			}
			int rowHeight = maxHeight / nRows;	        
			//lets see whether we have overlaps between the different items assuming the current number of rows
			// we only need to do this for the last entry.
			int lastentry = nComps;
			if(nComps > 0)
			{
				JLabel lastlabel = (JLabel)parent.getComponent(lastentry -1);
				lastlabel.setVisible(true);
				lastlabel.setFont(lastlabel.getFont().deriveFont((float) fontSize));
				FontMetrics fm = lastlabel.getFontMetrics(lastlabel.getFont());
				//the last label has to be there.
//				PrintFDebugger.Debugging(this, "The text of the last label is" + lastlabel.getText());
				int lwidth = fm.stringWidth(lastlabel.getText());
				lastlabel.setBounds(maxWidth-lwidth,lastentry%nRows*rowHeight,lwidth,rowHeight);
				xmaxpos[lastentry%nRows] = maxWidth - (fm.stringWidth(lastlabel.getText()) + 5);

				for(int i = 0; i < nComps - nRows ; i++)
				{
					JLabel current = (JLabel)parent.getComponent(i);
					fm = current.getFontMetrics(current.getFont().deriveFont((float) fontSize));	        	
					if(i < nRows)
					{
						int labelwidth = fm.stringWidth(current.getText());
						xpos[i%nRows] = labelwidth + 5;	       	        		
						current.setBounds((int)(maxWidth*xpositions[i] - (i%nRows * labelwidth/2)),i%nRows * rowHeight,labelwidth,rowHeight);
						current.setFont(current.getFont().deriveFont((float) fontSize));
						current.setVisible(true);
					}
					else
					{
						int centerlocation = (int) (xpositions[i] * maxWidth);
						int width = fm.stringWidth(current.getText());
						if(xpos[i%nRows] > centerlocation - width / 2 || xmaxpos[i%nRows] < centerlocation + width/2 )
						{
							//the item is unplaceable in its respective row... skip it.
							current.setVisible(false);	        			

						}
						else
						{
							xpos[i%nRows] =   centerlocation + width / 2 + 5;
							current.setBounds((int)(maxWidth*xpositions[i]- width/2),i%nRows * rowHeight,width,rowHeight);
							current.setVisible(true);
							current.setFont(current.getFont().deriveFont((float) fontSize));
						}
					}

				}	        
			}
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			int nComps = parent.getComponentCount();
			if(nComps > 0)
			{
				JLabel first = (JLabel)parent.getComponent(0);
				JLabel last = (JLabel)parent.getComponent(nComps -1);
				int MinfontSize = 10;
				FontMetrics fm = last.getFontMetrics(last.getFont().deriveFont((float) MinfontSize));
				int minwidth  = fm.stringWidth(first.getText() + last.getText()) + 5; 
				return new Dimension(minwidth,MinfontSize);
			}
			else{
				return new Dimension(0,0);
			}
		}
		@Override
		public Dimension preferredLayoutSize(Container parent) {
			int nComps = parent.getComponentCount();
			if(nComps > 0)
			{
				JLabel first = (JLabel)parent.getComponent(0);

				int fontSize = 20;
				Font optFont = first.getFont().deriveFont((float) fontSize);
				FontMetrics fm = first.getFontMetrics(optFont);
				int width = fm.stringWidth(first.getText());
				for(int i = 1; i < nComps; i ++)
				{
					JLabel current = (JLabel) parent.getComponent(i);
					width += 5 + fm.stringWidth(current.getText());
				}
				//PrintFDebugger.Debugging(this, "new preferred Size the layout is " + width + " /" + fontSize);
				return new Dimension(width,fontSize);
			}
			else{
				//PrintFDebugger.Debugging(this, "new preferred Size the layout is " + 0 + " /" + 0);
				return new Dimension(0,0);
			}
		}
		@Override
		public void removeLayoutComponent(Component comp) {
			// TODO Auto-generated method stub

		}

	}
	
}
