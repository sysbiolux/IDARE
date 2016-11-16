package idare.imagenode.Utilities.GUI;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.apache.batik.swing.JSVGCanvas;

public class JSVGGlassCanvas extends JSVGCanvas{

    //React to change button clicks.
    public void itemStateChanged(ItemEvent e) {
        setVisible(e.getStateChange() == ItemEvent.SELECTED);
    }


    public JSVGGlassCanvas(Container contentPane) {
        CBListener listener = new CBListener(this, contentPane);
        addMouseListener(listener);
        addMouseMotionListener(listener);
        setBackground(new Color(255,255,255,0));
    }
	
    @Override
    public void paintComponent(Graphics g)
    {
    	super.paintComponent(g);
//    	PrintFDebugger.Debugging(this, "Current Location" + this.getLocation() + " with size " + this.getSize() + " leads to bounds " +  this.getBounds());
    }
    
    @Override
    public void setBounds(int x, int y, int w, int h)
    {
    	super.setBounds(x,y,w,h);
    	//PrintFDebugger.Debugging(this, "New width and height are : " + w + " / " + h);
    	//PrintFDebugger.Trace(this);
    }
    
    class CBListener extends MouseInputAdapter {
        Toolkit toolkit;
        JSVGGlassCanvas glassPane;
        Container contentPane;

        public CBListener(JSVGGlassCanvas glassPane, Container contentPane) {
            toolkit = Toolkit.getDefaultToolkit();
            this.glassPane = glassPane;
            this.contentPane = contentPane;
        }

        public void mouseMoved(MouseEvent e) {
            redispatchMouseEvent(e, false);
        }

        public void mouseDragged(MouseEvent e) {
        	//mouseDraggedEvent should always be forwarded if they originate here as they keep dragging
            redispatchMouseEvent(e, true);
        }

        public void mouseClicked(MouseEvent e) {
            redispatchMouseEvent(e, false);
        }

        public void mouseEntered(MouseEvent e) {
            redispatchMouseEvent(e, false);
        }

        public void mouseExited(MouseEvent e) {
            redispatchMouseEvent(e, false);
        }

        public void mousePressed(MouseEvent e) {
            redispatchMouseEvent(e, false);
        }

        public void mouseReleased(MouseEvent e) {
        	//mouseRelease Events should always be forwarded if they originate here (end of a drag)
            redispatchMouseEvent(e, true);
        }

        //A basic implementation of redispatching events.
        private void redispatchMouseEvent(MouseEvent e,
                                          boolean alwaysforward) {
            Point glassPanePoint = e.getPoint();
            Container container = contentPane;
            Point containerPoint = SwingUtilities.convertPoint(
                                            glassPane,
                                            glassPanePoint,
                                            contentPane);
            if (alwaysforward || (containerPoint.y >= 0 && containerPoint.y < contentPane.getHeight()
            		&& containerPoint.x >= 0 && containerPoint.x < contentPane.getWidth()))
            {
                    //Forward events over to the content pane.
                    contentPane.dispatchEvent(new MouseEvent(contentPane,
                                                         e.getID(),
                                                         e.getWhen(),
                                                         e.getModifiers(),
                                                         containerPoint.x,
                                                         containerPoint.y,
                                                         e.getClickCount(),
                                                         e.isPopupTrigger()));                
            }            
        }
    }

    
}
