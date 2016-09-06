package idare.imagenode.internal.Services.JSBML;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.sbml.jsbml.SBMLReader;

public class SBMLManagerHolder {

	Object SBMLMgr;
	protected final FileUtil fUtil;
	protected CySwingApplication swingApp;

	public SBMLManagerHolder(FileUtil fileUtil, CySwingApplication cySwingApp)
	{
		this.swingApp = cySwingApp;
		this.fUtil = fileUtil;
	}
	public void setObject(Object o)
	{
		if(o == null)
		{
		}
		else
		{
		}

		SBMLMgr = o;
	}

	public SBMLDocument readSBML(CyNetwork network)
	{		
		String[] extensions = {"sbml","xml"};		
		SBMLDocument doc = null;
		if(SBMLMgr != null)
		{
//			System.out.println("IDARE: Found an SBMLManager object calling functions" );			
			try{
				Method m = SBMLMgr.getClass().getMethod("getSBMLDocument", CyNetwork.class);
				Object o = m.invoke(SBMLMgr, network);
				if( o != null)
				{
					doc = new SBMLDocument(o);
				}
			}
			catch(NoSuchMethodException|IllegalAccessException|InvocationTargetException e)
			{				
				e.printStackTrace(System.out);
				return null;
			}

		}
		if(doc == null)
		{
			File SBMLFile = fUtil.getFile(swingApp.getJFrame(), "Select sbml File for properties", FileUtil.LOAD,Collections.singletonList(new FileChooserFilter("SBML Files",extensions)));
			try{
				SBMLReader sr = new SBMLReader();
				doc =  new SBMLDocument(sr.readSBML(SBMLFile));
			}
			catch(Exception e)
			{
				//JOptionPane.showMessageDialog(null, "Could not read SBML File");
				e.printStackTrace(System.out);
				return null;
			}
		}
		return doc;
	}


}
