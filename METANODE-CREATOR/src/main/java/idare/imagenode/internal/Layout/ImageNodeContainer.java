package idare.imagenode.internal.Layout;

import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Properties.Localisation;
import idare.imagenode.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.imagenode.exceptions.layout.DimensionMismatchException;
import idare.imagenode.exceptions.layout.TooManyItemsException;
import idare.imagenode.internal.Layout.ImageBag.BAGPOSITON;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * An ImageNodeContainer encapsulates the parts of the Layout (right center and left bag) and can layout the 
 * respective bags according to their provided Panel-Container layouts.
 * @author Thomas Pfau
 *
 */
public class ImageNodeContainer extends JLabel{

	private ImageBag leftBag;
	private ImageBag rightBag;
	private ImageBag centerBag;	
	private HashMap<Integer,Vector<DataSet>> flexibleContainers = new HashMap<>();
	private HashMap<Integer,Vector<DataSet>> fixedContainers = new HashMap<>();	
	private HashMap<Integer,Vector<DataSet>> freecontainers = new HashMap<>();
	private Set<Integer> addedDataSets = new HashSet<Integer>();
	//private HashMap<Rectangle,Integer> Layout = new HashMap<>();
	
	int maxcenteritems;
	int maxedgeitems;
	int currentcenteritems = 0;
	int currentedgeitems = 0;
	int currentflexitem = 0;
	/**
	 * Generate a new, and empty imageContainer.
	 */
	public ImageNodeContainer() {
		// TODO Auto-generated constructor stub
		leftBag = new ImageBag(BAGPOSITON.LEFT, this);
		rightBag = new ImageBag(BAGPOSITON.RIGHT, this);
		centerBag = new ImageBag(BAGPOSITON.CENTER,this);
		maxcenteritems = centerBag.maxitemcount;
		maxedgeitems = leftBag.maxitemcount + leftBag.maxitemcount;
		Dimension ContainerSize = new Dimension(400,240);		
		this.setMaximumSize(ContainerSize);
		this.setMinimumSize(ContainerSize);
		this.setPreferredSize(ContainerSize);
		addedDataSets = new HashSet<Integer>();
	}
	
