package idare.imagenode.internal.VisualStyle;

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
public class IDAREDependentMapper<S> implements VisualMappingFunction<String,S>,DiscreteMapping<String, S>{
	
	NodeManager nm;
	boolean active;
	S imagenodeValue;
	VisualProperty<S> vp;
	String mappedColumnName;
	HashMap<String, S> mappedValues;
	/**
	 * Standard constructor of the Mapper
	 * @param MappedColumnName - The column that is mapped from 
	 * @param vp - the {@link VisualProperty} mapped to by this mapper
	 * @param nm The NodeManager to obtain information from 
	 * @param mappedValue - the Value used for imagenodes.
	 */
	public IDAREDependentMapper(String MappedColumnName, VisualProperty<S> vp,NodeManager nm,S mappedValue ) {
		super();
		this.nm = nm;
		active = false;
		this.imagenodeValue = mappedValue;
		this.vp = vp;
		mappedColumnName = MappedColumnName;
		mappedValues = new HashMap<String, S>();
		
	}

	@Override
	public Map<String, S> getAll() {
		//Return a Map containing the properly mapped values and the imagenode Value for all imagenodes. 
		HashMap<String, S> returnVals = new HashMap<String, S>();
		returnVals.putAll(mappedValues);
		for(String ID : nm.getLayoutedIDs())
		{
			returnVals.put(ID, imagenodeValue);
		}
		return returnVals;
	}

	@Override
	public S getMapValue(String arg0) {
		//Return either the properly mapped value OR The imagenode Value if the requested ID is a valid ID. 
		if(nm.isNodeLayouted(arg0))
		{
			return imagenodeValue;
		}
		else
		{
			return mappedValues.get(arg0);
		}	
	}

	@Override
	public <T extends S> void putAll(Map<String, T> arg0) {
		mappedValues.putAll(arg0);
	}

	@Override
	public <T extends S> void putMapValue(String arg0, T arg1) {
		mappedValues.put(arg0, arg1);
	}

	@Override
	public void apply(CyRow arg0, View<? extends CyIdentifiable> arg1) {
		arg1.setVisualProperty(vp, getMappedValue(arg0));
	}

	@Override
	public S getMappedValue(CyRow arg0) {
		//Return either the properly mapped value OR The imagenode Value if the requested row corresponds to a imagenode 

		if(nm.isNodeLayouted(arg0.get(mappedColumnName, String.class)))
		{
			return imagenodeValue;
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
	public VisualProperty<S> getVisualProperty() {
		return vp;
	}

}
