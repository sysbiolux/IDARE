package idare.NodeDuplicator.Internal;

import java.util.Collection;
import java.util.Vector;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import idare.Properties.IDAREProperties;

public class NodeRegistry implements RowsSetListener,TaskFactory{


	private boolean reactingToRowset = false;
	private CyEventHelper helper;
	private Vector<Long> UsedRowIDs;
	
	public NodeRegistry(CyServiceRegistrar reg) {
		// TODO Auto-generated constructor stub
		helper = reg.getService(CyEventHelper.class);
		UsedRowIDs = new Vector<Long>();
	}
	
	@Override
	public void handleEvent(RowsSetEvent arg0) {
		// TODO Auto-generated method stub
		if(!reactingToRowset)
		{
			reactingToRowset = true;
			for(RowSetRecord record : arg0.getPayloadCollection())
			{				
				if(record.getColumn().equals(CyNetwork.SELECTED))
				{
					//skip SELECTED statements
					continue;
				}
				//we have a duplicated entry				
				if(record.getRow().isSet(IDAREProperties.IDARE_DUPLICATED_NODE) && record.getRow().get(IDAREProperties.IDARE_DUPLICATED_NODE, Boolean.class))
				{
					Collection<CyRow> matchingRows = record.getRow().getTable().getMatchingRows(IDAREProperties.IDARE_ORIGINAL_NODE,record.getRow().get(IDAREProperties.IDARE_ORIGINAL_NODE, Long.class) );
					matchingRows.remove(record.getRow());
					for(CyRow row : matchingRows)
					{
						//change all others.
						if(row!=record.getRow())
						{
							//Get the class type.
							Class ColumnClass = record.getRow().getTable().getColumn(record.getColumn()).getListElementType() == null ? record.getRow().getTable().getColumn(record.getColumn()).getType() : record.getRow().getTable().getColumn(record.getColumn()).getListElementType();
							//And set it to all others.
							row.set(record.getColumn(), record.getRow().get(record.getColumn(),ColumnClass));
						}
						
					}
					

				}
			}			
			helper.flushPayloadEvents();
			reactingToRowset = false;
		}
	}
		
	public void deactivate()
	{
		reactingToRowset = true;
	}

	public void activate()
	{
		reactingToRowset = false;
	}

	@Override
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return true;
	}
}
