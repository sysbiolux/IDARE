package idare.imagenode.internal.Layout.Manual;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.batik.svggen.SVGGraphics2D;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.Properties.LabelGenerator;
import idare.imagenode.Utilities.EOOMarker;
import idare.imagenode.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.imagenode.exceptions.layout.DimensionMismatchException;
import idare.imagenode.exceptions.layout.TooManyItemsException;
import idare.imagenode.exceptions.layout.WrongDatasetTypeException;
import idare.imagenode.internal.IDAREService;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.DataManagement.Events.DataSetChangedEvent;
import idare.imagenode.internal.DataManagement.Events.DataSetsChangedEvent;
import idare.imagenode.internal.Layout.AbstractLayout;
import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;
import idare.imagenode.internal.Layout.DataSetLink;
import idare.imagenode.internal.Layout.SimpleLink;
import idare.imagenode.internal.Layout.Manual.Utilities.LayoutComparator;


/**
 * This is a very simplistic Node Layout that originates from a Manual layout generation. removed Datasets will simply be 
 * removed from the layout as we don't know what to do with them.
 * 
 * @author thomas
 *
 */
public class ManualNodeLayout extends AbstractLayout implements IDAREService {

	Vector<SimpleLink> alignment = new Vector<>();
	Vector<SimpleLink> order = new Vector<>();	
	HashMap<SimpleLink,ContainerLayout> layouts = new HashMap<>();
	HashMap<DataSet,Vector<SimpleLink>> linkmap= new HashMap<>(); 
	HashMap<SimpleLink,String> labels = new HashMap<>();
	HashMap<SimpleLink,ColorMap> colors = new HashMap<>();
	
	/**
	 * Create an empty layout. only usable when set up. 
	 */
	public ManualNodeLayout(int ImageHeight, int ImageWidth, int LabelHeight)
	{
		super(ImageHeight,ImageWidth,LabelHeight);
	}
	
