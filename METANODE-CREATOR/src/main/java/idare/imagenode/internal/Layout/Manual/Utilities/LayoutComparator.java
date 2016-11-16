package idare.imagenode.internal.Layout.Manual.Utilities;

import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;

/**
 * LayoutComparator.
 * Two entries are equal (i.e. represent the same data), if they have the same bundle.
 * They can be sorted according to their position (which makes rearranging the labels easier). 
 * @author thomas
 *
 */
public class LayoutComparator implements Comparable<LayoutComparator>
{
	public LayoutArea area;
	public DataSetLayoutInfoBundle bundle;
	public ContainerLayout bundlelayout;
	public LayoutComparator(LayoutArea area, DataSetLayoutInfoBundle bundle)
	{
		this.area = area;
		this.bundle = bundle;
	}

	@Override
	public int compareTo(LayoutComparator other)
	{
		return area.compareTo(other.area);
	}

	@Override
	public int hashCode() {
		return bundle.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LayoutComparator other = (LayoutComparator) obj;
		return bundle == other.bundle;
	}		

}