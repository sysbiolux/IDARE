package idare.imagenode.GUI.Legend.Utilities;

/**
 * This interface defines classes which allow to adjust their height when a given width is set. 
 * @author Thomas Pfau
 *
 */
public interface SizeAdaptableComponent {
	
	/**
	 * Adapt the width of the component to the provided visible width. 
	 * This does not necessarily indicate, that the component should have that preferred width, but it should 
	 * be informed, that the visible area has changed and adapt its layout accordingly.
	 * @param width the width to use
	 */
	public void setVisibleWidth(int width);
}
