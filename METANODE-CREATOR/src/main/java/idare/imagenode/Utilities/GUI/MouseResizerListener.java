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

public class MouseResizerListener<T extends JComponent> extends MouseInputAdapter
{
	private T target;	
	private JDesktopPane BoundingComponent;
	private Class targetclass;
	int posX;
	int posY;
	int posXOnFrame;
	int posYOnFrame;
	Rectangle origcomponentpos;
	boolean xchange = false;
	boolean ychange = false;
	Vector<Rectangle> otherFrames; 
	public MouseResizerListener(JDesktopPane BoundingComponent, Class<T> clazz)
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
		
		if(e.getX() <=  frameInsets.left || e.getX() >= origcomponentpos.width - frameInsets.right)				
		{        	
			xchange = true;	
		}
		if ( e.getY() <= frameInsets.top || e.getY() >= origcomponentpos.height - frameInsets.left)
		{
			ychange = true;
		}		
	}

	@Override	
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		super.mouseReleased(e);
		xchange = false;
		ychange = false;
		target = null;
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		super.mouseDragged(e);
//		PrintFDebugger.Debugging(this, "Got a mouse dragged event from " + e.getSource() + " while dragging");
		Rectangle origcomppos = target.getBounds();
		int dragdistance = e.getXOnScreen() - posX;
		int newXpos =  Math.min(Math.max(0, origcomponentpos.x + dragdistance), BoundingComponent.getWidth()-origcomponentpos.width);			
		int newWidth = origcomponentpos.width + dragdistance;
		int dragdistanceY = e.getXOnScreen() - posY;
		int newYpos = Math.min(Math.max(0, origcomponentpos.y + e.getYOnScreen() - posY),BoundingComponent.getHeight() - origcomponentpos.height);
		int newHeight = origcomponentpos.height + dragdistanceY;

		if(xchange)
		{	
										
//			System.out.println("Old bounds were: " + origcomponentpos);			
			
			// min is 0
			//max is the the width of the desktop;
			
			
			//Now, check all other frames
			for(Rectangle rec : otherFrames)
			{
				//Coming from left:
				if(( Math.abs(rec.x + rec.width - newXpos) < 4 ) 
						&& ((rec.y <= origcomppos.y) && (rec.y+rec.height >= origcomppos.y) 
						|| (rec.y >= origcomppos.y) && (rec.y <= origcomppos.y + origcomppos.height )))
				{
					newXpos = rec.x + rec.width;
					newWidth = newWidth + rec.x + rec.width - newXpos;
					//We only fit to one
					break;
				}
				//Coming from right 
				if(( Math.abs(rec.x - (newXpos + newWidth)) < 4 ) 
						&& ((rec.y <= origcomppos.y) && (rec.y+rec.height >= origcomppos.y) 
						|| (rec.y >= origcomppos.y) && (rec.y <= origcomppos.y + origcomppos.height )))
				{
					newWidth =  newWidth + rec.x - (newXpos + newWidth);
					break;
				}
			}
		}
		if(ychange)
		{
			
			for(Rectangle rec : otherFrames)
			{
				//Coming from top 
				if(( Math.abs(rec.y - (newYpos + newHeight)) < 4 ) 
						&& ((rec.x <= origcomppos.x) && (rec.x+rec.width >= origcomppos.x) 
						|| (rec.x >= origcomppos.x) && (rec.x <= origcomppos.x + origcomppos.width)))
				{
					newHeight =  newHeight + rec.y - (newYpos + newHeight);
					break;
				}
				//Coming from bottom 
				if(( Math.abs(rec.y  + rec.height - newYpos) < 4 ) 
						&& ((rec.x <= origcomppos.x) && (rec.x+rec.width >= origcomppos.x) 
						|| (rec.x >= origcomppos.x) && (rec.x <= origcomppos.x + origcomppos.width)))
				{
					newYpos = rec.y + rec.height;
					newHeight = newHeight + rec.y + rec.height- newYpos;
					break;
				}
			}		
		}
		if(xchange || ychange) 
		{
			System.out.println("The updated Properties were W: " + newWidth +"; H: " + newHeight+"; X: " + newXpos+"; Y: " + newYpos);		
			target.setBounds(new Rectangle(newXpos,newYpos,newWidth,newHeight));
//			System.out.println("New Bounds are: " +  target.getBounds());
			//frame.setLocation(origframepos.x + e.getXOnScreen() - posX , origframepos.y + e.getYOnScreen() - posY);
			//target.repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		super.mouseMoved(e);
	}

}
