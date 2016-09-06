package idare.imagenode.Interfaces.DataSetReaders.WorkBook.BasicImplementation;

import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARECell;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow;

import java.util.Iterator;

/**
 * This class provides a simple Iterator that can be used for all types of Workbooks that implement the IDARE interfaces 
 * Since it is likely necessary to provide this, it is directly supplied here.
 * @author Thomas Pfau
 *
 */
public class IDARECellIterator implements Iterator<IDARECell>{

	/**
	 * Cell iterator implementation to return the {@link IDARECell}s in the order they are in the {@link IDARERow}.
	 */
	Iterator<? extends IDARECell> iter;
			public IDARECellIterator(Iterator<? extends IDARECell> source)
			{
				iter = source;
			}
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public IDARECell next() {
				return iter.next();
			}
			
		
	
}
