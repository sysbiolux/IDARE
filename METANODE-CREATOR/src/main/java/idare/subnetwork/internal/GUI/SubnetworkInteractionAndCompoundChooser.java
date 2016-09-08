package idare.subnetwork.internal.GUI;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIDefaults;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;

public class SubnetworkInteractionAndCompoundChooser extends JPanel {
	private Color bgcolor;
	Vector<String> ColumnNames;
	CyNetwork network;
	HashMap<JComboBox<String>,Object> selectedObjects;
	private String SelectedColName;
	public final JComboBox<String> compoundTypeSelector = new JComboBox<String>();
	public final JComboBox<String> reactionTypeSelector = new JComboBox<String>();
	JTextPane compoundDesc;
	JTextPane reactionDesc;
	/**
	 * Default Constructor using the available columnNames 
	 * @param network
	 * @param ColumnNames
	 */
	public SubnetworkInteractionAndCompoundChooser(CyNetwork network, Vector<String> ColumnNames) {
		// TODO Auto-generated constructor stub		
		super();
		String CompoundString = "Value for branching nodes";		
		compoundDesc = createSelectionDescription(CompoundString);
		String reactionString = "Value for subsystem nodes";
		reactionDesc = createSelectionDescription(reactionString);
		
		this.network = network;
		this.ColumnNames = ColumnNames;
		bgcolor = this.getBackground();
		this.setLayout(new GridBagLayout());
		selectedObjects = new HashMap<JComboBox<String>, Object>();
		compoundTypeSelector.addItemListener(new SelectionChoiceListener(this));
		compoundTypeSelector.setPreferredSize(new Dimension(100,compoundTypeSelector.getPreferredSize().height));
		compoundTypeSelector.setEditable(false);
		reactionTypeSelector.addItemListener(new SelectionChoiceListener(this));
		reactionTypeSelector.setPreferredSize(new Dimension(100,reactionTypeSelector.getPreferredSize().height));
		reactionTypeSelector.setEditable(false);
		selectedObjects.put(compoundTypeSelector,null);
		selectedObjects.put(reactionTypeSelector,null);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		this.add(compoundDesc,gbc);
		gbc.gridy = 2;
		this.add(reactionDesc,gbc);		
		gbc.gridx = 1;
		gbc.gridy = 0;		
		this.add(compoundTypeSelector,gbc);
		gbc.gridy = 2;
		this.add(reactionTypeSelector,gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 4;
		//this.setPreferredSize(new Dimension(350,120));
	}
	
	/**
	 * Check whether the current selection is acceptable (i.e. no duplicate selections)
	 * @return whether the selection is acceptable or not
	 */
	public boolean acceptable()
	{
		List<CyRow> rows = network.getDefaultNodeTable().getAllRows();
		HashSet<Object> diffvals = new HashSet<Object>(); 
		for(CyRow row : rows)
		{
			diffvals.add(row.getRaw(SelectedColName));
		}
		Set<Object> choices = getSelectedItems(null);
		int selectedVals = getSelectionCount();
		if(selectedObjects.get(compoundTypeSelector) == null || selectedObjects.get(reactionTypeSelector) == null)
		{
			return false;
		}
		else 
		{
			return (selectedVals <= choices.size());  
		}		
	}
	/**
	 * Get the problem that causes this selection to be not acceptable.
	 * @return
	 */
	public String getProblemMessage()
	{
		List<CyRow> rows = network.getDefaultNodeTable().getAllRows();
		HashSet<Object> diffvals = new HashSet<Object>(); 
		for(CyRow row : rows)
		{
			diffvals.add(row.getRaw(SelectedColName));
		}
		Set<Object> choices = getSelectedItems(null);
		int selectedVals = getSelectionCount();
		if(selectedObjects.get(compoundTypeSelector) == null)
		{
			return "No identifier for compounds was selected";
		}
		if(selectedObjects.get(reactionTypeSelector) == null)
		{
			return "No identifier for interactions was selected";
		}
		
		if(selectedVals <= choices.size())
		{
			HashSet<Object> usedItems = new HashSet<Object>();
			for(JComboBox<String> box : selectedObjects.keySet())
			{
				if(selectedObjects.get(box) == null)
				{
					continue;
				}
				if(usedItems.contains(selectedObjects.get(box)))
				{
					return "Duplicate selection for compoind and interaction identifier: " + selectedObjects.get(box).toString(); 
				}
				usedItems.add(selectedObjects.get(box));
			}
			
		}		
		return "";
	}
	/**
	 * Get the number of non null entries of selected items.
	 * @return
	 */
	private int getSelectionCount()
	{
		int i = 0;
		for(JComboBox<String> box : selectedObjects.keySet())
		{
			if(selectedObjects.get(box) != null)
			{
				i+=1;
			}			
		}
		return i;
	}
	
	/**
	 * Update the Choose ComboBoxes according to a selected Column.
	 * The Items of the selected columns  will be investigated for options.
	 * @param colSelected
	 */
	public void updateChoosers(int colSelected)
	{
		//reset the selected Objects.
		selectedObjects.put(compoundTypeSelector,null);
		selectedObjects.put(reactionTypeSelector,null);
		//determine the number of diferent values.
		SelectedColName = ColumnNames.get(colSelected);
		List<CyRow> rows = network.getDefaultNodeTable().getAllRows();
		HashSet<Object> diffvals = new HashSet<Object>(); 
		for(CyRow row : rows)
		{
			diffvals.add(row.getRaw(ColumnNames.get(colSelected)));
		}
		diffvals.remove(null);
		diffvals.remove("");
		//and set the choosers accordingly.
		if(diffvals.size() >= 2)
		{
				updateChoosersWithTwoOptions();
				//Dimension prefSize = this.getPreferredSize();
				//prefSize.height *= 2;
				//this.setPreferredSize(prefSize);
				this.doLayout();
				this.revalidate();
		}
		else
		{
				//we have less than two different values, so we set everything invisible and accept by default. 				
				compoundTypeSelector.setVisible(false);
				reactionTypeSelector.setVisible(false);
				compoundDesc.setVisible(false);
				reactionDesc.setVisible(false);
		}
		
	}

	/**
	 * Update the Panel with two choosers.
	 */
	public void updateChoosersWithTwoOptions()
	{
		ComboBoxModel<String> compoundTypeModel = createBipartitionModel(network,SelectedColName);
		compoundTypeSelector.setModel(compoundTypeModel);
		if(compoundTypeModel.getSize() > 0)
		{
			compoundTypeSelector.setSelectedIndex(0);
			compoundTypeSelector.setSelectedItem("species");
			selectedObjects.put(compoundTypeSelector, compoundTypeSelector.getSelectedItem());
		}
		ComboBoxModel<String> reactionTypeModel = createBipartitionModel(network,SelectedColName);		
		reactionTypeSelector.setModel(reactionTypeModel);
		if(reactionTypeModel.getSize() > 0)
		{
			reactionTypeSelector.setSelectedIndex(0);
			if(reactionTypeModel.getSize() > 1)
			{
				reactionTypeSelector.setSelectedIndex(1);
				compoundTypeSelector.setSelectedItem("reaction");
			}
			selectedObjects.put(reactionTypeSelector, reactionTypeSelector.getSelectedItem());
		}
		compoundTypeSelector.setVisible(true);
		compoundTypeSelector.setSelectedItem("species");
		reactionTypeSelector.setVisible(true);		
		reactionTypeSelector.setSelectedItem("reaction");
		compoundDesc.setVisible(true);
		reactionDesc.setVisible(true);
		
		
	}

	/**
	 * Create a Selection description based on a given string
	 * @param DescriptionString
	 * @return
	 */
	private JTextPane createSelectionDescription(String DescriptionString)
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
		selectionDesc.setPreferredSize(new Dimension(200,45));
		selectionDesc.setBackground(bgcolor);
		return selectionDesc;		
	}
	/**
	 * Get the items used in the comboboxes, excluding those provided. 
	 * @param excluded
	 * @return a set of the selected Items from the choosers
	 */
	public Set<Object> getSelectedItems(JComboBox<String> excluded)
	{
		HashSet<Object> usedItems = new HashSet<Object>();
		for(JComboBox<String> box : selectedObjects.keySet())
		{
			if(box.equals(excluded))
			{
				continue;
			}
			if(selectedObjects.get(box) == null)
			{
				continue;
			}
			usedItems.add(selectedObjects.get(box));
		}
		return usedItems;
	}




