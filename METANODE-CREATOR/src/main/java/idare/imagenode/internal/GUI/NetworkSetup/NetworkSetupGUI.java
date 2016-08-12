package idare.imagenode.internal.GUI.NetworkSetup;

import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;
import idare.ThirdParty.BoundsPopupMenuListener;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Utilities.GUIUtils;
import idare.imagenode.internal.VisualStyle.IDAREVisualStyle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
/**
 * GUI to setup Network Properties to fit to the needs of the IDARE app. 
 * @author Thomas Pfau
 *
 */
public class NetworkSetupGUI extends JDialog{
	
	private CySwingApplication cySwingApp;
	private IDARESettingsManager mgr;
	private Color background;
	private JComboBox<String> typeColSelector = new JComboBox<String>();
	private JComboBox<String> IDColSelector = new JComboBox<String>();
	private JCheckBox overwrite = new JCheckBox();
	private ColumnTypeChooser ctc;
	private JButton acceptButton = new JButton("Accept");
	private CyNetwork network; 
	public String IDCol;
	public String TypeCol;
	NodeManager nm;
	/**
	 * A Constructor requiring the column names to select the columns for the different properties from,
	 * In addition, the current network is needed, along with the cySwingApp (for hierarchy reference), the {@link IDARESettingsManager} for 
	 * idare specific settings, and a nodemanager to be informed of the updated nodes.  
	 * @param ColumnNames
	 * @param network
	 * @param cySwingApp
	 * @param mgr
	 * @param nm
	 */
	public NetworkSetupGUI( Vector<String> ColumnNames,  CyNetwork network, 
			CySwingApplication cySwingApp, IDARESettingsManager mgr, NodeManager nm){
		super(cySwingApp.getJFrame(),"Select fields and names for network setup");
		background = this.getContentPane().getBackground();
		ctc = new ColumnTypeChooser(network, ColumnNames);
		this.network = network;
		this.mgr = mgr;		
		this.nm = nm;
		JPanel ContentPane = new JPanel();
		this.setContentPane(ContentPane);
		ContentPane.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridwidth = 2;		
		ContentPane.add(createTitle(),gbc);
		gbc.gridwidth = 1;
		gbc.weighty = 4;
		gbc.insets = new Insets(0,0,0,5);
		gbc.gridy++;
		setupSelectors(ColumnNames);
		ContentPane.add(createTypeAndIDSelection(),gbc);
		gbc.gridx = 1;
		//gbc.gridy = 1;
		gbc.insets = new Insets(0,5,0,0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		ContentPane.add(ctc,gbc);
		ctc.updateChoosersWithFourOptions();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weighty = 1;
		gbc.gridy++;
		gbc.insets = new Insets(5 ,  0 , 0 , 0);
		ContentPane.add(createAcceptButton(),gbc);
		this.setSize(new Dimension(400,300));
		this.pack();
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
	 * Create the Accept Button.
	 * @return
	 */
	private JButton createAcceptButton()
	{
		acceptButton.setPreferredSize(new Dimension(100,30));		
		acceptButton.setText("Accept");
		acceptButton.addActionListener(new TypeSelectionChoiceListener(mgr,this,cySwingApp,ctc));
		return acceptButton;
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
	 * Set the properties of the IDAREProperties according to the selection. 
	 */
	public void setNetworkProperties()
	{
		mgr.setType(IDAREProperties.NodeType.IDARE_SPECIES, ctc.getCompoundID());
		mgr.setType(IDAREProperties.NodeType.IDARE_GENE, ctc.getGeneID());
		mgr.setType(IDAREProperties.NodeType.IDARE_REACTION, ctc.getReactionID());
		mgr.setType(IDAREProperties.NodeType.IDARE_PROTEIN, ctc.getProteinID());
	}	
	
	/**
	 * Setup the Network (using the default methodology from the Visualstyle
	 */
	public void setupNetwork()
	{
		IDAREVisualStyle.SetNetworkData(network, mgr, typeColSelector.getSelectedItem().toString(), IDColSelector.getSelectedItem().toString(), overwrite.isSelected(),nm);
	}
	
	/**
	 * Listen to the choice of the columns
	 * @author Thomas Pfau
	 *
	 */
	private class ColumChoiceListener implements ItemListener
	{

		NetworkSetupGUI snc;
		ColumnTypeChooser ctc;
		public ColumChoiceListener(NetworkSetupGUI snc, ColumnTypeChooser ctc)
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
	 * Listen to the choice of the Selection and apply it to the network.
	 * @author Thomas Pfau
	 *
	 */
	private class TypeSelectionChoiceListener implements ActionListener
	{
		private NetworkSetupGUI chooser;
		private CySwingApplication cySwingApp;
		ColumnTypeChooser ctc;
		public TypeSelectionChoiceListener(IDARESettingsManager mgr,NetworkSetupGUI nsg, CySwingApplication cySwingApp, ColumnTypeChooser ctc)
		{
			chooser = nsg;
			this.cySwingApp = cySwingApp;
			this.ctc= ctc;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(!ctc.acceptable())
			{
				JOptionPane.showMessageDialog(chooser, "Selection invalid. No element may be choosen twice and both compound and reaction have to be selected", "Warning",
				        JOptionPane.WARNING_MESSAGE);
				}
			else{
				chooser.setNetworkProperties();
				chooser.setupNetwork();
				chooser.IDCol = IDColSelector.getSelectedItem().toString();
				chooser.TypeCol = typeColSelector.getSelectedItem().toString();
				chooser.dispose();
			}
			
		}
		
	}
}
