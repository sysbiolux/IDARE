package idare.imagenode.internal.Debug;

import idare.imagenode.internal.exceptions.debug.DebugException;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Helper class to provide formatted Output for PrintFDebugging, indicating time, and origin of the printf.
 * @author Thomas Pfau
 *
 */
public class PrintFDebugger {

	//private static boolean debug = true;
	
	public static void Debugging(Object obj, String Message)
	{
		try
		{
			throw new DebugException(Message, obj);
		}
		catch(DebugException e)
		{
			Calendar cal = Calendar.getInstance();
	        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");	        
			System.out.println(sdf.format(cal.getTime()) + "(" + obj.getClass().getSimpleName() + " " + e.getStackTrace()[1].getLineNumber()  + ":" + Message  );
		}
	}
	
	public static void Trace(Object obj)
	{
		try
		{
			throw new DebugException("", obj);
		}
		catch(DebugException e)
		{
			Calendar cal = Calendar.getInstance();
	        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");	        
			System.out.println(sdf.format(cal.getTime()) + "(" + obj.getClass().getSimpleName() + " " + e.getStackTrace()[1].getLineNumber() + ":"  );
			e.printStackTrace(System.out);
		}
	}
	
}
