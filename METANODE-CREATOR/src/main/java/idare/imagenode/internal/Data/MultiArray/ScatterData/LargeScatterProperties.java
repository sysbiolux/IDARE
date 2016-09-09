package idare.imagenode.internal.Data.MultiArray.ScatterData;

public class LargeScatterProperties extends ScatterDataSetProperties {

	private static final long serialVersionUID = 1L;

	@Override
	protected int getLabelSize() {
		// TODO Auto-generated method stub
		return 20;
	}

	@Override
	public String getTypeName() {
		// TODO Auto-generated method stub
		return super.getTypeName() +  " - Large Items";
	}

	public String toString()
	{
		return super.toString() +  " - Large Items";
	}
	
}
