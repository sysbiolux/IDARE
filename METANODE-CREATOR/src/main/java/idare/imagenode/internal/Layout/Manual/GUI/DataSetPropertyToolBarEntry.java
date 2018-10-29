package idare.imagenode.internal.Layout.Manual.GUI;

import java.awt.Dimension;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import idare.ThirdParty.BoundsPopupMenuListener;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;

public class DataSetPropertyToolBarEntry extends JPanel {

	JComboBox<DataSetLayoutProperties> visualisationTypeSelector;	
	JLabel visualisationTypeLabel = new JLabel("Visualisation Type:");
	
	public DataSetPropertyToolBarEntry()
	{
		visualisationTypeSelector = new JComboBox<>();
		visualisationTypeSelector.addPopupMenuListener(new BoundsPopupMenuListener(true,false));
		visualisationTypeSelector.setPreferredSize(new Dimension(200,50));
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(visualisationTypeLabel);
		this.add(visualisationTypeSelector);	
	}
	
	public void updatePropertyPane(DataSetLayoutInfoBundle bundle)
	{
		if(bundle != null)
		{
			visualisationTypeSelector.setModel(new DefaultComboBoxModel<>(bundle.dataset.getPropertyOptions()));
			visualisationTypeSelector.setSelectedItem(bundle.properties);
			visualisationTypeSelector.setEnabled(true);
		}
		else
		{
			visualisationTypeSelector.setModel(new DefaultComboBoxModel<>());
			visualisationTypeSelector.setEnabled(false);
		}
		revalidate();        
	}
	
	/**
	 * Get the Colormap corresponding to the selected scale
	 * @return LayoutProperties for the currently selected element.
	 */
	public DataSetLayoutProperties getSelectedProperties()
	{
		if(visualisationTypeSelector.isEnabled())
		{
			return (DataSetLayoutProperties)visualisationTypeSelector.getSelectedItem();
		}
		else
		{
			return null;
		}
	}
	
	public void addSelectionListener(ItemListener listener)
	{
		visualisationTypeSelector.addItemListener(listener);
	}
		
}
