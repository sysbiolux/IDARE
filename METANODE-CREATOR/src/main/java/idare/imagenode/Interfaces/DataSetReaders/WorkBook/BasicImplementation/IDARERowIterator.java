package idare.imagenode.Interfaces.DataSetReaders.WorkBook.BasicImplementation;

import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARESheet;

import java.util.Iterator;

/**
 * This class provides a simple Iterator that can be used for all types of Workbooks that implement the IDARE interfaces 
 * Since it is likely necessary to provide this, it is directly supplied here.
 * @author Thomas Pfau
 *
 */
public class IDARERowIterator implements Iterator<IDARERow>{

	/**
	 * Row iterator implementation to return the {@link IDARERow}s in the order they are in the {@link IDARESheet}.
	 * @author Thomas Pfau
	 *
	 */
	Iterator<? extends IDARERow> iter;
			public IDARERowIterator(Iterator<? extends IDARERow> source)
			{
				iter = source;
			}
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public IDARERow next() {
				return iter.next();
			}
			
		
	
}
