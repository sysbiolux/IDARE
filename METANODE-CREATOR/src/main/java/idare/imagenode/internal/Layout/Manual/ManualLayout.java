package idare.imagenode.internal.Layout.Manual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

import org.apache.batik.svggen.SVGGraphics2D;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.Properties.LabelGenerator;
import idare.imagenode.Utilities.LayoutUtils;
import idare.imagenode.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.imagenode.exceptions.layout.DimensionMismatchException;
import idare.imagenode.exceptions.layout.TooManyItemsException;
import idare.imagenode.exceptions.layout.WrongDatasetTypeException;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.DataManagement.Events.DataSetChangedEvent;
import idare.imagenode.internal.DataManagement.Events.DataSetsChangedEvent;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;
import idare.imagenode.internal.Layout.DataSetLink;
import idare.imagenode.internal.Layout.ImageNodeLayout;
import idare.imagenode.internal.Layout.Manual.GUI.DataSetFrame;
import idare.imagenode.internal.Layout.Manual.Utilities.LayoutArea;
import idare.imagenode.internal.Layout.Manual.Utilities.LayoutComparator;

public class ManualLayout implements ImageNodeLayout {

	Font IDFont = new Font(Font.MONOSPACED,Font.BOLD,IMAGENODEPROPERTIES.LABELHEIGHT-2);

	LinkedList<DataSetLayoutInfoBundle> data = new LinkedList<>();
	Rectangle displaySize;
	HashMap<DataSet,Vector<DataSetLayoutInfoBundle>> visualisations = new HashMap<>();
	HashMap<DataSetLayoutInfoBundle, LayoutComparator> comparators = new HashMap<>();
	LinkedList<LayoutComparator> order = new LinkedList<>();
	boolean labelsOK = true;

	public ManualLayout( ) {
		// TODO Auto-generated constructor stub
	}
	/**
	 * Update the position of a Dataset in the layoutbundle.
	 * The position is assumed to be in the IMAGENODEPROPERTIES.IMAGEWIDTH / IMAGENODEPROPERTIES.IMAGEHEIGHT range.  
	 * @param bundle
	 * @param Position
	 */
	public void updatePosition(DataSetLayoutInfoBundle bundle, Rectangle Position) throws WrongDatasetTypeException
	{
//		PrintFDebugger.Debugging(this, "Updating Bundle Position to " + Position);
		//Lets bring the current rectangle into a position appropriate for an image node.		
		updateOrder(bundle, new LayoutArea(Position));
		//at the same time the Label is assigned (as the position changed this might also have changed)
		updateBundleProperties(bundle);
		

	}

	/**
	 * Update the bundle properties and the corresponding comparator with a new ContainerLayout.
	 * @param bundle
	 * @throws WrongDatasetTypeException
	 */
	private void updateBundleProperties(DataSetLayoutInfoBundle bundle) throws WrongDatasetTypeException
	{
//		PrintFDebugger.Debugging(this, "Obtaining container");
		DataContainer cont = bundle.dataset.getLayoutContainer(bundle.properties);
//		PrintFDebugger.Debugging(this, "creating empty layout");
		ContainerLayout clayout = cont.createEmptyLayout();
//		PrintFDebugger.Debugging(this, "retrieving comparator");
		LayoutComparator comp = comparators.get(bundle);
//		PrintFDebugger.Debugging(this, "creating layout");
//		PrintFDebugger.Debugging(this, "The Area the layout is created in is " + comp.area.area);
		clayout.createLayout(cont.getData(), comp.area, bundle.Label,bundle.properties);
//		PrintFDebugger.Debugging(this, "setting layout");
		comp.bundlelayout = clayout;		
	}

	/**
	 * 
	 * @param bundle
	 * @throws WrongDatasetTypeException
	 */
	public void updateProperties(DataSetLayoutInfoBundle bundle) throws WrongDatasetTypeException
	{
		updateBundleProperties(bundle);		
	}


