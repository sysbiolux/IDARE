package idare.metanode.internal.Layout;

import idare.metanode.Interfaces.DataSets.DataContainer;
import idare.metanode.Interfaces.DataSets.DataSet;
import idare.metanode.Interfaces.DataSets.NodeData;
import idare.metanode.Interfaces.Layout.ContainerLayout;
import idare.metanode.Properties.LabelGenerator;
import idare.metanode.Properties.METANODEPROPERTIES;
import idare.metanode.internal.ColorManagement.ColorMap;
import idare.metanode.internal.DataManagement.DataSetManager;
import idare.metanode.internal.DataManagement.Events.DataSetAboutToBeChangedListener;
import idare.metanode.internal.DataManagement.Events.DataSetChangedEvent;
import idare.metanode.internal.DataManagement.Events.DataSetsChangedEvent;
import idare.metanode.internal.Debug.PrintFDebugger;
import idare.metanode.internal.Utilities.EOOMarker;
import idare.metanode.internal.Utilities.LayoutUtils;
import idare.metanode.internal.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.metanode.internal.exceptions.layout.DimensionMismatchException;
import idare.metanode.internal.exceptions.layout.TooManyItemsException;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
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

/**
 * Te NodeLAyout stores the localisation of different Containerlayouts and sets them up using the Datasets in this layout.
 * @author Thomas Pfau
 *
 */
public class NodeLayout implements DataSetAboutToBeChangedListener{

	//private Vector<DataContainer> containers = new Vector<>();
	ImageNodeContainer cont = new ImageNodeContainer();
	HashMap<DataSet,ContainerLayout> DataSetPositions = new HashMap<>(); 
	Font IDFont = new Font(Font.MONOSPACED,Font.BOLD,METANODEPROPERTIES.LABELHEIGHT-2);
	HashMap<DataSet,String> DataSetLabels = new HashMap<DataSet, String>();
	HashMap<DataSet,ColorMap> DataSetColors = new HashMap<DataSet, ColorMap>();
	Vector<DataSet> DatasetOrder = new Vector<DataSet>();
	private boolean layoutcreated = false;
	//public int ID = -1;
	
	/**
	 * Check whether this {@link NodeLayout} is valid. A Layout is valid, if its {@link ImageNodeContainer} contains 
	 * at least one {@link DataSet}. 
	 * Thus, by default a newly generated layout is invalid until a {@link DataSet} is added. 
	 * @return whether this layout is still valid (i.e. has contaniers to lay out)
	 */
	public boolean isValid()
	{
		return cont.isValidForIDARE();
	}
	
	
	
	public void writeLayout(ObjectOutputStream os) throws IOException
	{
		//ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(LayoutFile));
		for(DataSet ds : DatasetOrder)
		{			
			os.writeObject(new Integer(ds.getID()));
			os.writeObject(DataSetPositions.get(ds));
			os.writeObject(DataSetColors.get(ds));
			os.writeObject(DataSetLabels.get(ds));
		}
		os.writeObject(new EOOMarker());
		//os.close();
	}
	
