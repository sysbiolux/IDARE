package idare.subsystems.internal.GUI;

import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;
import idare.ThirdParty.BoundsPopupMenuListener;
import idare.imagenode.internal.Utilities.GUIUtils;
import idare.subsystems.internal.NoNetworksToCreateException;
import idare.subsystems.internal.SubNetworkCreator;

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

public class SubSystemColumnChooser extends JDialog {
	
	private CySwingApplication cySwingApp;
	private IDARESettingsManager mgr;
	private Color background;
	private JComboBox<String> typeColSelector = new JComboBox<String>();
	private JComboBox<String> IDColSelector = new JComboBox<String>();
	//private JCheckBox overwrite = new JCheckBox();
	private SubSystemInteractionAndCompoundChooser ctc;
	private JButton acceptButton = new JButton("Accept");
	private CyNetwork network; 
	public String IDCol;
	public String TypeCol;
	//NodeManager nm;
	/**
	 * A Constructor requiring the column names to select the columns for the different properties from,
	 * In addition, the current network is needed, along with the cySwingApp (for hierarchy reference), the {@link IDARESettingsManager} for 
	 * idare specific settings, and a nodemanager to be informed of the updated nodes.  
	 * @param ColumnNames
	 * @param network
	 * @param cySwingApp
	 * @param snc
	 */
	public SubSystemColumnChooser( Vector<String> ColumnNames,  CyNetwork network, 
			CySwingApplication cySwingApp, SubNetworkCreator snc){
		super(cySwingApp.getJFrame(),"Select fields and names for subsystem setup");
		this.cySwingApp = cySwingApp;
		background = this.getContentPane().getBackground();
		ctc = new SubSystemInteractionAndCompoundChooser(network, ColumnNames);
		this.network = network;
		this.mgr = mgr;		
		//this.nm = nm;
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
		ctc.updateChoosersWithTwoOptions();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weighty = 1;
		gbc.gridy++;
		gbc.insets = new Insets(5 ,  0 , 0 , 0);
		ContentPane.add(createAcceptButton(snc),gbc);
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
		TypeAndIDSelection.add(createSelectionPanel("Column to determine node types", typeColSelector));
		TypeAndIDSelection.add(createSelectionPanel("Column to determine node names", IDColSelector));
		//TypeAndIDSelection.add(createCheckBoxPanel("Overwrite existing values", overwrite));
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
		//overwrite.setSelected(true);
	}
	/**
	 * Create the Title panel.
	 * @return
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
	 * Create the Accept Button.
	 * @return
	 */
	private JButton createAcceptButton(SubNetworkCreator creator)
	{
		acceptButton.setPreferredSize(new Dimension(100,30));		
		acceptButton.setText("Accept");
		acceptButton.addActionListener(new TypeSelectionChoiceListener(creator,mgr,this,cySwingApp,ctc));
		return acceptButton;
	}
	/**
	 * Create the SelecionPanel
	 * @param SelectionName - The Name for the panel 
	 * @param selector - the {@link JComboBox} that represents the selection options.
	 * @return
	 */
	private JPanel createSelectionPanel(String SelectionName, JComboBox selector)
	{
		JPanel resultingPanel = new JPanel();
		resultingPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 1;
		resultingPanel.add(GUIUtils.createSelectionDescription(SelectionName,background,new Font(Font.SANS_SERIF, Font.BOLD, 16)),gbc);
		gbc.gridx++;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		resultingPanel.add(selector,gbc);
		selector.setPreferredSize(new Dimension(100,selector.getPreferredSize().height));
		selector.setMinimumSize(new Dimension(100,selector.getMinimumSize().height));
		//resultingPanel.setPreferredSize(new Dimension(200,40));
		return resultingPanel;
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
	
//	/**
//	 * Set the properties of the IDAREProperties according to the selection. 
//	 */
//	public void setNetworkProperties()
//	{
//		mgr.setType(IDAREProperties.NodeType.IDARE_SPECIES, ctc.getCompoundID());
//		mgr.setType(IDAREProperties.NodeType.IDARE_REACTION, ctc.getReactionID());
//	}	
//		
	
	/**
	 * Listen to the choice of the columns
	 * @author Thomas Pfau
	 *
	 */
	private class ColumChoiceListener implements ItemListener
	{

		SubSystemColumnChooser snc;
		SubSystemInteractionAndCompoundChooser ctc;
		public ColumChoiceListener(SubSystemColumnChooser snc, SubSystemInteractionAndCompoundChooser ctc)
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
		private SubSystemColumnChooser chooser;
		private CySwingApplication cySwingApp;
		SubSystemInteractionAndCompoundChooser ctc;
		SubNetworkCreator creator;
		public TypeSelectionChoiceListener(SubNetworkCreator creator, IDARESettingsManager mgr,SubSystemColumnChooser nsg, CySwingApplication cySwingApp, SubSystemInteractionAndCompoundChooser ctc)
		{
			this.creator = creator;
			chooser = nsg;
			this.cySwingApp = cySwingApp;
			this.ctc= ctc;
		}

		@SuppressWarnings("static-access")
		@Override
		public void actionPerformed(ActionEvent e) {
			if(!ctc.acceptable())
			{
				JOptionPane.showMessageDialog(chooser, "Selection invalid. No element may be choosen twice and both compound and reaction have to be selected", "Warning",
				        JOptionPane.WARNING_MESSAGE);
				}
			else{
				
				creator.setTypeColumn(chooser.getTypeCol());
				creator.setIDColName(chooser.getIDCol());
//				creator.IDAREIdmgr.setType(IDAREProperties.NodeType.IDARE_REACTION, chooser.getInteractionName());
//				creator.IDAREIdmgr.setType(IDAREProperties.NodeType.IDARE_SPECIES, chooser.getCompoundName());
				creator.setCompoundName(ctc.getCompoundID());
				creator.setInteractionName(ctc.getReactionID());
				creator.IDAREIdmgr.setSubNetworkType(IDAREProperties.NodeType.IDARE_REACTION, ctc.getReactionID());
				creator.IDAREIdmgr.setSubNetworkType(IDAREProperties.NodeType.IDARE_SPECIES, ctc.getCompoundID());
				creator.setupNetworkForSubNetworkCreation(network, creator.IDAREIdmgr, chooser.getTypeCol());;
				try{
					SubNetworkCreatorGUI scg = new SubNetworkCreatorGUI(creator.getAlgorithmNames(), creator.getNetworkColumns(), creator, network, cySwingApp);
					scg.setVisible(true);
					scg.repaint();
					scg.validate();
					chooser.dispose();
				}
				catch(NoNetworksToCreateException ex)
				{
					chooser.dispose();
					JOptionPane.showMessageDialog(cySwingApp.getJFrame(), "There are no new subnetworks available based on the current selection.\nEither all subnetworks defined by the current subnetwork column are already created, or the column does not have valid entries.");
				}
				//chooser.setNetworkProperties();
				//chooser.setupNetwork();
				//chooser.IDCol = IDColSelector.getSelectedItem().toString();
				//chooser.TypeCol = typeColSelector.getSelectedItem().toString();
				
			}
			
		}
		
	}

}
