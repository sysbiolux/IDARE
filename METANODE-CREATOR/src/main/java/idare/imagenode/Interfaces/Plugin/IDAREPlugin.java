package idare.imagenode.Interfaces.Plugin;

import java.util.Vector;

public interface IDAREPlugin{
	

	/**
	 * Provide all instances of IDAREInterfaces this plugin shall provide to the app.
	 * They have to be in the order in which they need to be registered. i.e. if a DataSetType is provided after corresponding properties this might fail.
	 * @return the services that this plugin provides. 
	 */
	public Vector<IDAREService> getServices();
}
