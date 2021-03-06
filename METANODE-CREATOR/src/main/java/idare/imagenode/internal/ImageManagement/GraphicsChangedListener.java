package idare.imagenode.internal.ImageManagement;


/**
 * A {@link GraphicsChangedListener} will listen to changes in the graphics controlled by the IDARE app.
 * @author Thomas Pfau
 *
 */
public interface GraphicsChangedListener  {

	/**
	 * Update according to the updated images provided in this event.
	 * @param e the {@link GraphicsChangedEvent} to process
	 */
	public void imageUpdated(GraphicsChangedEvent e);
}
