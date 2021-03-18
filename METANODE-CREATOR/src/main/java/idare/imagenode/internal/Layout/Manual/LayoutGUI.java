package idare.imagenode.internal.Layout.Manual;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicDesktopPaneUI;

import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.Utilities.GUI.MouseDraggingListener;
import idare.imagenode.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.imagenode.exceptions.layout.DimensionMismatchException;
import idare.imagenode.exceptions.layout.TooManyItemsException;
import idare.imagenode.exceptions.layout.WrongDatasetTypeException;
import idare.imagenode.internal.IDAREImageNodeApp;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.GUI.DataSetController.CreateNodesTaskFactory;
import idare.imagenode.internal.GUI.DataSetController.DataSetSelectionModel;
import idare.imagenode.internal.GUI.Legend.IDARELegend;
import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;
import idare.imagenode.internal.Layout.Automatic.AutomaticNodeLayout;
import idare.imagenode.internal.Layout.Manual.GUI.DataSetFrame;
import idare.imagenode.internal.Layout.Manual.GUI.DataSetLayoutToolBar;
import idare.imagenode.internal.Layout.Manual.GUI.DatasetMenu;
import idare.imagenode.internal.Layout.Manual.GUI.IDPanel;
import idare.imagenode.internal.Layout.Manual.GUI.ManualLayoutUpdater;
import idare.imagenode.internal.Layout.Manual.GUI.ManualNodeLayoutManager;

