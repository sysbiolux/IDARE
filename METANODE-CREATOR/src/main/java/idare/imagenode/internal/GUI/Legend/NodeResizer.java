package idare.imagenode.internal.GUI.Legend;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JScrollPane;

import idare.imagenode.GUI.Legend.Utilities.LegendSizeListener;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;

/**
 * The NOde Resizer will resize the Canvas containing the layouted node. 
 * @author Thomas Pfau
 *
 */
public class NodeResizer extends ComponentAdapter implements LegendSizeListener
{
	NodeSVGCanvas canvas;
	public NodeResizer(NodeSVGCanvas canvas)
	{
		this.canvas = canvas;
	}
	@Override
	public void componentResized(ComponentEvent e)
	{
		JScrollPane source = (JScrollPane)e.getSource();
		int availablewidth = source.getViewport().getSize().width-1;
		//these are necessary to update the preferred size.
		canvas.setCanUpdatePreferredSize(true);
		if(availablewidth < IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH)
		{
			canvas.setPreferredSize(new Dimension(IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH, 
					(int)((IMAGENODEPROPERTIES.IMAGEHEIGHT + IMAGENODEPROPERTIES.LABELHEIGHT) * (double)IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH / IMAGENODEPROPERTIES.IMAGEWIDTH)));
		}
		else
		{
			int optwidth = Math.min(availablewidth,IMAGENODEPROPERTIES.IMAGEWIDTH); 
			canvas.setPreferredSize(new Dimension(optwidth,(int)((IMAGENODEPROPERTIES.IMAGEHEIGHT + IMAGENODEPROPERTIES.LABELHEIGHT)  * (double)optwidth / IMAGENODEPROPERTIES.IMAGEWIDTH)));
		}
		canvas.setCanUpdatePreferredSize(false);
		
		canvas.revalidate();
	}
}