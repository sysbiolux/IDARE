package idare.imagenode.internal.Data.MultiArray.IndividualGraph;

import idare.imagenode.Data.BasicDataTypes.MultiArrayData.MultiArrayDescription;
import idare.imagenode.Data.BasicDataTypes.MultiArrayData.MultiArrayEntryDescriptionPane;
import idare.imagenode.Data.BasicDataTypes.MultiArrayData.MultiArrayEntryPanel;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Line2D;

import javax.swing.JScrollPane;

public class LineDescription extends MultiArrayDescription {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LineDescription(JScrollPane Parent) {
		// TODO Auto-generated constructor stub
		super(Parent);
	}
	
	
	@Override
	public MultiArrayEntryDescriptionPane getDescriptionPane(Color color,
			String EntryName) {
		// TODO Auto-generated method stub
		return new LineEntryDescriptionPane(color, EntryName);
	}
	
	private class LineEntryDescriptionPane extends MultiArrayEntryDescriptionPane
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public LineEntryDescriptionPane(Color entryColor,
				String descriptionString) {
			super(entryColor, descriptionString);
			// TODO Auto-generated constructor stub
		}

		@Override
		public MultiArrayEntryPanel getEntry(Color entrycolor) {
			// TODO Auto-generated method stub
			return new LineEntryPanel(entrycolor);
		}
		
	}
	
	private class LineEntryPanel extends MultiArrayEntryPanel
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public LineEntryPanel(Color ShapeColor) {
			super(ShapeColor);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Shape getShape(double xpos, double ypos, double width,
				double height) {
			Line2D shape = new Line2D.Double(xpos,ypos + 0.5*height,xpos+width,ypos+0.5*height); 
			return shape;
		}
		
	}

}
