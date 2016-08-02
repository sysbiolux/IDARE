package idare.metanode.internal.Debug;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import idare.metanode.internal.exceptions.debug.DebugException;

/**
 * Helper class to provide formatted Output for PrintFDebugging, indicating time, and origin of the printf.
 * @author Thomas Pfau
 *
 */
public class PrintFDebugger {

	private static boolean debug = true;
	
	public static void Debugging(Object obj, String Message)
	{
		try
		{
			throw new DebugException(Message, obj);
		}
		catch(DebugException e)
		{
			if(debug)
			{
			Calendar cal = Calendar.getInstance();
	        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");	        
			System.out.println(sdf.format(cal.getTime()) + ": "+ e.getObjClassame() + "(" + e.getStackTrace()[1].getLineNumber() + "):" + e.getMessage());
			}
		}
	}
	
	
}
