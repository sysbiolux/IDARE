package idare.imagenode.internal.GUI.Legend;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JScrollPane;

import idare.imagenode.GUI.Legend.Utilities.LegendSizeListener;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.internal.Layout.ImageNodeLayout;

/**
 * The NOde Resizer will resize the Canvas containing the layouted node. 
 * @author Thomas Pfau
 *
 */
public class NodeResizer extends ComponentAdapter implements LegendSizeListener
{
	NodeSVGCanvas canvas;
	ImageNodeLayout layout;
	public NodeResizer(NodeSVGCanvas canvas, ImageNodeLayout layout)
	{
		this.canvas = canvas;
		this.layout = layout;
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
					(int)((layout.getImageHeight()+ layout.getLabelHeight()) * (double)IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH / layout.getImageWidth())));
		}
		else
		{
			int optwidth = Math.min(availablewidth,IMAGENODEPROPERTIES.IMAGEWIDTH); 
			canvas.setPreferredSize(new Dimension(optwidth,(int)((layout.getImageHeight() + layout.getLabelHeight())  * (double)optwidth / layout.getImageWidth())));
		}
		canvas.setCanUpdatePreferredSize(false);
		
		canvas.revalidate();
	}
}