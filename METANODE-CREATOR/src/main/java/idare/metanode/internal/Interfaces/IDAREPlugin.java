package idare.metanode.internal.Interfaces;

import idare.metanode.IDAREMetaNodeApp;

public interface IDAREPlugin{
	
	/**
	 * DeRegister from a given app (this is necessary for the app to perform a cleanup).
	 * The plug-in should ensure, that it properly deregisters any additions (like DataSetPropertyAdditions or DataSet Additions).
	 */
	public void deRegister(IDAREMetaNodeApp app);
	/**
	 * Register with a given app.
	 */	
	public void register(IDAREMetaNodeApp app);
}
