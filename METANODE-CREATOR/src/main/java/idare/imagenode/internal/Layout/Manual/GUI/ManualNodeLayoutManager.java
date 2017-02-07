package idare.imagenode.internal.Layout.Manual.GUI;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JComponent;

import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.internal.Debug.PrintFDebugger;

public class ManualNodeLayoutManager implements LayoutManager{


	private JComponent Node;
	private JComponent ID;
	private Dimension nodeDimension;
	private Dimension IDDimension;

	public ManualNodeLayoutManager(JComponent Node, JComponent ID, Dimension NodeDimension, Dimension IDDimension)
	{
		this.Node = Node;
		this.ID = ID;
		this.nodeDimension = NodeDimension;
		this.IDDimension = IDDimension;
	}
	/*
	 * (non-Javadoc)
	 * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
	 */
	@Override
	public void addLayoutComponent(String name, Component comp) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
	 */
	@Override
	public void layoutContainer(Container parent) {
		//Determine the maximal dimensions and the number of components present in the container.
		Insets insets = parent.getInsets();
		int maxWidth = parent.getWidth()
				- (insets.left + insets.right);
		int maxHeight = parent.getHeight()
				- (insets.top + insets.bottom);
		
		
		System.out.println("New enclosing size is" + maxWidth + "/" + maxHeight);
		double scalingfactor = Math.min(maxHeight/(nodeDimension.getHeight() + IDDimension.getHeight()), maxWidth/IDDimension.getWidth());
		
		Rectangle nodeBounds = new Rectangle(0,0,(int)(scalingfactor*nodeDimension.getWidth()),(int)(scalingfactor * nodeDimension.getHeight()));
		Rectangle idBounds = new Rectangle(0,(int)(scalingfactor * nodeDimension.getHeight()),(int)(scalingfactor*IDDimension.getWidth()),(int)(scalingfactor * IDDimension.getHeight()));
//		PrintFDebugger.Debugging(this, "Setting the bounds of the desktop to: " + nodeBounds + " and the ID dimension to " + idBounds);
		Node.setBounds(nodeBounds);
		ID.setBounds(idBounds);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
	 */
	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(IMAGENODEPROPERTIES.IMAGEWIDTH,IMAGENODEPROPERTIES.IMAGEHEIGHT+IMAGENODEPROPERTIES.LABELHEIGHT);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
	 */
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int nComps = parent.getComponentCount();
		Insets insets = parent.getInsets();
		int maxWidth = parent.getWidth()
				- (insets.left + insets.right);
		int maxHeight = parent.getHeight()
				- (insets.top + insets.bottom);
//		PrintFDebugger.Debugging(this, "The Enclosing container size is " + parent.getSize());
		double scaling = Math.min(maxWidth/(double)IMAGENODEPROPERTIES.IMAGEWIDTH, maxHeight/(double)(IMAGENODEPROPERTIES.IMAGEHEIGHT+IMAGENODEPROPERTIES.LABELHEIGHT));
//		PrintFDebugger.Debugging(this, "The Preferred Size is " + new Dimension((int)(IMAGENODEPROPERTIES.IMAGEWIDTH*scaling), (int)((IMAGENODEPROPERTIES.IMAGEHEIGHT+IMAGENODEPROPERTIES.LABELHEIGHT)*scaling)));
		return new Dimension((int)(IMAGENODEPROPERTIES.IMAGEWIDTH*scaling), (int)((IMAGENODEPROPERTIES.IMAGEHEIGHT+IMAGENODEPROPERTIES.LABELHEIGHT)*scaling));

	}
	@Override
	public void removeLayoutComponent(Component comp) {
		// TODO Auto-generated method stub

	}

}

