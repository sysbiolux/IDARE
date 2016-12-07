package idare.imagenode.ColorManagement.ColorMapTypes;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.ColorManagement.ColorScale;
import idare.imagenode.ColorManagement.ColorUtils;
import idare.imagenode.GUI.Legend.Utilities.LegendSizeListener;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
/**
 * A Colormap that can provide colors for a discrete number of values (i.e. a limited well defined number of items)
 * The values can be anything comparable, as long as they are of the same type. 
 * @author Thomas Pfau
 *
 */
public class DiscreteColorMap extends ColorMap{
	private static final long serialVersionUID = 1001;
	HashMap<Comparable,Color> colors; 
	//Put at max 3 items per row and fill the first row first.	
	//private GridLayout layout;
	final int MINIMAL_FONT_SIZE = 20;

	/**
	 * Generate a Colormap based on a set of values and a given colorscale.
	 * The Values will be used in the order provided by the collection iterator.
	 * The map will only provide valid colors for the values provided during setup.
	 * @param values the discrete values to use
	 * @param cs The {@link ColorScale} to use
	 */
	public DiscreteColorMap(Collection<Comparable> values, ColorScale cs)
	{
		super(cs);
		//PrintFDebugger.Debugging(this, "building new Colormap, super consturctor worked");
		setup(values);
		//PrintFDebugger.Debugging(this, "building new Colormap, setup worked");
		
	}
	
