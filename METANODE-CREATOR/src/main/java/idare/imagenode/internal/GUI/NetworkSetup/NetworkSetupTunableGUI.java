package idare.imagenode.internal.GUI.NetworkSetup;

import idare.Properties.IDAREProperties;
import idare.ThirdParty.BoundsPopupMenuListener;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.GUI.NetworkSetup.Tasks.NetworkSetupProperties;
import idare.imagenode.internal.Utilities.GUIUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;

public class NetworkSetupTunableGUI extends JPanel {
	
	//private IDARESettingsManager mgr;
	private Color background;
	private JComboBox<String> typeColSelector = new JComboBox<String>();
	private JComboBox<String> IDColSelector = new JComboBox<String>();
	private JCheckBox overwrite = new JCheckBox();
	private ColumnTypeChooser ctc;
	boolean overwriteData;
	public String IDCol;
	public String TypeCol;
	/**
	 * Generate the Requesting fields using the given network  
	 * @param network
	 */
	public NetworkSetupTunableGUI(CyNetwork network){
		PrintFDebugger.Debugging(this, "Setting up new GUI");
		background = getBackground();
		Collection<CyColumn> cols = network.getDefaultNodeTable().getColumns();
		Vector<String> columnNames = new Vector<String>();
		for( CyColumn col : cols)
		{
			String colName = col.getName();
			if(!(columnNames.contains(colName)))
			{
				columnNames.add(colName);
			}
		}
		ctc = new ColumnTypeChooser(network, columnNames);

		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridwidth = 2;		
		this.add(createTitle(),gbc);
		gbc.gridwidth = 1;
		gbc.weighty = 4;
		gbc.insets = new Insets(0,0,0,5);
		gbc.gridy++;
		setupSelectors(columnNames);
		this.add(createTypeAndIDSelection(),gbc);
		gbc.gridx = 1;
		//gbc.gridy = 1;
		gbc.insets = new Insets(0,5,0,0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		this.add(ctc,gbc);
		ctc.updateChoosersWithFourOptions();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weighty = 1;
		gbc.gridy++;
		gbc.insets = new Insets(5 ,  0 , 0 , 0);
		//this.add(createAcceptButton(),gbc);
		this.setSize(new Dimension(400,300));
		//this.pack();
		ctc.updateChoosers(typeColSelector.getSelectedIndex());
		//this.setResizable(false);
		this.setVisible(true);		
	}	
	
	/**
	 * Create the Panel for Type and ID Selection (i.e. two dropdown choosers, with respective labels.)
	 * @return
	 */
	private JPanel createTypeAndIDSelection()
	{
		JPanel TypeAndIDSelection = new JPanel();
		TypeAndIDSelection.setLayout(new BoxLayout(TypeAndIDSelection, BoxLayout.PAGE_AXIS));
		TypeAndIDSelection.add(GUIUtils.createSelectionPanel("Column to determine node types", typeColSelector,background));
		TypeAndIDSelection.add(GUIUtils.createSelectionPanel("Column to determine node names", IDColSelector, background));
		TypeAndIDSelection.add(createCheckBoxPanel("Overwrite existing values", overwrite));
		return TypeAndIDSelection;
	}
	
	/**
	 * Set up the Selectors for ID and Type Column . 
	 * @param ColumnNames
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
		overwrite.setSelected(true);
	}
	/**
	 * Create the Title panel.
	 * @return
	 */
	private JPanel createTitle()
	{
		JPanel TitlePanel = new JPanel();
		TitlePanel.setBackground(Color.WHITE);
		JLabel Lab = new JLabel("Select Properties for IDARE Visual Style");
		Lab.setHorizontalAlignment(JLabel.CENTER);
		Lab.setFont(Lab.getFont().deriveFont(Font.BOLD,23f));		
		TitlePanel.setLayout(new BoxLayout(TitlePanel,BoxLayout.PAGE_AXIS));
		Lab.setBackground(Color.WHITE);
		TitlePanel.add(Lab);
		
		return TitlePanel;
	}

	/**
	 * Create a general checkbox panel
	 * @param Description - The Description of the Checkbox
	 * @param box - the Actual Checkbox.
	 * @return
	 */
	private JPanel createCheckBoxPanel(String Description, JCheckBox box)
	{
		JPanel resultingPanel = new JPanel();
		resultingPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 1;
		resultingPanel.add(GUIUtils.createSelectionDescription(Description,background,new Font(Font.SANS_SERIF, Font.BOLD, 16)),gbc);
		gbc.gridx++;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		box.setAlignmentX(JCheckBox.LEFT_ALIGNMENT);
		resultingPanel.add(box,gbc);		
		return resultingPanel;
	}
	

	
	/**
	 * Setup the Network (using the default methodology from the Visualstyle
	 */
	public void setupNetwork()
	{
	//	IDAREVisualStyle.SetNetworkData(network, mgr, typeColSelector.getSelectedItem().toString(), IDColSelector.getSelectedItem().toString(), overwrite.isSelected(),nm);
	}
	
	
	public NetworkSetupProperties getNetworkSetupProperties()
	{
		if(ctc.acceptable())
		{
			NetworkSetupProperties props = new NetworkSetupProperties();
			props.CompoundID = ctc.getCompoundID();
			props.GeneID = ctc.getGeneID();
			props.InteractionID = ctc.getReactionID();
			props.ProteinID = ctc.getProteinID();
			props.IDColID = IDColSelector.getSelectedItem().toString();
			props.TypeColID = typeColSelector.getSelectedItem().toString();
			props.overwrite = overwriteData;
			return props;
		}
		else
		{
			return null;
		}
	}
	/**
	 * Listen to the choice of the columns
	 * @author Thomas Pfau
	 *
	 */
	private class ColumChoiceListener implements ItemListener
	{

		NetworkSetupTunableGUI snc;
		ColumnTypeChooser ctc;
		public ColumChoiceListener(NetworkSetupTunableGUI snc, ColumnTypeChooser ctc)
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

}
