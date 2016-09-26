package idare.imagenode.internal.Services.JSBML;

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
		holder = new SBMLManagerHolder(fileUtil, cySwingApp,context);
		appContext = context;
		ref = context.getServiceReference("org.cy3sbml.SBMLManager");
		if(ref != null)
		{
//			System.out.println("SBMLServiceReg: Found a SBMLMAnager, using it");
			holder.setObject(appContext.getService(ref));
		}
	}
	
	public SBMLManagerHolder getHolder()
	{
		return holder;
	}

	
	@Override
	public void serviceChanged(ServiceEvent event) {

		String[] objectclass = (String[]) event.getServiceReference().getProperty("objectClass");					
		
		if(event.getType() == ServiceEvent.REGISTERED)
		{
			if(objectclass[0] == "org.cy3sbml.SBMLManager")
			{	
				ref = event.getServiceReference();
				Object mgr = appContext.getService(ref);
				holder.setObject(mgr);					
			}
		}
		else if(event.getType() == ServiceEvent.UNREGISTERING)
		{
				if(objectclass[0] == "org.cy3sbml.SBMLManager")
				{
					holder.setObject(null);					
				}
			
			
		}
	}

	
	
}