	/**
	 * Test whether there is still enough space left to place the freely moveable 
	 * Containers.
	 * @return
	 */
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
	/**
	 * Check whether this layoutcontainer is still valid.
	 * If the last dataset was removed, then it becomes invalid.
	 * @return whether this Container is valid wrt idare (i.e. has more than 0 {@link DataSet}s)
	 */
	public boolean isValidForIDARE()
	{
		return addedDataSets.size() > 0;
	}
	/**
	 * Create the layout for the contained {@link ImageBag}s and return the Panels generated from the {@link ImageBag}s (mapped to the {@link ImageBag}s and associated with their respective {@link DataContainer}s.
	 * @param frame - the frame to use to generate the layout.
	 * @return a Map matching the ImageBag (i.e. overall location) to maps mapping the Panels used for each container to their respective containers
	 * @throws ContainerUnplaceableExcpetion
	 * @throws DimensionMismatchException
	 * @throws TooManyItemsException
	 */
	public HashMap<ImageBag,HashMap<JPanel,DataContainer>> createLayout(JFrame frame ) throws ContainerUnplaceableExcpetion,DimensionMismatchException, TooManyItemsException
	{
		this.removeAll();
		//reset the image bags, as datasets could have changed.
		leftBag = new ImageBag(BAGPOSITON.LEFT, this);
		rightBag = new ImageBag(BAGPOSITON.RIGHT, this);
		centerBag = new ImageBag(BAGPOSITON.CENTER,this);
		//up the maps for the Image Positions
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
			for(DataSet set : fixedContainers.get(i))
			{
				DataContainer container = set.getLayoutContainer();
//				System.out.println("Trying to add non flexible container with " + i +" items to position " + container.getLocalisation().pos  );
				if(container.getLocalisationPreference().pos == Localisation.Position.CENTER)
				{
					centerBag.addContainer(set);
				}
				else
				{
					if(leftBag.itemcount > rightBag.itemcount)
					{
						rightBag.addContainer(set);
					}
					else
					{
						leftBag.addContainer(set);
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
			for(DataSet set : flexibleContainers.get(i))
			{
				DataContainer container = set.getLayoutContainer(); 
			//	System.out.println("Trying to add flexible container with " + i +" items to position " + container.getLocalisation().pos  );
				if(container.getLocalisationPreference().pos == Localisation.Position.CENTER)
				{
					centerBag.addContainer(set);
				}
				else
				{
					if(leftBag.itemcount > rightBag.itemcount)
					{
						rightBag.addContainer(set);
					}
					else
					{
						leftBag.addContainer(set);
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
			for(DataSet set : freecontainers.get(i))
			{
				//System.out.print("Trying to add freely placeable container with " + i +" items to position");
				if(centerBag.itemcount > rightBag.itemcount && centerBag.itemcount > leftBag.itemcount)
				{
					centerBag.addContainer(set);
					//System.out.print("to CENTER");
				}
				else
				{
					//System.out.print("to EDGE");
					if(leftBag.itemcount > rightBag.itemcount)
					{
						leftBag.addContainer(set);
					}
					else
					{
						rightBag.addContainer(set);

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
		frame.pack();
		return containerpositions;
	}
	/**
	 * Remove a {@link DataSet} from this layout container.
	 * @param set - The {@link DataSet} to remove
	 */
	public void removeDataSet(DataSet set)
	{
		int id = set.getID();
		if(addedDataSets.contains(id))
		{
			DataContainer container =  set.getLayoutContainer();		
			Localisation loc = container.getLocalisationPreference();
			if(loc.Flexible)
			{
				Rectangle contsize = container.getMinimalSize();
				int items = contsize.width * contsize.height;
						
				if(flexibleContainers.containsKey(items))
				{
					flexibleContainers.get(items).remove(set);
				}				
				if(loc.pos == Localisation.Position.CENTER )
				{
					currentcenteritems -= items;					
				}
				if(loc.pos == Localisation.Position.EDGE)
				{
					currentedgeitems -= items;
				}
				if(loc.pos == Localisation.Position.FREE)
				{				
					if(freecontainers.containsKey(items))
					{
						freecontainers.get(items).remove(set);
					}					
				}
			}
			else
			{
				Rectangle contsize = container.getMinimalSize();
				int items = contsize.width * contsize.height;
						
				if(fixedContainers.containsKey(items))
				{
					fixedContainers.get(items).remove(set);
				}				
				if(loc.pos == Localisation.Position.CENTER )
				{
					currentcenteritems -= items;
				}
				if(loc.pos == Localisation.Position.EDGE)
				{
					currentedgeitems -= items;
				}
				if(loc.pos == Localisation.Position.FREE)
				{				
					if(freecontainers.containsKey(items))
					{
						freecontainers.get(items).remove(set);
					}					
				}
			}
			addedDataSets.remove(set.getID());
		}

	}
	/**
	 * Add A {@link DataSet}. If the Set is already added, nothing will be done.
	 * @param set - The {@link DataSet} to add to the {@link ImageNodeContainer}
	 * @throws TooManyItemsException
	 */
	public void addDataSet(DataSet set) throws TooManyItemsException
	{
		DataContainer container =  set.getLayoutContainer();
		//Don't add duplicate datasets!
		if(addedDataSets.contains(container.getDataSet().getID())) return;
		Localisation loc = container.getLocalisationPreference();
		if(loc.Flexible)
		{
			Rectangle contsize = container.getMinimalSize();
			int items = contsize.width * contsize.height;
					
			if(!flexibleContainers.containsKey(items))
			{
				flexibleContainers.put(items,  new Vector<DataSet>());
			}
			flexibleContainers.get(items).add(set);
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
					freecontainers.put(items,new Vector<DataSet>());
				}
				freecontainers.get(items).add(set);
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
				fixedContainers.put(items,  new Vector<DataSet>());
			}
			fixedContainers.get(items).add(set);
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
					freecontainers.put(items,new Vector<DataSet>());
				}
				freecontainers.get(items).add(set);
				if(!canPlaceFreeContainers())
				{
					freecontainers.get(items).remove(container);
					throw new TooManyItemsException("All areas are full");
				}
				
			}
		}
		//if we did nto encounter any errors, add this set to the added sets.
		addedDataSets.add(set.getID());	

	}
	
}
