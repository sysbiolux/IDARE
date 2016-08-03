package idare.metanode.Data.BasicDataTypes.ValueSetData;

import idare.metanode.Interfaces.DataSets.DataContainer;
import idare.metanode.Interfaces.DataSets.DataSet;
import idare.metanode.Interfaces.DataSets.NodeData;
import idare.metanode.Interfaces.Layout.ContainerLayout;
import idare.metanode.Properties.Localisation;
import idare.metanode.Properties.METANODEPROPERTIES.LayoutStyle;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * A Container for a {@link ValueSetDataSet}
 * @author Thomas Pfau
 *
 */
public class ValueSetContainer implements DataContainer {

	

	protected Localisation loc;
	protected DataSet origin;
	protected ValueSetNodeData data;
	protected static int minwidth = 10;
	protected static int minheight = 4;
	/**
	 * Standard Constructor using the Source Dataset and a NodeData
	 * @param origin
	 * @param data
	 */
	public ValueSetContainer(DataSet origin, NodeData data)
	{
		this.data = (ValueSetNodeData)data;
		this.origin = origin;
		loc = new Localisation(origin.getPreferredposition(), origin.isFlexibility());
	}
	
	@Override
	public Rectangle getMinimalSize() {
		
		return new Rectangle(new Dimension(minwidth,minheight));
	}



	@Override
	public Localisation getLocalisationPreference() {		
		// TODO Auto-generated method stub
		return loc;		
	}

	@Override
	public DataSet getDataSet() {
		// TODO Auto-generated method stub
		return origin;
	}

	@Override
	public Dimension getPreferredSize(Dimension availablearea, LayoutStyle style ) {
		//For layouting purposes take at most the minimal size or, whatever is left..
		return new Dimension(Math.min(availablearea.width,minwidth), Math.min(availablearea.height,minheight));
	}

	@Override
	public NodeData getData()
	{
		return data;
	}

	@Override
	public ContainerLayout createEmptyLayout() {
		// TODO Auto-generated method stub
		return null;
	}
}
