package idare.subsystems.internal.GUI;


import idare.Properties.IDAREProperties;
import idare.ThirdParty.BoundsPopupMenuListener;
import idare.subsystems.internal.NoNetworksToCreateException;
import idare.subsystems.internal.SubNetworkCreator;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIDefaults;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

/**
 * A GUI for the selection of the properties for SubNetwork Generation.
 * @author Thomas Pfau
 *
 */

public class SubNetworkCreatorGUI extends JDialog{

	
	private CyNetwork network;
	
	private SubNetworkCreator creator;
	
	private JButton acceptButton = new JButton();
	public final JComboBox<String> layoutSelector = new JComboBox<String>();
	public final JComboBox<String> colSelector = new JComboBox<String>();
	private JTable metSelTab;
	private JTable subSysSelTab = new JTable();
	private MetaboliteSelectionModel metSelMod; 
	int colSelected = 0;
	String algoselected = "";
	boolean accepted; 
	private Color bgcolor;
	/**
	 * Generate the GUI for selection of Subnetwork generation properties
	 * @param AlgorithmNames - The Layout algorithms available to the generator
	 * @param ColumnNames - The ColumnNames available for Subnetwork column selection
	 * @param creator - the {@link SubNetworkCreator} Object that needs to be informed of the result
	 * @param network - the {@link CyNetwork} to generate subnetworks for
	 * @param cySwingApp - The {@link CySwingApplication} that is the parent of this GUI.
	 */
	public SubNetworkCreatorGUI(Collection<String> AlgorithmNames, Vector<String> ColumnNames, SubNetworkCreator creator, 
			CyNetwork network, CySwingApplication cySwingApp) throws NoNetworksToCreateException{
		super(cySwingApp.getJFrame(),"Select Properties for Subnetworks");
		
		this.network = network;
		this.creator = creator;		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setContentPane(new JPanel());
		this.getContentPane().setLayout(new GridBagLayout());
		System.out.println("Calling Pack");
		this.pack();
		GridBagConstraints outerConst = new GridBagConstraints();
		System.out.println("Getting Color");
		bgcolor = this.getContentPane().getBackground();
		SimpleAttributeSet attribs = new SimpleAttributeSet();  
		StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_CENTER);
		StyleConstants.setFontSize(attribs , 15);
		
