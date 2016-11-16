package idare.imagenode.Data.BasicDataTypes.ArrayData;

import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Properties.Localisation;
import idare.imagenode.Properties.IMAGENODEPROPERTIES.LayoutStyle;

import java.awt.Dimension;
import java.awt.Rectangle;
/**
 * A Basis class for container using {@link ArrayNodeData}. providing some functionality common to any item based data.
 * @author Thomas Pfau
 *
 */
public abstract class AbstractArrayContainer implements DataContainer {

	protected Localisation loc;
	protected DataSet origin;
	protected ArrayNodeData data;
	/**
	 * Basic constructor with a source {@link DataSet} and the data used in this container.
	 * @param origin the Dataset represented by this {@link DataContainer}
	 * @param data the data to be used in this {@link DataContainer}
	 */
	public AbstractArrayContainer(DataSet origin, NodeData data)
	{
		this.data = (ArrayNodeData)data;
		this.origin = origin;
		loc = new Localisation(origin.getPreferredposition(), origin.isFlexibility());
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataContainer#getMinimalSize()
	 */
	@Override
	public Rectangle getMinimalSize() {
		Rectangle rec = new Rectangle();
		rec.width = data.getValueCount();
		rec.height = 1;
		return rec;
	}


	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataContainer#getDataSet()
	 */
	@Override
	public DataSet getDataSet() {
		// TODO Auto-generated method stub
		return origin;
	}

	/**
	 * The common Item container will use a flexible adjustment of its height/width to best fill the availablearea.	 
	 */
	@Override
	public Dimension getPreferredSize(Dimension availablearea, LayoutStyle style ) {
	
		
		Rectangle rec = new Rectangle(availablearea);
		int items = data.getValueCount();
		//if we are on the edge, use as many columns as necessary.
		if(style == LayoutStyle.EDGE)
		{
			
			if(items < availablearea.height)
			{
				rec.width = 1;
				rec.height = items;
			}
			else{
				int currentwidth = 0;
				while(items > 0)
				{
					items = items - availablearea.height;
					currentwidth += 1;
				}				
				rec.width = currentwidth;
			}
			
		}
		else
		{
			//if we are on the center, use as many rows as necessary.
			if(items < availablearea.width)
			{
				rec.width = items;
				rec.height = 1;
			}
			else
			{
				int currentheight = 0;
			
			while(items > 0)
			{
				items = items - availablearea.height;
				currentheight += 1;
			}
			rec.height = currentheight;
			}
		}
		
		return new Dimension(rec.width,rec.height);
	}

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataContainer#getData()
	 */
	@Override
	public NodeData getData()
	{
		return data;
	}
}
