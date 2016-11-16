package idare.imagenode.Interfaces.DataSets;

import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.Properties.Localisation;
import idare.imagenode.Properties.IMAGENODEPROPERTIES.LayoutStyle;
import idare.imagenode.Properties.Localisation.Position;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * A {@link DataContainer} is a class that is able to provide a specific ContainerLayout for a node. 
 * @author Thomas Pfau
 *
 */
public interface DataContainer {
	
	/**
	 * A container should provide information on its size
	 * This dimension is assumed to simultaneously represent the number of items (i.e. Dimension.x * Dimension.y = Number of items
	 * The maximal dimension allowed for a non flexible container is 10*10 in the center and 10*6 in the edges
	 * For a flexible container it is assumed, that the container can be rescaled and thus a maximal item count of 100 is allowed for center and 60 for edge containers  
	 * @return the minimal Size of this container (in units)
	 */
	public Rectangle getMinimalSize();

//	/**
//	 * Get the localisation preferences for this container (i.e whether to be placed on the EDGE, or in the CENTER, See {@link Position}, and whether it is a flexible container or not).
//	 * @return the preferred {@link Localisation} of this Container
//	 */
//	public Localisation getLocalisationPreference();
	
	/**
	 * Get the source Dataset this Container was build from.
	 * @return the source dataset of this container
	 */
	public DataSet getDataSet();
	/**
	 * Get the preferred size (as a rectangle with Integer width and height, based on the available area provided
	 * The resulting {@link Rectangle} width must be smaller or equal to the provided {@link Rectangle}. The same applies to 
	 * its height.  
	 * @param availablearea The area available to generate the layout
	 * @param style the localisation (either CENTER or EDGE).
	 * @return the preferred size within the provided rectangle
	 */
	public Dimension getPreferredSize(Dimension availablearea, LayoutStyle style );
	/**
	 * Get the {@link NodeData} associated with this Container 
	 * @return the {@link NodeData} for this container
	 */
	public NodeData getData();
	/**
	 * Create a new Layout for this container.
	 * @return an uninitialized {@link ContainerLayout}
	 */
	public ContainerLayout createEmptyLayout();
}
