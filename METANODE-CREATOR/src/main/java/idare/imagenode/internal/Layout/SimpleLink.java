package idare.imagenode.internal.Layout;

import idare.imagenode.Interfaces.DataSets.DataSet;
/**
 * A Simple Holder class for a Dataset.
 */
public class SimpleLink implements DataSetLink
{
	public DataSet ds;
	public int position;
	public SimpleLink(DataSet ds, int position) {
		this.ds = ds;
		this.position = position;
	}
	
	/*
	 * (non-Javadoc)
	 * @see idare.imagenode.internal.Layout.DataSetLink#getDataSet()
	 */
	public DataSet getDataSet()
	{
		return ds;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ds == null) ? 0 : ds.hashCode());
		result = prime * result + position;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleLink other = (SimpleLink) obj;
		if (ds == null) {
			if (other.ds != null)
				return false;
		} else if (ds != other.ds)
			return false;
		if (position != other.position)
			return false;
		return true;
	}

}