package idare.imagenode.Data.BasicDataTypes.ArrayData;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

public class ArrayDataSetTest {

	
	public ArrayDataSet createTestSet()
	{
		Properties props = new Properties();
		props = ArrayDataSet.getDefaultProperties();
		ArrayDataSet testSet = new ArrayDataSet("TestSet");		
		return testSet;
	}
}
