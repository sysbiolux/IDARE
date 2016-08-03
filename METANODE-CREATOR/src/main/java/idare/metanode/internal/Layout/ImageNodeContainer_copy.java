package idare.metanode.internal.Layout;

import idare.metanode.Interfaces.DataSets.DataContainer;
import idare.metanode.Interfaces.DataSets.DataSet;
import idare.metanode.Properties.Localisation;
import idare.metanode.internal.Layout.ImageBag.BAGPOSITON;
import idare.metanode.internal.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.metanode.internal.exceptions.layout.DimensionMismatchException;
import idare.metanode.internal.exceptions.layout.TooManyItemsException;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImageNodeContainer_copy extends JLabel{

	private final ImageBag leftBag;
	private final ImageBag rightBag;
	private final ImageBag centerBag;	
	private HashMap<Integer,Vector<DataContainer>> flexibleContainers = new HashMap<>();
	private HashMap<Integer,Vector<DataContainer>> fixedContainers = new HashMap<>();	
	private HashMap<Integer,Vector<DataContainer>> freecontainers = new HashMap<>();
	private Set<Integer> addedDataSets = new HashSet<Integer>();
	//private HashMap<Rectangle,Integer> Layout = new HashMap<>();
	
	int maxcenteritems;
	int maxedgeitems;
	int currentcenteritems = 0;
	int currentedgeitems = 0;
	int currentflexitem = 0;
	public ImageNodeContainer_copy() {
		// TODO Auto-generated constructor stub
		leftBag = new ImageBag(BAGPOSITON.LEFT, null);
		rightBag = new ImageBag(BAGPOSITON.RIGHT, null);
		centerBag = new ImageBag(BAGPOSITON.CENTER,null);
		maxcenteritems = centerBag.maxitemcount;
		maxedgeitems = leftBag.maxitemcount + leftBag.maxitemcount;
		Dimension ContainerSize = new Dimension(400,240);		
		this.setMaximumSize(ContainerSize);
		this.setMinimumSize(ContainerSize);
		this.setPreferredSize(ContainerSize);
	}
	
	private boolean canPlaceFreeContainers()
	{
		int freecenteritems = maxcenteritems - currentcenteritems;
		int freeedgeitems = maxedgeitems - currentedgeitems;
		
		for(Integer size : freecontainers.keySet())
		{
			for(int i = 0; i < freecontainers.get(size).size(); i++)
			{
				//look for the larger area
					if(freecenteritems > freeedgeitems)
					{
						freeedgeitems-= size;
						if(freeedgeitems < 0)
							return false;
					}
					else
					{
						freecenteritems-= size;
						if(freecenteritems < 0)
							return false;
					}
			}
		}
		return true;
				
	}
	
	public HashMap<ImageBag,HashMap<JPanel,DataContainer>> createLayout(JFrame frame ) throws ContainerUnplaceableExcpetion,DimensionMismatchException, TooManyItemsException
	{
		
		HashMap<ImageBag,HashMap<JPanel,DataContainer>> containerpositions = new HashMap<ImageBag,HashMap<JPanel,DataContainer>>();
		containerpositions.put(leftBag, new HashMap<JPanel,DataContainer>());
		containerpositions.put(rightBag, new HashMap<JPanel,DataContainer>());
		containerpositions.put(centerBag, new HashMap<JPanel,DataContainer>());
		//first distribute all fixed items
		Vector<Integer> sizes = new Vector<Integer>();
		sizes.addAll(fixedContainers.keySet());
		Collections.sort(sizes,Collections.reverseOrder());
		for(Integer i : sizes)
		{			
			for(DataContainer container : fixedContainers.get(i))
			{
//				System.out.println("Trying to add non flexible container with " + i +" items to position " + container.getLocalisation().pos  );
				if(container.getLocalisationPreference().pos == Localisation.Position.CENTER)
				{
					centerBag.addContainer(container.getDataSet());
				}
				else
				{
					if(leftBag.itemcount > rightBag.itemcount)
					{
						rightBag.addContainer(container.getDataSet());
					}
					else
					{
						leftBag.addContainer(container.getDataSet());
					}
				}
			}
		}
		//then distribute the flexible items
		sizes = new Vector<Integer>();
		sizes.addAll(flexibleContainers.keySet());
		Collections.sort(sizes,Collections.reverseOrder());
		for(Integer i : sizes)
		{			
			for(DataContainer container : flexibleContainers.get(i))
			{
			//	System.out.println("Trying to add flexible container with " + i +" items to position " + container.getLocalisation().pos  );
				if(container.getLocalisationPreference().pos == Localisation.Position.CENTER)
				{
					centerBag.addContainer(container.getDataSet());
				}
				else
				{
					if(leftBag.itemcount > rightBag.itemcount)
					{
						rightBag.addContainer(container.getDataSet());
					}
					else
					{
						leftBag.addContainer(container.getDataSet());
					}
				}
			}
		}

		//and finally the free items
		sizes = new Vector<Integer>();
		sizes.addAll(freecontainers.keySet());
		Collections.sort(sizes,Collections.reverseOrder());
		for(Integer i : sizes)
		{			
			for(DataContainer container : freecontainers.get(i))
			{
				//System.out.print("Trying to add freely placeable container with " + i +" items to position");
				if(centerBag.itemcount > rightBag.itemcount && centerBag.itemcount > leftBag.itemcount)
				{
					centerBag.addContainer(container.getDataSet());
					//System.out.print("to CENTER");
				}
				else
				{
					//System.out.print("to EDGE");
					if(leftBag.itemcount > rightBag.itemcount)
					{
						leftBag.addContainer(container.getDataSet());
					}
					else
					{
						rightBag.addContainer(container.getDataSet());

					}

				}
			}
		}
		
		containerpositions.get(leftBag).putAll(leftBag.createLayout());
		containerpositions.get(centerBag).putAll(centerBag.createLayout());
		containerpositions.get(rightBag).putAll(rightBag.createLayout());
		
		frame.setContentPane(this);
		//Container paintArea = frame.getContentPane();
		//paintArea.setLayout(new GridBagLayout());
		//this.removeAll();
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.weightx = leftBag.currentwidth;
		gbc.weighty = 1;
		//System.out.println("Placing left bag with weight " + gbc.weightx);
		add(leftBag,gbc);
		gbc.gridx = 1;
		gbc.weightx = centerBag.currentwidth;
		//System.out.println("Placing center bag with weight " + gbc.weightx);
		add(centerBag,gbc);
		gbc.gridx = 2;
		gbc.weightx = rightBag.currentwidth;
		//System.out.println("Placing right bag with weight " + gbc.weightx);
		add(rightBag,gbc);
		
		gbc = new GridBagConstraints();
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		//paintArea.add(this,gbc);
		frame.pack();
//		System.out.println(frame.getSize());
		//frame.setVisible(true);
		return containerpositions;
	}
	
	public void removeContainer(DataContainer container)
	{
		int id = container.getDataSet().getID();
		if(addedDataSets.contains(id))
		{
			
		}
	}
	/**
	 * Add A DataContainer. This does not check whether this dataset is already represented.
	 * We assume, that there are no duplicates for datasets. 
	 * @param container
	 * @throws TooManyItemsException
	 */
	public void addDataContainer(DataContainer container) throws TooManyItemsException
	{
		//Don't add duplicate datasets!
		if(addedDataSets.contains(container.getDataSet().getID())) return;
			
		Localisation loc = container.getLocalisationPreference();
		if(loc.Flexible)
		{
			Rectangle contsize = container.getMinimalSize();
			int items = contsize.width * contsize.height;
					
			if(!flexibleContainers.containsKey(items))
			{
				flexibleContainers.put(items,  new Vector<DataContainer>());
			}
			flexibleContainers.get(items).add(container);
			if(loc.pos == Localisation.Position.CENTER )
			{
				if(currentcenteritems + items > maxcenteritems || !canPlaceFreeContainers())
				{
					throw new TooManyItemsException("Too many items placed into the center area");
				}
				else
				{
					currentcenteritems += items;
				}
			}
			if(loc.pos == Localisation.Position.EDGE)
			{
				if(currentedgeitems + items > maxedgeitems || !canPlaceFreeContainers())
				{
					throw new TooManyItemsException("Too many items placed into the edge areas ");
				}
				else
				{
					currentedgeitems += items;
				}
			}
			if(loc.pos == Localisation.Position.FREE)
			{				
				if(!freecontainers.containsKey(items))
				{
					freecontainers.put(items,new Vector<DataContainer>());
				}
				freecontainers.get(items).add(container);
				if(!canPlaceFreeContainers())
				{
					freecontainers.get(items).remove(container);
					throw new TooManyItemsException("All areas are full");
				}
				
			}
		}
		else
		{
			Rectangle contsize = container.getMinimalSize();
			int items = contsize.width * contsize.height;
					
			if(!fixedContainers.containsKey(items))
			{
				fixedContainers.put(items,  new Vector<DataContainer>());
			}
			fixedContainers.get(items).add(container);
			if(loc.pos == Localisation.Position.CENTER )
			{
				if(currentcenteritems + items > maxcenteritems || !canPlaceFreeContainers())
				{
					throw new TooManyItemsException("Too many items placed into the center area");
				}
				else
				{
					currentcenteritems += items;
				}
			}
			if(loc.pos == Localisation.Position.EDGE)
			{
				if(currentedgeitems + items > maxedgeitems || !canPlaceFreeContainers())
				{
					throw new TooManyItemsException("Too many items placed into the edge areas ");
				}
				else
				{
					currentedgeitems += items;
				}
			}
			if(loc.pos == Localisation.Position.FREE)
			{				
				if(!freecontainers.containsKey(items))
				{
					freecontainers.put(items,new Vector<DataContainer>());
				}
				freecontainers.get(items).add(container);
				if(!canPlaceFreeContainers())
				{
					freecontainers.get(items).remove(container);
					throw new TooManyItemsException("All areas are full");
				}
				
			}
		}
		
	}
	
	public void addDataContainer(DataSet set) throws TooManyItemsException
	{
		//Don't add duplicate datasets!
		if(addedDataSets.contains(set.getID())) return;
		DataContainer container = set.getLayoutContainer();
		Localisation loc = container.getLocalisationPreference();
		if(loc.Flexible)
		{
			Rectangle contsize = container.getMinimalSize();
			int items = contsize.width * contsize.height;
					
			if(!flexibleContainers.containsKey(items))
			{
				flexibleContainers.put(items,  new Vector<DataContainer>());
			}
			flexibleContainers.get(items).add(container);
			if(loc.pos == Localisation.Position.CENTER )
			{
				if(currentcenteritems + items > maxcenteritems || !canPlaceFreeContainers())
				{
					throw new TooManyItemsException("Too many items placed into the center area");
				}
				else
				{
					currentcenteritems += items;
				}
			}
			if(loc.pos == Localisation.Position.EDGE)
			{
				if(currentedgeitems + items > maxedgeitems || !canPlaceFreeContainers())
				{
					throw new TooManyItemsException("Too many items placed into the edge areas ");
				}
				else
				{
					currentedgeitems += items;
				}
			}
			if(loc.pos == Localisation.Position.FREE)
			{				
				if(!freecontainers.containsKey(items))
				{
					freecontainers.put(items,new Vector<DataContainer>());
				}
				freecontainers.get(items).add(container);
				if(!canPlaceFreeContainers())
				{
					freecontainers.get(items).remove(container);
					throw new TooManyItemsException("All areas are full");
				}
				
			}
		}
		else
		{
			Rectangle contsize = container.getMinimalSize();
			int items = contsize.width * contsize.height;
					
			if(!fixedContainers.containsKey(items))
			{
				fixedContainers.put(items,  new Vector<DataContainer>());
			}
			fixedContainers.get(items).add(container);
			if(loc.pos == Localisation.Position.CENTER )
			{
				if(currentcenteritems + items > maxcenteritems || !canPlaceFreeContainers())
				{
					throw new TooManyItemsException("Too many items placed into the center area");
				}
				else
				{
					currentcenteritems += items;
				}
			}
			if(loc.pos == Localisation.Position.EDGE)
			{
				if(currentedgeitems + items > maxedgeitems || !canPlaceFreeContainers())
				{
					throw new TooManyItemsException("Too many items placed into the edge areas ");
				}
				else
				{
					currentedgeitems += items;
				}
			}
			if(loc.pos == Localisation.Position.FREE)
			{				
				if(!freecontainers.containsKey(items))
				{
					freecontainers.put(items,new Vector<DataContainer>());
				}
				freecontainers.get(items).add(container);
				if(!canPlaceFreeContainers())
				{
					freecontainers.get(items).remove(container);
					throw new TooManyItemsException("All areas are full");
				}
				
			}
		}
		
	}
}
