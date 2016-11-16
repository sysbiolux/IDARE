package idare.imagenode.internal.Layout.Manual.GUI;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.exceptions.layout.WrongDatasetTypeException;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.GUI.Legend.IDARELegend;
import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;
import idare.imagenode.internal.Layout.Manual.LayoutGUI;
import idare.imagenode.internal.Layout.Manual.ManualLayout;

public class ManualLayoutUpdater extends ComponentAdapter implements InternalFrameListener{

	private ManualLayout layout;	
	private Vector<DataSetFrame> presentframes = new Vector<>();
	private IDARELegend legend;
	private LayoutGUI gui;
	private String selectedNode;	
	private NodeManager manager;
	
	public ManualLayoutUpdater(ManualLayout layout, IDARELegend legend, LayoutGUI gui, NodeManager nm) {
		this.layout = layout;
		this.legend = legend;
		this.gui = gui;
		this.manager = nm;
	}
	
	/**
	 * Inform the layout that properties have changed. This can be either the Properties or the colors.
	 * @param bundle
	 * @throws WrongDatasetTypeException
	 */
	public void updateProperties(DataSetLayoutInfoBundle bundle) throws WrongDatasetTypeException
	{
		layout.updateProperties(bundle);
	}
	
	public void addFrame(DataSetFrame frame) throws WrongDatasetTypeException
	{
		layout.addDataSet(frame.bundle, frame.getScaledBounds());
		presentframes.add(frame);		
		frame.addComponentListener(this);
		frame.addInternalFrameListener(this);
		updateLegend();
	}
	
	private void removeFrame(DataSetFrame frame)
	{
		//just in case we get this twice.
		if(presentframes.contains(frame))
		{
			layout.removeDataSet(frame.bundle);
			presentframes.remove(frame);		
			updateLegend();
		}
	}
	
	@Override
	public void componentResized(ComponentEvent e)
	{
		if(e.getComponent() instanceof DataSetFrame)
		{
			DataSetFrame frame = (DataSetFrame)e.getComponent();
			updateLayout(frame);
			updateLegend();
		}
	}
	
	@Override
	public void componentMoved(ComponentEvent e)
	{
		if(e.getComponent() instanceof DataSetFrame)
		{
			DataSetFrame frame = (DataSetFrame)e.getComponent();
			updateLayout(frame);
			updateLegend();
		}
	}
	
	private void updateLayout(DataSetFrame frame)
	{
		//if the frame is currently autoresized, we don't do anything.
		//also if we don't listen to it, we don't do anything.
		if(!frame.autoresize && presentframes.contains(frame))
		{
			try{
				layout.updatePosition(frame.bundle, frame.getScaledBounds());
			}
			catch(WrongDatasetTypeException ex)
			{
				ex.printStackTrace(System.out);
			}
		}
	}

	public void updateColors(ColorMap colors)
	{
		DataSetFrame current = gui.getSelectedFrame();
		if(current != null)
		{							
			//this call also updates the appropriate bundle.
			current.updateVisualisationColors(colors);
			//thus we can simply update the layout accordingly.
			try{
				layout.updateProperties(current.bundle);
				updateLegend();
			}
			catch(WrongDatasetTypeException ex)
			{
				//this should never happen
				ex.printStackTrace(System.out);
			}
		}
	}
	
	public void updateNode(String Node)
	{
		selectedNode = Node;
		for(DataSetFrame frame : presentframes)
		{
			frame.updateNode(Node);
		}	
		gui.updateID(selectedNode);
		updateLegend();
	}
	
	private void updateLegend()
	{
		PrintFDebugger.Debugging(this, "Trying to update the legend for Node " + selectedNode + " with layout " + layout + " and data " + manager.getNode(selectedNode));
		legend.setLegendData(layout,manager.getNode(selectedNode));
	}
	
	public void updateProperties(DataSetLayoutProperties props)
	{
		DataSetFrame current = gui.getSelectedFrame();
		try{
			if(current != null)
			{							
				//this call also updates the appropriate bundle.
				current.updateVisualisationType(props);
				//thus we can simply update the layout accordingly.
				layout.updateProperties(current.bundle);
				updateLegend();
			}
		}
		catch(WrongDatasetTypeException e)
		{
			JOptionPane.showMessageDialog(gui, "Could not use the type of layout for this dataset","Invalid layout type",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public String getNodeID()
	{
		return selectedNode;
	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		PrintFDebugger.Debugging(this, "A Frame is closing");
		
		if(e.getInternalFrame() instanceof DataSetFrame)
		{
			PrintFDebugger.Debugging(this, "Removing the frame");
			removeFrame((DataSetFrame) e.getInternalFrame());
		}
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		if(e.getInternalFrame() instanceof DataSetFrame)
		{
			PrintFDebugger.Debugging(this, "Moving a frame to the front");
			layout.moveBundleToFront(((DataSetFrame)e.getInternalFrame()).bundle);
			updateLegend();
		}
	}

	
	//UNIMPLEMENTED FUNCTIONS FROM THE INTERNALFRAMELISTENER INTERFACE WHICH ARE NOT NECESSARY
	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		PrintFDebugger.Debugging(this, "A Frame closed");
		if(e.getInternalFrame() instanceof DataSetFrame)
		{
			PrintFDebugger.Debugging(this, "Removing the frame");
			removeFrame((DataSetFrame) e.getInternalFrame());
		}
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
	public void internalFrameDeactivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}
}
