package idare.imagenode.Data.BasicDataTypes.ArrayData;



import idare.imagenode.GUI.Legend.Utilities.LegendSizeListener;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
/**
 * ItemDataDescription is a Panel, that manages the description of {@link ArrayNodeData} using the labels for these items.
 * @author Thomas Pfau
 *
 */
public class ArrayDataDescription extends JPanel{
	protected Vector<ItemDescriptionPane> Itemdescriptions;
	protected Insets separators = new Insets(2,5,2,5);
	protected Dimension oldsize;
	protected int maxitemwidth = 0; 
	protected GridLayout ComponentLayout; 
	protected static int MINIMAL_FONT_SIZE = 20;
	protected static int HGAP = 20;
	protected static int VGAP = 6;
	
	/**
	 * Get the rows and columns for this panel based on the available width.
	 * @param width The width to plot in.
	 * @return The dimension representing the rows and Cols for the given width.
	 */
	public Dimension getRowsAndCols(int width)
	{
		int columns =  Math.min(Math.max(width /(maxitemwidth+HGAP),1),4);
		while(columns * maxitemwidth + (columns-1) * HGAP > width & columns > 1)
		{
			columns--;
		}
		int rows = (Itemdescriptions.size() -1 ) / columns + 1;
		
		return new Dimension(columns,rows);
	}
	/**
	 * Redraw this panel.
	 */
	public void redraw()
	{
		this.removeAll();
		for(ItemDescriptionPane pane : Itemdescriptions)
		{
			//first item left aligned, second item right aligned
			add(pane);
		}
	}
	
	/**
	 * Set up the Itemdescription for a specific set of data and a given DatasetLabel.
	 * Adjust the size accoring to changes in the Legend provided.
	 * @param currentdata The {@link NodeData} to create the description from
	 * @param DataSetLabel The label of the dataset, to provide for clarification which set this description is referring to
	 * @param Legend The {@link JScrollPane} that encloses the legend this description is part of (for resizing events).
	 */
	public void setupItemDescription(NodeData currentdata, String DataSetLabel, JScrollPane Legend) {
		//First, get the size of the maximal Item to determine the number of rows.
		this.setBackground(Color.white);
		Legend.addComponentListener(new ItemDescriptionResizeListener(this));		
		ArrayNodeData ndata = (ArrayNodeData) currentdata;
		Itemdescriptions = new Vector<ItemDescriptionPane>();
		int itemposition = 1;
		int maxwidth = 0;
		
		for(int i = 0; i < ndata.getValueCount(); i++)
		{
			if(ndata.isValueSet(i))
			{
				String Label = DataSetLabel + "." + Integer.toString(itemposition) + ":";
				String ItemLabel = ((ArrayDataSet)(ndata.getDataSet())).getColumnLabel(i);
				ItemDescriptionPane pane = new ItemDescriptionPane(Label,ItemLabel);
//				System.out.println("Created a new Pane with ID: " + Label + " and Label " + ItemLabel);
				maxwidth = Math.max(pane.getMinSize(), maxwidth);
				Itemdescriptions.add(pane);
				itemposition++;
			}
			maxitemwidth = maxwidth;
			//Use a maximum of 3 description columns. Any larger amount makes it hard to read.

		}
		//once we know the maximal width, we can determine the rows and columns, based on the available scrollpane viewport.		
		int cwidth = Legend.getViewport().getWidth()-2;
		Dimension dim = getRowsAndCols(cwidth);
		int columns =  dim.width; 		
		int rows = dim.height; 

		//Set up the Layout accordingly
		ComponentLayout = new GridLayout();
		ComponentLayout.setColumns(columns);
		ComponentLayout.setRows(rows);
		ComponentLayout.setVgap(VGAP);
		if(columns == 1)
		{
			ComponentLayout.setHgap(HGAP);
		}
		else{
		ComponentLayout.setHgap(Math.max(HGAP,((cwidth - columns * maxitemwidth)-10) / (columns-1)));
		}
		setLayout(ComponentLayout);
		//and add all the itemdescriptionpanes.
		for(ItemDescriptionPane pane : Itemdescriptions)
		{
			//first item left aligned, second item right aligned
			add(pane);
		}
		this.setPreferredSize(new Dimension(IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH,rows * MINIMAL_FONT_SIZE + (rows -1) * VGAP));
	}
	/**
	 * Listener that updates the itemdescriptions based on the width of the enclosing legend.
	 * @author Thomas Pfau
	 *
	 */
	protected class ItemDescriptionResizeListener extends ComponentAdapter implements LegendSizeListener
	{
		ArrayDataDescription comp;
		public ItemDescriptionResizeListener(ArrayDataDescription desc) {
			this.comp = desc;
		}
		public void componentResized(ComponentEvent e) {
			// 			
			JScrollPane scroller = (JScrollPane) e.getComponent();
			int cwidth =  scroller.getViewport().getWidth()-2;
			//get the new Arrangement			
			Dimension newgrid = comp.getRowsAndCols(scroller.getViewport().getWidth()-2);
			comp.ComponentLayout.setColumns(newgrid.width);
			comp.ComponentLayout.setRows(newgrid.height);
			int newpadding = getNewPadding(cwidth,newgrid.width,comp.maxitemwidth, ArrayDataDescription.HGAP);
			comp.ComponentLayout.setHgap(newpadding);
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
	/**
	 * A Pane enclosing the layout of a single item description (Label and Name)
	 * @author Thomas Pfau
	 *
	 */
	protected class ItemDescriptionPane extends JPanel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JLabel ID;
		private JLabel Description;
		/**
		 * Set up the Descriptionpane using a IDString and a description string.
		 * @param IDString The id of the item this pane describes 
		 * @param DescriptionString The description of the item.
		 */
		public ItemDescriptionPane(String IDString, String DescriptionString)
		{
			this.setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
			setBackground(Color.WHITE);
			ID = new JLabel(IDString);
			ID.setBackground(Color.white);
			ID.setHorizontalAlignment(SwingConstants.LEFT);
			ID.setFont(ID.getFont().deriveFont(20f));
			//ID.setMinimumSize(new Dimension(getMinSize(ID),ID.getFontMetrics(ID.getFont()).getHeight()));
			Description = new JLabel(DescriptionString);
			Description.setBackground(Color.white);
			Description.setHorizontalAlignment(SwingConstants.RIGHT);
			Description.setFont(Description.getFont().deriveFont(20f));
			add(ID);
			add(Box.createHorizontalGlue());
			add(Description);
		}
		/**
		 * Get the minimal width necessary for this item.
		 * @return the minimal size of this item
		 */
		public int getMinSize()
		{			
			return getMinSize(ID) + getMinSize(Description) + 2;			
		}
		/**
		 * Get the minimial width necessary for a specific JLabel.
		 * @param label The label for which to determine the minimal size
		 * @return The size (in pixels) this label would need, with its current font 
		 */
		public int getMinSize(JLabel label)
		{
			FontMetrics fm = label.getFontMetrics(label.getFont());
			return fm.stringWidth(label.getText());			
		}

	}

	
}
