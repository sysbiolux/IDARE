package idare.imagenode.internal.Layout.Manual.GUI;

import java.awt.BorderLayout;
import java.awt.Rectangle;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicDesktopPaneUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.svg.SVGDocument;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.Utilities.LayoutUtils;
import idare.imagenode.Utilities.GUI.JSVGGlassCanvas;
import idare.imagenode.Utilities.GUI.MouseDraggingListener;
import idare.imagenode.exceptions.layout.WrongDatasetTypeException;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;


public class DataSetFrame extends JInternalFrame
{
	static int openFrameCount = 0;    
	public DataSetLayoutInfoBundle bundle;    
	ContainerLayout layout;
	Rectangle origboundingbox; 
	Rectangle origdesktopsize; 
	DataContainer cont;
	JComponent parent;
	String LayoutID;
	public boolean autoresize = false;

	public DataSetFrame(DataSetLayoutInfoBundle bundle, String LayoutID, JComponent parent) throws WrongDatasetTypeException{
		super("Document #" + (++openFrameCount), 
				true, //resizable
				true, //closable
				true, //maximizable
				true);//iconifiable
		this.setUI(new BasicInternalFrameUI(this));
		((javax.swing.plaf.basic.BasicInternalFrameUI)this.getUI()).setNorthPane(null);
		
		this.parent = parent;
		this.LayoutID = LayoutID;
		this.bundle = bundle;
		//Set the window's location.
		//setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
		cont = bundle.dataset.getLayoutContainer(bundle.properties);
		layout = cont.createEmptyLayout();
		//select a non empty node
		JPanel ContentPane = new JPanel();
		ContentPane.setOpaque(false);
		getRootPane().setOpaque(false);
		this.setContentPane(ContentPane);		
		this.getContentPane().setLayout(new BorderLayout());
		origdesktopsize = parent.getBounds();		
	}

	public void setDefaultSize(double scalingfactor) throws WrongDatasetTypeException
	{
		PrintFDebugger.Debugging(this, "Getting minimal size of container");		
		Rectangle minsize = cont.getMinimalSize();
//		PrintFDebugger.Debugging(this, "Setting size to " + minsize.width*20 + "/" +minsize.height*20);
		PrintFDebugger.Debugging(this, "Setting the size to" + (int)(minsize.width*20*scalingfactor) + "/" + (int)(minsize.height*20*scalingfactor));
		setSize((int)(minsize.width*20*scalingfactor),(int)(minsize.height*20*scalingfactor));	
	}

	public void layoutData()
	{
		//remove everything that was on the content pane.
		this.getContentPane().removeAll();
		//create an SVG document.
		SVGDocument doc = LayoutUtils.createSVGDoc();
		SVGGraphics2D g = new SVGGraphics2D(doc);
		//layout the data in the SVGGraphics Context of this document.		
		layout.LayoutDataForNode(cont.getDataSet().getDataForID(LayoutID), g, false, bundle.colormap);
		LayoutUtils.TransferGraphicsToDocument(doc, this.getContentPane().getSize(), g);
		JSVGCanvas canvas = new JSVGGlassCanvas(this);
		canvas.setSVGDocument(doc);		
		this.getContentPane().add(canvas,BorderLayout.CENTER);
		canvas.revalidate();
		//We have to check whether we have to pack or not..
		//this.pack();
	}

	public void updateLayoutToCurrentPosition()
	{
		if(layout != null)
		{
			try{
//				PrintFDebugger.Debugging(this, "Creating layout in area" + this.getContentPane().getBounds() + " while Frame has area " + getBounds());
				layout.createLayout(cont.getDataSet().getDataForID(LayoutID), this.getContentPane().getBounds(), bundle.Label, bundle.properties);
			}
			catch(Exception e)
			{
				//This should not happen. An existing Layout should always be ok for a new position.
				e.printStackTrace(System.out);
			}
		}
		else
		{
			try{
				layout = bundle.dataset.getLayoutContainer(bundle.properties).createEmptyLayout();
//				PrintFDebugger.Debugging(this, "Creating layout in area" + this.getContentPane().getBounds() + " while Frame has area " + getBounds());

				layout.createLayout(cont.getDataSet().getDataForID(LayoutID), this.getContentPane().getBounds(), bundle.Label, bundle.properties);
			}
			catch(Exception e)
			{
				//This shoudl not happen, as the dataset should only be added with a correct LayoutType.
				e.printStackTrace(System.out);
			}
		}
		layoutData();
	}
	public void updateVisualisationType(DataSetLayoutProperties props) throws WrongDatasetTypeException
	{		
		layout = bundle.dataset.getLayoutContainer(props).createEmptyLayout();
		bundle.properties = props;
		updateLayoutToCurrentPosition();
		layoutData();
	}

