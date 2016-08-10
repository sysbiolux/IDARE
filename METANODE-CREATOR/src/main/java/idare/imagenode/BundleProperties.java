package idare.imagenode;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * 
 * Code mainly taken from cy3sbml.
 *
 */
public class BundleProperties {
	private String name;
	private String version;
	
	public BundleProperties(BundleContext bc){
		Bundle bundle = bc.getBundle();
		name = bundle.getSymbolicName();
		version = bundle.getVersion().toString();
	}
	
	/** {name}-v{version} of bundle. */
	public String getInfo(){
		return getName() + "-v" + getVersion();
	}
	
	/** Name of bundle. */
	public String getName(){
		return name;
	}
	
	/** Version of bundle. */
	public String getVersion(){
		return version;
	}
	
}
