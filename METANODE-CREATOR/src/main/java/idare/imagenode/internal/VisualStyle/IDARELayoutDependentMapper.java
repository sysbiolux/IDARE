package idare.imagenode.internal.VisualStyle;

import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;

/**
 * A Visual Mapping function that provides its mapping dependent on IDARE 
 * @author Thomas Pfau
 *
 * @param <S> - The Class of the Mapping to be used as values
 */
public class IDARELayoutDependentMapper implements VisualMappingFunction<String,Object>,DiscreteMapping<String, Object>{	
	public enum layoutProperties{
		IMAGEHEIGHT,
		IMAGEWIDTH,
		LABELPOSITION,
		TRANSPARENCY,
		MAPPED
	}
	NodeManager nm;
	boolean active;
	Object imagenodeValue;
	VisualProperty vp;
	String mappedColumnName;
	HashMap<String, Object> mappedValues;
	layoutProperties target;
	/**
	 * Standard constructor of the Mapper
	 * @param MappedColumnName - The column that is mapped from 
	 * @param vp - the {@link VisualProperty} mapped to by this mapper
	 * @param nm The NodeManager to obtain information from 
	 * @param mappedValue - the Value used for imagenodes.
	 */
	public IDARELayoutDependentMapper(String MappedColumnName, VisualProperty vp,NodeManager nm, layoutProperties target, Object mappedValue ) {
		super();
		this.nm = nm;
		active = false;
		this.imagenodeValue = mappedValue;
		this.vp = vp;
		mappedColumnName = MappedColumnName;
		mappedValues = new HashMap<String, Object>();
		this.target = target;
	}

	@Override
	public Map<String, Object> getAll() {
		//Return a Map containing the properly mapped values and the imagenode Value for all imagenodes. 
		HashMap<String, Object> returnVals = new HashMap<String, Object>();
		returnVals.putAll(mappedValues);
		for(String ID : nm.getLayoutedIDs())
		{
				returnVals.put(ID,getValueForID(ID));						
		}
		return returnVals;
	}

	private Object getValueForID(String ID)
	{
		switch(target)
		{
		case IMAGEHEIGHT:
		{
			return nm.getLayoutForNode(ID).getDisplayDimensions().getHeight();			
		}
		case IMAGEWIDTH:
		{
			return nm.getLayoutForNode(ID).getDisplayDimensions().getWidth();
		}
		case TRANSPARENCY:
		{
			if(nm.getLayoutForNode(ID).imageIncludesLabel())
			{
				PrintFDebugger.Debugging(this,"Requested Transparency value for ID " + ID + " Returned " + imagenodeValue.toString());
				return imagenodeValue;
			}
			else
			{
				return null;
			}
		}
		case LABELPOSITION:
		{
			if(!nm.getLayoutForNode(ID).imageIncludesLabel())
			{
				PrintFDebugger.Debugging(this,"Requested label position for ID " + ID + " Returned " + imagenodeValue.toString());
				return imagenodeValue;
			}
			else
			{
				return null;
			}
		}
		default:
			return null;
		}
	}
	
	@Override
	public Object getMapValue(String arg0) {
		//Return either the properly mapped value OR The imagenode Value if the requested ID is a valid ID. 
		if(nm.isNodeLayouted(arg0))
		{
			Object returnvalue = getValueForID(arg0);
			if(returnvalue != null)
			{
				return returnvalue;
			}
			else
			{
				return mappedValues.get(arg0);	
			}
		}
		else
		{
			return mappedValues.get(arg0);
		}	
	}

	@Override
	public <T extends Object> void putAll(Map<String, T> arg0) {
		mappedValues.putAll(arg0);
	}

	@Override
	public <T extends Object> void putMapValue(String arg0, T arg1) {
		mappedValues.put(arg0, arg1);
	}

	@Override
	public void apply(CyRow arg0, View<? extends CyIdentifiable> arg1) {
		arg1.setVisualProperty(vp, getMappedValue(arg0));
	}

	@Override
	public Object getMappedValue(CyRow arg0) {
		//Return either the properly mapped value OR The imagenode Value if the requested row corresponds to a imagenode 

		if(nm.isNodeLayouted(arg0.get(mappedColumnName, String.class)))
		{
			return getValueForID(arg0.get(mappedColumnName,String.class));
		}
		else
		{
			return mappedValues.get(arg0.get(mappedColumnName,String.class));
		}		
	}

	@Override
	public String getMappingColumnName() {
		return mappedColumnName;
	}

	@Override
	public Class<String> getMappingColumnType() {
		return String.class;
	}

	@Override
	public VisualProperty<Object> getVisualProperty() {
		return vp;
	}

}
