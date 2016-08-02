package idare.metanode.internal.GUI.Legend.Utilities;

import java.awt.Dimension;
/**
 * This interface defines classes which allow to adjust their height when a given width is set. 
 * @author Thomas Pfau
 *
 */
public interface SizeAdaptableComponent {
	
	/**
	 * Adapt the width of the component to the provided visible width. 
	 * This does not necessarily indicate, that the component should have that preferred width, but it should 
	 * be informed, that the visible area has changed and adapt its laout accordingly.
	 * @param width
	 */
	public void setVisibleWidth(int width);
}
