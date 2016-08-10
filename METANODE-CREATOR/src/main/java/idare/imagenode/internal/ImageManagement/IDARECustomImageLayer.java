package idare.imagenode.internal.ImageManagement;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;

import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.ImageCustomGraphicLayer;
/**
 * An implementation of a {@link ImageCustomGraphicLayer} for imagenode Display
 * @author Thomas Pfau 
 */
public class IDARECustomImageLayer implements ImageCustomGraphicLayer {
    private BufferedImage bufferedImage;
    private Rectangle2D rectangle2D;
    private int x;
    private int y;
    Point2D extent;
    
    /**
     * Standard constructor with position and width/height information 
     * @param bufferedImage - the {@link BufferedImage} to use
     * @param x - the x position to paint the image at
     * @param y - the y position to paint the image at
     * @param width - The width of the image
     * @param height - the height of the image
     */
    public IDARECustomImageLayer(BufferedImage bufferedImage, int x, int y, int width, int height) {
        this.bufferedImage = bufferedImage;        
        //if(bufferedImage != null)
        //{
        //}
        this.x = x;
        this.y = y;
        extent = new Point();
        extent.setLocation(width, height);
        this.convertBufferedImageToRectangle();
    }
    
    /**
     * Get the {@link BufferedImage} used in this GraphicLayer
     * @return - The {@link BufferedImage} used
     */
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    /**
     * Set the {@link BufferedImage} used
     * @param bufferedImage - the {@link BufferedImage} that should be displayed in this graphics Layer
     */
    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }
    
    /**
     * Set the rectangle that contains the {@link BufferedImage} - or should contain it, according to x,y, width and height
     */
    public void convertBufferedImageToRectangle() {
        rectangle2D = new Rectangle(this.x,this.y,(int)extent.getX(),(int)extent.getY());//bufferedImage.getWidth(),bufferedImage.getHeight());
    }

    @Override
    public Rectangle2D getBounds2D() {
        return this.rectangle2D;
    }

    @Override
    public TexturePaint getPaint(Rectangle2D rectangle2D) {
        return new TexturePaint(bufferedImage, new Rectangle(this.x,this.y, (int) extent.getX(), (int) extent.getY()));        
    }

    @Override
    public CustomGraphicLayer transform(AffineTransform affineTransform) {
    	try{
    		//Apply the transformation to the extent of this image (we will only allow scaling anyways....
    		AffineTransformOp transop = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BICUBIC);
    		//create a new Extent according to the Affine Transform.
    		Point2D newExtent = new Point();    		
    		newExtent = transop.getPoint2D(extent, newExtent);
    		//the new image is sized according to the new extent.
    		//And now create the new Bufferedimage form the original using the new Extent.

    		IDARECustomImageLayer newLayer = new IDARECustomImageLayer(bufferedImage, x, y, (int)newExtent.getX(), (int)newExtent.getY());
    		newLayer.convertBufferedImageToRectangle();
    		return newLayer;
    	}
    	catch(ImagingOpException e)
    	{
    		return this;
    	}
    }
}