	public void updateVisualisationColors(ColorMap map)
	{
		bundle.colormap = map;
		layoutData();
	}

	public void updateNode(String Node)
	{
		LayoutID = Node;		
		layoutData();
	}

	public void updateDataSetLabel(String label)
	{
		bundle.Label = label;
		layout.updateLabel(label);
		layoutData();
	}

	@Override
	public void setLocation(int xpos,int ypos)
	{
		super.setLocation(xpos, ypos);
		if(!autoresize)
		{
			origboundingbox = this.getBounds();
			origdesktopsize = parent.getBounds();
			//PrintFDebugger.Debugging(this,"Manual or System change of the bounds to " + origboundingbox);	    		
		}    	
	}
	@Override
	public void setSize(int width, int height)
	{    	
		Rectangle currentbounds = getBounds();
		super.setSize(width, height);
		if(!autoresize)
		{
			origboundingbox = this.getBounds();
			origdesktopsize = parent.getBounds();
			//PrintFDebugger.Debugging(this,"Manual or System change of the bounds to " + origboundingbox);
		}
		if(currentbounds.width != width || currentbounds.height != height)
		{
			updateLayoutToCurrentPosition();
		}
	}
	@Override
	public void setBounds(int x, int y, int w, int h)
	{
		Rectangle currentbounds = getBounds();
		super.setBounds(x,y,w,h);
		if(!autoresize)
		{
			origboundingbox = getBounds();
			origdesktopsize = parent.getBounds();
//			PrintFDebugger.Debugging(this,"Manual or System change of the bounds to " + origboundingbox);
			//PrintFDebugger.Trace(this);

		}
		if(currentbounds.width != w || currentbounds.height != h)
		{
			updateLayoutToCurrentPosition();
		}
	}

	public void updatePosition()
	{
		PrintFDebugger.Debugging(this, "Getting parental bounds");
		Rectangle parentalBounds = parent.getBounds();    	
		double scalingfactor = Math.min(parentalBounds.getWidth()/origdesktopsize.getWidth(),parentalBounds.getHeight()/origdesktopsize.getHeight());
		PrintFDebugger.Debugging(this, "Parentalbounds are " + parentalBounds + " with scaling factor" + scalingfactor);
		PrintFDebugger.Debugging(this, "Setting bounds");
		setBounds((int)(scalingfactor*origboundingbox.x),(int)(scalingfactor*origboundingbox.y),(int)(scalingfactor*origboundingbox.width),(int)(scalingfactor*origboundingbox.height));
		PrintFDebugger.Debugging(this, "Updating Layout to current position");
		updateLayoutToCurrentPosition();
	}
	
	public Rectangle getScaledBounds()
	{
		PrintFDebugger.Debugging(this, "Getting Scaled bounds");
		Rectangle current = getBounds();		
		Rectangle parentBounds = parent.getBounds();
		PrintFDebugger.Debugging(this, "Frame bounds are " + current + " while parent bounds are " + parentBounds);
		double scalingfactor = IMAGENODEPROPERTIES.IMAGEHEIGHT/parentBounds.getHeight();		
		Rectangle ScaledBounds = new Rectangle((int)(current.getX()*scalingfactor),(int)(current.getY()*scalingfactor),(int)(current.getWidth()*scalingfactor),(int)(current.getHeight()*scalingfactor));
		PrintFDebugger.Debugging(this, "Scaled bounds are " + ScaledBounds );
		return ScaledBounds;
	}

}

