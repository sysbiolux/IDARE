package idare.metanode.internal.Data.ValueSetData;

import idare.ThirdParty.TextPaneEditorKit;
import idare.metanode.internal.ColorManagement.ColorMap;
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
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Line2D;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
/**
 * Class to Generate Descriptions for Lines 
 * @author Thomas Pfau
 *
 */
public abstract class SetDataDescription extends JPanel implements SizeAdaptableComponent {
	private static final long serialVersionUID = 1001;
	private Vector<SetEntryDescriptionPane> Itemdescriptions;

	/**
	 * Create a new LineDataDescription, which is contained in a JScrollPane.
	 * The ViewPort of the JScrollPane has to show the visible part of this Itemdescription. 
	 * @param Parent
	 */
	public SetDataDescription(JScrollPane Parent)
	{
		Parent.addComponentListener(new AreaAdapter(this));
	}
		
	/**
	 * Set up the data description using a specific {@link ColorMap} to obtain the line colors and 
	 * the respective {@link DataSet} to obtain the Line names. 
	 * @param currentdata
	 * @param map
	 */
	public void setupItemDescription(ValueSetDataSet currentdata,ColorMap map) {
		//First, get the size of the maximal Item to determine the number of rows.			
		this.setBackground(Color.white);					
		//			FontMetrics fm = this.getFontMetrics(new Font(Font.MONOSPACED,Font.PLAIN,20));
		Itemdescriptions = new Vector<SetEntryDescriptionPane>();		
		for(String LineName : currentdata.getSetNames())
		{
			SetEntryDescriptionPane pane = getDescriptionPane(map.getColor(LineName),LineName);															
			Itemdescriptions.add(pane);			
		}
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		for(SetEntryDescriptionPane pane : Itemdescriptions)
		{
			//first item left aligned, second item right aligned
			add(pane);
			add(Box.createVerticalGlue());				
		}


	}
	
	/**
	 * Get a Description Pane for the current Entry Description, with the given color and given ID
	 * @param color - the Color for the description
	 * @param EntryName - The name of the entry.
	 * @return
	 */
	public abstract SetEntryDescriptionPane getDescriptionPane(Color color, String EntryName);


