package idare.imagenode.internal.Layout.Manual.Utilities;

import java.awt.Rectangle;

/**
 * This is a comparable rectangle where the order is further left to right and top to bottom.
 * @author thomas
 *
 */
public class LayoutArea extends Rectangle implements Comparable<LayoutArea>
{

	
	public LayoutArea(Rectangle area) {
		// TODO Auto-generated constructor stub
		super(area);
	}

	@Override
	public int compareTo(LayoutArea o) {
		if(o.x < x)
		{
			return -1;
		}
		else if (o.x > x) {
			return 1;
		}
		if(o.y < y)
		{
			return -1;
		}
		else if (o.y > y) {
			return 1;
		}
		if(o.width < width)
		{
			return 1;
		}
		else if (o.width > width) {
			return -1;
		}
		if(o.height < height)
		{
			return 1;
		}
		else if (o.height> height) {
			return -1;
		}

		return 0;
	}

}