	/**
	 * setup using a Collection of discrete values.
	 * @param values The Values to use
	 */
	private void setup(Collection<Comparable> values)
	{
		//clear the container.
		cs.setColorCount(values.size());
		colors =  new HashMap<>();
		//We have to determine the maximal size of a value.
		Vector<Comparable> valuecopy = new Vector<Comparable>();
		valuecopy.addAll(values);
		Collections.sort(valuecopy);
		//generate one Double value per value in the provided collection
		double maxval = valuecopy.size()-1;
		
		for(int i = 0; i < valuecopy.size(); i++)
		{
			Comparable val = valuecopy.get(i);
			colors.put(val,cs.getColor(i/maxval));						
		}		

	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorMap#getColor(java.lang.Comparable)
	 */
	@Override
	public Color getColor(Comparable Value) {
		

		if(Value == null)
		{
			return new Color(0.9f,0.9f,0.9f); 
		}
		return colors.get(Value);
	}
	
	/**
	 * This is a helper class that holds a label and its associated color Shape
	 * @author Thomas Pfau
	 *
	 */
	private class ColorItemDescription
	{
		public JLabel Name;
		public JPanel ColorItem;
		public ColorItemDescription(Color itemcolor,String labelName) {
			Name = new JLabel(labelName);
			Name.setBackground(Color.white);
			Name.setHorizontalAlignment(SwingConstants.LEFT);
			Name.setFont(Name.getFont().deriveFont(20f));
			ColorItem = new ShapePanel(itemcolor);
			ColorItem.setBackground(Color.white);
			ColorItem.setPreferredSize(new Dimension(MINIMAL_FONT_SIZE+1,MINIMAL_FONT_SIZE+1));
			
		}
		/**
		 * get the minimal width of this description. (which is the  minimal size of the color indicator + the minimal size of the Label)
		 * @return The minimal width of this description in pixels
		 */
		public int getMinWidth()
		{
			FontMetrics fm = Name.getFontMetrics(Name.getFont());
			return fm.stringWidth(Name.getText()) + MINIMAL_FONT_SIZE + 1; 
		}
	}
	/**
	 * A JPanel that contains is painting a specific shape at a  with a given color 
	 * @author Thomas Pfau
	 *
	 */
	private class ShapePanel extends JPanel
	{
		private Color ShapeColor;
		
		/**
		 * Basic constructor using a given color. 
		 * @param ShapeColor
		 */
		public ShapePanel(Color ShapeColor)
		{
			super();
			this.ShapeColor = ShapeColor;
			
		}
		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			Dimension size = getSize();
			int ext = Math.min(size.height - 2, size.width - 2);
			//System.out.println("Current width is: " + size.width);
			int xpos = size.width - ext - 1;
			int ypos = size.height/2 - ext/2;
			g2d.setColor(ShapeColor);
			Ellipse2D shape = new Ellipse2D.Double(xpos,ypos,ext,ext); 
			g2d.fill(shape);
			g2d.setColor(ColorUtils.getContrastingColor(ShapeColor));
			g2d.draw(shape);
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorMap#setColorScale(idare.imagenode.internal.ColorManagement.ColorScale)
	 */
	@Override
	public void setColorScale(ColorScale scale) {
		// TODO Auto-generated method stub
		cs = scale;
		Vector<Comparable> colorvalues = new Vector<Comparable>();
		colorvalues.addAll(colors.keySet());
		setup(colorvalues);
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.ColorManagement.ColorMap#getColorMapComponent()
	 */
	@Override
	public JComponent getColorMapComponent(JScrollPane Legend) {
		ColorComponent colordesc = new ColorComponent();
		colordesc.setBackground(Color.WHITE);
		int maxitemwidth = Integer.MIN_VALUE;
		colordesc.setBorder(null); 
		colordesc.setLayout(new GridBagLayout());
		Vector<Comparable> valuecopy = new Vector<Comparable>();
		valuecopy.addAll(colors.keySet());
		Collections.sort(valuecopy);
		for(int i = 0; i < valuecopy.size(); i++)
		{
			
			Comparable val = valuecopy.get(i);
			ColorItemDescription desc = new ColorItemDescription(colors.get(val), ": " + val.toString());
			colordesc.addColorComp(desc);
			maxitemwidth = Math.max(maxitemwidth,desc.getMinWidth());			
		}
		
		ColorItemDescription desc = new ColorItemDescription(new Color(0.9f,0.9f,0.9f), ": NA");
		colordesc.addColorComp(desc);		
		maxitemwidth = Math.max(maxitemwidth,desc.getMinWidth());
		colordesc.maxitemwidth = maxitemwidth;
		ColorScaleResizer resizer = new ColorScaleResizer(colordesc);
		Legend.addComponentListener(resizer);
		resizer.componentResized(new ComponentEvent(Legend, ComponentEvent.COMPONENT_RESIZED));		
		return colordesc;
	}
	
	private class ColorComponent extends JPanel
	{
		public Dimension LayoutDimensions;
		public int maxitemwidth;
		public int itemcount = 0;

		private Vector<ColorItemDescription> Itemdescriptions = new Vector<ColorItemDescription>();
		private Dimension getRowsAndCols(int width)
		{
			int columns =  Math.min(Math.max(width /(maxitemwidth),1),3);
			while(columns * maxitemwidth > width & columns > 1)
			{
				columns--;
			}
			int rows = (itemcount -1 ) / columns + 1;
			return new Dimension(columns,rows);
		}
		
		/**
		 * Redraw this panel.
		 */
		public void redraw()
		{
			this.removeAll();
			int col = 0;
			int row = 0;
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			for(ColorItemDescription pane : Itemdescriptions)
			{
				//first item left aligned, second item right aligned
				gbc.gridx = col;
				gbc.gridy = row;
				gbc.weightx = MINIMAL_FONT_SIZE;
				add(pane.ColorItem,gbc);
				col++;
				gbc.gridx = col;
				gbc.weightx = maxitemwidth;
				add(pane.Name,gbc);
				col++;
				row+= col / (2*LayoutDimensions.width);
				col = col % (2*LayoutDimensions.width);
				
			}
		}
		/**
		 * Add the given {@link ColorItemDescription} to this {@link ColorMap}.
		 * @param comp
		 */
		public void addColorComp(ColorItemDescription comp)
		{
			Itemdescriptions.add(comp);
			itemcount++;
		}
		
	}
	
	private class ColorScaleResizer extends ComponentAdapter implements LegendSizeListener
	{
		ColorComponent comp;
		
		public ColorScaleResizer(ColorComponent comp)
		{
			this.comp = comp;
		}
		public void componentResized(ComponentEvent e) {
			//Get the width of the current component 			
			JScrollPane scroller = (JScrollPane) e.getComponent();
			//get the new Arrangement						
			comp.LayoutDimensions = comp.getRowsAndCols(scroller.getViewport().getWidth()-2);								
			comp.redraw();
		}
				
	}

}
