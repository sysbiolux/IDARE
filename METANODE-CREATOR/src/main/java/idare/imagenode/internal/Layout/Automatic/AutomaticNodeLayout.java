package idare.imagenode.internal.Layout.Automatic;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.batik.svggen.SVGGraphics2D;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
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

/**
 * Te NodeLAyout stores the localisation of different Containerlayouts and sets them up using the Datasets in this layout.
 * @author Thomas Pfau
 *
 */
public class AutomaticNodeLayout extends AbstractLayout implements IDAREService{

	//private Vector<DataContainer> containers = new Vector<>();
	ImageNodeContainer cont = new ImageNodeContainer();
	HashMap<DataSet,ContainerLayout> DataSetPositions = new HashMap<>();
	HashMap<DataSet,DataSetLayoutProperties> Properties = new HashMap<>();
	HashMap<DataSet,String> DataSetLabels = new HashMap<DataSet, String>();
	HashMap<DataSet,ColorMap> DataSetColors = new HashMap<DataSet, ColorMap>();
	Vector<DataSet> DatasetOrder = new Vector<DataSet>();
	private boolean layoutcreated = false;	
	
	
	private HashMap<DataSet,DataSetLayoutInfoBundle> dataSetsToUse = new HashMap<>();
		
	
	public AutomaticNodeLayout()
	{
		
	}
	
	/**
	 * Generate an automatic layout for a set of DataSetLayouts
	 * @param DataSetsToUse the Datasets and layouts to use.
	 */
	public AutomaticNodeLayout(Collection<DataSetLayoutInfoBundle> DataSetsToUse) {
		// TODO Auto-generated constructor stub
		for(DataSetLayoutInfoBundle bundle : DataSetsToUse)
		{
			this.dataSetsToUse.put(bundle.dataset,bundle);
		}
	}
	
	/* (non-Javadoc)
	 * @see idare.imagenode.internal.Layout.ImageLayout#isValid()
	 */
	@Override
	public boolean isValid()
	{
		return cont.isValidForIDARE();
	}
	
	
	/* (non-Javadoc)
	 * @see idare.imagenode.internal.Layout.ImageLayout#writeLayout(java.io.ObjectOutputStream)
	 */
	@Override
	public void writeLayout(ObjectOutputStream os) throws IOException
	{
		super.writeLayout(os);
		//ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(LayoutFile));
		for(DataSet ds : DatasetOrder)
		{			
			os.writeObject(new Integer(ds.getID()));
			os.writeObject(DataSetPositions.get(ds));
			os.writeObject(DataSetColors.get(ds));
			os.writeObject(Properties.get(ds));
			os.writeObject(DataSetLabels.get(ds));
		}
		os.writeObject(new EOOMarker());
		//os.close();
	}
	/* (non-Javadoc)
	 * @see idare.imagenode.internal.Layout.ImageLayout#readLayout(idare.imagenode.internal.DataManagement.DataSetManager, java.io.ObjectInputStream, java.lang.Object)
	 */
	@Override
	public boolean readLayout(DataSetManager dsm, ObjectInputStream os, Object currentobject) throws IOException
	{
		//Format of a Layout Description:
		//1 Line per Container Layout starting with the DataSet ID and :
		//ObjectInputStream os = new ObjectInputStream(new FileInputStream(LayoutFile));
		DataSetLabels = new HashMap<DataSet, String>();
		DatasetOrder = new Vector<DataSet>();
		try{
			currentobject = os.readObject();
			boolean success = super.readLayout(dsm, os, currentobject);
			if(success)
			{
				currentobject = os.readObject();
			}
			//Object currentobject = os.readObject();			
			while(!(currentobject instanceof EOOMarker))
			{
				DataSet currentDataSet = dsm.getDataSetForID((Integer) currentobject);				
				ContainerLayout layout = (ContainerLayout) os.readObject();
				DataSetPositions.put(currentDataSet, layout);				
				DatasetOrder.addElement(currentDataSet);
				ColorMap map = (ColorMap) os.readObject();
				DataSetLayoutProperties props = (DataSetLayoutProperties) os.readObject();
				//DataSetColors.put(currentDataSet, map);
				DataSetLayoutInfoBundle set = new DataSetLayoutInfoBundle();				
				set.colormap = map;
				set.dataset = currentDataSet;
				set.properties = props;
				addDataSet(set);
				currentobject = os.readObject();
				DataSetLabels.put(currentDataSet, (String) currentobject); 
				currentobject = os.readObject();
				layoutcreated = true;
			}
			return true;
		}
		catch(Exception e)
		{
//			PrintFDebugger.Debugging(e, "Could not read layout due to the exception.");
			e.printStackTrace(System.out);
			return false;
		}
		//os.close();
		
	}
	
