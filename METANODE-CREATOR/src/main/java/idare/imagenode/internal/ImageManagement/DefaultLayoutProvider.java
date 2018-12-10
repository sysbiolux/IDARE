package idare.imagenode.internal.ImageManagement;

import java.util.Vector;

import idare.imagenode.Interfaces.Plugin.IDAREPlugin;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.internal.IDAREService;
import idare.imagenode.internal.Layout.Automatic.AutomaticNodeLayout;
import idare.imagenode.internal.Layout.Manual.ManualNodeLayout;

public class DefaultLayoutProvider implements IDAREPlugin {

	@Override
	public Vector<IDAREService> getServices() {

		Vector<IDAREService> layoutservices = new Vector<IDAREService>();
		layoutservices.add(new AutomaticNodeLayout(IMAGENODEPROPERTIES.IMAGEHEIGHT,IMAGENODEPROPERTIES.IMAGEWIDTH,IMAGENODEPROPERTIES.LABELHEIGHT));
		layoutservices.add(new ManualNodeLayout(IMAGENODEPROPERTIES.IMAGEHEIGHT,IMAGENODEPROPERTIES.IMAGEWIDTH,IMAGENODEPROPERTIES.LABELHEIGHT));
		return layoutservices;	
	}

}
