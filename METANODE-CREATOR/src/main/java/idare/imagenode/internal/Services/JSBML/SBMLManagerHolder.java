package idare.imagenode.internal.Services.JSBML;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.sbml.jsbml.SBMLReader;

import idare.subnetwork.internal.Tasks.InvalidSelectionException;

public class SBMLManagerHolder {

	Object SBMLMgr;
	protected final FileUtil fUtil;
	protected CySwingApplication swingApp;
	BundleContext appContext;
	private String SBMLstatus = "";
	public SBMLManagerHolder(FileUtil fileUtil, CySwingApplication cySwingApp, BundleContext context)
	{
		this.appContext = context;				
		this.swingApp = cySwingApp;
		this.fUtil = fileUtil;
	}
	
	public boolean isSBMLManagerPresent()
	{
		if(SBMLMgr == null)
		{
			ServiceReference ref = appContext.getServiceReference("org.cy3sbml.SBMLManager");
			if(ref != null)
			{
				SBMLMgr = appContext.getService(ref);
			}
		}
		
		return SBMLMgr != null;			
	}
	
	public void setObject(Object o)
	{		
		SBMLMgr = o;
	}

	public SBMLDocument readSBML(CyNetwork network)
	{		
		SBMLstatus = "";
		String[] extensions = {"sbml","xml"};		
		SBMLDocument doc = null;
		if(isSBMLManagerPresent())
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
				SBMLstatus+= "Problem with cy3sbml: " + e.getClass().getSimpleName() + " : " + e.getMessage() + "\n Full Stacktrace available in the log file \n";
			}

		}
		if(doc == null)
		{
			try{
				File SBMLFile = fUtil.getFile(swingApp.getJFrame(), "Select sbml File for properties", FileUtil.LOAD,Collections.singletonList(new FileChooserFilter("SBML Files",extensions)));
				if(SBMLFile == null)
				{
					throw new InvalidSelectionException("No SBML File Selected to obtain the annotation from");
				}
				SBMLReader sr = new SBMLReader();
				Object sbmldoc = sr.readSBML(SBMLFile);
				if(sbmldoc != null)
				{
					doc =  new SBMLDocument(sbmldoc);
				}
			}
			catch(Exception e)
			{
				SBMLstatus+= "Could not read SBML due to: " + e.getClass().getSimpleName() + " : " + e.getMessage() + "\n Full Stacktrace available in the log file \n";
				e.printStackTrace(System.out);
			}
		}
		return doc;
	}
	
	public String getSBMLStatus()
	{
		return SBMLstatus;
	}


}