	/**
	 * Get the Container used for the provided dataset 
	 * @param set The {@link DataSet} for which the {@link ContainerLayout} is requested. 
	 * @return the associated {@link ContainerLayout} or null if there is no associated {@link ContainerLayout} 
	 */
	public ContainerLayout getLayoutContainerUsedFor(DataSet set)
	{
		return DataSetPositions.get(set);
	}
	
	
	/**
	 * Add a dataset to this layout (checking whether it fits) given the properties and colors in the provided bundle
	 * @param bundle a {@link DataSetLayoutInfoBundle} which has at least the dataset, colormap and properties fields set.
	 * @throws TooManyItemsException
	 * @throws WrongDatasetTypeException
	 */
	private void addDataSet(DataSetLayoutInfoBundle bundle) throws TooManyItemsException,WrongDatasetTypeException
	{		
		cont.addDataSet(bundle);
		DataSetColors.put(bundle.dataset, bundle.colormap);
		Properties.put(bundle.dataset, bundle.properties);
		//bundles.put(set.dataset, set);
		//set.dataset.addDataSetChangeListener(this);
	}
	/**
	 * Generate a Layout for a given set of {@link DataSet}s.
	 * @param Datasets the {@link DataSet}s to generate a layout for.
	 * @throws TooManyItemsException If there are too many items
	 * @throws ContainerUnplaceableExcpetion if a container is unplaceable 
	 * @throws DimensionMismatchException if the given dimensions cant fit 
	 * @throws WrongDatasetTypeException if an Invalid DatasetLayoutinfo bundle is added
	 */
	public void generateLayoutForDataSets(Collection<DataSetLayoutInfoBundle> Datasets) throws TooManyItemsException, ContainerUnplaceableExcpetion, DimensionMismatchException, WrongDatasetTypeException
	{		
		for(DataSetLayoutInfoBundle current : Datasets)
		{
			addDataSet(current);			
		}		
		calculateLayout();
	}
	/* (non-Javadoc)
	 * @see idare.imagenode.internal.Layout.ImageLayout#doLayout()
	 */
	@Override
	public void doLayout() throws TooManyItemsException, ContainerUnplaceableExcpetion, DimensionMismatchException, WrongDatasetTypeException
	{
		generateLayoutForDataSets(dataSetsToUse.values());
	}
	
	
	/**
	 * Calculate the layout with the Sets to use provided during construction.
	 * @throws TooManyItemsException if there are too many items to place
	 * @throws ContainerUnplaceableExcpetion if a container is unplacable
	 * @throws DimensionMismatchException if dimensions dont match
	 * @throws WrongDatasetTypeException if an Invalid DatasetLayoutinfo bundle is added
	 */
	private synchronized void calculateLayout() throws TooManyItemsException, ContainerUnplaceableExcpetion, DimensionMismatchException, WrongDatasetTypeException
	{
		DataSetPositions = new HashMap<DataSet, ContainerLayout>();
		DataSetLabels = new HashMap<DataSet, String>();
		DatasetOrder = new Vector<DataSet>();
		JFrame frame = new JFrame();
		HashMap<ImageBag,HashMap<JPanel, DataContainer>> positions = cont.createLayout(frame);
		//Since this is the layout for a specific type of node, we will define the Labels here.
		//We now have to create the rectangles for each DataContainer.
		Vector<Rectangle> ContainerOrder = new Vector<>();
		HashMap<Rectangle,DataContainer> containerlocations = new HashMap<>();
		for(ImageBag bag : positions.keySet())
		{
			HashMap<JPanel, DataContainer> currentpositions = positions.get(bag);
			
			int xpos = bag.getBounds().x;
			int ypos = bag.getBounds().y;
			for(JPanel pan : currentpositions.keySet())
			{
				Rectangle currentpos = pan.getBounds();

				Rectangle ContainerPos = new Rectangle(xpos + currentpos.x, ypos + currentpos.y, currentpos.width,currentpos.height);
				containerlocations.put(ContainerPos, currentpositions.get(pan));
				ContainerOrder.add(ContainerPos);
			}

		}	
		//Sort the Panes from top to bottom and from left to right (
		Collections.sort(ContainerOrder,new Comparator<Rectangle>() {
			@Override
			public int compare(Rectangle o1, Rectangle o2)
			{
				int res = Integer.compare(o1.y, o2.y) < 0 ? -1 : Integer.compare(o1.x, o2.x) ;
				return res;
			}
		});

		LabelGenerator lab = new LabelGenerator();
		for(Rectangle pane : ContainerOrder)
		{

			DataContainer current_container = containerlocations.get(pane);
			ContainerLayout current_layout = current_container.createEmptyLayout();
			String Label = lab.getLabel();
			current_layout.createLayout(current_container.getData(), pane, Label,Properties.get(current_container.getDataSet()));
			DataSetPositions.put(current_container.getDataSet(), current_layout);
			DataSetLabels.put(current_container.getDataSet(), Label);
			DatasetOrder.add(current_container.getDataSet());
		}
		frame.dispose();
		layoutcreated = true;
	}

