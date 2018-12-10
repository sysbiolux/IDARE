package idare.imagenode.Utilities;

import static org.junit.Assert.assertEquals;

import java.util.Vector;

import org.junit.Test;

public class LayoutUtilTest {
	
	@Test
	public void testStringDistribution() {
		String[] stringsToTest = new String[]{"This is a splitable String","This isanonsplittablestring"};
		Vector<int[]> splitpos = new Vector<>();
	    LayoutUtils.StringDrawer drawer = new LayoutUtils.StringDrawer("FirstTestString");	
	}

}
