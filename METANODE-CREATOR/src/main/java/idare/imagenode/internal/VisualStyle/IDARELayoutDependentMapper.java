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
public class IDARELayoutDependentMapper implements VisualMappingFunction<String,Double>,DiscreteMapping<String, Double>{
	
	public static enum MAPPINGTYPES
	{
		NODEIMAGEHEIGHT,
		NODEIMAGEWIDTH		
	}
	NodeManager nm;
	boolean active;
	VisualProperty<Double> vp;
	String mappedColumnName;
	HashMap<String, Double> mappedValues;
	MAPPINGTYPES maptarget;
	/**
	 * Standard constructor of the Mapper
	 * @param MappedColumnName - The column that is mapped from 
	 * @param vp - the {@link VisualProperty} mapped to by this mapper
	 * @param nm The NodeManager to obtain information from 
	 * @param mappedValue - the Value used for imagenodes.
	 */
	public IDARELayoutDependentMapper(String MappedColumnName, VisualProperty<Double> vp,NodeManager nm,MAPPINGTYPES type) {
		super();
		this.nm = nm;
		active = false;
		this.vp = vp;
		mappedColumnName = MappedColumnName;
		mappedValues = new HashMap<String, Double>();
		maptarget = type;
	}

	@Override
	public Map<String, Double> getAll() {
		//Return a Map containing the properly mapped values and the imagenode Value for all imagenodes. 
		HashMap<String, Double> returnVals = new HashMap<String, Double>();
		returnVals.putAll(mappedValues);
		for(String ID : nm.getLayoutedIDs())
		{
			returnVals.put(ID, getValueForMapping(ID));
		}
		return returnVals;
	}

	
	private Double getValueForMapping(String ID)
	{
		switch(maptarget)
		{
		case NODEIMAGEHEIGHT:
		{
			return new Double(nm.getLayoutForNode(ID).getImageHeight() +nm.getLayoutForNode(ID).getLabelHeight())*IMAGENODEPROPERTIES.IDARE_DISPLAY_SIZE_FACTOR;
			
		}
		case NODEIMAGEWIDTH:			
		{
			return new Double(nm.getLayoutForNode(ID).getImageWidth())*IMAGENODEPROPERTIES.IDARE_DISPLAY_SIZE_FACTOR;			
		}
		}
		// this should never be reached.
		return new Double(0);
	}
	
	@Override
	public Double getMapValue(String arg0) {
		//Return either the properly mapped value OR The imagenode Value if the requested ID is a valid ID. 
		if(nm.isNodeLayouted(arg0))
		{			
			return getValueForMapping(arg0);
		}
		else
		{
			return mappedValues.get(arg0);
		}	
	}

	@Override
	public <T extends Double> void putAll(Map<String, T> arg0) {
		mappedValues.putAll(arg0);
	}

	@Override
	public <T extends Double> void putMapValue(String arg0, T arg1) {
		mappedValues.put(arg0, arg1);
	}

	@Override
	public void apply(CyRow arg0, View<? extends CyIdentifiable> arg1) {
		arg1.setVisualProperty(vp, getMappedValue(arg0));
	}

	@Override
	public Double getMappedValue(CyRow arg0) {
		//Return either the properly mapped value OR The imagenode Value if the requested row corresponds to a imagenode 
		String id = arg0.get(mappedColumnName, String.class);
		if(nm.isNodeLayouted(id))
		{
			return getValueForMapping(id);
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
	public VisualProperty<Double> getVisualProperty() {
		return vp;
	}

}
