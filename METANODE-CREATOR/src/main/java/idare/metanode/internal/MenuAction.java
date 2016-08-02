package idare.metanode.internal;

import idare.metanode.IDAREMetaNodeApp;
import idare.metanode.internal.Data.ValueSetData.ValueSetDataSet;
import idare.metanode.internal.Data.itemizedData.AbstractItemDataSet;
import idare.metanode.internal.DataManagement.DataSetManager;
import idare.metanode.internal.DataManagement.NodeManager;
import idare.metanode.internal.GUI.Legend.IDARELegend;
import idare.metanode.internal.Interfaces.DataContainer;
import idare.metanode.internal.Interfaces.DataSet;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.FileUtil;


/**
 *TODO:  This class will be removed in the final version!
 */
public class MenuAction extends AbstractCyAction {

	CySwingApplication cySwingApp;
	FileUtil util;
	IDAREMetaNodeApp app; 
	boolean init = false;
	IDARELegend pan;
	CyApplicationManager cyAppMgr;
	public MenuAction(CyApplicationManager cyApplicationManager, final String menuTitle,IDAREMetaNodeApp app,CySwingApplication cySwingApp,FileUtil util,IDARELegend pan) {

		super(menuTitle, cyApplicationManager, null, null);
		setPreferredMenu("Apps.IDARE");
		this.pan = pan;
		this.app = app;		
		this.cySwingApp = cySwingApp;
		this.util = util;
		this.cyAppMgr = cyApplicationManager;
	}

