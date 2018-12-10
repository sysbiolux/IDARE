package idare.imagenode.internal.VisualStyle;

import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.internal.DataManagement.NodeManager;

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
public class IDARELabelPositionMapper implements VisualMappingFunction<String,Object>,DiscreteMapping<String, Object>{
	
	NodeManager nm;
	boolean active;
	VisualProperty<Object> vp;
	String mappedColumnName;
	HashMap<String, Object> mappedValues;
	/**
	 * Standard constructor of the Mapper
	 * @param MappedColumnName - The column that is mapped from 
	 * @param vp - the {@link VisualProperty} mapped to by this mapper
	 * @param nm The NodeManager to obtain information from 
	 * @param mappedValue - the Value used for imagenodes.
	 */
	public IDARELabelPositionMapper(String MappedColumnName, VisualProperty<Object> vp,NodeManager nm) {
		super();
		this.nm = nm;
		active = false;
		this.vp = vp;		
		mappedColumnName = MappedColumnName;
		mappedValues = new HashMap<String, Object>();
	}
	
	@Override
	public Object getMapValue(String arg0) {
		//Return either the properly mapped value OR The imagenode Value if the requested ID is a valid ID. 
		if(nm.isNodeLayouted(arg0))
		{			
			return vp.parseSerializableString(IMAGENODEPROPERTIES.NODE_LABEL_POSITION_STRING);
		}
		else
		{
			return mappedValues.get(arg0);
		}	
	}

	@Override
	public Map<String, Object> getAll() {
		HashMap<String, Object> returnVals = new HashMap<String, Object>();
		returnVals.putAll(mappedValues);
		for(String ID : nm.getLayoutedIDs())
		{
			returnVals.put(ID, getMapValue(ID));
		}
		return returnVals;			
	}

	@Override
	public <T> void putAll(Map<String, T> arg0) {
		// TODO Auto-generated method stub
		mappedValues.putAll(arg0);
	}

	@Override
	public <T> void putMapValue(String arg0, T arg1) {
		// TODO Auto-generated method stub
		mappedValues.put(arg0, arg1);
	}

	@Override
	public void apply(CyRow arg0, View<? extends CyIdentifiable> arg1) {
		// TODO Auto-generated method stub
		arg1.setVisualProperty(vp, getMappedValue(arg0));
	}

	@Override
	public Object getMappedValue(CyRow arg0) {
		// TODO Auto-generated method stub
		String id = arg0.get(mappedColumnName, String.class);
		if(nm.isNodeLayouted(id))
		{			
			return vp.parseSerializableString(IMAGENODEPROPERTIES.NODE_LABEL_POSITION_STRING);
		}
		else
		{
			return mappedValues.get(arg0);
		}	
	}

	@Override
	public String getMappingColumnName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<String> getMappingColumnType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VisualProperty<Object> getVisualProperty() {
		// TODO Auto-generated method stub
		return null;
	}



}
