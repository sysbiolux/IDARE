package idare.imagenode.internal.Layout;

import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Properties.Localisation;
import idare.imagenode.Properties.METANODEPROPERTIES.LayoutStyle;
import idare.imagenode.internal.exceptions.layout.ContainerUnplaceableExcpetion;
import idare.imagenode.internal.exceptions.layout.DimensionMismatchException;
import idare.imagenode.internal.exceptions.layout.TooManyItemsException;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JPanel;

/**
 * An {@link ImageBag} is a class that represents on part of the layout of an image node. Either a side (Bagposition = LEFT/RIGHT) or the center part.  
 * @author Thomas Pfau
 *
 */
public class ImageBag extends Container{

	public static enum BAGPOSITON {CENTER, LEFT ,RIGHT };
	public static int maxEdgeFlexItems = 60;
	public static int maxEdgeFixItems = 15;
	public static int maxCenterFlexItems = 100;
	public static int maxCenterFixItems = 25;

	public static int maxEdgeFixDimensionWidth = 6;
	public static int maxEdgeFixDimensionHeight = 10;	
	public static int maxCenterFixDimensionWidth = 10;
	public static int maxCenterFixDimensionHeight = 10;

	private ImageNodeContainer parent;
	
	HashMap<Integer,Vector<DataSet>> FixedContainers = new HashMap<>();
	HashMap<Integer,Vector<DataSet>> FlexContainers = new HashMap<>();
	HashMap<DataContainer, Rectangle> Layout = new HashMap<DataContainer, Rectangle>();
	int itemcount = 0;
	int maxitemcount = 0;
	int maxwidth = 0;
	int maxheight = 0;	
	int currentwidth = 0;	
	BAGPOSITON position;
	/**
	 * Default Constructor linking to the parent using this bag and providing the position of this bag.
	 * @param position
	 * @param parent
	 */
	public ImageBag(BAGPOSITON position, ImageNodeContainer parent)
	{
		this.position = position;
		this.parent = parent;
		//addComponentListener(new NewDimensionsListener());
		
		if(position == BAGPOSITON.CENTER)
		{
			maxwidth = maxCenterFixDimensionWidth;
			maxheight = maxCenterFixDimensionHeight;
			maxitemcount = maxCenterFlexItems;
		}
		else
		{
			maxwidth = maxEdgeFixDimensionWidth;
			maxheight = maxEdgeFixDimensionHeight;
			maxitemcount = maxEdgeFlexItems;
		}
		if(this.position == BAGPOSITON.RIGHT)
		{
			this.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		}
	}

	/**
	 * Add a {@link DataSet}, if there is sufficient space left in this bag. Otherwise throw a too many items exception.  
	 * @param set
	 * @throws TooManyItemsException
	 */
	public void addContainer(DataSet set) throws TooManyItemsException
	{
		DataContainer container = set.getLayoutContainer();
		Rectangle contDim = container.getMinimalSize();
		int items = contDim.height * contDim.width;
		Localisation contLoc = container.getLocalisationPreference();		
		if(itemcount + items < maxitemcount)
		{
			//We can still add this.
			if(!contLoc.Flexible)
			{
				if(!FixedContainers.containsKey(items))
				{
					FixedContainers.put(items,new Vector<DataSet>());	
				}
				FixedContainers.get(items).add(set);
			}
			else
			{
				if(!FlexContainers.containsKey(items))
				{
					FlexContainers.put(items,new Vector<DataSet>());	
				}
				FlexContainers.get(items).add(set);
			}
			itemcount += items;
		}
		else
		{
			throw new TooManyItemsException("Could not add items of current container. Present items = " + itemcount + " Items to Add: " + items + " Maximal items : " + maxitemcount );			
		}
		
	}
	
