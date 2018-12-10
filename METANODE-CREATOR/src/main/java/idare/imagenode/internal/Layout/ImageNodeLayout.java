package idare.imagenode.internal.Layout;

import java.awt.Dimension;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Vector;

import org.apache.batik.svggen.SVGGraphics2D;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.imagenode.exceptions.layout.DimensionMismatchException;
import idare.imagenode.exceptions.layout.TooManyItemsException;
import idare.imagenode.exceptions.layout.WrongDatasetTypeException;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.DataManagement.Events.DataSetAboutToBeChangedListener;
import idare.imagenode.internal.DataManagement.Events.DataSetChangedEvent;
import idare.imagenode.internal.DataManagement.Events.DataSetsChangedEvent;
import idare.imagenode.internal.Layout.Automatic.AutomaticNodeLayout;
import idare.imagenode.internal.Layout.Automatic.ImageNodeContainer;

public interface ImageNodeLayout extends DataSetAboutToBeChangedListener {

	/**
	 * Check whether this {@link AutomaticNodeLayout} is valid. A Layout is valid, if its {@link ImageNodeContainer} contains 
	 * at least one {@link DataSet}. 
	 * Thus, by default a newly generated layout is invalid until a {@link DataSet} is added. 
	 * @return whether this layout is still valid (i.e. has contaniers to lay out)
	 */
	boolean isValid();

	/**
	 * Write all data that is relevant to restore this nodelayout to a {@link ObjectOutputStream}
	 * @param os The {@link ObjectOutputStream} to write the dlayout infromation to
	 * @throws IOException If errors occur with the given stream
	 */
	void writeLayout(ObjectOutputStream os) throws IOException;

	/**
	 * Read a layout object from an {@link ObjectInputStream} given the last object read (which is the first object of the layout)
	 * @param dsm the {@link DataSetManager} to get information about DataSets from.
	 * @param os The {@link ObjectInputStream} to read from
	 * @param currentobject The last object read in the {@link ObjectInputStream} provided (the first object of the nodelayout save
	 * @return true, if reading was successful, false if there was a non {@link IOException}.
	 * @throws IOException If errors occur with the given stream
	 */
	boolean readLayout(DataSetManager dsm, ObjectInputStream os, Object currentobject) throws IOException;

	/**
	 * Produce the layout based on the added DataSets.
	 * @throws TooManyItemsException If there are too many Items to layout
	 * @throws ContainerUnplaceableExcpetion If a container is not placeable
	 * @throws DimensionMismatchException If the given dimensions don't fit.
	 * @throws WrongDatasetTypeException IF the given Dataset does not it to the layout  
	 */
	void doLayout() throws TooManyItemsException, ContainerUnplaceableExcpetion, DimensionMismatchException, WrongDatasetTypeException;

	/** 
	 * Get the label for a specific {@link DataSet}  used in this layout;
	 * @param ds the {@link DataSet} to get the label for
	 * @return The String label for the supplied {@link DataSet}
	 */
	String getDataSetLabel(DataSetLink ds);

	/**
	 * Get the {@link ColorMap} associated with this {@link DataSet} in this {@link AutomaticNodeLayout}.
	 * @param ds - The requested Dataset
	 * @return the {@link ColorMap} associated with the {@link DataSet} in this {@link AutomaticNodeLayout}
	 */
	ColorMap getColorsForDataSet(DataSetLink ds);

	/** 
	 * Get the DataSets used in this Layout in the order of labeling
	 * @return A {@link Vector} of {@link DataSet}s in the order they were added during the layout process 
	 */
	Vector<? extends DataSetLink> getDatasetsInOrder();

	/**
	 * Layout a specific node in a given context.
	 * @param datacollection The data to be used for drawing
	 * @param svg the {@link SVGGraphics2D} to draw in
	 */
	void layoutNode(Collection<NodeData> datacollection, SVGGraphics2D svg);

	/**
	 * Lay out the legend for a specific set of node data
	 * Always needs to also layout the ID of the node in addition to the image
	 * @param datacollection The data to be used for drawing
	 * @param svg the {@link SVGGraphics2D} to draw in
	 */
	void layoutLegendNode(Collection<NodeData> datacollection, SVGGraphics2D svg);

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.DataManagement.Events.DataSetAboutToBeChangedListener#datasetChanged(idare.imagenode.internal.DataManagement.Events.DataSetChangedEvent)
	 */
	void datasetChanged(DataSetChangedEvent e);

	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.DataManagement.Events.DataSetAboutToBeChangedListener#datasetsChanged(idare.imagenode.internal.DataManagement.Events.DataSetsChangedEvent)
	 */
	void datasetsChanged(DataSetsChangedEvent e);

	/**
	 * Set the image height (only the image not the label);
	 * @param height
	 */
	void setImageHeight(int height);
	
	/**
	 * Get the image height (only the image not the label);
	 * @return The height of the image
	 */
	int getImageHeight();
	
	/**
	 * Set the image  width (only the image not the label);
	 * @param width
	 */
	void setImageWidth(int width);
	
	/**
	 * Get the image width (only the image not the label);
	 * @return The width of the image
	 */
	int getImageWidth();
	
	/**
	 * Set the image dimensions (only the image not the label);
	 * @param dim The dimensions of the image 
	 */
	void setImageDimensions(Dimension dim);
	
	/**
	 * Get the image dimensions (only the image not the label);
	 * @return The dimensions of the image 
	 */
	Dimension getImageDimensions();
	
	/**
	 * Set the label height 
	 * @param height The height of the label 
	 */
	void setLabelHeight(int height);
	
	/**
	 * Get the label height 
	 * @return The height of the label 
	 */
	int getLabelHeight();
	
	/**
	 * Whether the layout in question does print a label on image nodes.
	 * @return Whether the layout prints a label on the image nodes.
	 */
	boolean printsLabel();
}