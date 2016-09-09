package idare.subnetwork.internal.GUI;

import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;
import idare.ThirdParty.BoundsPopupMenuListener;
import idare.imagenode.Utilities.GUIUtils;
import idare.subnetwork.internal.Tasks.propertySelection.SubnetworkColumnProperties;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.model.CyNetwork;

public class SubnetworkPropertyColumnChooser extends JPanel{
	
	//private IDARESettingsManager mgr;
	private Color background;
	private JComboBox<String> typeColSelector = new JComboBox<String>();
	private JComboBox<String> IDColSelector = new JComboBox<String>();
	//private JCheckBox overwrite = new JCheckBox();
	private SubnetworkInteractionAndCompoundChooser ctc;	
	public String IDCol;
	public String TypeCol;
	//NodeManager nm;
	/**
	 * A Constructor requiring the column names to select the columns for the different properties from,
	 * In addition, the current network is needed, along with the cySwingApp (for hierarchy reference), the {@link IDARESettingsManager} for 
	 * idare specific settings, and a nodemanager to be informed of the updated nodes.  
	 * @param ColumnNames The Column Names available in the network
	 * @param network The network to create subnetworks for.
	 */
	public SubnetworkPropertyColumnChooser( Vector<String> ColumnNames,  CyNetwork network){		
		background = this.getBackground();
		ctc = new SubnetworkInteractionAndCompoundChooser(network, ColumnNames);
		//this.nm = nm;
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridwidth = 2;		
		add(createTitle(),gbc);
		gbc.gridwidth = 1;
		gbc.weighty = 4;
		gbc.insets = new Insets(0,0,0,5);
		gbc.gridy++;
		setupSelectors(ColumnNames);
		add(createTypeAndIDSelection(),gbc);
		gbc.gridx = 1;
		//gbc.gridy = 1;
		gbc.insets = new Insets(0,5,0,0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		add(ctc,gbc);
		ctc.updateChoosersWithTwoOptions();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weighty = 1;
		gbc.gridy++;
		gbc.insets = new Insets(5 ,  0 , 0 , 0);		
		this.setSize(new Dimension(400,300));		
		ctc.updateChoosers(typeColSelector.getSelectedIndex());
		//this.setResizable(false);				
	}	
	
	/**
	 * Create the Panel for Type and ID Selection (i.e. two dropdown choosers, with respective labels.)
	 * @return the Type and ID selection {@link JPanel}
	 */
	private JPanel createTypeAndIDSelection()
	{
		JPanel TypeAndIDSelection = new JPanel();
		TypeAndIDSelection.setLayout(new BoxLayout(TypeAndIDSelection, BoxLayout.PAGE_AXIS));
		TypeAndIDSelection.add(GUIUtils.createSelectionPanel("Column to determine node types", typeColSelector,background));
		TypeAndIDSelection.add(GUIUtils.createSelectionPanel("Column to determine node names", IDColSelector,background));
		//TypeAndIDSelection.add(createCheckBoxPanel("Overwrite existing values", overwrite));
		return TypeAndIDSelection;
	}
	
	/**
	 * Set up the Selectors for ID and Type Column . 
	 * @param ColumnNames the column names to use to set up the selectors.
	 */
	private void setupSelectors(Vector<String> ColumnNames)
	{
		BoundsPopupMenuListener listener = new BoundsPopupMenuListener(true, false);		
		typeColSelector = new JComboBox<String>(new DefaultComboBoxModel<String>(ColumnNames));		
		typeColSelector.setSelectedIndex(0);
		typeColSelector.setEditable(false);
		typeColSelector.setSelectedItem("shared name");
		typeColSelector.setSelectedItem("sbml type");
		typeColSelector.setSelectedItem(IDAREProperties.IDARE_NODE_TYPE);
		typeColSelector.addPopupMenuListener( listener );
		typeColSelector.addItemListener(new ColumChoiceListener(this, ctc));
		IDColSelector = new JComboBox<String>(new DefaultComboBoxModel<String>(ColumnNames));
		IDColSelector.setSelectedIndex(0);
		IDColSelector.setEditable(false);
		IDColSelector.setSelectedItem("name");
		IDColSelector.setSelectedItem("sbml name");
		IDColSelector.setSelectedItem(IDAREProperties.IDARE_NODE_NAME);
		IDColSelector.addPopupMenuListener( listener );
		//overwrite.setSelected(true);
	}
	/**
	 * Create the Title panel.
	 * @return The Title Panel
	 */
	private JPanel createTitle()
	{
		JPanel TitlePanel = new JPanel();
		TitlePanel.setBackground(Color.WHITE);
		JLabel Lab = new JLabel("Select Properties for Subnetwork generation");
		Lab.setHorizontalAlignment(JLabel.CENTER);
		Lab.setFont(Lab.getFont().deriveFont(Font.BOLD,23f));		
		TitlePanel.setLayout(new BoxLayout(TitlePanel,BoxLayout.PAGE_AXIS));
		Lab.setBackground(Color.WHITE);
		TitlePanel.add(Lab);
		
		return TitlePanel;
	}
	
	/**
	 * Get the Column NAme for the Type Columnn
	 * @return A {@link String} representing the Column name of the Type column.
	 */
	public String getTypeCol()
	{
		return typeColSelector.getSelectedItem().toString();
	}
	
	/**
	 * Get the Column Name for the ID Columnn
	 * @return A {@link String} representing the Column name of the ID column.
	 */
	public String getIDCol()
	{
		return IDColSelector.getSelectedItem().toString();
	}
	
	
	/**
	 * Listen to the choice of the columns
	 * @author Thomas Pfau
	 *
	 */
	private class ColumChoiceListener implements ItemListener
	{

		SubnetworkPropertyColumnChooser snc;
		SubnetworkInteractionAndCompoundChooser ctc;
		public ColumChoiceListener(SubnetworkPropertyColumnChooser snc, SubnetworkInteractionAndCompoundChooser ctc)
		{
			this.snc = snc;
			this.ctc = ctc;
		}
		@Override
		public void itemStateChanged(ItemEvent e) {
			// TODO Auto-generated method stub
			int colSelected = 0;
			if(e.getStateChange() == ItemEvent.SELECTED)
			{
				colSelected = snc.typeColSelector.getSelectedIndex();
			}
			ctc.updateChoosers(colSelected);
			snc.doLayout();
		}

	}
	/**
	 * Get the properties selected in this Panel.
	 * @return the {@link SubnetworkColumnProperties} defined by this panel
	 */
	public SubnetworkColumnProperties getColumnProperties()
	{
		SubnetworkColumnProperties props = new SubnetworkColumnProperties();
		if(!ctc.acceptable())
		{
			props.ErrorMessage = ctc.getProblemMessage();
			return props;			
		}
		props.IDCol = getIDCol();
		props.TypeCol = getTypeCol();
		props.CompoundID = ctc.getCompoundID();
		props.ReactionID = ctc.getReactionID();
		return props;
		
	}
		
	

}
