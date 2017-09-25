package idare.imagenode.Data.BasicDataTypes.MultiArrayData;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Data.BasicDataTypes.ArrayData.ArrayDataSet;
import idare.imagenode.GUI.Legend.Utilities.LegendSizeListener;
import idare.imagenode.GUI.Legend.Utilities.SizeAdaptableComponent;
import idare.imagenode.Interfaces.DataSets.DataSet;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
/**
 * Abstract class that provides an entry to building basic Descriptions in a Legend for given shapes.  
 * @author Thomas Pfau
 *
 */
public abstract class MultiArrayDescription extends JPanel implements SizeAdaptableComponent {
	private static final long serialVersionUID = 1001;
	private Vector<MultiArrayEntryDescriptionPane> Itemdescriptions;

	/**
	 * Create a new LineDataDescription, which is contained in a JScrollPane.
	 * The ViewPort of the JScrollPane has to show the visible part of this Itemdescription. 
	 * @param Parent The ScrollPane to create the Description in.
	 */
	public MultiArrayDescription(JScrollPane Parent)
	{
		Parent.addComponentListener(new AreaAdapter(this));
	}
		
	/**
	 * Set up the data description using a specific {@link ColorMap} to obtain the line colors and 
	 * the respective {@link DataSet} to obtain the Line names. 
	 * @param currentdata The DataSet for which to set up an item description.
	 * @param map The {@link ColorMap} to use.
	 */
	public void setupItemDescription(DataSet currentdata,ColorMap map) {
		//First, get the size of the maximal Item to determine the number of rows.			
		this.setBackground(Color.white);					
		//			FontMetrics fm = this.getFontMetrics(new Font(Font.MONOSPACED,Font.PLAIN,20));
		Itemdescriptions = new Vector<MultiArrayEntryDescriptionPane>();
		Vector<String> LineNames = new Vector<String>();
		HashMap<String,Color> colors = new HashMap<String,Color>();
		if(currentdata instanceof MultiArrayDataSet)
		{
			LineNames = ((MultiArrayDataSet)currentdata).getSetNames();
			for(String LineName : LineNames)
			{
				colors.put(LineName,map.getColor(LineName));
			}
		}
		if(currentdata instanceof ArrayDataSet)
		{
			LineNames.add(ArrayDataSet.DEFAULT_SERIES_NAME);
			colors.put(ArrayDataSet.DEFAULT_SERIES_NAME,map.getDefaultColor());
		}
		for(String LineName : LineNames)
		{
			MultiArrayEntryDescriptionPane pane = getDescriptionPane(colors.get(LineName),LineName);															
			Itemdescriptions.add(pane);			
		}
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		for(MultiArrayEntryDescriptionPane pane : Itemdescriptions)
		{
			//first item left aligned, second item right aligned
			add(pane);
			add(Box.createVerticalGlue());				
		}


	}
	
	/**
	 * Get a Description Pane for the current Entry Description, with the given color and given ID
	 * @param color the Color for the description
	 * @param EntryName The name of the entry.
	 * @return The {@link MultiArrayEntryDescriptionPane} that can be used to visualise the entry with the given name and the given color.
	 */
	public abstract MultiArrayEntryDescriptionPane getDescriptionPane(Color color, String EntryName);


	/**
	 * A Component adapter that updates the sizes to adjust to a Viewport size of a scrollpane. 
	 * @author Thomas Pfau
	 *
	 */
	private class AreaAdapter extends ComponentAdapter implements LegendSizeListener
	{
		private SizeAdaptableComponent comp;

		public AreaAdapter(SizeAdaptableComponent comp)
		{
			this.comp = comp;
		}
		@Override
		public void componentResized(ComponentEvent e) {				
			int cwidth = 0;
			JScrollPane scroller = (JScrollPane)e.getSource();
			cwidth = scroller.getViewport().getWidth()-2;
			comp.setVisibleWidth(cwidth);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.GUI.Legend.Utilities.SizeAdaptableComponent#setVisibleWidth(int)
	 */
	@Override
	public void setVisibleWidth(int width) {
		// TODO Auto-generated method stub
		for(MultiArrayEntryDescriptionPane line : Itemdescriptions)
		{
			line.updatePreferredSize(width);
		}
	}
}
