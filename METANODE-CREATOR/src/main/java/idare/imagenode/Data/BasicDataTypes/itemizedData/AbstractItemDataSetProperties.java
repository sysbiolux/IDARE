package idare.imagenode.Data.BasicDataTypes.itemizedData;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.GUI.Legend.Utilities.TextPaneResizer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.Utilities.GUIUtils;
import idare.imagenode.exceptions.io.WrongFormat;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.Collection;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;
/**
 * Basic properties for a Itemized Dataset.
 * @author Thomas Pfau
 *
 */
public abstract class AbstractItemDataSetProperties extends DataSetLayoutProperties {

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.DataSetProperties#testValidity(idare.imagenode.Interfaces.DataSets.DataSet)
	 */
	@Override
	public void testValidity(DataSet set) throws WrongFormat
	{
		try{
			ItemDataSet ads = (ItemDataSet) set;
			if(ads.columncount > 60)
			{
				throw new WrongFormat("Maximal number of elements for an itemized dataset exceeded: " + ads.columncount + " > " + 60);
			}
		}
		catch(ClassCastException e)
		{
			throw new WrongFormat("Invalid dataset type for " + getTypeName());
		}
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.DataSetProperties#getDataSetDescriptionPane(javax.swing.JScrollPane, java.lang.String, idare.imagenode.internal.ColorManagement.ColorMap, idare.imagenode.Interfaces.DataSets.DataSet)
	 */
	@Override 
	public JPanel getDataSetDescriptionPane(JScrollPane Legend, String DataSetLabel, ColorMap map, DataSet set)
	{
		JPanel DataSetPane = new JPanel();

		try{
		Insets InnerInsets = new Insets(0,0,0,0);
		BoxLayout Layout = new BoxLayout(DataSetPane, BoxLayout.PAGE_AXIS);
		DataSetPane.setLayout(Layout);		
		JTextPane area = new JTextPane();
		//This ensures, that the enclosing Legend does not jump to this TextPane..
		DefaultCaret caret = (DefaultCaret)area.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		Legend.addComponentListener(new TextPaneResizer(area));
		area.setPreferredSize(new Dimension());
		area.setText(DataSetLabel + ": " + set.Description);
		area.setEditable(false);
		area.setFont(area.getFont().deriveFont(Font.BOLD,22f));
		area.setBorder(null);
		area.setMargin(InnerInsets);
		area.setPreferredSize(GUIUtils.getPreferredSize(area, Legend.getViewport().getSize(), 300));
		DataSetPane.add(area);
		DataSetPane.add(Box.createRigidArea(new Dimension(0,2)));//ContentPane.add(Box.createVerticalGlue());
		ItemDataDescription idd = new ItemDataDescription();
		idd.setupItemDescription(set.getDefaultData(), DataSetLabel,Legend);
		DataSetPane.add(idd);
		DataSetPane.add(Box.createRigidArea(new Dimension(0,2)));//ContentPane.add(Box.createVerticalGlue());
		//DataSetPane.add(Box.createVerticalGlue());
		DataSetPane.add(map.getColorMapComponent(Legend));
		
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
		return DataSetPane;
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.Layout.DataSetProperties#getWorkingClassTypes()
	 */
	@Override
	public Collection<Class<? extends DataSet>> getWorkingClassTypes()
	{
		Vector<Class<? extends DataSet>> acceptableclasses = new Vector<Class<? extends DataSet>>();
		acceptableclasses.add(ItemDataSet.class);
		return acceptableclasses;
	}

}