		//Creating the title
		JTextField titleField = new JTextField();
		titleField.setText("Create Subnetworks based on Node Properties");
		titleField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		titleField.setCursor(null);
		titleField.setEditable(false);
		outerConst.gridy = 0;
		outerConst.gridx = 0;		
		outerConst.weighty = 1;
		outerConst.gridwidth = GridBagConstraints.REMAINDER;
		outerConst.fill = GridBagConstraints.HORIZONTAL;
		System.out.println("Adding Title");
		this.getContentPane().add(titleField,outerConst);
		
		
		System.out.println("Creating Middle Section");
		outerConst.fill = GridBagConstraints.HORIZONTAL;
	    outerConst.anchor = GridBagConstraints.CENTER;
	    outerConst.weightx = 1;
		outerConst.gridx = 0;
		outerConst.gridy = 1;
		outerConst.weighty = 1;
		outerConst.gridwidth = 1;
		outerConst.fill = GridBagConstraints.HORIZONTAL;
		try{
			createMiddleSection(ColumnNames, AlgorithmNames,this.getContentPane(),outerConst);
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
		
		outerConst.gridx = 0;		
		outerConst.weighty = 1;
		outerConst.gridwidth = GridBagConstraints.REMAINDER;
		outerConst.fill = GridBagConstraints.HORIZONTAL;	
		outerConst.gridy++;		
		//Create the selection panel for Subsystems
		System.out.println("Creating Metabolite Selection");
		try{
			createMetaboliteSelection(network,outerConst,this.getContentPane());
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
//		
		System.out.println("Creating Subsystem Selection");
		outerConst.gridx = 0;		
		outerConst.weighty = 1;
		outerConst.gridwidth = GridBagConstraints.REMAINDER;
		outerConst.fill = GridBagConstraints.HORIZONTAL;				
		createSubSystemSelection(outerConst,this.getContentPane());		
		
		// Create the Bottom Pane with the Accept button.
		JPanel BottomPanel = new JPanel();		
		createAcceptButton();
		JPanel  AcceptButtonPanel = new JPanel();		
		AcceptButtonPanel.add(acceptButton);
		BottomPanel.add(AcceptButtonPanel);
		outerConst.gridy++;
		outerConst.weighty = 1;
		outerConst.fill = GridBagConstraints.HORIZONTAL;		
		this.getContentPane().add(BottomPanel,outerConst);
		this.getContentPane().doLayout();		
		this.pack();
		this.setSize(new Dimension(500,700));		
	}
	
	private JTextPane createDescription(String DescriptionString)
	{
		
		UIDefaults defaults = new UIDefaults();
		defaults.put("TextPane[Enabled].backgroundPainter", bgcolor);
		JTextPane selectionDesc = new JTextPane();
		selectionDesc.putClientProperty("Nimbus.Overrides", defaults);
		selectionDesc.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		
		selectionDesc.setBackground(null);
		selectionDesc.setEditable(false);
		selectionDesc.setBorder(BorderFactory.createEmptyBorder());			
		selectionDesc.setOpaque(true);				
		selectionDesc.setText(DescriptionString);		
		selectionDesc.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));			
		//selectionDesc.setPreferredSize(new Dimension(200,45));
		selectionDesc.setBackground(bgcolor);		
		return selectionDesc;
		
	}
	
	/**
	 * Create the Subsystem Selection Panel
	 * @return A {@link JPanel} for the Subsystem Selection
	 */
	private void createSubSystemSelection(GridBagConstraints gbc, Container MiddlePane) throws NoNetworksToCreateException
	{
		String TitleString = "Select the SubSystems to be generated";
		JTextPane TitlePanel = createDescription(TitleString);
		gbc.gridy++;
		gbc.weighty = 1;
		MiddlePane.add(TitlePanel,gbc);
			
		subSysSelTab.setAutoCreateRowSorter(true);
		subSysSelTab.setFillsViewportHeight(true);
		try
		{
			TableModel model = createSubSystemTableModel(network, creator);
			subSysSelTab.setModel(model);
		}
		catch(NoNetworksToCreateException ex)
		{
			subSysSelTab.setModel(new DefaultTableModel());
		}
		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 6;
		JScrollPane subSysPane = new JScrollPane(subSysSelTab);
		int colscount = subSysSelTab.getColumnModel().getColumnCount();
		subSysSelTab.getColumnModel().getColumn(colscount-1).setMaxWidth(100);
		subSysPane.setMinimumSize(new Dimension(200, 100));
		subSysPane.setMaximumSize(new Dimension(1000,800));
		MiddlePane.add(subSysPane,gbc);
		
		
	}
	/**
	 * Create the TableModel for the SubSystem Selection
	 * @param network - the {@link CyNetwork}Subsystems are created for
	 * @param creator - The {@link SubNetworkCreator} this GUI provides input to.
	 * @return A {@link TableModel} containing a list of potential Subnetworks
	 */
	public TableModel createSubSystemTableModel(CyNetwork network, SubNetworkCreator creator) throws NoNetworksToCreateException
	{
		DefaultTableModel subSysSelMod = new DefaultTableModel(){
			@Override
		    public Class getColumnClass(int c) {
				if(getRowCount() > 0)
				{
		        return getValueAt(0, c).getClass();
				}
				else
				{
					return String.class;
				}
		    }

			@Override
		    public boolean isCellEditable(int row, int col) {
				//The Data retrieved from the Model can not be altered. only the remainder can.
				if (col < 1) {
		            return false;
		        } else {
		            return true;
		        }
		    }
			
		};
		String[] ColIds = {"Subsystem", "Selected"};
		subSysSelMod.setColumnIdentifiers(ColIds);
		Vector<Object> subSysNames = creator.getDifferentSubSystems(network.getDefaultNodeTable(), colSelector.getSelectedItem().toString());

		Set<String> existingSubSystems = creator.getExistingSubSystemNames(network);
		for(Object subSys : subSysNames)
		{
			if(!existingSubSystems.contains(subSys) && subSys != null)
			{
				Vector<Object> row = new Vector<Object>();
				row.add(subSys);
				row.add(false);
				subSysSelMod.addRow(row);
			}
		}
		
		//if(subSysSelMod.getRowCount() == 0)
		//{
		//	throw new NoNetworksToCreateException();			
		//}		
		return subSysSelMod;
	}
	
	/**
	 * 
	 * @param metaboliteNodes
	 * @param metaboliteList
	 * @param node
	 * @param network
	 */
	private void updateMetaboliteLists(HashMap<CyNode,Integer> metaboliteNodes,	List<SortEntry> metaboliteList, CyNode node, CyNetwork network)
	{
		List<CyEdge> edges = network.getAdjacentEdgeList(node, CyEdge.Type.ANY);
		CyTable NodeTable = network.getDefaultNodeTable();
		int reactionEdgeCount = 0;
		for(CyEdge edge : edges)
		{
			if(edge.getSource() == node)
			{
				CyRow row = NodeTable.getRow(edge.getTarget().getSUID());
				if(row.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class) == IDAREProperties.NodeType.IDARE_REACTION)
				{
					reactionEdgeCount++;
				}
			}
			else
			{
				CyRow row = NodeTable.getRow(edge.getSource().getSUID());
				if(row.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class) == IDAREProperties.NodeType.IDARE_REACTION)
				{
					reactionEdgeCount++;
				}
			}
		}
		metaboliteList.add(new SortEntry(node, reactionEdgeCount));
		metaboliteNodes.put(node, reactionEdgeCount);
	}
	/**
	 * Create the Metabolite Selection Panel where you can choose which Metabolites to use and expand
	 * @param network - The {@link CyNetwork} the metabolites are chosen from
	 * @return A {@link JPanel} displaying the metabolite selection.
	 */
	private void createMetaboliteSelection(CyNetwork network, GridBagConstraints gbc, Container MiddleSection)
	{		
		Graphics graphics = this.getGraphics();
		gbc.gridy = gbc.gridy + 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
	
		String TitleString = "Select Nodes, which should not act as linkers or which should be removed from the Subsystem Representations";
		
		JTextPane TitlePanel = createDescription(TitleString);
		MiddleSection.add(TitlePanel,gbc);	
		metSelTab = new JTable();
		//metSelTab.setMinimumSize(new Dimension(200, 50));
		metSelTab.setAutoCreateRowSorter(true);
		metSelTab.setFillsViewportHeight(true);	
		FontMetrics FM = graphics.getFontMetrics(metSelTab.getFont());
				
		//Try if this Network has A Compartment (sbml compartment, compartment, Compartment)
		String[] CompartmentOptions = new String[]{"sbml compartment", "Compartment", "compartment"};
		String CompartmentString = null;
		for(String Comp : CompartmentOptions)
		{
			if(network.getDefaultNodeTable().getColumn(Comp) != null)
			{
				CompartmentString = Comp;
				break;
			}
		}
		metSelMod = new MetaboliteSelectionModel(CompartmentString);
		//set up the model
		//String[] ColumnName = {"Metabolite Name", "Involved Reactions", "Do not extend", "Remove","CyNode"};
		List<CyNode> nodes = network.getNodeList();
		HashMap<CyNode,Integer> metaboliteNodes = new HashMap<CyNode, Integer>();
		List<SortEntry> metaboliteList = new LinkedList<SortEntry>();
		double width = 0;
		//for(String s : ColumnName)
		//{
		//	width += FM.stringWidth(s)+5;
		//}
		//metSelTab.setPreferredScrollableViewportSize(new Dimension((int)width, 100));
		for(CyNode node : nodes)
		{
							
			
			CyRow nodeRow = network.getRow(node);
			String TypeEntry = nodeRow.get(creator.getNodeTypeColumn(), String.class);
			
			if(TypeEntry != null && TypeEntry.equals(creator.getCompoundName()))
			{
				updateMetaboliteLists(metaboliteNodes,metaboliteList, node, network);
				//metaboliteNodes.put(node,network.getAdjacentEdgeList(node, CyEdge.Type.ANY).size());
				//metaboliteList.add(new SortEntry(node,network.getAdjacentEdgeList(node, CyEdge.Type.ANY).size()));
				
			}
		}
				

		Collections.sort(metaboliteList,new SortItems());
		for(SortEntry entry: metaboliteList)
		{			
			Vector<Object> row = new Vector<Object>();
			row.add(network.getDefaultNodeTable().getRow(entry.key.getSUID()).get(creator.getIDColName(), String.class));
			//If we foudn a CompartmentID, we will display it.
			if(CompartmentString != null)
			{
				try
				{
					Object value = network.getDefaultNodeTable().getRow(entry.key.getSUID()).get(CompartmentString, String.class);
					if(value != null)
					{			
						row.add(value);
					}
					else
					{
						row.add("");
					}
				}
				catch(Exception e)
				{
					//just skip this if there is an exception and add ""
					row.add("");
				}
			}
			row.add(entry.value);
			//if a metabolite is involved in more than 1% of all reactions
			//Do not use it as a linker
			if(entry.value > 10 && entry.value > network.getDefaultNodeTable().getMatchingRows(creator.getNodeTypeColumn(), creator.getInteractionName()).size()*0.005)
			{
				row.add(true);
			}
			else
			{
				row.add(false);
			}
			//
			if(entry.value > 25 && entry.value > network.getDefaultNodeTable().getMatchingRows(creator.getNodeTypeColumn(), creator.getInteractionName()).size()*0.02)
			{
				row.add(true);
			}			
			else
			{
				row.add(false);
			}			
			row.add(entry.key);
			System.out.println("Trying to add row: " + entry.key.toString());
			for(Object item : row)
			{
				System.out.print(item + "\t");
			}
			System.out.println("");
			metSelMod.addRow(row);
		}
		metSelTab.setModel(metSelMod);
		metSelTab.getColumnModel().getColumn(metSelMod.getExtendCol()).setMaxWidth(100);
		metSelTab.getColumnModel().getColumn(metSelMod.getRemoveCol()).setMaxWidth(70);
		// and now hide the CyNode Column.
		TableColumnModel tcm = metSelTab.getColumnModel();
		tcm.removeColumn(tcm.getColumn(metSelMod.getCyNodeCol()));
		gbc.gridy++;
		gbc.weighty = 4;
		gbc.fill = GridBagConstraints.BOTH;
		JScrollPane metSelPane = new JScrollPane(metSelTab);
		metSelPane.setMinimumSize(new Dimension(200,100));
		metSelPane.setMaximumSize(new Dimension(1000,800));
		MiddleSection.add(metSelPane,gbc);
	}
	
	/**
	 * Get the selected SubSystems from the SubSystemSelector 
	 * @return A {@link Vector} of all Objects which were selected
	 */
	public Vector<Object> SubSystemsToGenerate()
	{
		Vector<Object> subSystems = new Vector<Object>();
		for(int i=0; i < subSysSelTab.getModel().getRowCount(); i++)
		{
			if((Boolean)subSysSelTab.getModel().getValueAt(i, 1))
			{
				subSystems.add(subSysSelTab.getModel().getValueAt(i, 0));
			}
		}
		return subSystems;
	}
	/**
	 * Get All Nodes that are to be ignored in the SubNetwork Generation
	 * @return A {@link Set} of {@link CyNode}s that are ignored for the subnetwork generation
	 */
	public Set<CyNode> getNodesToRemove()
	{

		int RemPos = metSelMod.usecompartment ? 4 : 3;
		int ValPos = metSelMod.usecompartment ? 5 : 4;		
		Set<CyNode> NodesToRemove = new HashSet<CyNode>();
		for(int i=0; i < metSelTab.getModel().getRowCount(); i++)
		{
			if((Boolean)metSelTab.getModel().getValueAt(i, RemPos ))
			{
				NodesToRemove.add((CyNode)metSelTab.getModel().getValueAt(i, ValPos));
			}
		}
		return NodesToRemove;
		
	}
	/**
	 * Get the {@link CyNode} which should not link to other networks 
	 * @return a {@link Set} of {@link CyNode}s that are not used to create links
	 */
	public Set<CyNode> getNodesToSkip()
	{
		Set<CyNode> NodesToSkip= new HashSet<CyNode>();
		int SkipPos = metSelMod.usecompartment ? 3 : 2;
		int ValPos = metSelMod.usecompartment ? 5 : 4;		
		for(int i=0; i < metSelTab.getModel().getRowCount(); i++)
		{
			
			if((Boolean)metSelTab.getModel().getValueAt(i, SkipPos))
			{
				NodesToSkip.add((CyNode)metSelTab.getModel().getValueAt(i, ValPos));
			}
		}
		return NodesToSkip;
		
	}
	/**
	 * A Helper class to obtain a sorting of CyNodes based on additional information.
	 * @author Thomas Pfau
	 *
	 */
	class SortEntry
	{

		private CyNode key;
		private  Integer value;
		public SortEntry(CyNode key, Integer Value)
		{
			this.key = key;
			this.value = Value;
		}
	
	}
	/**
	 * A Sorter for {@link SortEntry}s 
	 * @author Thomas Pfau
	 *
	 */
	class SortItems implements Comparator<SortEntry>
	{

		@Override
		public int compare(SortEntry arg0, SortEntry arg1) {
			return arg1.value.compareTo(arg0.value);
		}		
	}
	/**
	 * Create the Accept button and assign a {@link NetworkGuiListener} to it.
	 */
	private void createAcceptButton()
	{
		//acceptButton.setPreferredSize(new Dimension(100,30));		
		acceptButton.setText("Accept");
		acceptButton.addActionListener(new NetworkGuiListener(this,creator));			
	}
	
	/**
	 * Create the Middle section of the GUI
	 * @param ColumnNames - The Column names to choose from to obtain the Subnetwork possibilities
	 * @param AlgorithmNames - The Names of all potential Layouting algorithms
	 * @return A {@link JPanel} with selectors for a layout algorithm and a Column for Subnetwork selection
	 */
	private void createMiddleSection(Vector<String> ColumnNames, Collection<String> AlgorithmNames,Container MiddlePane, GridBagConstraints gbc)
	{				
		
		String ColString = "Column to determine Subnetworks";
		JTextPane colSelectionDesc = createDescription(ColString);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1;
		gbc.gridy++;		
		gbc.gridx = 0;
//		colTextPanel.setPreferredSize(new Dimension(400,60));

		MiddlePane.add(colSelectionDesc,gbc);		
		

		for(String column : ColumnNames)
		{
			colSelector.addItem(column);
		}
		//colSelector.setPreferredSize(new Dimension( 150, 30));
		BoundsPopupMenuListener listener = new BoundsPopupMenuListener(true, false);
		colSelector.addPopupMenuListener( listener );
		colSelector.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					colSelected = colSelector.getSelectedIndex();
				}
				if(colSelector.getSelectedIndex() != -1)
				{
					try{
						subSysSelTab.setModel(createSubSystemTableModel(network, creator));
					}
					catch(NoNetworksToCreateException ex)
					{
						//There are no SubSystems, so we can't select anything.
						subSysSelTab.setModel(new DefaultTableModel());
					}
				}
			}
		});
		//this should set the selection to SUBSYSTEM IIF it is in the list.
		colSelector.setSelectedIndex(0);
		colSelector.setEditable(false);
		colSelector.setSelectedItem("COBRA_" +"SUBSYSTEM");
			

		//columnSelection.add(colSelectionPanel,BorderLayout.LINE_END);
		gbc.gridx = 1;
		MiddlePane.add(colSelector,gbc);
			
		
		String layoutString = "Layout to be used for the subnetworks";
		JTextPane layoutSelectionDesc = createDescription(layoutString);
		gbc.gridy++;
		gbc.gridx = 0;		
		MiddlePane.add(layoutSelectionDesc,gbc);		

		
		//layoutSelector.setPreferredSize(new Dimension(150, 30));
		//layoutSelector.setMaximumSize(new Dimension(200, 30));
		layoutSelector.addItem("Keep Layout");
		for(String algo : AlgorithmNames)
		{
			layoutSelector.addItem(algo);
		}

		BoundsPopupMenuListener layoutlistener = new BoundsPopupMenuListener(true, false);
		layoutSelector.addPopupMenuListener( layoutlistener );		
		layoutSelector.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				algoselected = (String) layoutSelector.getSelectedItem();
			}
		});
		layoutSelector.setSelectedItem("Keep Layout");
		gbc.gridx++;		
		MiddlePane.add(layoutSelector,gbc);		
	}
	

	/**
	 * This Listener listens to the Network GUI to report the selections and items to the NetworkCreator
	 * It 
	 * @author Thomas Pfau
	 *
	 */
	public class NetworkGuiListener implements ActionListener {
		private SubNetworkCreator creator;
		private SubNetworkCreatorGUI GUI;
		/**
		 * Constructor for a NetworkGuiListener listening to <b>GUI</b> and informing the {@link SubNetworkCreator}
		 * @param GUI - The GUI Listened to
		 * @param creator - The Creator which needs to be informed.
		 */
		public NetworkGuiListener(SubNetworkCreatorGUI GUI,SubNetworkCreator creator)
		{
			this.creator = creator;
			this.GUI = GUI;
		}



		@Override
		public void actionPerformed(ActionEvent arg0) {		

			creator.setChoosenAlgorithm(GUI.algoselected);
			creator.setChoosenColumn(GUI.colSelected);
			creator.setCreatedSubSystems(GUI.SubSystemsToGenerate());
			creator.setIgnoredCyNodes(GUI.getNodesToRemove());
			creator.setNoBranchMetas(GUI.getNodesToSkip());
			creator.accept();
			//close the GUI. It's no longer needed. 
			GUI.dispose();
			//and start the network generation.
			creator.runSubNetworkCreation();				
		}

	}
	
	private class MetaboliteSelectionModel extends DefaultTableModel 
	{
					
		
		private String[] ColumnName;
		private int mineditrows = 2;
		private int maxeditrows = 3;
		public boolean usecompartment = false;
		public MetaboliteSelectionModel(String CompName)
		{
			
			if(CompName == null)
			{
				ColumnName = new String[]{"Node Name","Edgecount", "Do not extend", "Remove","CyNode"};				
			}
			else
			{
				ColumnName = new String[]{"Node Name","Compartment","Edgecount", "Do not extend", "Remove","CyNode"};
				mineditrows = 3;
				maxeditrows = 4;
				usecompartment = true;
			}			
			setColumnCount(ColumnName.length);
			setColumnIdentifiers(ColumnName);
			
		}
		@Override
		public String getColumnName(int col)
		{
			return ColumnName[col];
		}
				
		@Override
	    public Class getColumnClass(int c) {
	        return getValueAt(0, c).getClass();
	    }
		/**
		 * get the index of the remove column
		 * @return index of the remove column
		 */
		public int getExtendCol()
		{
			return ColumnName.length - 3;
		}		
		
		/**
		 * get the index of the CyNode column
		 * @return index of the CyNode column
		 */
		public int getCyNodeCol()
		{
			return ColumnName.length - 1;
		}
		
		
		/**
		 * Get the column position representing the Remove Option.
		 * @return the index of the remove column
		 */
		public int getRemoveCol()
		{
			return ColumnName.length - 2;
		}
		@Override
	    public boolean isCellEditable(int row, int col) {
			//The Data retrieved from the Model can not be altered. only the remainder can.
			if (col < mineditrows || col > maxeditrows) {
	            return false;
	        } else {
	            return true;
	        }
	    }
	};

	
}
