package idare.metanode.internal.ColorManagement.ColorMapTypes;

import idare.metanode.internal.ColorManagement.ColorMap;
import idare.metanode.internal.ColorManagement.ColorScale;
import idare.metanode.internal.ColorManagement.ColorUtils;
import idare.metanode.internal.Debug.PrintFDebugger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
	private GridLayout layout;
	//int maxitemwidth;
	final int HGAP = 20;
	/**
	 * Generate a Colormap based on a set of values and a given colorscale.
	 * The Values will be used in the order provided by the collection iterator.
	 * @param values
	 * @param cs
	 */
	public DiscreteColorMap(Collection<Comparable> values, ColorScale cs)
	{
		super(cs);
		setup(values);
		
	}
	
	
	private void setup(Collection<Comparable> values)
	{
		//clear the container.
		cs.setColorCount(values.size());
		colors =  new HashMap<>();
		//We have to determine the maximal size of a value.
		//PrintFDebugger.Debugging(this,"Creating a new discrete Colorscale with values " );		
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
			PrintFDebugger.Debugging(this,"Adding Color " + i/maxval+  " for value " + val  );
			colors.put(val,cs.getColor(i/maxval));
			//ColorItemDescription desc = new ColorItemDescription(colors.get(val), ": " + val.toString());
			//add(desc);
						
		}		
		//add(desc);				
	}
	
	
	@Override
	public Color getColor(Comparable Value) {
		
		//PrintFDebugger.Debugging(this,"Requesting Color for Value " + Value );		

		if(Value == null)
		{
			PrintFDebugger.Debugging(this, "Trying to get Color for null value");
			return new Color(0.9f,0.9f,0.9f); 
		}
		PrintFDebugger.Debugging(this, "Trying to get Color for " + Value.toString() + " ; Got Color " + colors.get(Value));		
		return colors.get(Value);
	}
	
	
	private class ColorItemDescription extends JPanel
	{
		private JLabel Name;
		private JPanel ColorItem;
		public ColorItemDescription(Color itemcolor,String labelName) {
			// TODO Auto-generated constructor stub
			this.setLayout(new GridLayout(1,2));			
			setBackground(Color.WHITE);
			Name = new JLabel(labelName);
			Name.setBackground(Color.white);
			Name.setHorizontalAlignment(SwingConstants.LEFT);
			Name.setFont(Name.getFont().deriveFont(20f));
			ColorItem = new ShapePanel(itemcolor);
			ColorItem.setBackground(Color.white);
			//ColorItem.setHorizontalAlignment(SwingConstants.RIGHT);
			//Description.setFont(Description.getFont().deriveFont(20f));
			//ID.addComponentListener(new FontResizeListener());
			//Description.addComponentListener(new FontResizeListener());
			add(ColorItem);
			add(Name);
		}
		public int getMinWidth()
		{
			FontMetrics fm = Name.getFontMetrics(Name.getFont());
			return fm.stringWidth(Name.getText()) + 11; //the minimum size of the rectangle should be 10
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

	@Override
	public void setColorScale(ColorScale scale) {
		// TODO Auto-generated method stub
		cs = scale;
		Vector<Comparable> colorvalues = new Vector<Comparable>();
		colorvalues.addAll(colors.keySet());
		setup(colorvalues);
	}

	@Override
	public JComponent getColorMapComponent() {
		PrintFDebugger.Debugging(this, "Setting up component");
		JComponent colordesc = new JComponent() {
		};
		int maxitemwidth = Integer.MIN_VALUE;
		colordesc.setBorder(null); 
		int rows = (colors.keySet().size()) / 3 + 1;
		int columns = Math.max(colors.keySet().size()+1,3);
		layout = new GridLayout(rows,columns);
		PrintFDebugger.Debugging(this, "Setting Layout");
		colordesc.setLayout(layout);
		Vector<Comparable> valuecopy = new Vector<Comparable>();
		valuecopy.addAll(colors.keySet());
		Collections.sort(valuecopy);
		double maxval = valuecopy.size()-1;
		PrintFDebugger.Debugging(this, "Generating and adding Color Item descriptions panes");
		for(int i = 0; i < valuecopy.size(); i++)
		{
			
			Comparable val = valuecopy.get(i);
			//PrintFDebugger.Debugging(this,"Adding Color " + i/maxval+  " for value " + val  );
			//colors.put(val,cs.getColor(i/maxval));
			ColorItemDescription desc = new ColorItemDescription(colors.get(val), ": " + val.toString());
			colordesc.add(desc);
			maxitemwidth = Math.max(maxitemwidth,desc.getMinWidth());			
		}
		PrintFDebugger.Debugging(this, "Adding NA description");
		ColorItemDescription desc = new ColorItemDescription(new Color(0.9f,0.9f,0.9f), ": NA");
		colordesc.add(desc);		
		maxitemwidth = Math.max(maxitemwidth,desc.getMinWidth());
		
		// TODO Auto-generated method stub
		return colordesc;
	}

}