	/**
	 * Add a dataset to this Layout. The set is represented by the appropriate bundle.
	 * It also has a position at which it is added, which will be used to assign its position in the order of datasets.
	 * @param bundle
	 * @param Position
	 * @throws WrongDatasetTypeException
	 */	
	public void addDataSet(DataSetLayoutInfoBundle bundle, Rectangle Position) throws WrongDatasetTypeException
	{				
		data.add(bundle);		
		if(!visualisations.containsKey(bundle.dataset))
		{
			visualisations.put(bundle.dataset, new Vector<>());
		}
		visualisations.get(bundle.dataset).add(bundle);
		LayoutArea pos = new LayoutArea(Position);
		LayoutComparator comp = new LayoutComparator(pos, bundle);		
		comparators.put(bundle, comp);
		//insert it in the order and update the labels accordingly.		
		updateBundleProperties(bundle);
		updateOrder(bundle, pos);
	}

	
	/**
	 * Update the order of the Layouts (to provide the correct labeling.
	 * @param current
	 * @param position
	 */
	private void updateOrder(DataSetLayoutInfoBundle current, LayoutArea position)
	{
		boolean inserted = false;
//		PrintFDebugger.Debugging(this, "Updating the position of " + current + " which has label " + current.Label);
		LayoutComparator newelement = new LayoutComparator(position, current);
		ListIterator<LayoutComparator> iter = order.listIterator();
//		PrintFDebugger.Debugging(this, "Updating Order");
		while(iter.hasNext())
		{
			LayoutComparator comp = iter.next();
			if(!inserted)
			{
				if(comp.compareTo(newelement) > 0)
				{
					if(comp.equals(newelement))
					{
						//PrintFDebugger.Debugging(this, "Removing the old layout position");
						iter.remove();
						labelsOK = false;
					}
				}				
				else
				{
					if(comp.equals(newelement))
					{
						//PrintFDebugger.Debugging(this, "Replacing the LayoutArea" + comp.area.area + " with " + newelement.area.area);
						comp.area = newelement.area;
						//PrintFDebugger.Debugging(this, "The new LayoutArea is " + comp.area.area );
						inserted = true;
						break;
					}
					else
					{
						iter.previous();
						//PrintFDebugger.Debugging(this, "Adding the new layout");
						comparators.put(current, newelement);
						iter.add(newelement);
						labelsOK = false;
						inserted = true;
					}
				}
			}
			if(inserted)
			{
				if(comp.equals(newelement))
				{
					iter.remove();
					labelsOK = false;
					break;
				}
			}
		}
		if(!inserted)
		{
			iter.add(newelement);
			comparators.put(current, newelement);
			labelsOK = false;
		}
		//create the new layout of the 
		try{
			updateBundleProperties(current);
		}
		catch(WrongDatasetTypeException e)
		{
			//this should never occur
			e.printStackTrace(System.out);
		}
//		PrintFDebugger.Debugging(this, "New Order:");
		for(LayoutComparator comp : order)
		{
//			PrintFDebugger.Debugging(this, comp.bundle.Label + " : " + comp.bundle + " : " + comp.area );	
		}
		//lets update the labels if necessary.
		updateLabels();
	}
	/**
	 * Remove a dataset represented by the given bundle. 
	 * @param bundle
	 */
	public void removeDataSet(DataSetLayoutInfoBundle bundle)
	{
		data.remove(bundle);
		LayoutComparator comp = new LayoutComparator(null, bundle);
		visualisations.get(bundle.dataset).remove(bundle);		
		order.remove(comp);
		labelsOK = false;
		updateLabels();
	}

	/**
	 * Set the size of the display. (to calculate the actual size)
	 * @param rec
	 */
	public void setDisplaySize(Rectangle rec)
	{
		if(!rec.getSize().equals(displaySize.getSize()))
		{
			displaySize = rec;
		}
	}

	

	/**
	 * Update the labels according to the order provided by the order vector.
	 */
	private void updateLabels()
	{
		if(!labelsOK)
		{
			LabelGenerator lab = new LabelGenerator();
			for(LayoutComparator comp : order)
			{
				comp.bundle.Label = lab.getLabel();
				comp.bundlelayout.updateLabel(comp.bundle.Label);
			}
			labelsOK = true;
		}
	}