public class LayoutGUI	extends JFrame
implements ActionListener, InternalFrameListener{
	JDesktopPane desktop;		
	DataSetManager dsm;	
	IDAREImageNodeApp app;
	ManualLayout layout;	
	FrameResizer resizer;
	DataSetLayoutToolBar toolbar;
	IDPanel IdentifierPanel;	
	ManualLayoutUpdater updater;
	MouseDraggingListener<DataSetFrame> draglistener;
//	MouseResizerListener<DataSetFrame> resizeMouseListener;
	CreateNodesTaskFactory nodeFactory;
	public LayoutGUI(DataSetManager dsm, IDAREImageNodeApp app, CreateNodesTaskFactory nodeFactory) {		
		super("Manual Layout Generation");		
		layout = new ManualLayout();
		this.nodeFactory = nodeFactory;
		this.app = app;
		this.dsm = app.getDatasetManager();
		//Create the legend.		
		IDARELegend legend = new IDARELegend(new JPanel(), app.getNodeManager());
		//Create the updater
		updater = new ManualLayoutUpdater(layout, legend, this, app.getNodeManager());

		//Make the big window be indented 50 pixels from each edge
		//of the screen.
		int inset = 50;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();		
		
		//Set up the GUI.
		BasicDesktopPaneUI ui = new BasicDesktopPaneUI();
		
		//Set up the Task Bar
		Vector<String> nodeswithData = new Vector<String>(app.getNodeManager().getNodesWithData());
		toolbar = new DataSetLayoutToolBar(updater, nodeswithData);
		//selectedNodeID = nodeswithData.firstElement();
		
		//create the Menu bar
		setJMenuBar(createMenuBar());
		
		//Set up the content Pane
		JPanel contentpan = new JPanel(new BorderLayout());
		
		// set up the Left Panel
		JPanel LeftPanel = new JPanel();

		
		//set up the desktopPane.
		desktop = new JDesktopPane(); //a specialized layered pane
		desktop.setUI(ui);
		desktop.setBackground(Color.WHITE);
		desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		
		//Set p the MouseDraglistener
		draglistener = new MouseDraggingListener<DataSetFrame>(desktop, DataSetFrame.class);
		//resizeMouseListener = new MouseResizerListener<DataSetFrame>(desktop, DataSetFrame.class);
		//desktop.addMouseMotionListener(draglistener);
		//desktop.addMouseListener(draglistener);
		//set up the IDentifierPanel
		IdentifierPanel = new IDPanel();
		//IdentifierPanel.setID(selectedNodeID);
		//Add Visualisation to the left panel
		
		LeftPanel.add(desktop);
		LeftPanel.add(IdentifierPanel);
		//Set minimal size and initialize resizer
		LeftPanel.setMinimumSize(new Dimension(400,290));		
		LeftPanel.setLayout(new ManualNodeLayoutManager(desktop,IdentifierPanel, new Dimension(IMAGENODEPROPERTIES.IMAGEWIDTH,IMAGENODEPROPERTIES.IMAGEHEIGHT),new Dimension(IMAGENODEPROPERTIES.IMAGEWIDTH,IMAGENODEPROPERTIES.LABELHEIGHT)));
		
		//Set up the Right Panel
		JPanel RightPanel = new JPanel();
		RightPanel.setMinimumSize(new Dimension(200,100));
		RightPanel.setLayout(new BoxLayout(RightPanel, BoxLayout.PAGE_AXIS));
		
		//Set the properties of the legend.		
		//Add the legend to the Right Panel
		RightPanel.add(legend);		
		
		//Set up the Split Pane.
		JSplitPane content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, LeftPanel,RightPanel);		
		//Set up the Accept Button
		JButton Accept = new JButton("Create Layout");
		Accept.addActionListener(new ManualLayoutGenerator(this));
		
		//Add items to the content pane.
		contentpan.add(toolbar,BorderLayout.PAGE_START);	
		contentpan.add(content,BorderLayout.CENTER);
		contentpan.add(Accept,BorderLayout.PAGE_END	);
				
		//and set the content pane of this frame.
		setContentPane(contentpan);
		//and do an initial layout of this GUI.
		setBounds(inset, inset,
				screenSize.width  - inset*2,
				screenSize.height - inset*2);
//		PrintFDebugger.Debugging(this, "The size of the frame is " + getSize() + " while the size of the contentpane is " + contentpan.getSize());
		// and update the position of the splitpane divider		
		setVisible(true);
		//I don't get why the sizes are not assigned before the Frame is made visible...
		content.setDividerLocation(LeftPanel.getLayout().preferredLayoutSize(content).getWidth()/ (double)content.getWidth());
		//init the updater.
		updater.updateNode(nodeswithData.firstElement());
	}

	
	/**
	 * Create the selected Frames
	 * @param dssm the datasetModel to create frames for.
	 */
	public void createSelectedFrames(DataSetSelectionModel dssm)
	{
				
		AutomaticNodeLayout autolayout = new AutomaticNodeLayout();
		try{
			autolayout.generateLayoutForDataSets(dssm.getSelectedDataSets());			
		}
		catch(WrongDatasetTypeException | ContainerUnplaceableExcpetion | DimensionMismatchException | TooManyItemsException e)
		{
			autolayout = null;
		}
		for(DataSetLayoutInfoBundle bundle: dssm.getSelectedDataSets())
		{
			if(updater.getNodeID() == null)
			{
				updater.updateNode(bundle.dataset.getNodeIDs().iterator().next());
			}	
			try{
				DataSetFrame current = createFrame(bundle);
				if(autolayout != null)
				{
					Rectangle targetarea = autolayout.getLayoutContainerUsedFor(bundle.dataset).getLayoutArea();
					//scale the targetarea to the current desktop;
					double xscaling = desktop.getWidth()/((double)IMAGENODEPROPERTIES.IMAGEWIDTH);
					double yscaling = desktop.getHeight()/((double)IMAGENODEPROPERTIES.IMAGEHEIGHT);
					current.setBounds((int)(targetarea.getX()*xscaling),(int)(targetarea.getY()*yscaling),(int)(targetarea.getWidth()*xscaling),(int)(targetarea.getHeight()*yscaling));
				}
				
			}
			catch(WrongDatasetTypeException e)
			{
				try{
					bundle.properties = bundle.dataset.getPropertyOptions().firstElement();
					createFrame(bundle);
				}
				catch(WrongDatasetTypeException ex)
				{
					//nothing we can do...
					continue;
				}
			}
		}
	}
	protected JMenuBar createMenuBar() {		
		JMenuBar menuBar = new JMenuBar();		
		//Set up the lone menu.
		JMenu addmenu = new JMenu("Add Dataset");		
		addmenu.setMnemonic(KeyEvent.VK_A);
		menuBar.add(addmenu);
		
		for(DataSet set: dsm.getDataSets())
		{			
			JMenu currentMenu = new DatasetMenu(set,this);
			addmenu.add(currentMenu);					
		}
		
		JMenu removeMenu = new JMenu("Remove Dataset");
		
		//Set up the first menu item.
		JMenuItem removeCurrent = new JMenuItem("Remove Selected");
		
		//Set up the second menu item.
		removeCurrent.setMnemonic(KeyEvent.VK_DELETE);
		removeCurrent.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_DELETE, ActionEvent.ALT_MASK));
		removeCurrent.setActionCommand("REMOVECURRENT");
		removeCurrent.addActionListener(this);
		removeMenu.add(removeCurrent);
		JMenuItem removeAll = new JMenuItem("Remove all");
				
		removeAll.setActionCommand("REMOVEALL");
		removeAll.addActionListener(this);
		removeMenu.add(removeAll);
		
		menuBar.add(removeMenu);
		return menuBar;
	}

	
	
	//React to menu selections.
	public void actionPerformed(ActionEvent e) {
		if ("REMOVEALL".equals(e.getActionCommand())) { //new
			for(JInternalFrame frame : desktop.getAllFrames())
			{
				frame.dispose();
				resizer.internalframes.remove(frame);
			}
		} 
		if("REMOVECURRENT".equals(e.getActionCommand())) { //new
			desktop.getSelectedFrame().dispose();
		}
	}

	/**
	 * create A Frame for the given Bundle
	 * @param bundle the bundle to create a frame for
	 * @return the created DatasetFrame
	 * @throws WrongDatasetTypeException if an invalid dataset was supplied
	 */
	public DataSetFrame createFrame(DataSetLayoutInfoBundle bundle) throws WrongDatasetTypeException{
		
		//Give the bundle a temporary label: X ( which should fit most letters
		bundle.Label = "X";
		//This is an ugly hack to get around the LF problems caused by Cytoscape, as its otherwise a pain to get proper borders on the internal frames.
		LookAndFeel cytoscapeLF = UIManager.getLookAndFeel();
		DataSetFrame frame;
		 try {
		        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		        frame = new DataSetFrame(bundle,updater.getNodeID(),desktop);
		        UIManager.setLookAndFeel(cytoscapeLF);
		    } 
		 catch (IllegalAccessException | UnsupportedLookAndFeelException | InstantiationException | ClassNotFoundException e) {
			 frame = new DataSetFrame(bundle,updater.getNodeID(),desktop);		      
		 }
	
//		PrintFDebugger.Debugging(desktop, "The Desktop is located at" + desktop.getBounds());			
		desktop.add(frame);		
		frame.setLocation(0, 0);
		frame.setOpaque(false);
		frame.setVisible(true);		
		try{
			frame.addInternalFrameListener(this);
			try {
				
				frame.setSelected(true);			
			} 
			catch (java.beans.PropertyVetoException e) {}
			Dimension desktopdim = desktop.getSize();
			double scalingfactor = Math.min(desktopdim.getHeight()/IMAGENODEPROPERTIES.IMAGEHEIGHT, desktopdim.getWidth()/IMAGENODEPROPERTIES.IMAGEWIDTH);
//			PrintFDebugger.Debugging(this, "The scaling factor is: " + scalingfactor);
			frame.setDefaultSize(scalingfactor);
			frame.setVisible(true);					
//			PrintFDebugger.Debugging(this, "Updating Frame position");
			frame.updatePosition();
//			PrintFDebugger.Debugging(this, "Updating Frame layoutData");
			frame.layoutData();						
			//when the frame is created, add the frame to the layout
			//and update layout and legend
//			PrintFDebugger.Debugging(this, "Updating Layout with frame");
//			PrintFDebugger.Debugging(this, "Revalidating Frame");
			frame.revalidate();
			updater.addFrame(frame);
//			PrintFDebugger.Debugging(this, "Adding Frame to internal resizer");
			resizer.internalframes.add(frame);
			toolbar.updateDataSetOptions(frame.bundle);
			frame.addMouseMotionListener(draglistener);
			frame.addMouseListener(draglistener);
			//frame.addMouseListener(resizeMouseListener);
			//frame.addMouseMotionListener(resizeMouseListener);
		}
		catch(WrongDatasetTypeException ex)
		{			
			desktop.remove(frame);
			frame.dispose();
			JOptionPane.showMessageDialog(this, "Could not use the type of layout for this dataset","Invalid layout type",JOptionPane.ERROR_MESSAGE);
			desktop.revalidate();
		}		

		return frame;
	}
	
	/**
	 * Build the gui
	 * @param dssm THe DatasetSelectionModel to use
	 * @param dsm The {@link DataSetManager} to get Datasets
	 * @param app The {@link IDAREImageNodeApp} to obtain information
	 * @param nodeFactory the {@link CreateNodesTaskFactory} to activate if layout is requested
	 */
    public static void createAndShowGUI(DataSetSelectionModel dssm, DataSetManager dsm, IDAREImageNodeApp app, CreateNodesTaskFactory nodeFactory) {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
 
        //Create and set up the window.
        LayoutGUI frame = new LayoutGUI(dsm, app, nodeFactory);         
        frame.buildResizer();
        if(dssm.getSelectedDataSets().size() > 0)
        {
        	frame.createSelectedFrames(dssm);
        }
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	        
        //Display the window.
        frame.setVisible(true);
    }


	
	public DataSetFrame getSelectedFrame()
	{
		return (DataSetFrame)desktop.getSelectedFrame();
	}

	
	public void updateID(String nodeID)
	{
		IdentifierPanel.setID(nodeID);
				
	}
			
	public void buildResizer()
    {
        resizer = new FrameResizer(desktop.getSize());
        desktop.addComponentListener(resizer);
    }
			
	
    private class FrameResizer extends ComponentAdapter
    {
    	private Vector<DataSetFrame> internalframes;
    	private Dimension currentsize;
    	public FrameResizer(Dimension origsize)
    	{
    		currentsize = origsize;
    		System.out.println(origsize);
    		internalframes = new Vector<>();
    	}
    	@Override
    	public void componentResized(ComponentEvent e)
    	{
    		
    		Dimension dim = e.getComponent().getSize();
    		System.out.println("New Size of the desktop is " + dim);
    		double scalingfactor = Math.min(dim.getHeight()/currentsize.getHeight(), dim.getWidth()/currentsize.getWidth());
    		for(DataSetFrame frame: internalframes)
    		{
    			frame.autoresize = true;
    			//Rectangle origlocation = frame.getBounds();
    			//Rectangle newlocation = new Rectangle((int)(origlocation.getX()*scalingfactor),(int)(origlocation.getY()*scalingfactor),(int)(origlocation.getWidth()*scalingfactor),(int)(origlocation.getHeight()*scalingfactor));
    			frame.updatePosition();
    			frame.autoresize = false;    			
    		}
    		currentsize = dim;
    	}
    }
    
    private class ManualLayoutGenerator implements ActionListener
    {
    	
    	LayoutGUI source;
    	
    	public ManualLayoutGenerator(LayoutGUI source) {
			// TODO Auto-generated constructor stub
    		this.source = source;
		}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
//				PrintFDebugger.Debugging(this, "Creating new Layout");
				if(!source.layout.data.isEmpty())
				{
//					PrintFDebugger.Debugging(this, "Data not empty");
					Set<DataSet> dataSetsUsed = new HashSet<>();
					for(DataSetLayoutInfoBundle bundle : source.layout.data)
					{
						dataSetsUsed.add(bundle.dataset);
					}
					ManualNodeLayout finallayout = layout.getFinalLayout();
//					PrintFDebugger.Debugging(this, "Calling generation");
					nodeFactory.run(dataSetsUsed, finallayout);
//					PrintFDebugger.Debugging(this, "Disposing GUI");
					source.dispose();
				}
			}
		
    }
    
