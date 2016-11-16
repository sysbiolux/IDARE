package idare.imagenode.internal.ImageManagement;

import java.util.Vector;

import idare.imagenode.Interfaces.Plugin.IDAREPlugin;
import idare.imagenode.internal.IDAREService;
import idare.imagenode.internal.Layout.Automatic.AutomaticNodeLayout;
import idare.imagenode.internal.Layout.Manual.ManualNodeLayout;

public class DefaultLayoutProvider implements IDAREPlugin {

	@Override
	public Vector<IDAREService> getServices() {

		Vector<IDAREService> layoutservices = new Vector<IDAREService>();
		layoutservices.add(new AutomaticNodeLayout());
		layoutservices.add(new ManualNodeLayout());
		return layoutservices;	
	}

}