	/* (non-Javadoc)
	 * @see idare.imagenode.internal.Layout.ImageLayout#getDataSetLabel(idare.imagenode.Interfaces.DataSets.DataSet)
	 */
	@Override
	public String getDataSetLabel(DataSetLink dsl)
	{
		if(dsl instanceof SimpleLink)
		{
			return DataSetLabels.get(((SimpleLink)dsl).ds);
		}		
		return "";
	}
	/* (non-Javadoc)
	 * @see idare.imagenode.internal.Layout.ImageLayout#getColorsForDataSet(idare.imagenode.Interfaces.DataSets.DataSet)
	 */
	@Override
	public ColorMap getColorsForDataSet(DataSetLink dsl)
	{
		if(dsl instanceof SimpleLink)
		{
			return DataSetColors.get(((SimpleLink)dsl).ds);
		}
		return DataSetColors.get(dsl);
	}
	/* (non-Javadoc)
	 * @see idare.imagenode.internal.Layout.ImageLayout#getDatasetsInOrder()
	 */
	@Override
	public Vector<? extends DataSetLink> getDatasetsInOrder()
	{
		Vector<DataSetLink> orderedsets = new Vector<>();
		for(int i = 0; i < DatasetOrder.size(); i++)
		{
			orderedsets.add(new SimpleLink(DatasetOrder.get(i), i));
		}
		return orderedsets;
	}
	/* (non-Javadoc)
	 * @see idare.imagenode.internal.Layout.ImageLayout#layoutNode(java.util.Collection, org.apache.batik.svggen.SVGGraphics2D)
	 */
	@Override
	public synchronized void layoutNode(Collection<NodeData> datacollection, SVGGraphics2D svg)	
	{
		HashSet<DataSet> PlacedDataSets = new HashSet<DataSet>();
		//svg.setBackground(Color.WHITE);
		for(NodeData data : datacollection)
		{
			
			DataSet cds = data.getDataSet();
			if(DataSetPositions.containsKey(cds))
			{				
			DataSetPositions.get(cds).LayoutDataForNode(data, svg, false,DataSetColors.get(cds));
			PlacedDataSets.add(cds);
			}
			else{
			}						

		}	

		Set<DataSet> emptyset =   new HashSet<DataSet>();
		emptyset.addAll(DataSetPositions.keySet());
		emptyset.removeAll(PlacedDataSets);
		for(DataSet ds : emptyset)
		{
			DataSetPositions.get(ds).LayoutDataForNode(ds.getDefaultData(), svg, false, DataSetColors.get(ds));
		}
		//Replace by the actual nodelabel (we need a link to the appropriate imagenode for this
		if(imageIncludesLabel())
		{
			drawIdentifier(svg, datacollection.iterator().next().getLabel());
		}

	}
	/* (non-Javadoc)
	 * @see idare.imagenode.internal.Layout.ImageLayout#layoutLegendNode(java.util.Collection, org.apache.batik.svggen.SVGGraphics2D)
	 */
	@Override
	public synchronized void layoutLegendNode(Collection<NodeData> datacollection, SVGGraphics2D svg)	
	{

		HashSet<DataSet> PlacedDataSets = new HashSet<DataSet>();
		for(NodeData data : datacollection)
		{
			if(DataSetPositions.containsKey(data.getDataSet()))
			{
				DataSetPositions.get(data.getDataSet()).LayoutDataForNode(data, svg, true,DataSetColors.get(data.getDataSet()));

				PlacedDataSets.add(data.getDataSet());
			}
		}
		Set<DataSet> emptyset =   new HashSet<DataSet>();
		emptyset.addAll(DataSetPositions.keySet());
		emptyset.removeAll(PlacedDataSets);
		for(DataSet ds : emptyset)
		{
			DataSetPositions.get(ds).LayoutDataForNode(ds.getDefaultData(), svg, true,DataSetColors.get(ds));
		}
		drawIdentifier(svg, datacollection.iterator().next().getLabel());

	}
	
