package idare.metanode.internal.Services.JSBML;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.FileUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class SBMLServiceRegistrar implements
		ServiceListener {
	SBMLManagerHolder holder;
	BundleContext appContext;
	private ServiceReference ref;
	
	public SBMLServiceRegistrar(BundleContext context, FileUtil fileUtil, CySwingApplication cySwingApp)
	{
		super();
		holder = new SBMLManagerHolder(fileUtil, cySwingApp);
		appContext = context;
		ref = context.getServiceReference("org.cy3sbml.SBMLManager");
		if(ref != null)
		{
			System.out.println("SBMLServiceReg: Found a SBMLMAnager, using it");
			holder.setObject(appContext.getService(ref));
		}
	}
	
	public SBMLManagerHolder getHolder()
	{
		return holder;
	}

	
	@Override
	public void serviceChanged(ServiceEvent event) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		String[] objectclass = (String[]) event.getServiceReference().getProperty("objectClass");			
		String res = "";
		for( String cclass : objectclass)
		{
			res += cclass + "\n";
		}
		//System.out.println("Obtaining a registry event for the for the following clases:\n " + res);
		
		if(event.getType() == ServiceEvent.REGISTERED)
		{
			if(objectclass[0] == "org.cy3sbml.SBMLManager")
			{	
				System.out.println("SBMLServiceReg:  Found a SBMLMAnager, registering it");
				ref = event.getServiceReference();
				Object mgr = appContext.getService(ref);
				holder.setObject(mgr);					
			}
		}
		else if(event.getType() == ServiceEvent.UNREGISTERING)
		{
				if(objectclass[0] == "org.cy3sbml.SBMLManager")
				{
					System.out.println("SBMLServiceReg: SBMLManager deregistered, removing it");									
					holder.setObject(null);					
				}
			
			
		}
	}

	
	
}
