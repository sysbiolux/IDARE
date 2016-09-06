package idare.imagenode.internal.Layout;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.Properties.LabelGenerator;
import idare.imagenode.Utilities.EOOMarker;
import idare.imagenode.Utilities.LayoutUtils;
import idare.imagenode.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.imagenode.exceptions.layout.DimensionMismatchException;
import idare.imagenode.exceptions.layout.TooManyItemsException;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.DataManagement.Events.DataSetAboutToBeChangedListener;
import idare.imagenode.internal.DataManagement.Events.DataSetChangedEvent;
import idare.imagenode.internal.DataManagement.Events.DataSetsChangedEvent;
import idare.imagenode.internal.Debug.PrintFDebugger;

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
	Font IDFont = new Font(Font.MONOSPACED,Font.BOLD,IMAGENODEPROPERTIES.LABELHEIGHT-2);
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
			current_layout.createLayout(current_container.getData(), pane, Label);
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