	public void actionPerformed(ActionEvent e) {

		// Write your own function here.

		NodeManager nm = app.getNodeManager();
		DataSetManager dsm = app.getDatasetManager();
		try{
			if(!init)
			{
				init = true;
				AbstractItemDataSet aitd = new AbstractItemDataSet("Dataset 1");
				ValueSetDataSet aitd2 = new ValueSetDataSet();
				aitd2.Description = "A GraphSet with less items";
				AbstractItemDataSet aitd3 = new AbstractItemDataSet("This Dataset has a very very long description and probably needs to be truncated quite significantly when displayed.");
				AbstractItemDataSet aitd4 = new AbstractItemDataSet("Dataset 4");
				ValueSetDataSet aitd5 = new ValueSetDataSet();
				aitd5.Description = "A Graph DataSet";
				aitd.parseFile(new File("/home/thomas/Temp/test.xls"));
				aitd2.parseFile(new File("/home/thomas/Temp/test2a.xlsx"));
				aitd3.parseFile(new File("/home/thomas/Temp/test4.xlsx"));
				aitd4.parseFile(new File("/home/thomas/Temp/test.xls"));
				aitd5.parseFile(new File("/home/thomas/Temp/test2.xlsx"));
				Vector<DataSet> temp = new Vector<DataSet>();
				temp.add(aitd);
				temp.add(aitd2);
				temp.add(aitd3);
				dsm.addDataSet(aitd);
				dsm.addDataSet(aitd2);
				dsm.addDataSet(aitd3);
				dsm.addDataSet(aitd4);
				dsm.addDataSet(aitd5);				
				DataContainer aic1 = aitd.getContainerForID("Obj1");
				DataContainer aic2 = aitd2.getContainerForID("Obj1");
				DataContainer aic3 = aitd3.getContainerForID("Obj1");
				DataContainer aic4 = aitd4.getContainerForID("Obj1");
				DataContainer aic5 = aitd5.getContainerForID("Obj1");
				//nm.generateLayoutsForNodes(temp);
				//pan.setLegendData(nm.getLayoutForNode("Obj3"), nm.getNode("Obj3"));				
			}
			else
			{
				//LegendPainter lp = new LegendPainter(pan);
				//lp.paintLegendToFile(new File("/home/thomas/Temp/Test2.svg"));
				//DataSetAdderGUI AddDataset = new DataSetAdderGUI(cySwingApp, dsm, util);				
				//AddDataset.setVisible(true);				
				//DataSetControlPanel mnb = new DataSetControlPanel(cySwingApp, dsm, nm,app.getVisualStyle());
				//mnb.setVisible(true);
				//Vector<DataSet> temp = new Vector<DataSet>();				
				//pan.setLegendData(nm.getLayoutForNode("Obj1"), nm.getNode("Obj1"));
			}
			//dsm.saveData("DataSets.zip", "DataSetProperties");

			/*List<File> SourceFiles = new LinkedList<>();
			ZipFile zf = new ZipFile(new File("DataSets.zip"));
			File properties = new File("DataSetProperties");
			ZipEntry entry;
			Enumeration entries = zf.entries();
			int BUFFER = 2048;
			while(entries.hasMoreElements())
			{
				entry = (ZipEntry) entries.nextElement();
				File cfile = new File(System.getProperty("java.io.tmpdir") + File.separator + entry.getName());
				System.out.println("The name of the zip entry is " + entry.getName());
				FileOutputStream os = new FileOutputStream(cfile);
				BufferedOutputStream bos = new BufferedOutputStream(os);
				BufferedInputStream bf = new BufferedInputStream(zf.getInputStream(entry));
				byte data[] = new byte[BUFFER];
				int count ;
				while ((count = bf.read(data, 0, BUFFER)) 
						!= -1) {
					bos.write(data, 0, count);
				}
				bos.flush();
				bos.close();				
				bf.close();
				SourceFiles.add(cfile);
			}
			dsm.readDataSets(properties, SourceFiles);*/



			//nm.generateLayoutsForNodes(dsm.getDataSets());
			//NodeLayout layout = nm.getLayoutForNode("Obj4");
			//Collection<NodeData> data = nm.getNode("Obj4").getData();




			// Do some drawing.

			/*Shape circle = new Ellipse2D.Double(0, 0, 50, 50);	    
			    g.setPaint(Color.red);	    
			    g.setStroke(new BasicStroke(5));
			    g.draw(circle);
			    g.setStroke(new BasicStroke());
			    g.translate(60, 60);
			    g.setPaint(Color.green);
			    g.fill(circle);
			    g.translate(-60, -60);
			    g.translate(60, 0);
			    g.setStroke(new BasicStroke(5));
			    g.setPaint(Color.blue);
			    g.fill(circle);
			    g.setSVGCanvasSize(new Dimension(180, 50));
			 */
			// Populate the document root with the generated SVG content.
			//JPanel ContentPane = new JPanel();
			//ContentPane.setPreferredSize(new Dimension(300,2000));
/*
			ContentPane.setBackground(Color.black);
			GridBagConstraints gbc1 = new GridBagConstraints();
			gbc1.weighty = 1;
			gbc1.weightx = 1;
			gbc1.fill = GridBagConstraints.BOTH;

			DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
			String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
			SVGDocument doc = (SVGDocument) impl.createDocument(svgNS, "svg", null);			    
			// Create a converter for this document.
			SVGGraphics2D g = new SVGGraphics2D(doc);
			//PrintFDebugger.Debugging(this,"Asking for Node with ID : " + builder.dssm.getSelectedDataSets().get(0).getNodeIDs().iterator().next());
			layout.layoutLegendNode(data, g);			
			g.setSVGCanvasSize(new Dimension(300,(int)(METANODEPROPERTIES.IMAGEHEIGHT * ((double)METANODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH/METANODEPROPERTIES.IMAGEWIDTH))));
			Element root = doc.getDocumentElement();
			g.getRoot(root);
			root.setAttribute("viewBox", "0 0 400 270");
			JSVGCanvas canvas = new JSVGCanvas();
			canvas.setAlignmentY(canvas.TOP_ALIGNMENT);
			canvas.setSVGDocument(doc);
			canvas.addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					PrintFDebugger.Debugging(this,"New Preferred Size is " + ((JComponent)e.getSource()).getPreferredSize());
					//int width = Math.max(((JComponent)e.getSource()).getSize().width,METANODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH);
					//int preferredHeight = Math.
			
				}
			});
			PrintFDebugger.Debugging(this, "" + canvas.getPreferredSize());
			//canvas.setPreferredSize(new Dimension(METANODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH,(int)(METANODEPROPERTIES.IMAGEHEIGHT * ((double)METANODEPROPERTIES.IMAGEWIDTH/METANODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH))));
			//canvas.setMaximumSize(new Dimension(Short.MAX_VALUE,270));
			//canvas.setMinimumSize(new Dimension(METANODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH,(int)(METANODEPROPERTIES.IMAGEHEIGHT * ((double)METANODEPROPERTIES.IMAGEWIDTH/METANODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH))));
			JFrame f = new JFrame();

			ContentPane.setLayout(new BoxLayout(ContentPane,BoxLayout.PAGE_AXIS));
			ContentPane.add(canvas);
			OutputStream os = new FileOutputStream("/home/thomas/Temp/Node.svg");
			Writer w = new OutputStreamWriter(os,"UTF-8");
			g.stream(root,w);
			w.close();
			os.close();
			JScrollPane scroller = new JScrollPane(ContentPane);
			//scroller.setPreferredSize(new Dimension(100,0));

			scroller.setBorder(null);
			scroller.setViewportBorder(null);
			//f.getContentPane().add(canvas);

			Insets InnerInsets = new Insets(0,0,0,0);
			Insets OuterInsets = new Insets(2,0,0,0);
			for(DataSet ds : dsm.getDataSets())
			{
			//	JPanel DataSetPane = ds.getDataSetDescriptionPane(scroller, layout.getDataSetLabel(ds));
			//	ContentPane.add(Box.createRigidArea(new Dimension(0,2)));
			//	ContentPane.add(DataSetPane);
				JPanel DataSetPane = new JPanel();
				DataSetPane.setLayout(new BoxLayout(DataSetPane, BoxLayout.PAGE_AXIS));		
				JTextPane area = new JTextPane();
				scroller.addComponentListener(new TextPaneResizer(area));
				area.setPreferredSize(new Dimension());
				area.setText(ds.Description);
				area.setEditable(false);
				area.setFont(area.getFont().deriveFont(Font.BOLD,22f));
				area.setBorder(null);
				area.setMargin(InnerInsets);

				DataSetPane.add(area);
				DataSetPane.add(Box.createRigidArea(new Dimension(0,2)));//ContentPane.add(Box.createVerticalGlue());
				ItemDataDescription idd = new ItemDataDescription();				
				idd.setupItemDescription(ds.getDataForID("Obj1"), layout.getDataSetLabel(ds));
				DataSetPane.add(idd);
				DataSetPane.add(Box.createVerticalGlue());
				DataSetPane.add(ds.getColorMap());
				
			}*/
			//PrintFDebugger.Debugging(this, "Got a preferred height of " + preferredHeight(ContentPane));
			//f.add(scroller);
			//f.pack();
			//f.setSize(new Dimension(300,800));

			//f.setVisible(true);



		}
		catch(Exception ex)
		{
			System.out.println("Caught exception: Stack");
			ex.printStackTrace(System.out);
			System.out.println("Cause:");
			ex.getCause().printStackTrace(System.out);
		}


	}

	public static int preferredHeight(JComponent comp)
	{
		int res = 0;
		for(Component ccomp : comp.getComponents())
		{
			if(ccomp.isVisible())
			{
				res += comp.getPreferredSize().height;
			}
		}
		return res;
	}

	
}
