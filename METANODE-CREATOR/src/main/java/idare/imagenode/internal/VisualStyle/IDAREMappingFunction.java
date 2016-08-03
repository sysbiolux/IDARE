package idare.imagenode.internal.VisualStyle;

import idare.Properties.IDAREProperties;
import idare.imagenode.internal.ImageManagement.GraphicsChangedEvent;
import idare.imagenode.internal.ImageManagement.GraphicsChangedListener;
import idare.imagenode.internal.ImageManagement.ImageStorage;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;

/**
 * A Mapping Function That is dependent on IDARE.
 * @author Thomas Pfau
 *
 */
public class IDAREMappingFunction implements VisualMappingFunction<String,CyCustomGraphics<CustomGraphicLayer>>,
											 DiscreteMapping<String, CyCustomGraphics<CustomGraphicLayer>>,
											 GraphicsChangedListener
											 {

	VisualProperty costumVP;
	ImageStorage is;
	private Map<String, CyCustomGraphics<CustomGraphicLayer>> graphicmap;	
	boolean active;
	
	/**
	 * Default constructor 
	 * @param is - the {@link ImageStorage} used that handles the Images.
	 * @param vp - The Visual Property this Mapping function maps.
	 */
	public IDAREMappingFunction(ImageStorage is, VisualProperty vp) {
		super();
		
		this.is = is;	
		is.addImageLayoutChangedListener(this);
		costumVP = vp;		
		graphicmap = new HashMap<String, CyCustomGraphics<CustomGraphicLayer>>();
		active = false;
	}

	/**
	 * Activate this mapping. This should only be called by the DisplayManager.
	 */
	public void activate()
	{
		active = true;
	}
	/**
	 * Deactivate this mapping. This should only be called by the DisplayManager.
	 */
	public void deactivate()
	{
		active = false;
	}
	/**
	 * Clear the mapping that is currently used
	 */
	public void clear()
	{
		graphicmap.clear();		
	}
//	
	@Override
	public void apply(CyRow arg0, View arg1) {
		if(!active)
		{
			return;
		}
		
		String idareName = arg0.get(IDAREProperties.IDARE_NODE_NAME, String.class);
		//this will induce an addition of the Costum graphics.
		is.generateGraphicsForID(idareName, false);		
		//if this idareName is already mapped to a node, use the mapped graphics.
		if(graphicmap.containsKey(idareName))
		{
			arg1.setVisualProperty(costumVP, graphicmap.get(idareName));
		}
		/*else 
		{
			if(icg != null)
			{
				graphicmap.put(idareName, icg);
				arg1.setVisualProperty(costumVP, icg);	
			}
		}*/


	}

	@Override
	public CyCustomGraphics<CustomGraphicLayer> getMappedValue(CyRow arg0) {
		if(!active)
		{
			return null;
		}
		String idareName = arg0.get(IDAREProperties.IDARE_NODE_NAME, String.class);			
		return getMapValue(idareName);
	}

	@Override
	public String getMappingColumnName() {
		return IDAREProperties.IDARE_NODE_NAME;
	}

	@Override
	public Class<String> getMappingColumnType() {
		return String.class;
	}

	@Override
	public VisualProperty getVisualProperty() {
		return costumVP;
	}





	@Override
	public Map<String, CyCustomGraphics<CustomGraphicLayer>> getAll() {
		is.generateGraphicsForID("", true);
		return graphicmap;
	}





	@Override
	public CyCustomGraphics<CustomGraphicLayer> getMapValue(String arg0) {
		if(!active)
		{
			return null;
		}
		if(!graphicmap.containsKey(arg0) || graphicmap.get(arg0) == null)
		{
			is.generateGraphicsForID(arg0, false);
			//graphicmap.put(arg0,is.getGraphicForID(arg0, false));			
		}
		CyCustomGraphics<CustomGraphicLayer> test = graphicmap.get(arg0);
		if(test != null)
		{
		}
		return graphicmap.get(arg0);
	}





	@Override
	public <T extends CyCustomGraphics<CustomGraphicLayer>> void putAll(
			Map<String, T> arg0) {
		graphicmap.putAll(arg0);
	}





	@Override
	public <T extends CyCustomGraphics<CustomGraphicLayer>> void putMapValue(
			String arg0, T arg1) {
		graphicmap.put(arg0, arg1);
	}

	@Override
	public void imageUpdated(GraphicsChangedEvent e) {
		
		for(String id : e.getIDs())
		{
			
			CyCustomGraphics<CustomGraphicLayer> cg = e.getGraphicForID(id);		
			if(cg == null)
			{
				graphicmap.remove(id);
			}
			else
			{
				graphicmap.put(id, cg);
			}
		}
		is.updateStyle();
	}




}
