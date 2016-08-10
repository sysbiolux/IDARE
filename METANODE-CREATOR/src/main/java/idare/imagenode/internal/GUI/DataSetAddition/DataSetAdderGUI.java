package idare.imagenode.internal.GUI.DataSetAddition;

import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.GUI.DataSetAddition.Tasks.DataSetAdderTaskFactory;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.Tunable;
/**
 * A GUI Component which allows the Addition of Datasets.
 * @author Thomas Pfau
 *
 */
public class DataSetAdderGUI extends JDialog{
	FileUtil util;
	DataSetManager dsm;
	JTextField DataSetFileLocation;
	JTextField DataSetDescriptionField;
	JCheckBox useTwoColCheckBox;
	JComboBox<String> DataSetTypeSelector;			
	
	DataSetAdderTaskFactory dsatf;
	public DataSetAdderGUI(CySwingApplication cySwingApp, DataSetManager dsm, FileUtil util, DataSetAdderTaskFactory dsatf) {
		super(cySwingApp.getJFrame(),"DataSet Property Selection");
		// TODO Auto-generated constructor stub
		this.dsm = dsm;
		this.util = util;
		this.dsatf = dsatf;
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		JTextField titleField = new JTextField();
		titleField.setText("Select File and Dataset parameters:");
		titleField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		titleField.setBackground(this.getBackground());
		titleField.setCursor(null);
		titleField.setEditable(false);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;			
		add(titleField,gbc);
		
		createMiddleSection(gbc);
		
		
		this.pack();				
		this.setLocationRelativeTo(cySwingApp.getJFrame());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
		
	}
		
	private void createMiddleSection(GridBagConstraints gbc)
	{
		gbc.anchor = GridBagConstraints.WEST;   
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.fill = gbc.BOTH;
		JLabel title = new JLabel("Dataset File Location:");		
		add(title,gbc);
		gbc.gridx++;
		gbc.gridx = 1;
		gbc.weightx = 3;
		gbc.gridwidth = gbc.REMAINDER;
		DataSetFileLocation = new JTextField();
		DataSetFileLocation.setEditable(true);
		add(DataSetFileLocation,gbc);
		JButton but = new JButton("Choose File");
		gbc.fill = gbc.NONE;
		but.addActionListener(new FileSelection(this));
		gbc.weightx = 1;
		gbc.gridy++;		
		gbc.gridwidth = 1;
		gbc.fill = gbc.VERTICAL;
		add(but,gbc);
		JLabel title2 = new JLabel("Dataset Description:");
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = gbc.BOTH;
		add(title2,gbc);
		gbc.gridx++;	
		gbc.weightx = 3;
		gbc.gridwidth = gbc.REMAINDER;
		gbc.fill = gbc.HORIZONTAL;
		DataSetDescriptionField = new JTextField("new Dataset");
		add(DataSetDescriptionField,gbc);
		
		gbc.fill = gbc.BOTH;
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 1;
		gbc.gridwidth = 1;
		JLabel title3 = new JLabel("Dataset Type:");
		add(title3,gbc);
				
		Vector<String> dataSetTypes = new Vector<String>();
		dataSetTypes.addAll(dsm.getAvailableDataSetTypes());
		DataSetTypeSelector = new JComboBox<String>(dataSetTypes);
		gbc.gridx++;
		gbc.weightx = 3;
		gbc.fill = gbc.HORIZONTAL;
		gbc.gridwidth = gbc.REMAINDER;
		add(DataSetTypeSelector,gbc);
		
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.weightx = 1;
		JLabel twoHeaderLabel = new JLabel("Use two column headers:");
		twoHeaderLabel.setToolTipText("The First Column in the Datafile is the id which will be matched,\n the second the label on the node.\n");		
		add(twoHeaderLabel, gbc);
		gbc.gridx++;		
		useTwoColCheckBox = new JCheckBox();
		useTwoColCheckBox.setSelected(true);
		gbc.fill = GridBagConstraints.BOTH;
		add(useTwoColCheckBox,gbc);
		JButton CreateDS = new JButton("Create Dataset");
		gbc.gridx++;
		gbc.gridwidth = 1;		
		add(CreateDS,gbc);
		CreateDS.addActionListener(new CreateDataSetSelection(dsm, this));
	}
	/**
	 * An Actionlistener, that pops a {@link FileUtil} fileselection for datasets.
	 * @author Thomas Pfau
	 *
	 */
	private class FileSelection implements ActionListener
	{
		DataSetAdderGUI source; 
		/**
		 * Constructor with a back reference to the originating guid, to be able to update its fields.
		 * @param sourcegui
		 */
		public FileSelection(DataSetAdderGUI sourcegui){
			this.source = sourcegui;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			File selectedFile = source.util.getFile(source, "Select Dataset file", FileUtil.LOAD,Collections.singletonList(new FileChooserFilter("DataSet Files",new String[]{""})));					
			source.DataSetFileLocation.setText(selectedFile.getPath());
		}
		
	}
	
	/**
	 * Class to listen to a request to create a DataSet.
	 * @author Thomas Pfau
	 *
	 */
	private class CreateDataSetSelection implements ActionListener
	{
		DataSetManager dsm;
		DataSetAdderGUI gui;
		/**
		 * Constructor with a back reference to the originating gui, to be able to update and access its fields.
		 * Also access to the {@link DataSetManager} to build the selected {@link DataSet}
		 * @param gui
		 * @param dsm
		 */
		public CreateDataSetSelection(DataSetManager dsm,DataSetAdderGUI gui)
		{
			this.dsm = dsm;
			this.gui = gui;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//get the properties.
			File f = new File(gui.DataSetFileLocation.getText());
			boolean useTwoColHeaders = gui.useTwoColCheckBox.isSelected();
			String Description = gui.DataSetDescriptionField.getText();
			//Check Description 
			if(Description == "")
			{
				JOptionPane.showMessageDialog(gui, "Description needed for Dataset", "No Description Set", JOptionPane.ERROR_MESSAGE);
				return;
			}				
			//dsatf.addDataset(f, useTwoColHeaders, Description, gui.DataSetTypeSelector.getSelectedItem().toString());
			//create DataSet with the given properties.
			gui.dispose();
		}
		
	}
	
}
