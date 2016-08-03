package idare.imagenode.internal.GUI.NetworkSetup;

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
/**
 * ColumnTypeChoose is a JPanel that Provides multiple comboboxes depending on the number of options provided by a selected column. 
 * selected column
 * @author Thomas Pfau
 *
 */
public class ColumnTypeChooser extends JPanel{
	private Color bgcolor;
	Vector<String> ColumnNames;
	CyNetwork network;
	boolean acceptbydefault = false;
	HashMap<JComboBox<String>,Object> selectedObjects;
	private String SelectedColName;
	public final JComboBox<String> compoundTypeSelector = new JComboBox<String>();
	public final JComboBox<String> reactionTypeSelector = new JComboBox<String>();
	public final JComboBox<String> geneTypeSelector = new JComboBox<String>();
	public final JComboBox<String> proteinTypeSelector = new JComboBox<String>();	
	JTextPane compoundDesc;
	JTextPane reactionDesc;
	JTextPane geneDesc;
	JTextPane proteinDesc;
	/**
	 * Default Constructor using the available columnNames 
	 * @param network
	 * @param ColumnNames
	 */
	public ColumnTypeChooser(CyNetwork network, Vector<String> ColumnNames) {
		// TODO Auto-generated constructor stub		
		super();
		String CompoundString = "Value for compound nodes";		
		compoundDesc = createSelectionDescription(CompoundString);
		String reactionString = "Value for interaction nodes";
		reactionDesc = createSelectionDescription(reactionString);
		String geneString = "Value for gene nodes";		
		geneDesc = createSelectionDescription(geneString);
		String proteinString = "Value for protein nodes";
		proteinDesc = createSelectionDescription(proteinString);
		
		this.network = network;
		this.ColumnNames = ColumnNames;
		bgcolor = this.getBackground();
		this.setLayout(new GridBagLayout());
		selectedObjects = new HashMap<JComboBox<String>, Object>();
		compoundTypeSelector.addItemListener(new SelectionChoiceListener(this));
		compoundTypeSelector.setPreferredSize(new Dimension(100,compoundTypeSelector.getPreferredSize().height));
		reactionTypeSelector.addItemListener(new SelectionChoiceListener(this));
		reactionTypeSelector.setPreferredSize(new Dimension(100,reactionTypeSelector.getPreferredSize().height));
		geneTypeSelector.addItemListener(new SelectionChoiceListener(this));
		geneTypeSelector.setPreferredSize(new Dimension(100,geneTypeSelector.getPreferredSize().height));
		proteinTypeSelector.addItemListener(new SelectionChoiceListener(this));
		proteinTypeSelector.setPreferredSize(new Dimension(100,proteinTypeSelector.getPreferredSize().height));
		selectedObjects.put(compoundTypeSelector,null);
		selectedObjects.put(reactionTypeSelector,null);
		selectedObjects.put(geneTypeSelector,null);
		selectedObjects.put(proteinTypeSelector,null);
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
		this.add(geneDesc,gbc);
		gbc.gridy = 6;
		this.add(proteinDesc,gbc);		
		gbc.gridx = 1;
		gbc.gridy = 4;		
		this.add(geneTypeSelector,gbc);
		gbc.gridy = 6;
		this.add(proteinTypeSelector,gbc);
		//this.setPreferredSize(new Dimension(350,120));
	}
	
