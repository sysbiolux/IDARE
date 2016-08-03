package idare.imagenode.internal.GUI.Legend.Utilities;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * A Listener that adapts a JTextPane according to the Scrollpane enclosing the pane. 
 * Without this listener, the TextPane would always try to assume the maximal extension, which would make the Scrollpane have an extremely large viewport.
 * @author Thomas Pfau
 *
 */
public class TextPaneResizer extends ComponentAdapter implements LegendSizeListener
{
	JTextPane pane;
	int minsize;
	int offset;
	/**
	 * Default constructor with a provided minimal width, an offset to adjust the width coming in and the Pane to resize.
	 * @param pane
	 * @param minsize
	 * @param offset
	 */
	public TextPaneResizer(JTextPane pane, int minsize, int offset)
	{
		this.pane = pane;
		this.minsize = minsize;
		this.offset = offset;
	}
	/**
	 * Default parameters will be used for the given pane (300 width, 2 offset)
	 * @param pane
	 */
	public TextPaneResizer(JTextPane pane)
	{
		this(pane, 300, 2);
	}
	
	@Override
	public void componentResized(ComponentEvent e) {
		Dimension dim = e.getComponent().getSize();
		int cwidth = 0;
		if(e.getSource()  instanceof JScrollPane) {
			JScrollPane scroller = (JScrollPane)e.getSource();
			cwidth = scroller.getViewport().getWidth()-offset;	
			
		}
		else
		{
			cwidth = dim.width - offset;;
		}
		/**
		 * Code Snippet obtained from java-sl.com/tip_text_height_measuring.html
		 */
		JTextPane dummyPane=new JTextPane();
		dummyPane.setFont(pane.getFont());
		
		dummyPane.setSize(Math.max(minsize,cwidth),Short.MAX_VALUE);
		dummyPane.setText(pane.getText());		        		        			
		int preferredHeight = dummyPane.getPreferredSize().height;
		pane.setPreferredSize(new Dimension(Math.max(minsize,cwidth),preferredHeight));
		pane.revalidate();
	}

}