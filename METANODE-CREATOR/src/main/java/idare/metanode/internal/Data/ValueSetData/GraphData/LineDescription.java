package idare.metanode.internal.Data.ValueSetData.GraphData;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Line2D;

import javax.swing.JScrollPane;

import idare.metanode.Data.BasicDataTypes.ValueSetData.SetDataDescription;
import idare.metanode.Data.BasicDataTypes.ValueSetData.SetEntryDescriptionPane;
import idare.metanode.Data.BasicDataTypes.ValueSetData.SetEntryPanel;

public class LineDescription extends SetDataDescription {

	public LineDescription(JScrollPane Parent) {
		// TODO Auto-generated constructor stub
		super(Parent);
	}
	
	
	@Override
	public SetEntryDescriptionPane getDescriptionPane(Color color,
			String EntryName) {
		// TODO Auto-generated method stub
		return new LineEntryDescriptionPane(color, EntryName);
	}
	
	private class LineEntryDescriptionPane extends SetEntryDescriptionPane
	{

		public LineEntryDescriptionPane(Color entryColor,
				String descriptionString) {
			super(entryColor, descriptionString);
			// TODO Auto-generated constructor stub
		}

		@Override
		public SetEntryPanel getEntry(Color entrycolor) {
			// TODO Auto-generated method stub
			return new LineEntryPanel(entrycolor);
		}
		
	}
	
	private class LineEntryPanel extends SetEntryPanel
	{

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