	/* (non-Javadoc)
	 * @see idare.imagenode.internal.Layout.ImageLayout#datasetChanged(idare.imagenode.internal.DataManagement.Events.DataSetChangedEvent)
	 */
	@Override
	public void datasetChanged(DataSetChangedEvent e) {
		if(e.wasRemoved())
		{
			cont.removeDataSet(e.getSet());
			DataSetColors.remove(e.getSet());
			DataSetPositions.remove(e.getSet());
			DatasetOrder.remove(e.getSet());
			DataSetLabels.remove(e.getSet());
			dataSetsToUse.remove(e.getSet());
			if(layoutcreated)
			{
				try{
					doLayout();
				}
				catch(Exception ex)
				{
					//this should never happen, as we d
					ex.printStackTrace(System.out);
				}
			}
			if(!cont.isValidForIDARE())
			{
				e.getSource().removeDataSetAboutToBeChangedListener(this);
			}
			if(DataSetPositions.containsKey(e.getSet()))
			{
			}
		}
		
	}


	/* (non-Javadoc)
	 * @see idare.imagenode.internal.Layout.ImageLayout#datasetsChanged(idare.imagenode.internal.DataManagement.Events.DataSetsChangedEvent)
	 */
	@Override
	public void datasetsChanged(DataSetsChangedEvent e) {
		// TODO Auto-generated method stub
		if(e.wasRemoved())
		{
			for(DataSet ds : e.getSet())
			{
				cont.removeDataSet(ds);
				DataSetColors.remove(ds);
				DatasetOrder.remove(ds);
				DataSetPositions.remove(ds);
				DataSetLabels.remove(ds);
				dataSetsToUse.remove(ds);			
				}
			if(layoutcreated)
			{
				
				try{
					if(cont.isValidForIDARE())
					{
						doLayout();
					}
				}
				catch(Exception ex)
				{
					//this should never happen, as we d
					ex.printStackTrace(System.out);
				}
			}
			if(!cont.isValidForIDARE())
			{
				e.getSource().removeDataSetAboutToBeChangedListener(this);
			}
		}

	}
	

}
