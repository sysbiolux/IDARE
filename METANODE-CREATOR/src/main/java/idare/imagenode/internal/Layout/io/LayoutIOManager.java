package idare.imagenode.internal.Layout.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.ContainerLayout;
import idare.imagenode.Utilities.EOOMarker;
import idare.imagenode.internal.DataManagement.DataSetManager;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;
import idare.imagenode.internal.Layout.ImageNodeLayout;

public class LayoutIOManager {

	
	
	HashMap<String,Class<? extends ImageNodeLayout>> layoutclasses = new HashMap<>();
	
	
	/**
	 * Register a specific type of Layout.
	 * This will be stored using its name and can be retrieved in the same way.
	 */
	public void registerLayout(ImageNodeLayout layout)
	{
		layoutclasses.put(layout.getClass().getName(), layout.getClass());
	}
	
	/**
	 * DeRegister a specific type of Layout, it will no longer be available. 
	 */
	public void deRegisterLayout(ImageNodeLayout layout)
	{
		layoutclasses.remove(layout.getClass().getName());
	}
	
	public ImageNodeLayout readLayout(ObjectInputStream os, Object currentobject, DataSetManager dsm) throws IOException
	{
		String clazzname = (String) currentobject;
		try{
			if(!layoutclasses.containsKey(clazzname))
			{
				PrintFDebugger.Debugging(this, "No layout type of class " + clazzname + " Registered with the app.");
				return null;
			}
			ImageNodeLayout layout = layoutclasses.get(clazzname).newInstance();
			
			boolean readsuccess = layout.readLayout(dsm, os, os.readObject());
			if(readsuccess)
			{
				return layout;
			}
			else
			{
				return null;
			}
		}
		catch(ClassNotFoundException | InstantiationException | IllegalAccessException e)
		{
			PrintFDebugger.Debugging(e, "Could not read layout due to the exception.");
			e.printStackTrace(System.out);
			return null;
		}
	}
	
	public void writeLayout(ImageNodeLayout layout, ObjectOutputStream os) throws IOException
	{		
		os.writeObject(layout.getClass().getName());
		layout.writeLayout(os);
	}
	
}
