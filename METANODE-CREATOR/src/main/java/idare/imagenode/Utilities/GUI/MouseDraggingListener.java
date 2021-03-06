package idare.imagenode.Utilities.GUI;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.MouseInputAdapter;

import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.Layout.Manual.GUI.DataSetFrame;

public class MouseDraggingListener<T extends JComponent> extends MouseInputAdapter
{
	private T target;	
	private JDesktopPane BoundingComponent;
	private Class targetclass;
	int posX;
	int posY;
	int posXOnFrame;
	int posYOnFrame;
	Rectangle origcomponentpos;
	boolean componentselected = false;
	Vector<Rectangle> otherFrames; 
	public MouseDraggingListener(JDesktopPane BoundingComponent, Class<T> clazz)
	{	    					
		this.BoundingComponent = BoundingComponent;		
		targetclass = clazz;		
	}
	@Override
	public void mousePressed(MouseEvent e) {
		
		if(!(targetclass.isInstance(e.getSource())))
		{
			System.out.println("The target was: " + e.getSource().getClass().getSimpleName());
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
		
		JInternalFrame[] frames = BoundingComponent.getAllFrames();
		otherFrames = new Vector<>();
		for(JInternalFrame cframe : frames)
		{
			if(cframe != target)
			{
				otherFrames.add(cframe.getBounds());
			}
		}
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
			//Now, check all other frames
			for(Rectangle rec : otherFrames)
			{
				//Coming from left:
				if(( Math.abs(rec.x + rec.width - newXpos) < 5 ) 
						&& ((rec.y <= origcomponentpos.y) && (rec.y+rec.height >= origcomponentpos.y) 
						|| (rec.y >= origcomponentpos.y) && (rec.y <= origcomponentpos.y + origcomponentpos.height )))
				{
					newXpos = rec.x + rec.width;
				}
				//Coming from right 
				if(( Math.abs(rec.x - (newXpos + origcomponentpos.width)) < 5 ) 
						&& ((rec.y <= origcomponentpos.y) && (rec.y+rec.height >= origcomponentpos.y) 
						|| (rec.y >= origcomponentpos.y) && (rec.y <= origcomponentpos.y + origcomponentpos.height )))
				{
					newXpos = rec.x - origcomponentpos.width;
				}
				//Coming from top 
				if(( Math.abs(rec.y - (newYpos + origcomponentpos.height)) < 5 ) 
						&& ((rec.x <= origcomponentpos.x) && (rec.x+rec.width >= origcomponentpos.x) 
						|| (rec.x >= origcomponentpos.x) && (rec.x <= origcomponentpos.x + origcomponentpos.width)))
				{
					newYpos = rec.y - origcomponentpos.height;
				}
				//Coming from bottom 
				if(( Math.abs(rec.y  + rec.height - newYpos) < 5 ) 
						&& ((rec.x <= origcomponentpos.x) && (rec.x+rec.width >= origcomponentpos.x) 
						|| (rec.x >= origcomponentpos.x) && (rec.x <= origcomponentpos.x + origcomponentpos.width)))
				{
					newYpos = rec.y + rec.height;
				}
			}		
			
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
