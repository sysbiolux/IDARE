package idare.imagenode.internal.Layout.Resizer.Tasks;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.AbstractCyEdit;

import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Layout.ImageNodeLayout;

public class ResizeNodeImageTask extends AbstractCyEdit implements Task {
	
	
	public double scalingfactor;
	private HashMap<ImageNodeLayout,Dimension> originalDimensions;
	public ResizeNodeImageTask(NodeManager nm, ImageNodeLayout selectedLayout, double scalingFactor)
	{
		super("Resizing Images");
		Collection<ImageNodeLayout> layoutsToResize;
		if(selectedLayout == null)
		{
			layoutsToResize = nm.getCurrentLayouts();
		}
		else
		{
			layoutsToResize = Collections.singleton(selectedLayout);
		}			
		for(ImageNodeLayout layout : layoutsToResize)
		{
			originalDimensions.put(layout, layout.getImageSize()); 
		}
		this.scalingfactor = scalingFactor;
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		arg0.setTitle("Resizing Nodes");		
		arg0.setProgress(0);				
		redo();
		arg0.setProgress(1);
		
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		for(ImageNodeLayout layout : originalDimensions.keySet())
		{
			if(!layout.getImageSize().equals(originalDimensions.get(layout)))
			{
				layout.setImageDimension(originalDimensions.get(layout));
			}
		}
			
	}

	@Override
	public void redo() {
		for(ImageNodeLayout layout : originalDimensions.keySet())
		{
			Dimension current = originalDimensions.get(layout);
			Dimension newDim = new Dimension(0,0);
			newDim.setSize(current.width*scalingfactor, current.height*scalingfactor);
			layout.setImageDimension(newDim);
		}		
	}

	@Override
	public void undo() {
		for(ImageNodeLayout layout : originalDimensions.keySet())
		{
			if(!layout.getImageSize().equals(originalDimensions.get(layout)))
			{
				layout.setImageDimension(originalDimensions.get(layout));
			}
		}
	}

}
