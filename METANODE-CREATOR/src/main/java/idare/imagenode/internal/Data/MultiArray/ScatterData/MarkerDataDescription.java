package idare.imagenode.internal.Data.MultiArray.ScatterData;

import idare.imagenode.Data.BasicDataTypes.MultiArrayData.MultiArrayDescription;
import idare.imagenode.Data.BasicDataTypes.MultiArrayData.MultiArrayEntryDescriptionPane;
import idare.imagenode.Data.BasicDataTypes.MultiArrayData.MultiArrayEntryPanel;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Point2D;

import javax.swing.JScrollPane;
/**
 * Class to Generate Descriptions for Lines 
 * @author Thomas Pfau
 *
 */
public class MarkerDataDescription extends MultiArrayDescription {
	private static final long serialVersionUID = 1001;	

	/**
	 * Create a new LineDataDescription, which is contained in a JScrollPane.
	 * The ViewPort of the JScrollPane has to show the visible part of this Itemdescription. 
	 * @param Parent the parent scroll pane
	 */
	public MarkerDataDescription(JScrollPane Parent)
	{
		super(Parent);
	}


	/**
	 * A Pane that visualises a Line and its description.
	 * @author Thomas Pfau
	 *
	 */
	private class ScatterDescriptionPane extends MultiArrayEntryDescriptionPane
	{
		private static final long serialVersionUID = 1001;
		//private String descriptionString;
		public ScatterDescriptionPane(Color LineColor, String DescriptionString)
		{
			super(LineColor,DescriptionString);
			//We have to set the id after initialization, as otherwise it wont be set since the getEntry happens during the constructor of the superclass, 
			//were this class does not yet have its descriptionstring. 
			((PathPanel)entry).id = DescriptionString;
		}
		@Override
		public MultiArrayEntryPanel getEntry(Color entrycolor) {
			// TODO Auto-generated method stub
			
			return new PathPanel(entrycolor);
		}
	}
	/**
	 * A Single Panel for a Line, that only draws the line shape.
	 * @author Thomas Pfau
	 *
	 */
	private class PathPanel extends MultiArrayEntryPanel
	{
		private static final long serialVersionUID = 1001;		
		protected String id;
		
		/**
		 * A PathPanel needs both the ID (to generate the correct marker and the color to color it correctly.
		 * @param ShapeColor The Color of the shape used by this marker.
		 */
		public PathPanel(Color ShapeColor)
		{			
			super(ShapeColor);			
			this.id = null;

		}
		@Override
		public Shape getShape(double xpos, double ypos, double width,
				double height) {
			// TODO Auto-generated method stub			
			Point2D center = new Point2D.Double(xpos + 0.5*width,ypos + 0.5*height);
			return ScatterContainerLayout.createMarker(id, center , Math.min(width,height));
		}


	}

	@Override
	public MultiArrayEntryDescriptionPane getDescriptionPane(Color color,
			String EntryName) {
		// TODO Auto-generated method stub
		return new ScatterDescriptionPane(color, EntryName);
	}
}