	/**
	 * Move a specific bundle to the front (i.e. it will be plotted last overwriting any former containerlayouts 
	 * @param bundle
	 */
	public void moveBundleToFront(DataSetLayoutInfoBundle bundle)
	{
		data.remove(bundle);
		data.addLast(bundle);
	}
	
	
	public ManualNodeLayout getFinalLayout()
	{
		ManualNodeLayout layout = new ManualNodeLayout();
		layout.setupLayout(data, order);
		return layout;
	}

	/**
	 * Lay out the legend for a specific set of node data
	 * @param svg the {@link SVGGraphics2D} to draw in
	 * @param identifier the identifier to draw
	 */
	private void drawIdentifier(SVGGraphics2D svg, String identifier)
	{		
		Font currentFont = svg.getFont();		
		svg.setFont(LayoutUtils.scaleFont(new Dimension(IMAGENODEPROPERTIES.IMAGEWIDTH, IMAGENODEPROPERTIES.LABELHEIGHT),IDFont, svg, identifier));
		svg.setColor(Color.black);		
		FontMetrics fm = svg.getFontMetrics();		
		Rectangle2D bounds = fm.getStringBounds(identifier, svg);		
		int xpos = (int) ((IMAGENODEPROPERTIES.IMAGEWIDTH - bounds.getWidth())/2);		
		int ypos = IMAGENODEPROPERTIES.IMAGEHEIGHT + fm.getAscent();
		svg.drawString(identifier, xpos, ypos);
		svg.setFont(currentFont);
	}
	
	@Override
	public Vector<DataSetLink> getDatasetsInOrder() {
		Vector<DataSetLink> orderedsets = new Vector<>();
		for(LayoutComparator comp : order)
		{
			orderedsets.add(comp.bundle);
		}
		return orderedsets;
	}


	@Override
	public String getDataSetLabel(DataSetLink ds) {
		// TODO Auto-generated method stub
		if(ds instanceof DataSetLayoutInfoBundle)
		{
			return ((DataSetLayoutInfoBundle) ds).Label;
		}
		return null;
	}

	@Override
	public ColorMap getColorsForDataSet(DataSetLink ds) {
		if(ds instanceof DataSetLayoutInfoBundle)
		{
			return ((DataSetLayoutInfoBundle) ds).colormap;
		}
		return null;
	}

	@Override
	public void layoutNode(Collection<NodeData> datacollection, SVGGraphics2D svg) {
		//First assign labels according to the current localisation.
		layoutNode(datacollection,svg,false);		
	}

	private void layoutNode(Collection<NodeData> datacollection, SVGGraphics2D svg, boolean legend)
	{
		//Do the layout in the order defined by the data. since we in a manual layout can have overlapping 
		//containers, the order is relevant.
		HashMap<DataSet,NodeData> nodeData = new HashMap<>();
		for(NodeData data : datacollection)
		{
			nodeData.put(data.getDataSet(), data);
		}
		for(DataSetLayoutInfoBundle bundle : data)
		{
			NodeData cdata = nodeData.get(bundle.dataset);
			cdata = cdata != null ? cdata : bundle.dataset.getDefaultData(); 
			comparators.get(bundle).bundlelayout.LayoutDataForNode(cdata, svg, legend, bundle.colormap);			
		}	

		drawIdentifier(svg, datacollection.iterator().next().getLabel());
	}



	
	//Since this is a layout which is only modified, but never stored (its only used during manual generation), we don't need storing methods.
	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void writeLayout(ObjectOutputStream os) throws IOException {
		// TODO Auto-generated method stub

	}
	@Override
	public void doLayout() throws TooManyItemsException, ContainerUnplaceableExcpetion, DimensionMismatchException,
			WrongDatasetTypeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean readLayout(DataSetManager dsm, ObjectInputStream os, Object currentobject) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void layoutLegendNode(Collection<NodeData> datacollection, SVGGraphics2D svg) {
		layoutNode(datacollection,svg,true);

	}

	@Override
	public void datasetChanged(DataSetChangedEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void datasetsChanged(DataSetsChangedEvent e) {
		// TODO Auto-generated method stub

	}	

}
