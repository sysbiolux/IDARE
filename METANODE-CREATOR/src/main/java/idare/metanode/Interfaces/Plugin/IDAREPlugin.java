package idare.metanode.Interfaces.Plugin;

import java.util.Collection;
import java.util.Vector;

public interface IDAREPlugin{
	
	/**
	 * DeRegister from a given app (this is necessary for the app to perform a cleanup).
	 * The plug-in should ensure, that it properly deregisters any additions (like DataSetPropertyAdditions or DataSet Additions).
	 */
	//public void deRegister(IDAREMetaNodeApp app);
	/**
	 * Register with a given app.
	 */	
	//public void register(IDAREMetaNodeApp app);
	
	/**
	 * Provide all instances of IDAREInterfaces this plugin shall provide to the app.
	 * They have to be in the order in which they need to be registered. i.e. if a DataSetType is provided after corresponding properties this might fail. 
	 */
	public Vector<IDAREService> getServices();
}
