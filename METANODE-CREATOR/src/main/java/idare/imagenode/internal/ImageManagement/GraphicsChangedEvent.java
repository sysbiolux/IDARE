package idare.imagenode.internal.ImageManagement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

/**
 * An event that informs about changes in graphics for specific IDs.
 * @author Thomas Pfau
 *
 */
public class GraphicsChangedEvent {

	private Vector<String> ID = new Vector<String>();
	private HashMap<String,CyCustomGraphics<CustomGraphicLayer>> newgraphics;
	
	/**
	 * Default constructor using a HAshMap with Strings mapping to the new graphics for those strings.
	 * @param newgraphics the new graphics used
	 */
	GraphicsChangedEvent(HashMap<String,CyCustomGraphics<CustomGraphicLayer>> newgraphics)
	{
		this.ID.addAll(newgraphics.keySet());
		this.newgraphics = newgraphics; 
	}
	
	/**
	 * Get the updated IDs.
	 * @return ID - The IDs updated in this event
	 */
	public Collection<String> getIDs()
	{
		return ID;
	}
	
	/**
	 * get The {@link CyCustomGraphics} object for the requested ID, or <code>null</code> if it is not part of this event.
	 * @param id the id of the graphic to obtain
	 * @return {@link CyCustomGraphics} - the graphics associated with the given id.
	 */
	public CyCustomGraphics<CustomGraphicLayer> getGraphicForID(String id)
	{
		return newgraphics.get(id);
	}
	
}