//    private class NodeResizer extends ComponentAdapter
//    {
//    	private JComponent Node;
//    	private JComponent ID;
//    	private Dimension nodeDimension;
//    	private Dimension IDDimension;
//    	public NodeResizer(JComponent Node, JComponent ID, Dimension NodeDimension, Dimension IDDimension)
//    	{
//    		this.Node = Node;
//    		this.ID = ID;
//    		this.nodeDimension = NodeDimension;
//    		this.IDDimension = IDDimension;
//    	}
//    	
//    	@Override
//    	public void componentResized(ComponentEvent e)
//    	{
//    		       
//    		
//    		Dimension dim = e.getComponent().getSize();
//    		
//    		System.out.println("New enclosing size is" + dim);
//    		double scalingfactor = Math.min(dim.getHeight()/(nodeDimension.getHeight() + IDDimension.getHeight()), dim.getWidth()/IDDimension.getWidth());
//    		
//    		Rectangle nodeBounds = new Rectangle(0,0,(int)(scalingfactor*nodeDimension.getWidth()),(int)(scalingfactor * nodeDimension.getHeight()));
//    		Rectangle idBounds = new Rectangle(0,(int)(scalingfactor * nodeDimension.getHeight()),(int)(scalingfactor*IDDimension.getWidth()),(int)(scalingfactor * IDDimension.getHeight()));
////    		PrintFDebugger.Debugging(this, "Setting the bounds of the desktop to: " + nodeBounds + " and the ID dimension to " + idBounds);
//    		Node.setBounds(nodeBounds);
//    		ID.setBounds(idBounds);
//    		//Node.revalidate();
//    		//ID.revalidate();
//    	}
//    }



	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() instanceof DataSetFrame)
		{
			toolbar.updateDataSetOptions(null);
		}
	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() instanceof DataSetFrame)
		{			
			toolbar.updateDataSetOptions(((DataSetFrame)e.getSource()).bundle);
		}
	}


	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}

