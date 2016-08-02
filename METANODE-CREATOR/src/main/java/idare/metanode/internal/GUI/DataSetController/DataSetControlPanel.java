package idare.metanode.internal.GUI.DataSetController;

import idare.metanode.internal.DataManagement.DataSetManager;
import idare.metanode.internal.DataManagement.NodeManager;
import idare.metanode.internal.GUI.DataSetAddition.DataSetAdderGUI;
import idare.metanode.internal.GUI.DataSetAddition.Tasks.DataSetAdderTaskFactory;
import idare.metanode.internal.Interfaces.DataSet;
import idare.metanode.internal.Layout.ColorMapDataSetBundle;
import idare.metanode.internal.Layout.NodeLayout;
import idare.metanode.internal.Properties.METANODEPROPERTIES;
import idare.metanode.internal.Utilities.GUIUtils;
import idare.metanode.internal.Utilities.LayoutUtils;
import idare.metanode.internal.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.metanode.internal.exceptions.layout.DimensionMismatchException;
import idare.metanode.internal.exceptions.layout.TooManyItemsException;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.swing.JSVGCanvas;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.util.swing.FileUtil;
import org.w3c.dom.svg.SVGDocument;
/**
 * A Panel that controls the {@link DataSet}s, and allows the management of said sets.
 * @author Thomas Pfau
 *
 */
public class DataSetControlPanel extends JPanel implements CytoPanelComponent{
	DataSetSelectionTable DataSetTable;
	IDValueLabel commons = new IDValueLabel("Common:", "0");
	IDValueLabel totals = new IDValueLabel("Total:", "0");
	JDialog PreviewFrame;
	JDialog PreviewLegendFrame;
	DataSetSelectionModel dssm;
	private DataSetManager dsm;
	private CreateNodesTaskFactory nodeFactory;
	private DataSetAdderTaskFactory dsatf;	

