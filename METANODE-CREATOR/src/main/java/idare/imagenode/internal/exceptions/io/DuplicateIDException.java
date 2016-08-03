package idare.imagenode.internal.exceptions.io;

/**
 * Exception indicating that there was a duplicate ID.
 * @author Thomas Pfau
 *
 */
public class DuplicateIDException extends Exception{

	String duplicateID;
	public DuplicateIDException(String ID, String Information) {
		// TODO Auto-generated constructor stub
		super("Duplicate ID detected " + ID,new Throwable(Information) );
		duplicateID = ID;		
	}
		
}