	public boolean readLayout(DataSetManager dsm, ObjectInputStream os, Object currentobject) throws IOException
	{
		//Format of a Layout Description:
		//1 Line per Container Layout starting with the DataSet ID and :
		//ObjectInputStream os = new ObjectInputStream(new FileInputStream(LayoutFile));
		DataSetLabels = new HashMap<DataSet, String>();
		DatasetOrder = new Vector<DataSet>();
		try{
			//Object currentobject = os.readObject();			
			while(!(currentobject instanceof EOOMarker))
			{
				DataSet currentDataSet = dsm.getDataSetForID((Integer) currentobject);				
				ContainerLayout layout = (ContainerLayout) os.readObject();
				DataSetPositions.put(currentDataSet, layout);				
				DatasetOrder.addElement(currentDataSet);
				ColorMap map = (ColorMap) os.readObject();
				//DataSetColors.put(currentDataSet, map);
				ColorMapDataSetBundle set = new ColorMapDataSetBundle();
				set.map = map;
				set.dataset = currentDataSet;
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
			PrintFDebugger.Debugging(e, "Could not read layout due to the exception.");
			e.printStackTrace(System.out);
			return false;
		}
		//os.close();
		
	}
	
	private void addDataSet(ColorMapDataSetBundle set) throws TooManyItemsException	
	{		
		cont.addDataSet(set.dataset);
		DataSetColors.put(set.dataset, set.map);
		//set.dataset.addDataSetChangeListener(this);
	}
	/**
	 * Generate a Layout for a given set of DataSets.
	 * @param Datasets
	 * @throws TooManyItemsException
	 * @throws ContainerUnplaceableExcpetion
	 * @throws DimensionMismatchException
	 */
	public void generateLayoutForDataSets(Collection<ColorMapDataSetBundle> Datasets) throws TooManyItemsException, ContainerUnplaceableExcpetion, DimensionMismatchException
	{		
		for(ColorMapDataSetBundle current : Datasets)
		{
			addDataSet(current);			
		}		
		doLayout();
	}
	
	/**
	 * Produce the layout based on the added DataSets.
	 * @throws TooManyItemsException
	 * @throws ContainerUnplaceableExcpetion
	 * @throws DimensionMismatchException
	 */
	public synchronized void doLayout() throws TooManyItemsException, ContainerUnplaceableExcpetion, DimensionMismatchException
	{
		DataSetPositions = new HashMap<DataSet, ContainerLayout>();
		DataSetLabels = new HashMap<DataSet, String>();
		DatasetOrder = new Vector<DataSet>();
		JFrame frame = new JFrame();
		PrintFDebugger.Debugging(this, "Creating new layout");			
		HashMap<ImageBag,HashMap<JPanel, DataContainer>> positions = cont.createLayout(frame);
		PrintFDebugger.Debugging(this, "Layout created");			
		//Since this is the layout for a specific type of node, we will define the Labels here.
		//We now have to create the rectangles for each DataContainer.
		Vector<Rectangle> ContainerOrder = new Vector<>();
		HashMap<Rectangle,DataContainer> containerlocations = new HashMap<>();
		for(ImageBag bag : positions.keySet())
		{
			HashMap<JPanel, DataContainer> currentpositions = positions.get(bag);
			
			int xpos = bag.getBounds().x;
			int ypos = bag.getBounds().y;
			PrintFDebugger.Debugging(this,"The current bag has the following position:" + xpos + "/" +  ypos);
			for(JPanel pan : currentpositions.keySet())
			{
				Rectangle currentpos = pan.getBounds();
				PrintFDebugger.Debugging(this,"The current pane has the following position:" + currentpos.x + "/" +  currentpos.y);

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
			PrintFDebugger.Debugging(this,"Creating layout for Dataset " + current_container.getDataSet().getID() + " at position: " + pane.x + "/" +  pane.y + " ; " + pane.width + "/" + pane.height);
			PrintFDebugger.Debugging(this,"Current container class is " + current_container.getClass().getName());
			ContainerLayout current_layout = current_container.createEmptyLayout();
			PrintFDebugger.Debugging(this,"Current layout is " + current_layout);
			String Label = lab.getLabel();
			current_layout.createLayout(current_container.getData(), pane, Label);
			PrintFDebugger.Debugging(this, "Adding a layout position for Dataset with id " + current_container.getDataSet().getID() + " at position: " + pane.x + "/" +  pane.y + " ; " + pane.width + "/" + pane.height);			
			DataSetPositions.put(current_container.getDataSet(), current_layout);
			DataSetLabels.put(current_container.getDataSet(), Label);
			DatasetOrder.add(current_container.getDataSet());
		}
		frame.dispose();
		layoutcreated = true;
	}

	/** 
	 * Get the label for a specific dataset  used in this layout;
	 * @param ds
	 * @return The String label for the supplied {@link DataSet}
	 */
	public String getDataSetLabel(DataSet ds)
	{
		return DataSetLabels.get(ds);
	}
	/**
	 * Get the {@link ColorMap} associated with this {@link DataSet} in this {@link NodeLayout}.
	 * @param ds - The requested Dataset
	 * @return the {@link ColorMap} associated with the {@link DataSet} in this {@link NodeLayout}
	 */
	public ColorMap getColorsForDataSet(DataSet ds)
	{
		return DataSetColors.get(ds);
	}
	/** 
	 * Get the DataSets used in this Layout in the order of labeling
	 * @return A {@link Vector} of {@link DataSet}s in the order they were added during the layout process 
	 */
	public Vector<DataSet> getDatasetsInOrder()
	{
		return (Vector<DataSet>)DatasetOrder.clone();
	}
	/**
	 * Layout a specific node in a given context.
	 * @param datacollection
	 * @param svg
	 */
	public synchronized void layoutNode(Collection<NodeData> datacollection, SVGGraphics2D svg)	
	{
		HashSet<DataSet> PlacedDataSets = new HashSet<DataSet>();
		//PrintFDebugger.Debugging(this, "Nodelayout " + ID + ": " + datacollection.iterator().next().getLabel() + " is being plotted with " + datacollection.size() + " items");
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
				PrintFDebugger.Debugging(this, "Did not find position for dataset " + data.getDataSet().Description );	
			}						

		}	

		Set<DataSet> emptyset =   new HashSet<DataSet>();
		emptyset.addAll(DataSetPositions.keySet());
		emptyset.removeAll(PlacedDataSets);
		for(DataSet ds : emptyset)
		{
			PrintFDebugger.Debugging(this,"Plotting a value for a dataset that was not part of the original node data.");
			DataSetPositions.get(ds).LayoutDataForNode(ds.getDefaultData(), svg, false, DataSetColors.get(ds));
		}
		//Replace by the actual nodelabel (we need a link to the appropriate MetaNode for this
		drawIdentifier(svg, datacollection.iterator().next().getLabel());

	}
	/**
	 * Lay out the legend for a specific set of node data
	 * @param datacollection
	 * @param svg
	 */
	public synchronized void layoutLegendNode(Collection<NodeData> datacollection, SVGGraphics2D svg)	
	{

		HashSet<DataSet> PlacedDataSets = new HashSet<DataSet>();
		//PrintFDebugger.Debugging(this, "Nodelayout " + ID + ": " + datacollection.iterator().next().getLabel() + " is being plotted with " + datacollection.size() + " items");
		for(NodeData data : datacollection)
		{
			PrintFDebugger.Debugging(this,"Plotting Items for DataSet " + data.getDataSet().getID());
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
		drawIdentifier(svg, "IDENTIFIER");

	}
	/**
	 * Lay out the legend for a specific set of node data
	 * @param datacollection
	 * @param svg
	 */
	private void drawIdentifier(SVGGraphics2D svg, String identifier)
	{
		Font currentFont = svg.getFont();		
		svg.setFont(LayoutUtils.scaleFont(new Dimension(METANODEPROPERTIES.IMAGEWIDTH, METANODEPROPERTIES.LABELHEIGHT),IDFont, svg, identifier));
		svg.setColor(Color.black);		
		FontMetrics fm = svg.getFontMetrics();		
		Rectangle2D bounds = fm.getStringBounds(identifier, svg);		
		int xpos = (int) ((METANODEPROPERTIES.IMAGEWIDTH - bounds.getWidth())/2);		
		int ypos = METANODEPROPERTIES.IMAGEHEIGHT + fm.getAscent();
		PrintFDebugger.Debugging(this,"the x position of the identifier is:" + xpos + "ypos is " + ypos);
		svg.drawString(identifier, xpos, ypos);
		svg.setFont(currentFont);
	}

	@Override
	public void datasetChanged(DataSetChangedEvent e) {
		if(e.wasRemoved())
		{
			cont.removeDataSet(e.getSet());
			DataSetColors.remove(e.getSet());
			DataSetPositions.remove(e.getSet());
			DatasetOrder.remove(e.getSet());
			DataSetLabels.remove(e.getSet());
			if(layoutcreated)
			{
				try{
					//PrintFDebugger.Debugging(this, "Updating Layout " + ID + " after removing a DataSet");
					doLayout();
				}
				catch(Exception ex)
				{
					//this should never happen, as we d
					PrintFDebugger.Debugging(this, "Removing a Dataset let to the following error:" );
					ex.printStackTrace(System.out);
				}
			}
			if(!cont.isValidForIDARE())
			{
				e.getSource().removeDataSetAboutToBeChangedListener(this);
			}
			if(DataSetPositions.containsKey(e.getSet()))
			{
				PrintFDebugger.Debugging(this,"DataSet was not properly removed");
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
				cont.removeDataSet(ds);
				DataSetColors.remove(ds);
				DatasetOrder.remove(ds);
				DataSetPositions.remove(ds);
				DataSetLabels.remove(ds);				
			}
			if(layoutcreated)
			{
				
				try{
					if(cont.isValidForIDARE())
					{
						//PrintFDebugger.Debugging(this, "Updating Layout " + ID + " after removing a DataSet");
						doLayout();
					}
				}
				catch(Exception ex)
				{
					//this should never happen, as we d
					PrintFDebugger.Debugging(this, "Removing a Dataset let to the following error:" );
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