	/**
	 * The Constructor for a DataSetControlPanel.
	 * @param cySwingApp - The App which this Panel will become a part of.
	 * @param dsm - The {@link DataSetManager}, this panel will represent
	 * @param util - A {@link FileUtil} to obtain files.
	 * @param nmr - The {@link NodeManager} used for node preview
	 */
	public DataSetControlPanel(CySwingApplication cySwingApp, DataSetManager dsm, FileUtil util, NodeManager nmr) {
		//initialize the fields;
		this.dsm = dsm;
		this.setLayout(new GridBagLayout());
		Vector<DataSet> currentSets = new Vector<DataSet>();
		for(DataSet ds : this.dsm.getDataSets())
		{
			currentSets.add(ds);
		}		
		dssm = new DataSetSelectionModel(dsm);
		dssm.addTableModelListener(new SelectionListener(this));		

		//Set up the DataSetAdder Button
		JButton addDSButton = new JButton("Add Dataset");		
		addDSButton.addActionListener(new DataSetAdderGuiAction(cySwingApp,dsm,util));



		//Set up the accept button
		JButton acceptButton = new JButton("Create Visualisation");		
		acceptButton.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				nodeFactory.run();
			}
		});
		//Set up the Preview button
		JButton previewButton = new JButton("Preview Visualisation");
		previewButton.addActionListener(new PreviewLayoutListener(this,nmr, cySwingApp));

		//Set up the DataSetTable
		DataSetTable = new DataSetSelectionTable(dssm);
		//Add DataSetRemover Button
		JButton removeDSButton = new JButton("Remove Dataset");		
		removeDSButton.addActionListener(new DataSetRemoveAction(DataSetTable,dsm));		
		// create the Up/Down buttons
		JButton moveUp = new JButton("Move up");		
		moveUp.addActionListener(new MoveListener(true,DataSetTable));
		JButton moveDown = new JButton("Move Down");
		moveDown.addActionListener(new MoveListener(false,DataSetTable));
		//Set up the table properties
		//DataSetTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		DataSetTable.getColumnModel().getColumn(2).setMaxWidth(70);
		DataSetTable.getColumnModel().getColumn(2).setMinWidth(70);
		DataSetTable.getColumnModel().getColumn(4).setMinWidth(70);
		DataSetTable.getColumnModel().getColumn(1).setMaxWidth(55);		
		DataSetTable.getColumnModel().getColumn(1).setMinWidth(55);
		DataSetTable.getColumnModel().getColumn(0).setMinWidth(100);
		DataSetTable.getSelectionModel().addListSelectionListener(new RowSelectionListener(DataSetTable, moveUp, moveDown));

		//Set up the title
		JTextPane titleField = GUIUtils.createSelectionDescription("Dataset Management", this.getBackground(), new Font(Font.SANS_SERIF, Font.BOLD, 18));

		GridBagConstraints gbc = new GridBagConstraints();
		//generate the layout.
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 0.01;			
		add(titleField,gbc);
		gbc.gridwidth = 2;
		gbc.gridy++;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		add(addDSButton,gbc);		
		gbc.gridx+=2;
		add(removeDSButton,gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy++;
		//Layout position of the Table within a ScrollPane.
		gbc.gridx = 0;
		//gbc.gridy = 1;
		//gbc.gridheight = 1;
		//gbc.gridwidth = 3;
		gbc.weighty = 10;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		JScrollPane DataSetSelectionPane = new JScrollPane(DataSetTable);
		add(DataSetSelectionPane,gbc);
		//Add the move Up and MOve Down buttons beside the DataSetSelection Table
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.weighty = 1;
		gbc.gridheight = 1;
		gbc.gridy++;
		gbc.fill = GridBagConstraints.NONE;
		add(acceptButton,gbc);

		gbc.gridx+=2;
		add(previewButton,gbc);

		//Set up the info fields for
		gbc.gridx = 0;
		gbc.gridheight = 1;
		gbc.weighty = 1;
		gbc.weightx = 2;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(commons,gbc);
		gbc.gridx = 1;
		add(totals,gbc);
		gbc.weightx = 0.3;
		//The Accept button is at position 3 in this row.
		gbc.gridx = 2;
		add(moveUp,gbc);
		//And the prview button at position 4
		gbc.gridx = 3;
		add(moveDown,gbc);
	}
	/**
	 * Set the {@link CreateNodesTaskFactory} used by this Panel.
	 * @param nodeFactory
	 */
	public void setNodeFactory(CreateNodesTaskFactory nodeFactory) {
		this.nodeFactory = nodeFactory;
	}

	/**
	 * Set the {@link DataSetAdderTaskFactory} used by this Panel.
	 * @param dsadderFactory
	 */
	public void setDatasetAdderFactory(DataSetAdderTaskFactory dsadderFactory) {
		this.dsatf = dsadderFactory;
	}
	/**
	 * Create A Preview of the node and its legend node. 
	 * @author Thomas Pfau
	 *
	 */
	private class PreviewLayoutListener implements ActionListener
	{

		DataSetControlPanel builder;
		NodeManager nm;	
		CySwingApplication cyApp;
		/**
		 * Default constructor, with a Controlpanale to obtain the data, 
		 * @param builder - The control panel from which to obtain the data. 
		 * @param nm - The node manager to obtain a sample node from.  
		 * @param cySwingApp
		 */
		public PreviewLayoutListener(DataSetControlPanel builder, NodeManager nm, CySwingApplication cySwingApp)
		{
			this.builder = builder;
			this.nm = nm;
			cyApp = cySwingApp;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			try{
				builder.dssm.setDataSetProperties();
				NodeLayout layout = new NodeLayout();
				layout.generateLayoutForDataSets(builder.dssm.getSelectedDataSets());
				createFrame(true, layout);
				createFrame(false, layout);

			}
			catch(TooManyItemsException ex)
			{
				JOptionPane.showMessageDialog(builder, ex.getMessage());
			}
			catch(ContainerUnplaceableExcpetion ex)
			{
				JOptionPane.showMessageDialog(builder, ex.getMessage());
			}
			catch(DimensionMismatchException ex)
			{
				JOptionPane.showMessageDialog(builder, ex.getMessage());
			}

		}
		/**
		 * Create the Preview Frame
		 * @param legend - whether to generate it for a legend frame
		 * @param layout - Which layout to use
		 */
		private void createFrame(boolean legend, NodeLayout layout)
		{
			JDialog current = null;
			// Get/Reset the correct Dialog/Frame
			if(legend)
			{
				if(builder.PreviewLegendFrame != null)
				{
					builder.PreviewLegendFrame.dispose();
				}
				builder.PreviewLegendFrame = new JDialog();				
				current = builder.PreviewLegendFrame;
				current.setTitle("Legend Preview");
			}
			else
			{
				if(builder.PreviewFrame != null)
				{
					builder.PreviewFrame.dispose();					
				}
				builder.PreviewFrame = new JDialog();
				current = builder.PreviewFrame;
				current.setTitle("Node Preview");
			}


			//plot the node (either as legend or as normal node)
			current.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			current.getContentPane().setLayout(new GridBagLayout());
			current.getContentPane().setPreferredSize(new Dimension(METANODEPROPERTIES.IMAGEWIDTH,METANODEPROPERTIES.IMAGEHEIGHT + METANODEPROPERTIES.LABELHEIGHT));
			GridBagConstraints gbc1 = new GridBagConstraints();
			gbc1.weighty = 1;
			gbc1.weightx = 1;
			gbc1.fill = GridBagConstraints.BOTH;

			SVGDocument doc = LayoutUtils.createSVGDoc();
			SVGGraphics2D g = new SVGGraphics2D(doc);
			if(legend)
			{
				layout.layoutLegendNode(nm.getNode(builder.dssm.getSelectedDataSets().get(0).dataset.getNodeIDs().iterator().next()).getData(), g);
			}
			else
			{
				layout.layoutNode(nm.getNode(builder.dssm.getSelectedDataSets().get(0).dataset.getNodeIDs().iterator().next()).getData(), g);
			}
			LayoutUtils.TransferGRaphicsToDocument(doc, null, g);
			JSVGCanvas canvas = new JSVGCanvas();
			canvas.setSVGDocument(doc);
			current.getContentPane().add(canvas,gbc1);
			current.pack();
			if(legend)
			{
				//current.setLocationRelativeTo(cyApp.getJFrame());
				Point FramePos = cyApp.getJFrame().getLocation();				
				Point currentpos  = new Point(FramePos.x + cyApp.getJFrame().getWidth()/2-current.getWidth() - 5, FramePos.y + cyApp.getJFrame().getHeight()/2 - current.getHeight() / 2);
				current.setLocation(currentpos);
			}
			else
			{
				if(builder.PreviewLegendFrame != null)
				{
					Point LegendPos =builder.PreviewLegendFrame.getLocation();
					Point NodePos = new Point(LegendPos.x + builder.PreviewLegendFrame.getWidth()+10,LegendPos.y); 
					current.setLocation(NodePos);
				}
				else
				{
					current.setLocationRelativeTo(cyApp.getJFrame());	
				}
			}
			current.setVisible(true);	

		}
	}
	/**
	 * Listener, that listens to the currently selected Datasets to determine the currently available nodes and their overlap.
	 * @author Thomas Pfau
	 *
	 */
	private class SelectionListener implements TableModelListener
	{

		private DataSetControlPanel builder;
		/**
		 * Default constructor that uses the Controlpanel.
		 * @param builder
		 */
		public SelectionListener(DataSetControlPanel builder) {
			this.builder = builder; 
		}
		@Override
		public void tableChanged(TableModelEvent e) {
			// TODO Auto-generated method stub
			if(e.getColumn() == 2)
			{
				Vector<ColorMapDataSetBundle> datasets = dssm.getSelectedDataSets();
				HashSet<String> commonids = new HashSet<String>();
				HashSet<String> totalids = new HashSet<String>();
				boolean init = true;
				for(ColorMapDataSetBundle ds : datasets)
				{
					if(init){
						commonids.addAll(ds.dataset.getNodeIDs());
						init = false;
					}
					else
					{
						commonids.retainAll(ds.dataset.getNodeIDs());
					}
					totalids.addAll(ds.dataset.getNodeIDs());
				}
				builder.commons.setValue(Integer.toString(commonids.size()));
				builder.totals.setValue(Integer.toString(totalids.size()));
			}
		}

	}
	/**
	 * A Simple class combining, combining a Label and a Value. 
	 * @author Thomas Pfau
	 *
	 */
	private class IDValueLabel extends JPanel
	{
		private JLabel ID;
		private JLabel Value;
		/**
		 * Simple constructor with two Strings (for Value and Label)
		 * @param ID
		 * @param Value
		 */
		public IDValueLabel(String ID, String Value) {
			// TODO Auto-generated constructor stub
			this.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			this.Value = new JLabel(Value);
			this.ID = new JLabel(ID);
			gbc.weightx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			this.ID.setHorizontalAlignment(SwingConstants.LEFT);		
			gbc.gridx = 0;
			add(this.ID,gbc);
			this.Value.setHorizontalAlignment(SwingConstants.CENTER);
			gbc.gridx = 1;
			add(this.Value,gbc);

		}
		/**
		 * set the Value display alongside the label.
		 * @param value
		 */
		public void setValue(String value)
		{
			Value.setText(value);
		}		
	}
	
	/**
	 * Listener to react to a request to move certain Datasets up or down in the list (this can influence the layouting 
	 * when datasets of the same size are moved around.
	 * @author Thomas Pfau
	 *
	 */
	private class MoveListener implements ActionListener
	{

		private DataSetSelectionTable targetTable;
		private boolean up;
		/**
		 * Basic constructor with an indicator whether to move things up or down.
		 * @param up
		 * @param targetTable
		 */
		public MoveListener(boolean up, DataSetSelectionTable targetTable)		
		{
			this.up = up;
			this.targetTable = targetTable;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if(up)
			{
				targetTable.moveEntryUp();
			}
			else
			{
				targetTable.moveEntryDown();		
			}
		}

	}
	
	/**
	 * Listener that updates the availability of the move up or move down buttons. 
	 * @author Thomas Pfau
	 *
	 */
	private class RowSelectionListener implements ListSelectionListener
	{

		JTable targetTable;
		JButton moveUp;
		JButton moveDown;
		/**
		 * Standard constructor, which is provided the target Table, and the up and down buttons which are either enabled or disbled.
		 * @param target
		 * @param MoveUp
		 * @param MoveDown
		 */
		public RowSelectionListener(JTable target, JButton MoveUp, JButton MoveDown) {
			// TODO Auto-generated constructor stub
			targetTable = target;
			moveUp = MoveUp;
			moveDown = MoveDown;
		}
		@Override
		public void valueChanged(ListSelectionEvent e) {
			// TODO Auto-generated method stub
			int selectedRow = targetTable.getSelectedRow();
			if(selectedRow <= 0)
			{
				moveUp.setEnabled(false);
			}
			else
			{
				moveUp.setEnabled(true);
			}

			if(selectedRow >= targetTable.getRowCount() -1 | selectedRow < 0)
			{
				moveDown.setEnabled(false);
			}
			else
			{
				moveDown.setEnabled(true);
			}
		}

	}
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public String getTitle() {		
		return "IDARE";		
	}	

	@Override
	public Icon getIcon() {
		return null;
	}
	/**
	 * A Action that creates a GUI for Dataset addition. 
	 * @author Thomas Pfau
	 *
	 */
	private class DataSetAdderGuiAction extends AbstractAction
	{

		CySwingApplication app;
		DataSetManager dsm;
		FileUtil util;
		/**
		 * The Basic constructor needs a {@link FileUtil}, and the {@link DataSetManager} to allow loading of files and 
		 * addition of DataSets to the System. 
		 * The {@link CySwingApplication} is mainly to keep the component hierarchy in order.
		 * @param cyapp
		 * @param dsm
		 * @param util
		 */
		public DataSetAdderGuiAction(CySwingApplication cyapp, DataSetManager dsm, FileUtil util) {
			// TODO Auto-generated constructor stub
			this.app = cyapp;
			this.dsm = dsm;
			this.util = util;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub			
			DataSetAdderGUI addergui = new DataSetAdderGUI(app, dsm, util,dsatf);
		}

	}
	/**
	 * A Simple action that removes the currently selected DataSet from the available Datasets. 
	 * @author Thomas Pfau
	 *
	 */
	private class DataSetRemoveAction extends AbstractAction
	{

		DataSetManager dsm;
		JTable DataSetTable;
		/**
		 * BAsic Constructor, requiring the Table displaying the datasets for {@link DataSet} 
		 * selection and the {@link DataSetManager} to remove it. 
		 * @param table
		 * @param dsm
		 */
		public DataSetRemoveAction(JTable table, DataSetManager dsm) {
			// TODO Auto-generated constructor stub
			this.DataSetTable= table;
			this.dsm = dsm;			
		}
		@Override
		public void actionPerformed(ActionEvent e) {			
			// TODO Auto-generated method stub					
			int[] selectedrows = DataSetTable.getSelectedRows();
			Vector<DataSet> setsToRemove = new Vector<DataSet>();
			int DataSetColumn = 0;
			for(int j = 0 ; j < DataSetTable.getColumnCount(); j++)
			{
				if(DataSetTable.getColumnName(j).equals(DataSetSelectionModel.Column_Identifiers[0]))
				{
					DataSetColumn = j;
					break;
				}
			}
			for(int i : selectedrows)
			{
				setsToRemove.add((DataSet)DataSetTable.getValueAt(i, DataSetColumn));
			}
			for(DataSet ds : setsToRemove)
			{
				dsm.removeDataSet(ds);
			}
		}

	}
}