	/**
	 * A Pane that visualises a Line and its description.
	 * @author Thomas Pfau
	 *
	 */
/*	private class LineDescriptionPane extends JPanel
	{
		private static final long serialVersionUID = 1001;
		private JPanel Line;
		private JTextPane Description;
		private int minimaldescriptionwidth = 260;
		private int minimalLineWidth = 40;			
		public LineDescriptionPane(Color LineColor, String DescriptionString)
		{
			this.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.weightx = 0.1;
			gbc.weighty = 1;
			gbc.fill = GridBagConstraints.BOTH;
			setBackground(Color.WHITE);
			Description = new JTextPane();
			DefaultCaret caret = (DefaultCaret)Description.getCaret();
			caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
			TextPaneEditorKit tek = new TextPaneEditorKit();
			Description.setEditorKit(tek);
			//SimpleAttributeSet attribs = new SimpleAttributeSet();
			//StyleConstants.setAlignment(attribs,StyleConstants.ALIGN_CENTER);			
			Description.setText(DescriptionString);				
			Description.setBackground(Color.white);				
			Description.setFont(Description.getFont().deriveFont(20f));				
			//StyledDocument doc = (StyledDocument)Description.getDocument();
			//doc.setParagraphAttributes(0, doc.getLength()-1, attribs, false);
			Line = new LinePanel(LineColor);
			Line.setBackground(Color.white);				
			gbc.gridx = 0;
			gbc.gridy = 0;
			add(Line,gbc);
			gbc.weightx = 1;
			gbc.gridx = 1;
			add(Description,gbc);
		}
		/**
		 * Update the preferredsize of this Line according to a provided width.
		 * 
		 * @param width the width to which adjust the height of the Component.
		 */
		/*public void updatePreferredSize(int width)
		{		
			if(width < 300)
			{
				Line.setPreferredSize(new Dimension(minimalLineWidth,20));
				Description.setPreferredSize(new Dimension(minimaldescriptionwidth,getDescriptionHeight(minimaldescriptionwidth)));
				//					PrintFDebugger.Debugging(this, "Setting Size of Description to " + minimaldescriptionwidth + "/" +getDescriptionHeight(minimaldescriptionwidth) + "and Line to " + (minimalLineWidth) + "/"	+20);

			}
			else
			{
				int DescriptionWidth = (int) (width * (double)minimaldescriptionwidth / (300));					
				Line.setPreferredSize(new Dimension(width - DescriptionWidth,20));
				Description.setPreferredSize(new Dimension(DescriptionWidth,getDescriptionHeight(DescriptionWidth)));
				//					PrintFDebugger.Debugging(this, "Setting Size of Description to " + DescriptionWidth + "/" +getDescriptionHeight(DescriptionWidth) + "and Line to " + (width - DescriptionWidth) + "/"	+20);
			}
			Description.invalidate();
			Line.invalidate();
			revalidate();
			repaint();

		}
		/**
		 * Get the description height of this LineDescriptions textual description.
		 * @param width - the width to obtain a height for.
		 * @return - the height for the given width.
		 */
		/*private int getDescriptionHeight(int width)
		{
			JTextPane dummyPane=new JTextPane();
			dummyPane.setFont(Description.getFont());

			dummyPane.setSize(Math.max(minimaldescriptionwidth,width),Short.MAX_VALUE);
			dummyPane.setText(Description.getText());		        		        			
			int preferredHeight = dummyPane.getPreferredSize().height;
			return preferredHeight;
		}
	}
	/**
	 * A Single Panel for a Line, that only draws the line shape.
	 * @author Thomas Pfau
	 *
	 *
	private class LinePanel extends JPanel
	{
		private static final long serialVersionUID = 1001;
		private Color ShapeColor;

		public LinePanel(Color ShapeColor)
		{
			super();
			this.ShapeColor = ShapeColor;

		}
		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);

			Graphics2D g2d = (Graphics2D) g;
			Paint cpaint = g2d.getPaint();
			Stroke cstroke = g2d.getStroke();
			Color ccolor= g2d.getColor();
			Dimension size = getSize();
			int ext = Math.min(size.height - 2, size.width - 2);
			int xpos = size.width - ext - 1;
			int ypos = size.height/2;
			g2d.setPaint(ShapeColor);
			g2d.setColor(ShapeColor);
			g2d.setStroke(new BasicStroke(3));				
			Line2D shape = new Line2D.Double(Math.max(xpos+1,0),ypos,Math.max(xpos+ext -2,0),ypos); 
			g2d.draw(shape);
			g2d.setStroke(cstroke);
			g2d.setColor(ccolor);
			g2d.setPaint(cpaint);
		}


	}*/


	/**
	 * A Component adapter that updates the sizes to adjust to a Viewport size of a scrollpane. 
	 * @author Thomas Pfau
	 *
	 */
	private class AreaAdapter extends ComponentAdapter implements LegendSizeListener
	{
		private SizeAdaptableComponent comp;

		public AreaAdapter(SizeAdaptableComponent comp)
		{
			this.comp = comp;
		}
		@Override
		public void componentResized(ComponentEvent e) {				
			int cwidth = 0;
			JScrollPane scroller = (JScrollPane)e.getSource();
			cwidth = scroller.getViewport().getWidth()-2;
			//				PrintFDebugger.Debugging(this, "Viewport of scroller has a width of " + cwidth);
			comp.setVisibleWidth(cwidth);
		}
	}

	@Override
	public void setVisibleWidth(int width) {
		// TODO Auto-generated method stub
		for(SetEntryDescriptionPane line : Itemdescriptions)
		{
			line.updatePreferredSize(width);
		}
	}
}
