package idare.imagenode.internal.Data.MultiArray.ScatterData;

public class SmallScatterProperties extends ScatterDataSetProperties {

	private static final long serialVersionUID = 1L;

	@Override
	protected int getLabelSize() {
		// TODO Auto-generated method stub
		return 8;
	}

	@Override
	public String getTypeName() {
		// TODO Auto-generated method stub
		return super.getTypeName() +  " - Small Items";
	}

	public String toString()
	{
		return super.toString() +  " - Small Items";
	}
	
}
