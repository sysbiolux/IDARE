package idare.imagenode.Utilities.GUI;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;

import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.Layout.Manual.GUI.DataSetFrame;

public class MouseDraggingListener<T extends JComponent> extends MouseInputAdapter
{
	private T target;	
	private JComponent BoundingComponent;
	private Class targetclass;
	int posX;
	int posY;
	int posXOnFrame;
	int posYOnFrame;
	Rectangle origcomponentpos;
	boolean componentselected = false;
	public MouseDraggingListener(JComponent BoundingComponent, Class<T> clazz)
	{	    					
		this.BoundingComponent = BoundingComponent;		
		targetclass = clazz;		
	}
	@Override
	public void mousePressed(MouseEvent e) {
		
		if(!(targetclass.isInstance(e.getSource())))
		{
			return;
		}
		if(target == null)
		{
			target = (T) e.getSource();
		} 		
		posX = e.getXOnScreen();
		posY = e.getYOnScreen();
		posXOnFrame = target.getBounds().x;
		posXOnFrame = target.getBounds().y;
		origcomponentpos = target.getBounds();       
		Insets frameInsets = target.getInsets();		
		if(e.getX() >  frameInsets.left && e.getX() < origcomponentpos.width - frameInsets.right
				&& e.getY() > frameInsets.top && e.getY() < origcomponentpos.height - frameInsets.left)
		{        	
			componentselected = true;	
		}
	}

	@Override	
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		super.mouseReleased(e);
//		PrintFDebugger.Debugging(this, "Mouse was released");
		componentselected = false;
		target = null;
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		super.mouseDragged(e);
//		PrintFDebugger.Debugging(this, "Got a mouse dragged event from " + e.getSource() + " while dragging");

		if(componentselected)
		{
//			System.out.println("Old bounds were: " + origcomponentpos);			
			int dragdistance = e.getXOnScreen() - posX;
			// min is 0
			//max is the the width of the desktop;
			int newXpos =  Math.min(Math.max(0, origcomponentpos.x + dragdistance), BoundingComponent.getWidth()-origcomponentpos.width);
			int newYpos = Math.min(Math.max(0, origcomponentpos.y + e.getYOnScreen() - posY),BoundingComponent.getHeight() - origcomponentpos.height);
			target.setBounds(new Rectangle(newXpos, newYpos, origcomponentpos.width,origcomponentpos.height));
//			System.out.println("New Bounds are: " +  target.getBounds());
			//frame.setLocation(origframepos.x + e.getXOnScreen() - posX , origframepos.y + e.getYOnScreen() - posY);
			target.repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		super.mouseMoved(e);
		if(componentselected)
		{
//			PrintFDebugger.Debugging(this, "Got a mouse moved event from " + e.getSource() + " while dragging");
			System.out.println("Old bounds were: " + origcomponentpos);

			int dragdistance = e.getXOnScreen() - posX;
			// min is 0
			//max is the the width of the desktop;
			int newXpos =  Math.min(Math.max(0, origcomponentpos.x + dragdistance), BoundingComponent.getWidth()-origcomponentpos.width);
			int newYpos = Math.min(Math.max(0, origcomponentpos.y + e.getYOnScreen() - posY),BoundingComponent.getHeight() - origcomponentpos.height);
			target.setBounds(new Rectangle(newXpos, newYpos, origcomponentpos.width,origcomponentpos.height));
//			System.out.println("New Bounds are: " +  target.getBounds());
			//frame.setLocation(origframepos.x + e.getXOnScreen() - posX , origframepos.y + e.getYOnScreen() - posY);
			target.repaint();
		}
	}

}
