package idare.imagenode.internal.Layout.Manual.GUI;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboPopup;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.ColorManagement.ColorScalePane;
import idare.imagenode.Utilities.ColorScalePopupAdjuster;
import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;

public class ColorToolBarEntry extends JPanel{

	JComboBox<ColorScalePane> colorselector;
	HashMap<ColorScalePane,ColorMap> colormapping; 
	JLabel colorLabel = new JLabel("Colorscale:");
	ColorScalePopupAdjuster cpa;

	public ColorToolBarEntry()
	{
		colorselector = new JComboBox<>();
		colorselector.setPreferredSize(new Dimension(200, 50));
		colorselector.setRenderer(new ColorBoxRenderer());
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(colorLabel);
		this.add(colorselector);	
	}

	public void updateColorScalePane(DataSetLayoutInfoBundle bundle)
	{
		if(bundle != null)
		{
			
			Vector<ColorScalePane> coloroptions = new Vector<>();
			//this.colorselector.removeAllItems();		
			colormapping = new HashMap<>();
			ColorScalePane selectedpane = null; 
			for(ColorMap map : bundle.dataset.getColorMapOptions())
			{
				ColorScalePane pane = map.getColorScalePane(); 
				coloroptions.add(pane);
				colormapping.put(pane,map);
//				PrintFDebugger.Debugging(this, "The original map was " + bundle.map + " while the new map is " + map);
				if(map == bundle.colormap)
				{
					
					selectedpane = pane;
				}
			}
			colorselector.setModel(new DefaultComboBoxModel<>(coloroptions));
			if(selectedpane != null)
			{
				colorselector.setSelectedItem(selectedpane);
			}
			BasicComboPopup popup = ((BasicComboPopup)colorselector.getUI().getAccessibleChild(colorselector,0));
			popup.getList().setFixedCellHeight(30);
			popup.getList().setVisibleRowCount(coloroptions.size());
			popup.getList().setFixedCellHeight(30*coloroptions.size());		
			JScrollPane c = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, popup.getList());
			c.setPreferredSize(popup.getList().getPreferredSize());
			if(cpa == null)
			{
				cpa = new ColorScalePopupAdjuster(coloroptions.size());
				colorselector.addPopupMenuListener(cpa);
			}
			else
			{
				cpa.changeItemCount(coloroptions.size());
			}
			colorselector.setEnabled(true);
		}
		else
		{
			colorselector.setModel(new DefaultComboBoxModel<>());
			colorselector.setEnabled(false);
		}
		invalidate();
		repaint();

	}

	/**
	 * Get the Colormap corresponding to the selected scale
	 * @return The {@link ColorMap} currently selected
	 */
	public ColorMap getSelectedMap()
	{
		if(colorselector.isEnabled())
		{
			return colormapping.get(colorselector.getSelectedItem());
		}
		else
		{
			return null;
		}
	}

	/**
	 * A Renderer for Boxes with {@link ColorScalePane}s. Takes care both of Cell rendering and Popup rendering. 
	 * @author Thomas Pfau
	 *
	 */
	class ColorBoxRenderer extends JPanel
	implements ListCellRenderer{
		public ColorBoxRenderer() {
			setOpaque(true);
			setBorder(null);
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		}

		/*
		 * This method finds the image and text corresponding
		 * to the selected value and returns the label, set up
		 * to display the text and image.
		 */
		public Component getListCellRendererComponent(
				JList list,				
				Object value,
				int index,
				boolean isSelected,
				boolean cellHasFocus) {
			//Get the selected index. (The index param isn't
			//always valid, so just use the value.)		
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			//CostumPanel.setPreferredSize(new Dimension(0,20));
			return (ColorScalePane)value;
		}

	}
	
	public void addSelectionListener(ItemListener listener)
	{
		colorselector.addItemListener(listener);
	}
}
