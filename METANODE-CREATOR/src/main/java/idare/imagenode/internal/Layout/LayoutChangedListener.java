package idare.imagenode.internal.Layout;

public interface LayoutChangedListener {

	/**
	 * Indicate that the given layout has changed, and act accordingly.
	 * @param layout
	 */
	public void layoutsChanged(ImageNodeLayout layout);
	
}
