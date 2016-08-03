package idare.metanode.Data.BasicDataTypes.itemizedData;

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

import idare.metanode.Interfaces.DataSets.DataSet;
import idare.metanode.Interfaces.Layout.DataSetProperties;
import idare.metanode.internal.ColorManagement.ColorMap;
import idare.metanode.internal.GUI.Legend.Utilities.TextPaneResizer;
import idare.metanode.internal.Utilities.GUIUtils;
import idare.metanode.internal.exceptions.io.WrongFormat;
/**
 * Basic properties for a Itemized Dataset.
 * @author Thomas Pfau
 *
 */
public abstract class AbstractItemDataSetProperties extends DataSetProperties {

	@Override
	public void testValidity(DataSet set) throws WrongFormat
	{
		try{
			AbstractItemDataSet ads = (AbstractItemDataSet) set;
		}
		catch(ClassCastException e)
		{
			throw new WrongFormat("Invalid dataset type for " + getTypeName());
		}
	}
	
	@Override 
	public JPanel getDataSetDescriptionPane(JScrollPane Legend, String DataSetLabel, ColorMap map, DataSet set)
	{
		Insets InnerInsets = new Insets(0,0,0,0);
		JPanel DataSetPane = new JPanel();
		BoxLayout Layout = new BoxLayout(DataSetPane, BoxLayout.PAGE_AXIS);
		//PrintFDebugger.Debugging(this, "Setting up layout");
		DataSetPane.setLayout(Layout);		
		JTextPane area = new JTextPane();
		//This ensures, that the enclosing Legend does not jump to this TextPane..
		DefaultCaret caret = (DefaultCaret)area.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		//PrintFDebugger.Debugging(this, "Adding Description as listener");
		Legend.addComponentListener(new TextPaneResizer(area));
		//PrintFDebugger.Debugging(this, "Setting Area Properties");
		area.setPreferredSize(new Dimension());
		area.setText(DataSetLabel + ": " + set.Description);
		area.setEditable(false);
		area.setFont(area.getFont().deriveFont(Font.BOLD,22f));
		area.setBorder(null);
		area.setMargin(InnerInsets);
		area.setPreferredSize(GUIUtils.getPreferredSize(area, Legend.getViewport().getSize(), 300));
		//PrintFDebugger.Debugging(this, "Adding Area");
		DataSetPane.add(area);
		DataSetPane.add(Box.createRigidArea(new Dimension(0,2)));//ContentPane.add(Box.createVerticalGlue());
		ItemDataDescription idd = new ItemDataDescription();
		//PrintFDebugger.Debugging(this, "Setting up item descriptions");
		idd.setupItemDescription(set.getDefaultData(), DataSetLabel,Legend);
		DataSetPane.add(idd);
		DataSetPane.add(Box.createVerticalGlue());
		//PrintFDebugger.Debugging(this, "Adding Colomap");
		DataSetPane.add(map.getColorMapComponent());
		//PrintFDebugger.Debugging(this, "Colormap size: "+colormap.getPreferredSize());		
		return DataSetPane;
	}
	@Override
	public Collection<Class<? extends DataSet>> getWorkingClassTypes()
	{
		Vector<Class<? extends DataSet>> acceptableclasses = new Vector<Class<? extends DataSet>>();
		acceptableclasses.add(AbstractItemDataSet.class);
		return acceptableclasses;
	}

}