	/**
	 * This Listener listens to the choice of selection and fire Warnings
	 * @author Thomas Pfau
	 *
	 */
	private class SelectionChoiceListener implements ItemListener
	{

		SubnetworkInteractionAndCompoundChooser ctc;
		public SelectionChoiceListener(SubnetworkInteractionAndCompoundChooser ctc)
		{
			this.ctc = ctc;
		}
		@Override
		public void itemStateChanged(ItemEvent e) {

			if(e.getStateChange() == ItemEvent.SELECTED)
			{

				JComboBox<String> box = (JComboBox<String>)e.getSource();
				Object selected = box.getSelectedItem();
				Set<Object> taken = ctc.getSelectedItems(box);
				/*if(taken.contains(selected))
				{
					JOptionPane.showMessageDialog(ctc, "Value already used", "Warning",
							JOptionPane.WARNING_MESSAGE);
				}*/
				ctc.selectedObjects.put(box, selected);
			}
		}
	}
	
	/**
	 * Get The ID to use for Compounds
	 * @return the item selected by the compoundSelector
	 */
	public String getCompoundID()
	{
		return compoundTypeSelector.getSelectedItem().toString();		
	}
	/**
	 * Get The ID to use for Reactions
	 * @return the item selected by the interactionSelector
	 */
	public String getReactionID()
	{
		return reactionTypeSelector.getSelectedItem().toString();		
	}

	/**
	 * Create a Comboboxmodel with one element per different string present in the Column in the network. 
	 * @return
	 */
	private ComboBoxModel<String> createBipartitionModel(CyNetwork network,String ColName)
	{
		DefaultComboBoxModel<String> compoundColumnSelectionModel = new DefaultComboBoxModel<String>();
		List<CyRow> rows = network.getDefaultNodeTable().getAllRows();
		HashSet<Object> diffvals = new HashSet<Object>(); 
		for(CyRow row : rows)
		{
			try{
				diffvals.add(row.getRaw(ColName));
			}
			catch(NullPointerException e)
			{
			}
		}
		//add all different items 
		for(Object o : diffvals)
		{
			if(o != null)
			{
				compoundColumnSelectionModel.addElement(o.toString());
			}
			else
			{
				compoundColumnSelectionModel.addElement(null);
			}
		}
		return compoundColumnSelectionModel;
	}
}
