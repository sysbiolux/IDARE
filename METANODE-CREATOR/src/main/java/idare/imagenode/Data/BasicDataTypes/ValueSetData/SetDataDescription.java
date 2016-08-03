package idare.imagenode.Data.BasicDataTypes.ValueSetData;

import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.internal.ColorManagement.ColorMap;
import idare.imagenode.internal.GUI.Legend.Utilities.LegendSizeListener;
import idare.imagenode.internal.GUI.Legend.Utilities.SizeAdaptableComponent;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
/**
 * Class to Generate Descriptions for Lines 
 * @author Thomas Pfau
 *
 */
public abstract class SetDataDescription extends JPanel implements SizeAdaptableComponent {
	private static final long serialVersionUID = 1001;
	private Vector<SetEntryDescriptionPane> Itemdescriptions;

	/**
	 * Create a new LineDataDescription, which is contained in a JScrollPane.
	 * The ViewPort of the JScrollPane has to show the visible part of this Itemdescription. 
	 * @param Parent
	 */
	public SetDataDescription(JScrollPane Parent)
	{
		Parent.addComponentListener(new AreaAdapter(this));
	}
		
	/**
	 * Set up the data description using a specific {@link ColorMap} to obtain the line colors and 
	 * the respective {@link DataSet} to obtain the Line names. 
	 * @param currentdata
	 * @param map
	 */
	public void setupItemDescription(ValueSetDataSet currentdata,ColorMap map) {
		//First, get the size of the maximal Item to determine the number of rows.			
		this.setBackground(Color.white);					
		//			FontMetrics fm = this.getFontMetrics(new Font(Font.MONOSPACED,Font.PLAIN,20));
		Itemdescriptions = new Vector<SetEntryDescriptionPane>();		
		for(String LineName : currentdata.getSetNames())
		{
			SetEntryDescriptionPane pane = getDescriptionPane(map.getColor(LineName),LineName);															
			Itemdescriptions.add(pane);			
		}
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		for(SetEntryDescriptionPane pane : Itemdescriptions)
		{
			//first item left aligned, second item right aligned
			add(pane);
			add(Box.createVerticalGlue());				
		}


	}
	
	/**
	 * Get a Description Pane for the current Entry Description, with the given color and given ID
	 * @param color - the Color for the description
	 * @param EntryName - The name of the entry.
	 * @return
	 */
	public abstract SetEntryDescriptionPane getDescriptionPane(Color color, String EntryName);


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
		for(SetEntryDescriptionPane line : Itemdescriptions)
		{
			line.updatePreferredSize(width);
		}
	}
}
