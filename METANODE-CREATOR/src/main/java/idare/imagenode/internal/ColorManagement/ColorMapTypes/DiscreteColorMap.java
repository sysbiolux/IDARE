package idare.imagenode.internal.ColorManagement.ColorMapTypes;

import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.internal.ColorManagement.ColorMap;
import idare.imagenode.internal.ColorManagement.ColorScale;
import idare.imagenode.internal.ColorManagement.ColorUtils;
import idare.imagenode.internal.GUI.Legend.Utilities.LegendSizeListener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BoxLayout;
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
//	int maxitemwidth;
	final int HGAP = 5;
	final int VGAP = 6;
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
		setup(values);
		
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
		int rows = (valuecopy.size()) / 3 + 1;
		int columns = Math.max(valuecopy.size()+1,3);
		//layout = new GridLayout(rows,columns);
		//this.setLayout(layout);
		
		for(int i = 0; i < valuecopy.size(); i++)
		{
			Comparable val = valuecopy.get(i);
			colors.put(val,cs.getColor(i/maxval));
			
//			ColorItemDescription desc = new ColorItemDescription(colors.get(val), ": " + val.toString());
//			maxitemwidth = Math.max(maxitemwidth, desc.getMinWidth());
						
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
	 * An itemized Color Description, assigning colors to the provided values.
	 * @author Thomas Pfau
	 *
	 */
	private class ColorItemDescription extends JPanel
	{
		private JLabel Name;
		private JPanel ColorItem;
		public ColorItemDescription(Color itemcolor,String labelName) {
			// TODO Auto-generated constructor stub
			this.setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
			setBackground(Color.WHITE);
			Name = new JLabel(labelName);
			Name.setBackground(Color.white);
			Name.setHorizontalAlignment(SwingConstants.LEFT);
			Name.setFont(Name.getFont().deriveFont(20f));
			ColorItem = new ShapePanel(itemcolor);
			ColorItem.setBackground(Color.white);
			add(ColorItem);
			add(Name);
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
		colordesc.HGAP = this.HGAP;
		colordesc.setBackground(Color.WHITE);
		int maxitemwidth = Integer.MIN_VALUE;
		colordesc.setBorder(null); 
		int rows = (colors.keySet().size()) / 3 + 1;
		int columns = Math.max(colors.keySet().size()+1,3);
		GridLayout layout = new GridLayout(rows,columns);
		colordesc.setLayout(layout);
		Vector<Comparable> valuecopy = new Vector<Comparable>();
		valuecopy.addAll(colors.keySet());
		Collections.sort(valuecopy);
		//double maxval = valuecopy.size()-1;
		for(int i = 0; i < valuecopy.size(); i++)
		{
			
			Comparable val = valuecopy.get(i);
			//colors.put(val,cs.getColor(i/maxval));
			ColorItemDescription desc = new ColorItemDescription(colors.get(val), ": " + val.toString());
			colordesc.addColorComp(desc);
			maxitemwidth = Math.max(maxitemwidth,desc.getMinWidth());			
		}
		
		ColorItemDescription desc = new ColorItemDescription(new Color(0.9f,0.9f,0.9f), ": NA");
		colordesc.addColorComp(desc);		
		maxitemwidth = Math.max(maxitemwidth,desc.getMinWidth());
		colordesc.maxitemwidth = maxitemwidth;
		colordesc.componentLayout = layout;
		ColorScaleResizer resizer = new ColorScaleResizer(colordesc);
		Legend.addComponentListener(resizer);
		resizer.componentResized(new ComponentEvent(Legend, ComponentEvent.COMPONENT_RESIZED));
		
		return colordesc;
	}
	
	private class ColorComponent extends JPanel
	{
		public GridLayout componentLayout;
		public int maxitemwidth;
		public int itemcount = 0;
		public int HGAP;
		private Vector<ColorItemDescription> Itemdescriptions = new Vector<ColorItemDescription>();
		private Dimension getRowsAndCols(int width)
		{
			int columns =  Math.min(Math.max(width /(maxitemwidth+HGAP),1),3);
			while(columns * maxitemwidth + (columns-1) * HGAP > width & columns > 1)
			{
				columns--;
			}
			int rows = (itemcount -1 ) / columns + 1;
			System.out.println("For a width of " + width + " we calculated " + columns + " columns and " + rows + " rows with a max item width of " + maxitemwidth);
			return new Dimension(columns,rows);
		}
		
		/**
		 * Redraw this panel.
		 */
		public void redraw()
		{
			this.removeAll();
			for(ColorItemDescription pane : Itemdescriptions)
			{
				//first item left aligned, second item right aligned
				add(pane);
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
			add(comp);			
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
			int cwidth =  scroller.getViewport().getWidth()-2;
			//get the new Arrangement			
			
			Dimension newgrid = comp.getRowsAndCols(scroller.getViewport().getWidth()-2);
			comp.componentLayout.setColumns(newgrid.width);
			comp.componentLayout.setRows(newgrid.height);
			int newpadding = getNewPadding(cwidth,newgrid.width,comp.maxitemwidth, comp.HGAP);
			System.out.println("The new padding is " + newpadding + " at a width of " + cwidth + " with a maximal item width of " + comp.maxitemwidth + " and a Gap-Size of " + comp.HGAP + " and " + newgrid.width + " columns");
			comp.componentLayout.setHgap(newpadding);
			comp.setPreferredSize(new Dimension(IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH,newgrid.height * MINIMAL_FONT_SIZE + (newgrid.height -1) * VGAP));
			comp.redraw();
		}
		
		/**
		 * Get the new padding based on  the current width, the number of columns used, the maximal item width
		 * and the minimal padding size. 
		 * @param width The currently available width 
		 * @param columncount the number of columns used
		 * @param maxitemwidth The maximal width of an individual item
		 * @param minGap The minimal Gap between two items
		 * @return The new padding based on the provided information
		 */
		private int getNewPadding(int width, int columncount,int maxitemwidth, int minGap)
		{
			if(columncount <= 1)
			{
				return minGap;
			}
			else
			{
				return Math.max(minGap,((width - columncount * maxitemwidth)-10) / (columncount-1));
			}
		}
	}

}
