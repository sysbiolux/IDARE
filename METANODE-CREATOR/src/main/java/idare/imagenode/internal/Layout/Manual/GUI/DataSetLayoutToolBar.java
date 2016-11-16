package idare.imagenode.internal.Layout.Manual.GUI;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JToolBar;

import idare.imagenode.ColorManagement.ColorScalePane;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;

public class DataSetLayoutToolBar extends JToolBar implements ItemListener{

	ManualLayoutUpdater updater;
	ColorToolBarEntry colors;
	DataSetPropertyToolBarEntry properties;
	NodeIDSelectorToolBarEntry nodeID;
	
	boolean updatingoptions = false;
	public DataSetLayoutToolBar(ManualLayoutUpdater updater, Vector<String> idoptions)
	{
		super("DataSet Properties");
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		colors = new ColorToolBarEntry();
		properties = new DataSetPropertyToolBarEntry();
		this.updater = updater;
		this.add(colors);		
		this.add(properties);
		colors.addSelectionListener(this);
		properties.addSelectionListener(this);		
		nodeID = new NodeIDSelectorToolBarEntry(idoptions);
		this.add(nodeID);	
		nodeID.addSelectionListener(this);
		//Initialize the selectors.
		updateDataSetOptions(null);
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		//we only have to do something when something was selected.
		if(e.getStateChange() == ItemEvent.SELECTED && !updatingoptions)
		{
			if(e.getItem() instanceof ColorScalePane)
			{
				updater.updateColors(colors.getSelectedMap());
			}
			if(e.getItem() instanceof DataSetLayoutProperties)
			{
				updater.updateProperties(properties.getSelectedProperties());
			}
			if(e.getItem() instanceof String)
			{				
				updater.updateNode((String) e.getItem());
			}
		}
	}
	
	public void updateDataSetOptions(DataSetLayoutInfoBundle bundle)
	{
		updatingoptions = true;
		colors.updateColorScalePane(bundle);
		properties.updatePropertyPane(bundle);
		updatingoptions = false;
	}
	
	public void addNodeSelectionListener(ItemListener listener)
	{
	}
	
	
	
}
