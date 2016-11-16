package idare.imagenode.internal.Layout.Manual.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import idare.imagenode.ColorManagement.ColorMap;
import idare.imagenode.ColorManagement.ColorScalePane;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.Layout.DataSetLayoutProperties;
import idare.imagenode.exceptions.layout.WrongDatasetTypeException;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.imagenode.internal.Layout.DataSetLayoutInfoBundle;
import idare.imagenode.internal.Layout.Manual.LayoutGUI;

public class DatasetMenu extends JMenu implements MenuListener, ActionListener{
	
	
	DataSet set;	
	HashMap<ColorScalePane,ColorMap> mapping = new HashMap<>();
	LayoutGUI gui;
	DataSetLayoutProperties props;
	
	public DatasetMenu(DataSet set,LayoutGUI gui)
	{
		super(set.Description);
		this.gui = gui;
		this.set = set;
		props = set.getPropertyOptions().firstElement();
		
		this.set = set;
		HashMap<ColorMap,ColorScalePane> panes = new HashMap();
		for(ColorMap map : set.getColorMapOptions())
		{
			panes.put(map, map.getColorScalePane());
			mapping.put(panes.get(map),map);
		}
		for(DataSetLayoutProperties props : set.getPropertyOptions())
		{
			PropertyMenu submenu = new PropertyMenu(props);
			submenu.addMenuListener(this);
			for(ColorMap map : panes.keySet())
			{
				ColorMenuItem item = new ColorMenuItem(panes.get(map));				
				item.addActionListener(this);
				submenu.add(item);
			}
			this.add(submenu);
		}
	}
	
	
	private class PropertyMenu extends JMenu
	{
		
		DataSetLayoutProperties props;
		
		public PropertyMenu(DataSetLayoutProperties properties )
		{			
			super(properties.getTypeName());
			this.props = properties;
		}
		
		DataSetLayoutProperties getProperties()
		{
			return props;
		}
	}

	@Override
	public void menuSelected(MenuEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() instanceof PropertyMenu)
		{
			PropertyMenu propmenu = (PropertyMenu) e.getSource();
			props = propmenu.getProperties();
		}
	}


	@Override
	public void menuDeselected(MenuEvent e) {
		// TODO Auto-generated method stub
		//do nothing
	}


	@Override
	public void menuCanceled(MenuEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if( e.getSource() instanceof ColorMenuItem)
		{
			
			try{
				DataSetLayoutInfoBundle bundle = new DataSetLayoutInfoBundle();
				bundle.colormap = mapping.get(((ColorMenuItem)e.getSource()).getPane());
				PrintFDebugger.Debugging(this, "Selected Map is " + bundle.colormap);
				bundle.dataset = set;
				bundle.properties = props;
				gui.createFrame(bundle);
			}
			catch(WrongDatasetTypeException ex)
			{
				JOptionPane.showMessageDialog(gui, "Could not use the type of layout for this dataset","Invalid layout type",JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	
}
