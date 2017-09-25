package idare.imagenode.internal.GUI.Legend;

import java.awt.Dimension;

import org.apache.batik.swing.JSVGCanvas;

/**
 * This class is mainly a hack to only allow preferredsize changes when they are requested by the
 * IDAREapp. The Original {@link JSVGCanvas} fires an awt event upon construction, that is only processed at some later stage, which updates the preferredsize and strongly interferes with initial layouting
 * Not sure, whether this is fixed in newer BATIK version, but those newer versions did not work for me..
 * 
 * @author Thomas Pfau
 *
 */
public class NodeSVGCanvas extends JSVGCanvas
{
	
	private boolean canupdatepreferredsize = false;
	/**
	 * define whether a preferresizeupdate is accepted
	 * @param can Whether the size can be updated.
	 */
	public void setCanUpdatePreferredSize(boolean can)
	{
		canupdatepreferredsize = can;
	}
	@Override
	public void setPreferredSize(Dimension dim)
	{
		if(canupdatepreferredsize)
			super.setPreferredSize(dim);			
	}
}
