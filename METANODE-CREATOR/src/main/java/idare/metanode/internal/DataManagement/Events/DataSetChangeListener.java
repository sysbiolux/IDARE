package idare.metanode.internal.DataManagement.Events;

import idare.metanode.Interfaces.DataSets.DataSet;
/**
 * A Listener of this kind will be informed if a Dataset HAS been changed.
 * @author Thomas Pfau
 *
 */
public interface DataSetChangeListener {

	/**
	 * Inform that there was a change in the {@link DataSet}s 
	 * @param e
	 */
	public void datasetChanged(DataSetChangedEvent e);

	/**
	 * Inform that a Collection of {@link DataSet}s changed.
	 * @param e
	 */
	public void datasetsChanged(DataSetsChangedEvent e);
}
