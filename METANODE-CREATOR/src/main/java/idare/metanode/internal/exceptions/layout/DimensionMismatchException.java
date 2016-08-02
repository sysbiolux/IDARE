package idare.metanode.internal.exceptions.layout;

/**
 * Exception indicating, that a Container was too larger and could not be placed.
 * @author Thomas Pfau
 *
 */
public class DimensionMismatchException extends Exception{

	public DimensionMismatchException(String Message)
	{
		super(Message);
	}
	
}
