package idare.imagenode.internal.Layout.Manual.GUI.Tasks;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.GUI.NetworkSetup.Tasks.NetworkSetupProperties;
import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;
import idare.imagenode.internal.Layout.Manual.GUI.ColorToolBarEntry;

public class LayoutPropertiesGUI extends JPanel {

	private ColorToolBarEntry colorselector = new ColorToolBarEntry();
	private JComboBox<DataSetLayoutProperties> propertySelector;
	private JComboBox<DataSet> datasetSelector;
	
	/**
	 * Generate the Requesting fields using the given dataset  
	 * @param dsm The {@link DataSetManager} to request datasets from. 
	 */
	
	public LayoutPropertiesGUI(DataSetManager dsm){
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		this.add(new JLabel("Select DataSet"), gbc);
		gbc.gridx++;
		Vector<DataSet> datasets = new Vector<DataSet>(dsm.getDataSets());
		datasetSelector = new JComboBox<>(datasets);
		datasetSelector.setSelectedIndex(0);
		datasetSelector.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if(e.getStateChange() == (ItemEvent.SELECTED))
				{
					//colorselector.updateColorScalePane((DataSet)e.getItem());
					propertySelector.setModel(new DefaultComboBoxModel<>(((DataSet)e.getItem()).getPropertyOptions()));
					colorselector.revalidate();
					propertySelector.revalidate();
				}								
			}
		});
		add(datasetSelector,gbc);		
		gbc.gridy++;
		gbc.gridx = 0;		
		this.add(new JLabel("Select Visualisation Type"), gbc);
		gbc.gridx++;		
		propertySelector = new JComboBox<DataSetLayoutProperties>(((DataSet)datasetSelector.getSelectedItem()).getPropertyOptions());
		this.add(propertySelector, gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		this.add(new JLabel("Select ColorScale:"));
		gbc.gridx++;		
		this.add(colorselector, gbc);
		//this.setSize(new Dimension(400,300));
		this.setVisible(true);		
	}	

	/**
	 * Get the properties selected in this GUI
	 * @return the {@link NetworkSetupProperties} as selected by the user, or null, if the selection is invalid.
	 */
	public DataSetLayoutInfoBundle getNetworkSetupProperties()
	{
		DataSetLayoutInfoBundle props = new DataSetLayoutInfoBundle();
		props.dataset = (DataSet)datasetSelector.getSelectedItem();
		props.properties = (DataSetLayoutProperties) propertySelector.getSelectedItem();
		props.colormap = colorselector.getSelectedMap();
		return props;
	}

}
