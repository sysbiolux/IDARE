package idare.imagenode.internal.ImageManagement;



import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

/**
 * A class for Custom graphics in the Cytoscape visualisation of IDARE
 * @author Thomas Pfau
 *
 */
public class IDARECustomGraphics implements CyCustomGraphics<CustomGraphicLayer> {

    private BufferedImage image;
    private Long identifier;
    private String displayName;
    private int width;
    private int height;
    private float ratio;

    /**
     * Default constructor with an Image and initial Heights and width
     * @param image - A {@link BufferedImage} contained in this CustomGraphics 
     * @param width - The initial width of the graphic
     * @param height - The initial height of the graphic 
     */
    public IDARECustomGraphics(BufferedImage image,int width, int height) 
    {
        this.image = image;
        if(image != null)
        {
        }
        this.width = width;
        this.height = height;
        //this.ratio = 1;
    }
    @Override 
    public boolean equals(Object icg)
    {
    	if(icg instanceof IDARECustomGraphics)
    	{
    		return this.image == ((IDARECustomGraphics)icg).image;
    	}
    	return false;
    }
    @Override
    public Long getIdentifier() {
        return this.identifier;
    }

   
    @Override
    public void setIdentifier(Long id) {
        this.identifier = id;
    }

   
    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

   
    @Override
    public String toSerializableString() {
        return this.toString();
    }

   
    @Override
    public List<CustomGraphicLayer> getLayers(CyNetworkView networkView, View view) {
        List<CustomGraphicLayer> list = new ArrayList<CustomGraphicLayer>();        
        int x = 0-getWidth()/2;
        int y = 0-getHeight()/2;
        IDARECustomImageLayer micgl = new IDARECustomImageLayer(image,x,y,width,height);
        micgl.convertBufferedImageToRectangle();
        list.add(micgl);

        return list;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public float getFitRatio() {
        return this.ratio;
    }

   
    @Override
    public void setFitRatio(float ratio) {
        this.ratio = ratio;
    }

   
    @Override
    public BufferedImage getRenderedImage() {
//    	return image;
    	int cheight = getHeight();
    	int iheight = image.getHeight();
    	int cwidth= getWidth();
    	int iwidth = image.getWidth();
    	double scalefactor = Math.min(cwidth/(double)iwidth,cheight/(double)iheight); 
    	AffineTransform trans = AffineTransform.getScaleInstance(scalefactor, scalefactor);
    	BufferedImage returnimage = new BufferedImage((int)(iwidth*scalefactor),(int)(scalefactor*iheight), BufferedImage.TYPE_INT_ARGB);
    	AffineTransformOp top = new AffineTransformOp(trans, AffineTransformOp.TYPE_BILINEAR);
    	returnimage = top.filter(image, returnimage);
        return returnimage;
    }
}