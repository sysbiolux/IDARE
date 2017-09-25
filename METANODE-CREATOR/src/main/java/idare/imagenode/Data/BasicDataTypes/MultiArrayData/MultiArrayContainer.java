package idare.imagenode.Data.BasicDataTypes.MultiArrayData;

import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.Properties.Localisation;
import idare.imagenode.Properties.IMAGENODEPROPERTIES.LayoutStyle;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * A Container for a {@link MultiArrayDataSet}
 * @author Thomas Pfau
 *
 */
public class MultiArrayContainer implements DataContainer {

	
	/**
	 * The localisation information of this {@link DataContainer}
	 */
	protected Localisation loc;
	/**
	 * The {@link DataSet} this container is associated with
	 */
	protected DataSet origin;
	/**
	 * The {@link NodeData} this container is associated with 
	 */
	protected MultiArrayNodeData data;
	/**
	 * The minimal width (out of 10) of the container
	 */
	protected static int minwidth = 10;
	/**
	 * The minimal height (out of 10) of the container.
	 */
	protected static int minheight = 4;
	/**
	 * Standard Constructor using the Source Dataset and a NodeData
	 * @param origin The Origin of this Container
	 * @param data the data for which to create a container.
	 */
	public MultiArrayContainer(DataSet origin, NodeData data)
	{
		this.data = (MultiArrayNodeData)data;
		this.origin = origin;
		loc = new Localisation(origin.getPreferredposition(), origin.isFlexibility());
	}
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataContainer#getMinimalSize()
	 */
	@Override
	public Rectangle getMinimalSize() {		
		return new Rectangle(new Dimension(minwidth,minheight));
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

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataContainer#getPreferredSize(java.awt.Dimension, idare.imagenode.Properties.METANODEPROPERTIES.LayoutStyle)
	 */
	@Override
	public Dimension getPreferredSize(Dimension availablearea, LayoutStyle style ) {
		//For layouting purposes take at most the minimal size or, whatever is left..
		return new Dimension(Math.min(availablearea.width,minwidth), Math.min(availablearea.height,minheight));
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
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.Interfaces.DataSets.DataContainer#createEmptyLayout()
	 */
	@Override
	public ContainerLayout createEmptyLayout() {
		// TODO Auto-generated method stub
		return null;
	}
}
