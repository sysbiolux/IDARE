package idare.metanode.internal.exceptions.layout;

/**
 * Exception indicating that a container was too large and could not be placed.
 * @author Thomas Pfau
 *
 */
public class TooManyItemsException extends Exception{
	
	public TooManyItemsException(String Message)
	{
		super(Message);
	}

}
