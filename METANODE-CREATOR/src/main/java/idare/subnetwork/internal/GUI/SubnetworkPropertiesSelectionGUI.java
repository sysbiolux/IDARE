package idare.subnetwork.internal.GUI;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
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

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIDefaults;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import idare.Properties.IDAREProperties;
import idare.ThirdParty.BoundsPopupMenuListener;
import idare.subnetwork.internal.NetworkViewSwitcher;
import idare.subnetwork.internal.NoNetworksToCreateException;
import idare.subnetwork.internal.SubNetworkUtils;
import idare.subnetwork.internal.Tasks.SubsystemGeneration.SubNetworkProperties;

/**
 * A GUI for the selection of the properties for SubNetwork Generation.
 * @author Thomas Pfau
 *
 */

public class SubnetworkPropertiesSelectionGUI extends JPanel{

	
	private CyNetwork network;
	
//	private SubNetworkCreator creator;
	NetworkViewSwitcher nvs;
	public final JComboBox<String> layoutSelector = new JComboBox<String>();
	public final JComboBox<String> colSelector = new JComboBox<String>();
	private JTable metSelTab;
	private JTable subNetSelTab = new JTable();
	private MetaboliteSelectionModel metSelMod;
	private Vector<String> columnNames;
	private String IDCol;
	int colSelected = 0;
	String algoselected = "";
	boolean accepted; 
	private Color bgcolor;
	/**
	 * Generate the GUI for selection of Subnetwork generation properties
	 * @param AlgorithmNames  The Layout algorithms available to the generator
	 * @param ColumnNames  The ColumnNames available for Subnetwork column selection
	 * @param network  the {@link CyNetwork} to generate subnetworks for
	 * @param nvs  the {@link NetworkViewSwitcher} Object to obtain present subnetworks from.
	 * @param IDCol The String Identifying the column used for the compound IDs
	 */
	public SubnetworkPropertiesSelectionGUI(Collection<String> AlgorithmNames, Vector<String> ColumnNames, 
			CyNetwork network, NetworkViewSwitcher nvs, String IDCol){
		this.nvs = nvs;
		this.IDCol = IDCol;		
		columnNames = ColumnNames;
		this.network = network;
		this.setLayout(new GridBagLayout());
		GridBagConstraints outerConst = new GridBagConstraints();
		bgcolor = this.getBackground();
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
		this.add(titleField,outerConst);
		
		
		outerConst.fill = GridBagConstraints.HORIZONTAL;
	    outerConst.anchor = GridBagConstraints.CENTER;
	    outerConst.weightx = 1;
		outerConst.gridx = 0;
		outerConst.gridy = 1;
		outerConst.weighty = 1;
		outerConst.gridwidth = 1;
		outerConst.fill = GridBagConstraints.HORIZONTAL;
		try{
			createMiddleSection(ColumnNames, AlgorithmNames,this,outerConst);
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
		try{
			createMetaboliteSelection(network,outerConst,this);
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
		outerConst.gridx = 0;		
		outerConst.weighty = 1;
		outerConst.gridwidth = GridBagConstraints.REMAINDER;
		outerConst.fill = GridBagConstraints.HORIZONTAL;				
		createSubnetworkSelection(outerConst,this);		
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
		selectionDesc.setBackground(bgcolor);		
		return selectionDesc;
		
	}
	
	/**
	 * Create the Subnetwork Selection Panel
	 * @param gbc the overall {@link GridBagConstraints} to properly place this panel
	 * @param MiddlePane the Pane that the subsnetworkselection gets added to.
	 */
	private void createSubnetworkSelection(GridBagConstraints gbc, Container MiddlePane)
	{
		String TitleString = "Select the Subnetworks to be generated";
		JTextPane TitlePanel = createDescription(TitleString);
		gbc.gridy++;
		gbc.weighty = 1;
		MiddlePane.add(TitlePanel,gbc);
			
		subNetSelTab.setAutoCreateRowSorter(true);
		subNetSelTab.setFillsViewportHeight(true);
		try
		{
			TableModel model = createSubNetworkTableModel(network);
			subNetSelTab.setModel(model);
		}
		catch(NoNetworksToCreateException ex)
		{
			subNetSelTab.setModel(new DefaultTableModel());
		}
		gbc.gridy++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 6;
		JScrollPane subSysPane = new JScrollPane(subNetSelTab);
		int colscount = subNetSelTab.getColumnModel().getColumnCount();
		subNetSelTab.getColumnModel().getColumn(colscount-1).setMaxWidth(100);
		Dimension dim = subNetSelTab.getPreferredSize();
		dim.height = 200;
		subNetSelTab.setPreferredScrollableViewportSize(dim);
		JButton SelectAllButton = new JButton(new AbstractAction() {						
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int nrows = subNetSelTab.getModel().getRowCount();
				for(int i = 0; i < nrows; i++)
				{
					subNetSelTab.getModel().setValueAt(true, i, 1);
				}
			}
		});
		
		SelectAllButton.setText("Select All");
		JButton DeSelectAllButton = new JButton(new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int nrows = subNetSelTab.getModel().getRowCount();
				for(int i = 0; i < nrows; i++)
				{
					subNetSelTab.getModel().setValueAt(false, i, 1);
				}
			}
		});
		JPanel ButtonPanel = new JPanel();
		ButtonPanel.setLayout(new BorderLayout(5,0));
		DeSelectAllButton.setText("Clear Selection");
		MiddlePane.add(subSysPane,gbc);
		gbc.gridy = gbc.gridy+1;
		gbc.gridx = 1;
		JPanel P2 = new JPanel();
		P2.add(SelectAllButton);
		P2.add(DeSelectAllButton);
		ButtonPanel.add(P2,BorderLayout.LINE_END);			
		MiddlePane.add(ButtonPanel,gbc);		
	}
	/**
	 * Create the TableModel for the SubSystem Selection
	 * @param network - the {@link CyNetwork}Subsystems are created for
	 * @return A {@link TableModel} containing a list of potential Subnetworks
	 * @throws NoNetworksToCreateException if all selected networks are already created or none are equested.
	 */
	public TableModel createSubNetworkTableModel(CyNetwork network) throws NoNetworksToCreateException
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
		Vector<Object> subSysNames = SubNetworkUtils.getDifferentSubSystems(network.getDefaultNodeTable(), colSelector.getSelectedItem().toString());

		Set<String> existingSubSystems = nvs.getSubNetworkWorksForNetwork(network, colSelector.getSelectedItem().toString());
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
		return subSysSelMod;
	}
	
	/**
	 * Update the List of selectable species 
	 * @param speciesNodes A map of species representing nodes with the their respective number of interactions. 
	 * @param speciesList a List of (sortable) Nodes (i.e. nodes with a corresponding integer value)
	 * @param node the node to add to the map and list
	 * @param network the network the node is in.
	 */
	private void updateSpeciesLists(HashMap<CyNode,Integer> speciesNodes,	List<SortEntry> speciesList, CyNode node, CyNetwork network)
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
		speciesList.add(new SortEntry(node, reactionEdgeCount));
		speciesNodes.put(node, reactionEdgeCount);
	}
	/**
	 * Create the Metabolite Selection Panel where you can choose which Metabolites to use and expand
	 * @param network  The {@link CyNetwork} the metabolites are chosen from
	 * @param gbc The global {@link GridBagConstraints} used for placement
	 * @param MiddleSection the Container to add the MetaboliteSelection to. 
	 */
	private void createMetaboliteSelection(CyNetwork network, GridBagConstraints gbc, Container MiddleSection)
	{		
				
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
		List<CyNode> nodes = network.getNodeList();
		HashMap<CyNode,Integer> metaboliteNodes = new HashMap<CyNode, Integer>();
		List<SortEntry> metaboliteList = new LinkedList<SortEntry>();
	
		for(CyNode node : nodes)
		{
							
			
			CyRow nodeRow = network.getRow(node);			
			String TypeEntry = nodeRow.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class);
			
			if(TypeEntry != null && TypeEntry.equals(IDAREProperties.NodeType.IDARE_SPECIES))
			{
				updateSpeciesLists(metaboliteNodes,metaboliteList, node, network);
			}
		}
				

		Collections.sort(metaboliteList,new SortItems());
		int reactioncount = network.getDefaultNodeTable().getMatchingRows(IDAREProperties.IDARE_SUBNETWORK_TYPE, IDAREProperties.NodeType.IDARE_REACTION).size();
		for(SortEntry entry: metaboliteList)
		{			
			Vector<Object> row = new Vector<Object>();
			row.add(network.getDefaultNodeTable().getRow(entry.key.getSUID()).get(IDCol, String.class));
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
			if(entry.value > (int) Math.ceil(Math.sqrt(reactioncount)*0.7))
			{
				row.add(true);
			}
			else
			{
				row.add(false);
			}
			//
			if(entry.value > (int) Math.ceil(Math.sqrt(reactioncount) * 0.7) && (entry.value < (int) Math.ceil(Math.sqrt(reactioncount))))
			{
				row.add(true);
			}			
			else
			{
				row.add(false);
			}
			if(entry.value > (int) Math.ceil(Math.sqrt(reactioncount)))
			{
				row.add(true);
			}			
			else
			{
				row.add(false);
			}	
			row.add(entry.key);
			metSelMod.addRow(row);
		}
		metSelTab.setModel(metSelMod);
		metSelTab.getColumnModel().getColumn(metSelMod.getExtendCol()).setMaxWidth(130);
		metSelTab.getColumnModel().getColumn(metSelMod.getRemoveCol()).setMaxWidth(70);
		metSelTab.getColumnModel().getColumn(metSelMod.getDuplicateCol()).setMaxWidth(70);
		Dimension dim = metSelTab.getPreferredSize();
		dim.height = 300;
		metSelTab.setPreferredScrollableViewportSize(dim);
		metSelTab.getModel().addTableModelListener(new TableModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent e) {
				// TODO Auto-generated method stub
				if(e.getColumn() == metSelMod.getDuplicateCol())
				{
					for(int i = e.getFirstRow(); i <= e.getLastRow(); i++)
					{
						if((Boolean) metSelMod.getValueAt(i, e.getColumn()))
						{
							if((Boolean) metSelMod.getValueAt(i, metSelMod.getRemoveCol()))
							{
								metSelMod.setValueAt(false, i, metSelMod.getRemoveCol());
							}
						}
					}
				}
				if(e.getColumn() == metSelMod.getRemoveCol())
				{
					for(int i = e.getFirstRow(); i <= e.getLastRow(); i++)
					{
						if((Boolean) metSelMod.getValueAt(i, e.getColumn()))
						{
							if((Boolean) metSelMod.getValueAt(i, metSelMod.getDuplicateCol()))
							{
								metSelMod.setValueAt(false, i, metSelMod.getDuplicateCol());
							}
						}
					}
				}
					
			}
		});
		// and now hide the CyNode Column.
		TableColumnModel tcm = metSelTab.getColumnModel();
		tcm.removeColumn(tcm.getColumn(metSelMod.getCyNodeCol()));
		gbc.gridy++;
		gbc.weighty = 4;
		gbc.fill = GridBagConstraints.BOTH;
		JScrollPane metSelPane = new JScrollPane(metSelTab);
		//metSelPane.setMinimumSize(new Dimension(200,100));
		//metSelPane.setMaximumSize(new Dimension(1000,800));
		MiddleSection.add(metSelPane,gbc);
	}
	
	/**
	 * Get the selected SubSystems from the SubSystemSelector 
	 * @return A {@link Vector} of all Objects which were selected
	 */
	public Vector<Object> SubSystemsToGenerate()
	{
		Vector<Object> subSystems = new Vector<Object>();
		for(int i=0; i < subNetSelTab.getModel().getRowCount(); i++)
		{
			if((Boolean)subNetSelTab.getModel().getValueAt(i, 1))
			{
				subSystems.add(subNetSelTab.getModel().getValueAt(i, 0));
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

		int RemPos = metSelMod.usecompartment ? 5 : 4;
		int ValPos = metSelMod.usecompartment ? 6 : 5;		
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
		int ValPos = metSelMod.usecompartment ? 6 : 5;		
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
	 * Get the {@link CyNode} which should not link to other networks 
	 * @return a {@link Set} of {@link CyNode}s that are not used to create links
	 */
	public Set<CyNode> getNodesToDuplicate()
	{
		Set<CyNode> NodesToDuplicate= new HashSet<CyNode>();
		int DupPos = metSelMod.usecompartment ? 4 : 3;
		int ValPos = metSelMod.usecompartment ? 6 : 5;		
		for(int i=0; i < metSelTab.getModel().getRowCount(); i++)
		{
			
			if((Boolean)metSelTab.getModel().getValueAt(i, DupPos))
			{
				NodesToDuplicate.add((CyNode)metSelTab.getModel().getValueAt(i, ValPos));
			}
		}
		return NodesToDuplicate;
		
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
	 * Create the Middle section of the GUI
	 * @param ColumnNames - The Column names to choose from to obtain the Subnetwork possibilities
	 * @param AlgorithmNames - The Names of all potential Layouting algorithms
	 * @param MiddlePane The {@link Container} to create the middle section in
	 * @param gbc the global {@link GridBagConstraints} to layout this section correctly.
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
						subNetSelTab.setModel(createSubNetworkTableModel(network));
						Dimension dim = subNetSelTab.getPreferredSize();
						dim.height = 200;
						subNetSelTab.setPreferredScrollableViewportSize(dim);						
					}
					catch(NoNetworksToCreateException ex)
					{
						//There are no SubSystems, so we can't select anything.
						subNetSelTab.setModel(new DefaultTableModel());
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
	 * Get the Properties for the Subnetwork generation task as defined in this GUI
	 * @return The {@link SubNetworkProperties} to be used.
	 */
	public SubNetworkProperties getProperties()
	{
		SubNetworkProperties props = new SubNetworkProperties();
		
		props.selectedLayoutAlgorithmName = algoselected;
		props.ColumnName = columnNames.get(colSelected);		
		props.subSystems = SubSystemsToGenerate();
		props.ignoredNodes = getNodesToRemove();
		props.noBranchNodes = getNodesToSkip();
		props.duplicateNodes = getNodesToDuplicate();
		props.currentNetwork = network;
		return props;
	}
	
	
	/**
	 * A slightly modified DefaultTableModel to visualise the MetaboliteSelection
	 * @author Thomas Pfau
	 *
	 */
	private class MetaboliteSelectionModel extends DefaultTableModel 
	{
	
		private String[] ColumnName;
		private int mineditrows = 2;
		private int maxeditrows = 4;
		public boolean usecompartment = false;
		public MetaboliteSelectionModel(String CompName)
		{
			
			if(CompName == null)
			{
				ColumnName = new String[]{"Node Name","Edgecount", "Do not extend", "Duplicate", "Remove","CyNode"};				
			}
			else
			{
				ColumnName = new String[]{"Node Name","Compartment","Edgecount", "Do not extend", "Duplicate", "Remove","CyNode"};
				mineditrows = 3;
				maxeditrows = 5;
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
			return ColumnName.length - 4;
		}		
		
		/**
		 * get the index of the remove column
		 * @return index of the remove column
		 */
		public int getDuplicateCol()
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
