package idare.metanode.internal.IO;

public class StringUtils {
	/**
	 * Test, whether a string is numeric (i.e. any decimal (without grouping symbols).
	 * @param str
	 * @return Whether the supplied string is a number or not
	 */
	public static boolean isNumeric(String str)
	{
	  return str.matches("-?\\d+(\\.\\d+)?([eE]-?\\d+)?");  //match a number with optional '-' and decimal.
	}
}
