package idare.imagenode.internal.GUI.Legend.Tasks;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.Utilities.LayoutUtils;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.GUI.Legend.IDARELegend;
import idare.imagenode.internal.Layout.DataSetLink;
import idare.imagenode.internal.Layout.ImageNodeLayout;

public class CreateNodeImageTask extends AbstractTask {

	IDARELegend legend;
	NodeManager manager;	
	JFrame descriptionFrame;
	String fileExtension;
	File targetFile;
	/**
	 * Default Constructor
	 * @param legend The reference {@link IDARELegend}
	 * @param targetFile the target to write the data to
	 * @param manager the NodeManager used to obtain nodes to create
	 * @param FileExtension The file extension (svg or png) to generate the images in.
	 */
	public CreateNodeImageTask(IDARELegend legend, File targetFile,
			NodeManager manager, String FileExtension) {
		super();
		this.legend = legend;
		this.targetFile = targetFile;
		this.manager = manager;
		fileExtension = FileExtension;
		
	}

	
	/**
	 * Convert the legend to an Image in the given ZipOutput.
	 * @param zipout - The outputstream to write to
	 * @param coder - a potential {@link Transcoder} to convert into non SVG files. If null, and SVG will be generated.
	 * @throws IOException
	 */
	private void paintLegendToFile(OutputStreamWriter zipout, Transcoder coder) throws IOException
	{
		// Unfortunately, the Legend Node is not in SVG forma, but I could not figure out a way to actually 
		// have it in SVG... 
		SVGDocument svgdoc2 = LayoutUtils.createSVGDoc();
		SVGGraphics2D g2 = new SVGGraphics2D(svgdoc2);
		ImageNodeLayout layout = manager.getLayoutForNode(legend.getCurrentlyUsedNode());
		JPanel ContentPane = buildLegendDescriptionFrame(layout, layout.getDatasetsInOrder());
		layout.layoutLegendNode(manager.getNode(legend.getCurrentlyUsedNode()).getData(), g2);
		g2.translate(0, IMAGENODEPROPERTIES.IMAGEHEIGHT+IMAGENODEPROPERTIES.LABELHEIGHT);		
		descriptionFrame.setVisible(true);
		int width = ContentPane.getSize().width;
		int height = ContentPane.getSize().height + IMAGENODEPROPERTIES.IMAGEHEIGHT+IMAGENODEPROPERTIES.LABELHEIGHT;
		g2.setSVGCanvasSize(new Dimension(width,height));
		ContentPane.paint(g2);		
		descriptionFrame.dispose();
		Element root2 = svgdoc2.getDocumentElement();
		g2.getRoot(root2);
		root2.setAttribute("viewBox", "0 0 " + width + " " + height );
		
		if(coder == null)
		{
			g2.stream(root2,zipout);			
		}
		else
		{	
			//If we want to transcode to a non SVG format.
			TranscoderInput input = new TranscoderInput(svgdoc2);
			TranscoderOutput out = new TranscoderOutput(zipout);
			try{
				coder.transcode(input, out);
			}
			catch(TranscoderException e)
			{
				return;
			}
			zipout.flush();
		}		
	}
	
