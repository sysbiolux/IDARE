package idare.imagenode.internal.GUI.DataSetAddition;

import idare.imagenode.internal.DataManagement.DataSetManager;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
/**
 * A GUI Component which allows the Addition of Datasets.
 * @author Thomas Pfau
 *
 */
public class TunableDataSetAdderGUI extends JPanel{
	FileUtil util;
	DataSetManager dsm;
	JTextField DataSetFileLocation;
	JTextField DataSetDescriptionField;
	JCheckBox useTwoColCheckBox;
	JComboBox<String> DataSetTypeSelector;			
	boolean descriptionModified = false;
	boolean fileNameUpdate = false;
	CySwingApplication cySwingApp;
	public TunableDataSetAdderGUI(DataSetManager dsm, FileUtil util, 	CySwingApplication cySwingApp) {
		super();
		// TODO Auto-generated constructor stub
		this.dsm = dsm;
		this.util = util;
		this.cySwingApp = cySwingApp;
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
	}
		
	private void createMiddleSection(GridBagConstraints gbc)
	{
		gbc.anchor = GridBagConstraints.WEST;   
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		JLabel title = new JLabel("Dataset File Location:");		
		add(title,gbc);
		gbc.gridx++;
		gbc.gridx = 1;
		gbc.weightx = 3;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		DataSetFileLocation = new JTextField();
		DataSetFileLocation.setEditable(true);
		add(DataSetFileLocation,gbc);
		JButton but = new JButton("Choose File");
		gbc.fill = GridBagConstraints.NONE;
		but.addActionListener(new FileSelection(this));
		gbc.weightx = 1;
		gbc.gridy++;		
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.VERTICAL;
		add(but,gbc);
		JLabel title2 = new JLabel("Dataset Description:");
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;
		add(title2,gbc);
		gbc.gridx++;	
		gbc.weightx = 3;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		DataSetDescriptionField = new JTextField("new Dataset");
		DataSetDescriptionField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				if(!fileNameUpdate)
				{
					descriptionModified = true;
				}
			}
		});
		add(DataSetDescriptionField,gbc);
		
		gbc.fill = GridBagConstraints.BOTH;
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
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
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
	}
	/**
	 * An Actionlistener, that pops a {@link FileUtil} fileselection for datasets.
	 * @author Thomas Pfau
	 *
	 */
	private class FileSelection implements ActionListener
	{
		TunableDataSetAdderGUI source; 
		/**
		 * Constructor with a back reference to the originating guid, to be able to update its fields.
		 * @param sourcegui
		 */
		public FileSelection(TunableDataSetAdderGUI sourcegui){
			this.source = sourcegui;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			File selectedFile = source.util.getFile(source.cySwingApp.getJFrame(), "Select Dataset file", FileUtil.LOAD,Collections.singletonList(new FileChooserFilter("DataSet Files",new String[]{""})));					
			source.DataSetFileLocation.setText(selectedFile.getPath());
			source.fileNameUpdate = true;
			if(!source.descriptionModified)
			{
				source.DataSetDescriptionField.setText(selectedFile.getName());
			}
			source.fileNameUpdate = false;
		}
		
	}
	
	public DataSetGenerationParameters getDataSetParameters()
	{
		DataSetGenerationParameters data = new DataSetGenerationParameters();
				data.inputFile = new File(DataSetFileLocation.getText());
		data.useTwoColumns = useTwoColCheckBox.isSelected();
		data.SetDescription = DataSetDescriptionField.getText();
		data.DataSetType = DataSetTypeSelector.getSelectedItem().toString();				
		return data;
		}
	
}