	/**
	 * The assumption is, that the order in the given Vector is the expected order!
	 * @param alignedlayoutData the Bundles in the correct alignment (back to front)
	 * @param orderedlayoutData the Comparators to obtain the Containerlayouts from. in the correct order (to determine the labels.
	 */
	public void setupLayout(List<DataSetLayoutInfoBundle> alignedlayoutData, List<LayoutComparator> orderedlayoutData)
	{
		int i = 0;
		HashMap<DataSetLayoutInfoBundle,SimpleLink> bundlelinks = new HashMap<>();
		for(DataSetLayoutInfoBundle bundle : alignedlayoutData)
		{
			SimpleLink sl = new SimpleLink(bundle.dataset, i++);
//			PrintFDebugger.Debugging(this, "Creating new link " + sl);
			bundlelinks.put(bundle, sl);
			colors.put(sl, bundle.colormap);
			labels.put(sl, bundle.Label);
			if(!linkmap.containsKey(bundle.dataset))
			{
				linkmap.put(bundle.dataset, new Vector<>());
			}
			linkmap.get(bundle.dataset).add(sl);
			alignment.add(sl);
		}
		for(LayoutComparator comp : orderedlayoutData)
		{
			SimpleLink sl = bundlelinks.get(comp.bundle);
			
			layouts.put(sl, comp.bundlelayout);
//			PrintFDebugger.Debugging(this, "Adding " + comp.bundlelayout + " to link for dataset " + sl);
			order.add(sl);
		}
	}
	
	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return !linkmap.isEmpty();
	}

	@Override
	public void writeLayout(ObjectOutputStream os) throws IOException {
		// TODO Auto-generated method stub
		for(SimpleLink link : alignment)
		{			
			os.writeObject(new Integer(link.getDataSet().getID()));
			os.writeObject(new Integer(order.indexOf(link)));
			os.writeObject(layouts.get(link));
			os.writeObject(colors.get(link));					
		}
		os.writeObject(new EOOMarker());
	}

	@Override
	public boolean readLayout(DataSetManager dsm, ObjectInputStream os, Object currentobject) throws IOException {
		// TODO Auto-generated method stub
		try{
			//Object currentobject = os.readObject();			
			while(!(currentobject instanceof EOOMarker))
			{
				DataSet currentDataSet = dsm.getDataSetForID((Integer) currentobject);
				if(!linkmap.containsKey(currentDataSet))
				{
					linkmap.put(currentDataSet, new Vector<>());
				}
				
				Integer orderpos = (Integer) os.readObject();
				SimpleLink sl = new SimpleLink(currentDataSet, orderpos);
				alignment.add(sl);
				linkmap.get(currentDataSet).add(sl);
				ContainerLayout layout = (ContainerLayout) os.readObject();
				layouts.put(sl,layout);
				ColorMap map = (ColorMap) os.readObject();
				colors.put(sl, map);
				currentobject = os.readObject();
			}
			order.addAll(alignment);
			Collections.sort(order, new Comparator<SimpleLink>() {
				public int compare(SimpleLink o1, SimpleLink o2)
				{
					return Integer.compare(o1.position,o2.position);
				}
			});
			updateLabels();
			return true;
		}
		catch(Exception e)
		{
//			PrintFDebugger.Debugging(e, "Could not read layout due to the exception.");
			e.printStackTrace(System.out);
			return false;
		}	}

	@Override
	public String getDataSetLabel(DataSetLink ds) {
		// TODO Auto-generated method stub
		return labels.get(ds);
	}

	@Override
	public ColorMap getColorsForDataSet(DataSetLink ds) {
		return colors.get(ds);
	}

	@Override
	public Vector<? extends DataSetLink> getDatasetsInOrder() {
		return order;
	}

	
	
	@Override
	public void layoutNode(Collection<NodeData> datacollection, SVGGraphics2D svg) {
		
		//Replace by the actual nodelabel (we need a link to the appropriate imagenode for this
		layoutNode(datacollection,svg, false);
		//drawIdentifier(svg, datacollection.iterator().next().getLabel());
	}

	@Override
	public void layoutLegendNode(Collection<NodeData> datacollection, SVGGraphics2D svg) {
		// TODO Auto-generated method stub
		layoutNode(datacollection,svg, true);
		drawIdentifier(svg, datacollection.iterator().next().getLabel());
	}
	
	
	/**
	 * Do the layout in the order defined by the alignment vector. since we in a manual layout can have overlapping
	 * containers, the order is relevant. 
	 * @param datacollection the node data used to plot.
	 * @param svg the context to plot in
	 * @param legend whether the plot is for a legend or not
	 */
	private void layoutNode(Collection<NodeData> datacollection, SVGGraphics2D svg, boolean legend)
	{
		HashMap<DataSet,NodeData> nodeData = new HashMap<>();
		for(NodeData data : datacollection)
		{
			nodeData.put(data.getDataSet(), data);
		}
		for(SimpleLink link : alignment)
		{
			NodeData cdata = nodeData.get(link.getDataSet());
			
			cdata = cdata != null ? cdata : link.getDataSet().getDefaultData();
			layouts.get(link).LayoutDataForNode(cdata, svg, legend, colors.get(link));			
		}	
		
	}

	
	private void removeDataSet(DataSet ds)
	{
		if(linkmap.containsKey(ds)){
			for(SimpleLink link : linkmap.get(ds))
			{
				order.remove(link);
				layouts.remove(link);
				labels.remove(link);
				colors.remove(link);
			}
			linkmap.remove(ds);
		}
	}
	
	/**
	 * Update the labels according to the order.
	 */
	private void updateLabels()
	{
		LabelGenerator lab = new LabelGenerator();
		for(SimpleLink link : order)
		{
			labels.put(link, lab.getLabel());
		}
	}
	
	@Override
	public void datasetChanged(DataSetChangedEvent e) {
		// TODO Auto-generated method stub
		if(e.wasRemoved())
		{
			removeDataSet(e.getSet());
			if(!isValid())
			{
				e.getSource().removeDataSetAboutToBeChangedListener(this);
			}
			else
			{
				updateLabels();
			}
		}
	}

	@Override
	public void datasetsChanged(DataSetsChangedEvent e) {
		// TODO Auto-generated method stub
		if(e.wasRemoved())
		{
			for(DataSet ds : e.getSet())
			{
				removeDataSet(ds);				
			}
			if(!isValid())
			{
				e.getSource().removeDataSetAboutToBeChangedListener(this);
			}
			else
			{
				updateLabels();
			}
		}

	}

	@Override
	public void doLayout() throws TooManyItemsException, ContainerUnplaceableExcpetion, DimensionMismatchException,
			WrongDatasetTypeException {
		//NOTHING TO BE DONE HERE. Everything is ready once this Layout is created.
		
	}

}