	/**
	 * Test, whether this Bag is emptys
	 * @return whether there are any containers in this bag.
	 */
	public boolean isempty()
	{
		return FlexContainers.isEmpty() && FixedContainers.isEmpty();
	}
	/**
	 * Create the layout, if possible. The Layout provides a JPanel to DataContainer map, that can be used to extract the Container for each panel, thus getting the positions for the containers.
	 * @return A Map matching {@link JPanel} s to their respective {@link DataContainer}s. 
	 * @throws DimensionMismatchException
	 * @throws ContainerUnplaceableExcpetion
	 */
	public HashMap<JPanel,DataContainer> createLayout() throws DimensionMismatchException, ContainerUnplaceableExcpetion
	{
		int[][] grid = new int[maxwidth][maxheight];
		//First add all non flexible items
		Vector<Integer> fixedsizes = new Vector<>();
		fixedsizes.addAll(FixedContainers.keySet());
		Collections.sort(fixedsizes, Collections.reverseOrder());
		for(Integer size : fixedsizes)
		{
			Vector<DataSet> current_container_set = FixedContainers.get(size);
			for(DataSet current_set : current_container_set)
			{
				DataContainer current_container = current_set.getLayoutContainer();
				Rectangle dim = current_container.getMinimalSize();
				//System.out.println("Current Container has size: " + dim.width + "/" + dim.height);
				if(dim.width > maxwidth)
				{
					throw new DimensionMismatchException("Could not add container. Width larger than maximal width");					
				}
				int cwidth = dim.width;
				int cheight = dim.height;
				//System.out.println("Trying to add a container for Dataset " + current_container.getDataSet().getID() + " with width/height: " + cwidth + "/" + cheight);
				Rectangle empty = getLargestEmptyArea(grid,cwidth,cheight);				
				if(empty != null)					
				{
					//System.out.println("Placing the Container in the area: " + empty.x + "/" + empty.y + " to " + (empty.x + empty.width) + "/" + (empty.y + empty.height) );
					empty.height = cheight;
					empty.width = cwidth;					
					updategrid(grid, empty, current_container.getDataSet().getID(), current_container);
				}
				else
				{
					//TODO: Better Error Message!
					throw new ContainerUnplaceableExcpetion("Could not fit the container");
				}
			}
		}
		//And now compute the flexible containers. These can be resized, as long as they still contain 
		Vector<Integer> flexsizes = new Vector<>();
		flexsizes.addAll(FlexContainers.keySet());
		Collections.sort(flexsizes, Collections.reverseOrder());
		for(Integer size : flexsizes)
		{
			Vector<DataSet> current_container_set = FlexContainers.get(size);
			for(DataSet current_set : current_container_set)
			{
				DataContainer current_container = current_set.getLayoutContainer();
				//Simply get the largest area, and place it in this area. there wont be a larger container left.
				Rectangle empty = getLargestEmptyArea(grid,0,0);
				if(empty.height * empty.width < size)
				{
					//TODO: Give more information. (probably add a container or the dataset ID)
					throw new ContainerUnplaceableExcpetion("Tried to add a container with " + size + " items , but the maximal remainig area was " + empty.height * empty.width );
				}
				Dimension area = null;
				if(position == BAGPOSITON.CENTER)
				{
					area = current_container.getPreferredSize(new Dimension(empty.width,empty.height),LayoutStyle.CENTER);	
				}
				else
				{
					area = current_container.getPreferredSize(new Dimension(empty.width,empty.height),LayoutStyle.EDGE);
				}
//				System.out.println("Trying to add a container for Dataset " + current_container.getDataSet().getID() + " with width/height: " + area.width + "/" + area.height);
				
				empty.height = area.height;
				empty.width = area.width;
//				System.out.println("Placing the Container in the area: " + empty.x + "/" + empty.y + " to " + (empty.x + empty.width) + "/" + (empty.y + empty.height) );
				updategrid(grid, empty, current_container.getDataSet().getID(), current_container);
			}			
		}
		//printgrid(grid);
		HashMap<JPanel,DataContainer> result = new HashMap<>();
		//JFrame frame = new JFrame("Position: " + position);
		//Container pane = frame.getContentPane();
		//pane.setPreferredSize(new Dimension(400,270));
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();		
		gbc.fill = GridBagConstraints.BOTH;
		Random rng = new Random();
		for(DataContainer i : Layout.keySet())
		{
			
			Rectangle rec = Layout.get(i);
			JPanel lab = new JPanel();
			//lab.addComponentListener(new NewDimensionsListener());
			//lab.setPreferredSize(new Dimension(300,300));			
			gbc.gridx = rec.x;
			gbc.gridy = rec.y;
			gbc.gridwidth = rec.width;
			gbc.weightx = rec.width;
			gbc.weighty = rec.height;
//			System.out.println("Adding item at position " + gbc.gridx + "/" + gbc.gridy + " with width " + gbc.gridwidth + " and height " + gbc.gridheight); 
			lab.setBackground(new Color(rng.nextInt(255),rng.nextInt(255),rng.nextInt(255)));
			add(lab,gbc);					
			result.put(lab, i);
		}	
		//doLayout();
		//pane.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.weightx = 1;
		gbc.weighty = 1;
		//pane.add(this,gbc);
		//pane.doLayout();
		//frame.pack();		
		//frame.setVisible(true);
		//System.out.println(Reader.plotArea(grid));
		return result;
		
	}
	/**
	 * Update the grid, filling in the container with its id at the tofill rectangle.
	 * @param grid
	 * @param tofill
	 * @param id
	 * @param cont
	 */
	private void updategrid(int[][] grid, Rectangle tofill, int id, DataContainer cont)
	{
		Layout.put(cont, tofill);
		currentwidth = Math.max(currentwidth,tofill.x+tofill.width);
		for(int x = tofill.x ; x < tofill.x + tofill.width; x++)
		{
			for(int y = tofill.y ; y < tofill.y + tofill.height; y++)
			{
				grid[x][y] = id;
			}	
		}
	}
	/**
	 * Get the largest empty area that fits to both the requested height and width.
	 * @param grid
	 * @param requestedwidth
	 * @param requestedheight
	 * @return
	 */
	private Rectangle getLargestEmptyArea(int[][] grid, int requestedwidth, int requestedheight)
	{
		//GETLARGEST get the largest empty field in the provided grid.
		//Developed by David Vandevoorde obtained from Dr.Dobbs database
		//             
		// Variables to keep track of the best rectangle so far: best_ll = (0, 0; best_ur = (-1, -1)
		// The cache starts with all zeros:
		Stack<Integer> stack = new Stack();
		stack.push(0);
		stack.push(0);
		int[] best_ll = new int[2];
		int[] best_ur = new int[]{-1,-1};
		int y0 = 0;
		int w0 = 0;
		int width;
		int[] c = new int[grid[0].length + 1]; // create the cache for each row
		for(int x = grid.length-1;x >= 0; x--)
		{
			update_cache(c,x,grid);
			//System.out.print("Current c is:\t");
			//for(int i = 0; i < c.length; i++)
			//{
			//	System.out.print(c[i] + "\t"); 
			//}
			//System.out.print("\n");
			width = 0; // Width of widest opened rectangle
			int y = 0;
			while(y <= grid[0].length)
			{
				if(c[y]>width) //Opening new rectangle(s)?
				{
					//System.out.println("Pushing y = " + y + " and width = " + width + " to the stack");
					stack.push(y);
					stack.push(width);
					width = c[y];
				}
				if( c[y] < width ) //	%% Closing rectangle(s)?) 
				{	                    	                   
					while(c[y] < width)
					{
						//if(stack.size() > 0)
							//Otherwise we try to close a smaller rectangle "below" the current maximum rectangle"
							//{
							//System.out.println("Popping from the stack");
							
							w0 = stack.pop();
							y0 = stack.pop();
							if(width*(y-y0)>getArea(best_ll, best_ur))
							{
								if(width >= requestedwidth && (y -y0) >= requestedheight)
								{
									best_ll = new int[]{x, y0};
									best_ur = new int[]{x+width-1, y-1};
								}
							}
						//	System.out.println("Current y = " + y + " Current width = " + width +  " Current w0 = " + w0  );
						
							width = w0;
							//}
						//else
						//{
						//	break;
						//}
					}
					width = c[y];
					if(width!=0) // Popped an active "opening"?
					{
					//	System.out.println("Pushing y = " + y0 + " and width = " + width + " to the stack");
						stack.push(y0);
						stack.push(w0);
					}

				}

				y = y+1;
			}
		}
		Rectangle res = null;
		if(best_ur[0] != -1)
		{
			res = new Rectangle();
			res.x = best_ll[0];
			res.y = best_ll[1];
			res.width = best_ur[0] - best_ll[0]+1;
			res.height = best_ur[1] - best_ll[1]+1;
			
		}
		return res;

	}
	/**
	 * Update the cache used for greatest empty area determination
	 * @param c
	 * @param x
	 * @param grid
	 */
	private void update_cache(int[] c, int x, int[][] grid)
	{
		//Developed by David Vandevoorde obtained from Dr.Dobbs database
		//This function is a helper function of GetLargest. To extend the cache of
		//open places.
		for(int y = 0 ; y < grid[0].length; y++)
			if(grid[x][y]==0)
				c[y]++ ;
			else
				c[y] = 0;
	}
	/**
	 * get Area function for a lower left and upper right corner
	 * @param c
	 * @param x
	 * @param grid
	 */
	private int getArea(int[] ll, int[] ur)
	{
		//Developed by David Vandevoorde obtained from Dr.Dobbs database		
		if(ll[0] > ur[0] || ll[1] > ur[1]) //there is no opened rectangle as both corners are at the same position 
			return 0;
		else
			return (ur[0] - ll[0] +1 ) * (ur[1] - ll[1] +1 ); 
	}
	

	/**
	 * Utility function to print the Grid present in this bag.
	 * @param grid
	 */
	public void printgrid(int[][] grid)	
	{
		String rv = "";
		if(position == BAGPOSITON.CENTER)
		{
		}
		if(position == BAGPOSITON.LEFT)
		{
		}
		if(position == BAGPOSITON.RIGHT)
		{
		}
		for(int y = 0; y < grid[0].length; y++)
		{
			for(int x = 0; x < grid.length; x++)
			{
				rv += "\t" + grid[x][y];  
			}
			rv += "\n";
		}
	}
}