	/**
	 * Write the node Image to the {@link OutputStreamWriter} using the given {@link Transcoder} if non <code>null</code>, and the given ID.
	 * @param zipout - The Outstream to write to.  
	 * @param coder - The coder to convert to another format (if <code>null</code> svg will be generated
	 * @param NodeID - The NodeID to use as label.
	 * @throws IOException
	 */
	private void writeNodeImageToFile(OutputStreamWriter zipout, Transcoder coder, String NodeID) throws IOException
	{
		SVGDocument svgdoc2 = LayoutUtils.createSVGDoc();
		SVGGraphics2D g2 = new SVGGraphics2D(svgdoc2);
		manager.getLayoutForNode(NodeID).layoutNode(manager.getNode(NodeID).getData(), g2);
		Element root2 = svgdoc2.getDocumentElement();
		g2.getRoot(root2);
		
		LayoutUtils.TransferGraphicsToDocument(svgdoc2, null, g2);		
		
		//OutputStream os2 = new FileOutputStream(F);
		if(coder == null)
		{
			
			g2.stream(root2,zipout);
			
		}
		else
		{
			TranscoderInput input = new TranscoderInput(svgdoc2);
			TranscoderOutput out = new TranscoderOutput(zipout);
			try{
				coder.transcode(input, out);
			}
			catch(TranscoderException e)
			{
				//JOptionPane.showConfirmDialog(cySwingApp.getJFrame(), "Could not save as " + F.getName().substring(F.getName().lastIndexOf(".")) + " try svg instead.");
				//os2.close();
				return;
			}
			zipout.flush();
		}		
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		taskMonitor.setTitle("Generating Image Zip File");

		if(!targetFile.getName().endsWith("zip"))
		{
			targetFile = new File(targetFile.getPath()+".zip");
		}
		
		Transcoder coder = null;		
			switch(fileExtension)
			{
			case  "svg":
				coder = null;
				break;
			case "png":
				coder = new PNGTranscoder();
				break;
			default:
				coder = null;
			}
		//IF no extension is given, we assume this is a svg...
			
		try{
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(targetFile));
			OutputStreamWriter outwriter = new OutputStreamWriter(out);
			ZipEntry entry = new ZipEntry("Legend." + fileExtension);
			out.putNextEntry(entry);
			taskMonitor.setStatusMessage("Writing Legend Image");
			paintLegendToFile(outwriter , coder);
			out.closeEntry();
			taskMonitor.setStatusMessage("Writing Node Images");
			Collection<String> IDs = manager.getNodesForLayout(manager.getLayoutForNode(legend.getCurrentlyUsedNode()));
			int current = 0;
			int Steps = Math.max(IDs.size()/10,1);
			for(String ID : IDs)
			{
				current++;
				if(current % Steps == 0)
				{
					taskMonitor.setProgress(((double) current) / IDs.size());
				}
				ZipEntry nodeentry = new ZipEntry(ID + "." + fileExtension);
				out.putNextEntry(nodeentry);
				writeNodeImageToFile(outwriter , coder, ID);
				out.closeEntry();
			}
			outwriter.close();
			out.close();
			
		}
		catch(IOException e)
		{
			taskMonitor.showMessage(Level.ERROR, "Could not write file " + targetFile.getName());			
		}
	}
	
	/**
	 * Build a DataDescriptionpane to  print the legend descriptions.
	 * @param layout  the layouts to use
	 * @param datasets  the datasets in correct order
	 * @return The LegendDescription Panel
	 */
	private JPanel buildLegendDescriptionFrame(ImageNodeLayout layout, Vector<? extends DataSetLink> datasets)
	{
		descriptionFrame = new JFrame();
		JPanel ContentPane = new JPanel();
		JScrollPane scroller = new JScrollPane(ContentPane);		
		descriptionFrame.add(scroller);
		ContentPane.setBackground(Color.BLACK);
		ContentPane.setLayout(new BoxLayout(ContentPane,BoxLayout.PAGE_AXIS));
		//The 32 here is a hack, but I currently don't know how else to get the width to be 400...
		descriptionFrame.setSize(new Dimension(IMAGENODEPROPERTIES.IMAGEWIDTH+31, 100));
		setDataSetDescriptions(scroller, datasets, layout, ContentPane);
		descriptionFrame.setSize(new Dimension(IMAGENODEPROPERTIES.IMAGEWIDTH+31, 100));
		return ContentPane;
	}
	/**
	 * Essentially a copy of the Method from the {@link IDARELegend}. using a ScrollPane and a given content to do the process.
	 * @param scroller The scrollpane to add the DataSetDescription to.
	 * @param datasets The datasets to add to this "legend"
	 * @param layout the layout to use
	 * @param Content the Content pane to use plot to
	 */
	private void setDataSetDescriptions(JScrollPane scroller, Vector<? extends DataSetLink> datasets, ImageNodeLayout layout, JPanel Content)
	{

		for(DataSetLink dsl : datasets)
		{
			JPanel DataSetPane = dsl.getDataSet().getDataSetDescriptionPane(scroller,layout.getDataSetLabel(dsl),layout.getColorsForDataSet(dsl));			
			Content.add(Box.createRigidArea(new Dimension(0,2)));
			Content.add(DataSetPane);
			//DataSetPane.doLayout();
		}
		Content.revalidate();
	}


}
