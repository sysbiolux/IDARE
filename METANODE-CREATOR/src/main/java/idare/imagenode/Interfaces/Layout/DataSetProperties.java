package idare.imagenode.Interfaces.Layout;

import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Plugin.IDAREService;
import idare.imagenode.Properties.Localisation.Position;
import idare.imagenode.internal.ColorManagement.ColorMap;
import idare.imagenode.internal.exceptions.io.WrongFormat;

import java.io.Serializable;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.w3c.tools.resources.serialization.Serializer;

/**
 * Datasetproperties allow the use of a specific class of {@link DataSet}s for multiple different layouts. 
 * The cotain information about localisation preferences and can provide different {@link DataContainer}s (and thus layouts),
 * for a single Class of DataSets. 
 * @author Thomas Pfau
 *
 */
public abstract class DataSetProperties implements IDAREService,Serializable{

	
	/**
	 * Get the preferred Localisation {@link Position}.CENTER, {@link Position}.EDGE, {@link Position}.FREE of this dataset.
	 * @return the preferred Localisation
	 */
	public abstract Position getLocalisationPreference();
	/**
	 * Get information whether this dataset can be layouted flexibly or whether it has to obay the precisoe dimensions provided.
	 * @return whether this container has a flexible layout.
	 */
	public abstract boolean getItemFlexibility();
	/**
	 * Create a new Container for a specific {@link DataSet} and a given {@link NodeData}.
	 * @param origin
	 * @param data
	 * @return A new container for the given {@link DataSet} and {@link NodeData}
	 */
	public abstract DataContainer newContainerInstance(DataSet origin, NodeData data);
	/**
	 * Generate A DataContainer for a specific {@link NodeData} object (obtaining the {@link DataSet} from there.
	 * @param data
	 * @return an empty {@link DataContainer} for the given NodeData entry . 
	 */
	public abstract DataContainer newContainerForData(NodeData data);
	/**
	 * Get the type of DataSet (this can be used for display purposes
	 * @return the type name of these properties
	 */
	public abstract String getTypeName();
	/**
	 * Test, whether the provided DataSet is valid to be used with these properties. 
	 * @param set
	 * @throws WrongFormat
	 */
	public abstract void testValidity(DataSet set) throws WrongFormat;
	
	/**
	 * Plot the Legend for this Dataset.
	 * @param Legend - The Legend the returned Panel will be added to (to listen to resize events.
	 * @param DataSetLabel - the Label of the DataSet these properties are used for
	 * @param map - the colormap used for the corresponding dataset
	 * @param set - the dataset for which to generate a DescriptionPane
	 * @return - a JPanel that contains descriptive information about the provided dataset and its visualisation.
	 */
	public abstract JPanel getDataSetDescriptionPane(JScrollPane Legend, String DataSetLabel, ColorMap map, DataSet set);
	
	
	/**
	 * Get the DataSet class Types these Properties are supposed to work with.
	 * @return All Classes these properties can be used for.
	 */
	public abstract Collection<Class<? extends DataSet>> getWorkingClassTypes();
	
	/**
	 * Two Datasetproperties are equal, if they have the same Type name.
	 */
	public boolean equals(Object o)
	{
		if(o == null)
		{
			return false;
		}

		if(o instanceof DataSetProperties)
		{			
			return getTypeName().equals(((DataSetProperties) o).getTypeName());
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Override the ToString Method
	 */
	@Override
	public String toString()
	{
		return getTypeName();
	}
}
