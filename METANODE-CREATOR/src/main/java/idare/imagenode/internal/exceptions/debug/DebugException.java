package idare.imagenode.internal.exceptions.debug;

import idare.imagenode.internal.Debug.PrintFDebugger;

/**
 * Class to be used with the {@link PrintFDebugger}
 * @author Thomas Pfau
 *
 */
public class DebugException extends Exception
{
	
	Object source;		
	public DebugException(String Message, Object obj)
	{
		super(Message);
		source = obj;
	}
	/**
	 * Get the class name of the Object
	 * @return
	 */
	public String getObjClassame()
	{
		return source.getClass().getSimpleName();
	}
}