	/**
	 * Check whether the current selection is acceptable (i.e. no duplicate selections)
	 * @return whether the selection is acceptable or not
	 */
	public boolean acceptable()
	{
		if(acceptbydefault)
			return true;
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
		selectedObjects.put(geneTypeSelector,null);
		selectedObjects.put(proteinTypeSelector,null);
		acceptbydefault = false;
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
		if(diffvals.size() == 3)
		{
			updateChoosersWithThreeOptions();
			this.doLayout();
			this.revalidate();
		}
		else if(diffvals.size() > 3)
		{
			updateChoosersWithFourOptions();
			this.doLayout();
			this.revalidate();
		}
		else
		{
			if(diffvals.size() == 2)
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
				acceptbydefault = true;
				compoundTypeSelector.setVisible(false);
				reactionTypeSelector.setVisible(false);
				geneTypeSelector.setVisible(false);
				proteinTypeSelector.setVisible(false);
				compoundDesc.setVisible(false);
				reactionDesc.setVisible(false);
				geneDesc.setVisible(false);
				proteinDesc.setVisible(false);
			}
		}
	}

	/**
	 * Update the Panel with two choosers.
	 */
	private void updateChoosersWithTwoOptions()
	{
		//this.removeAll();

		//Create two selection Descriptions for Compounds and Interactions

		ComboBoxModel<String> compoundTypeModel = createBipartitionModel(network,SelectedColName);
		compoundTypeSelector.setModel(compoundTypeModel);
		if(compoundTypeModel.getSize() > 0)
		{
			compoundTypeSelector.setSelectedIndex(0);
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
			}
			selectedObjects.put(reactionTypeSelector, reactionTypeSelector.getSelectedItem());
		}
		compoundTypeSelector.setVisible(true);
		reactionTypeSelector.setVisible(true);		
		compoundDesc.setVisible(true);
		reactionDesc.setVisible(true);
		this.proteinTypeSelector.setVisible(false);
		this.geneTypeSelector.setVisible(false);
		this.proteinDesc.setVisible(false);
		this.geneDesc.setVisible(false);
		
		
	}
	/**
	 * Update the Panel with four choosers.
	 * This method will also be used to generate an initial layout with room for four choosers.
	 */
	public void updateChoosersWithThreeOptions()
	{
		//this.removeAll();
		updateChoosersWithTwoOptions();

		ComboBoxModel<String> geneTypeModel = createBipartitionModel(network,SelectedColName);
		geneTypeSelector.setModel(geneTypeModel);
		if(geneTypeModel.getSize() > 0)
		{
			geneTypeSelector.setSelectedIndex(0);
			if(geneTypeModel.getSize() > 2)
			{
				geneTypeSelector.setSelectedIndex(2);	
			}
			selectedObjects.put(geneTypeSelector, geneTypeSelector.getSelectedItem());
		}
/*		ComboBoxModel<String> proteinTypeModel = createBipartitionModel(network,SelectedColName);
		proteinTypeSelector.setModel(proteinTypeModel);
		if(proteinTypeModel.getSize() > 0)
		{
			proteinTypeSelector.setSelectedIndex(0);
			if(proteinTypeModel.getSize() > 3)
			{
				proteinTypeSelector.setSelectedIndex(3);	
			}
			selectedObjects.put(proteinTypeSelector, proteinTypeSelector.getSelectedItem());
		}*/
		/*GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 4;
		this.add(geneDesc,gbc);
		gbc.gridy = 6;
		this.add(proteinDesc,gbc);		
		gbc.gridx = 1;
		gbc.gridy = 4;		
		this.add(geneTypeSelector,gbc);
		gbc.gridy = 6;
		this.add(proteinTypeSelector,gbc);*/
		//this.proteinTypeSelector.setVisible(true);
		this.geneTypeSelector.setVisible(true);
		//this.proteinDesc.setVisible(true);
		this.geneDesc.setVisible(true);
		
	}

	
	
	/**
	 * Update the Panel with four choosers.
	 * This method will also be used to generate an initial layout with room for four choosers.
	 */
	public void updateChoosersWithFourOptions()
	{
		//this.removeAll();
		updateChoosersWithThreeOptions();

		ComboBoxModel<String> proteinTypeModel = createBipartitionModel(network,SelectedColName);
		proteinTypeSelector.setModel(proteinTypeModel);
		if(proteinTypeModel.getSize() > 0)
		{
			proteinTypeSelector.setSelectedIndex(0);
			if(proteinTypeModel.getSize() > 3)
			{
				proteinTypeSelector.setSelectedIndex(3);	
			}
			selectedObjects.put(proteinTypeSelector, proteinTypeSelector.getSelectedItem());
		}
		/*GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 4;
		this.add(geneDesc,gbc);
		gbc.gridy = 6;
		this.add(proteinDesc,gbc);		
		gbc.gridx = 1;
		gbc.gridy = 4;		
		this.add(geneTypeSelector,gbc);
		gbc.gridy = 6;
		this.add(proteinTypeSelector,gbc);*/
		this.proteinTypeSelector.setVisible(true);
		this.proteinDesc.setVisible(true);
		
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

		ColumnTypeChooser ctc;
		public SelectionChoiceListener(ColumnTypeChooser ctc)
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
				//if(taken.contains(selected))
				//{
				//	JOptionPane.showMessageDialog(ctc, "Value already used", "Warning",
				//			JOptionPane.WARNING_MESSAGE);
				//}
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
		if(acceptbydefault)
		{
			return null;
		}
		return compoundTypeSelector.getSelectedItem().toString();		
	}
	/**
	 * Get The ID to use for Reactions
	 * @return the item selected by the interactionSelector
	 */
	public String getReactionID()
	{
		if(acceptbydefault)
		{
			return null;
		}
		return reactionTypeSelector.getSelectedItem().toString();		
	}
	/**
	 * Get The ID to use for Proteins ( if it is available)
	 * @return the item selected by the proteinSelector
	 */
	public String getProteinID()
	{
		if(proteinTypeSelector.getSelectedItem() != null & !acceptbydefault)
		{
			return proteinTypeSelector.getSelectedItem().toString();
		}
		return null;
	}
	/**
	 * Get the identifier to use for Genes ( if it is available)
	 * @return the item selected by the geneSelector
	 */
	public String getGeneID()
	{		
		if(geneTypeSelector.getSelectedItem() != null & !acceptbydefault)
		{
			return geneTypeSelector.getSelectedItem().toString();
		}
		return null;		
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
