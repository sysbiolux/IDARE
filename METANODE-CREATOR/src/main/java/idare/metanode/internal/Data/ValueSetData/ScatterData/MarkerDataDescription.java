package idare.metanode.internal.Data.ValueSetData.ScatterData;

import idare.metanode.internal.ColorManagement.ColorMap;
import idare.metanode.internal.Data.ValueSetData.SetDataDescription;
import idare.metanode.internal.Data.ValueSetData.SetEntryDescriptionPane;
import idare.metanode.internal.Data.ValueSetData.SetEntryPanel;
import idare.metanode.internal.Data.ValueSetData.ValueSetDataSet;
import idare.metanode.internal.Debug.PrintFDebugger;
import idare.metanode.internal.GUI.Legend.Utilities.LegendSizeListener;
import idare.metanode.internal.GUI.Legend.Utilities.SizeAdaptableComponent;
import idare.metanode.internal.Interfaces.DataSet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;
/**
 * Class to Generate Descriptions for Lines 
 * @author Thomas Pfau
 *
 */
public class MarkerDataDescription extends SetDataDescription {
	private static final long serialVersionUID = 1001;	

	/**
	 * Create a new LineDataDescription, which is contained in a JScrollPane.
	 * The ViewPort of the JScrollPane has to show the visible part of this Itemdescription. 
	 * @param Parent
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
	private class ScatterDescriptionPane extends SetEntryDescriptionPane
	{
		private static final long serialVersionUID = 1001;
		//private String descriptionString;
		public ScatterDescriptionPane(Color LineColor, String DescriptionString)
		{
			super(LineColor,DescriptionString);
			//We have to set the id after initialization, as otherwise it wont be set since the getEntry happens during the constructor of the superclass, 
			//were this class does not yet have its descriptionstring. 
			((PathPanel)entry).id = DescriptionString;
//			PrintFDebugger.Debugging(this, "The description string for the Pane is " + descriptionString);
		}
		@Override
		public SetEntryPanel getEntry(Color entrycolor) {
			// TODO Auto-generated method stub
			
			return new PathPanel(entrycolor);
		}
	}
	/**
	 * A Single Panel for a Line, that only draws the line shape.
	 * @author Thomas Pfau
	 *
	 */
	private class PathPanel extends SetEntryPanel
	{
		private static final long serialVersionUID = 1001;		
		protected String id;
		
		/**
		 * A PathPanel needs both the ID (to generate the correct marker and the color to color it correctly.
		 * @param ShapeColor
		 * @param id
		 */
		public PathPanel(Color ShapeColor)
		{			
			super(ShapeColor);			
			this.id = null;
			//PrintFDebugger.Debugging(this, "The description string for the Panel is " + id);

		}
		@Override
		public Shape getShape(double xpos, double ypos, double width,
				double height) {
			// TODO Auto-generated method stub			
			Point2D center = new Point2D.Double(xpos + 0.5*width,ypos + 0.5*height);
			PrintFDebugger.Debugging(this, "Trying to obtain Marker for entry " + id + " at position " + center);
			return ScatterContainerLayout.createMarker(id, center , Math.min(width,height));
		}


	}

	@Override
	public SetEntryDescriptionPane getDescriptionPane(Color color,
			String EntryName) {
		// TODO Auto-generated method stub
		PrintFDebugger.Debugging(this, "Trying to obtain Scatterdescription for Color " + color + " and name " + EntryName);
		return new ScatterDescriptionPane(color, EntryName);
	}
}
