package idare.imagenode.internal.Data.ValueSetData.ScatterData;

import idare.imagenode.Data.BasicDataTypes.ValueSetData.ValueSetDataSet;
import idare.imagenode.Data.BasicDataTypes.ValueSetData.ValueSetNodeData;
import idare.imagenode.Interfaces.DataSets.DataContainer;
import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Interfaces.Layout.DataSetProperties;
import idare.imagenode.Properties.Localisation.Position;
import idare.imagenode.internal.ColorManagement.ColorMap;
import idare.imagenode.internal.GUI.Legend.Utilities.TextPaneResizer;
import idare.imagenode.internal.Utilities.GUIUtils;
import idare.imagenode.internal.exceptions.io.WrongFormat;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.Collection;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;

public abstract class ScatterDataSetProperties extends DataSetProperties {

	@Override
	public Position getLocalisationPreference() {
		// TODO Auto-generated method stub
		return Position.CENTER;
	}

	@Override
	public boolean getItemFlexibility() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataContainer newContainerInstance(DataSet origin, NodeData data) {
		// TODO Auto-generated method stub
		return new ScatterContainer(data.getDataSet(), (ValueSetNodeData)data, getLabelSize());
	}

	@Override
	public DataContainer newContainerForData(NodeData data) {
		// TODO Auto-generated method stub
		return new ScatterContainer(data.getDataSet(), (ValueSetNodeData)data, getLabelSize());
	}

	@Override
	public String getTypeName() {
		// TODO Auto-generated method stub
		return "Scatter";
	}

	public String toString()
	{
		return "Scatter";
	}
	
	


	@Override
	public void testValidity(DataSet set) throws WrongFormat {
		// TODO Auto-generated method stub
		try{
			ValueSetDataSet vds = (ValueSetDataSet) set;
			if(vds.stringheaders || vds.mixedheaders)
			{				
				if(vds.getAllHeaders().size() > 10)
				{
					throw new WrongFormat("At most 10 Columns are allowed with String Headers");
				}
			}
			
		}
		catch(ClassCastException e)
		{
			throw new WrongFormat("Cannot create a Scatter Dataset on this type of data.");
		}
		
	}
	@Override
	public JPanel getDataSetDescriptionPane(JScrollPane Legend, String DataSetLabel, ColorMap map, DataSet set)
	{
		Insets InnerInsets = new Insets(0,0,0,0);
		JPanel DataSetPane = new JPanel();
		DataSetPane.setLayout(new BoxLayout(DataSetPane, BoxLayout.PAGE_AXIS));		
		JTextPane area = new JTextPane();
		DefaultCaret caret = (DefaultCaret)area.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		Legend.addComponentListener(new TextPaneResizer(area));
		area.setPreferredSize(new Dimension());
		area.setText(DataSetLabel + ": " + set.Description);
		area.setEditable(false);
		area.setFont(area.getFont().deriveFont(Font.BOLD,22f));
		area.setBorder(null);
		area.setMargin(InnerInsets);
		area.setPreferredSize(GUIUtils.getPreferredSize(area, Legend.getViewport().getSize(), 300));
		DataSetPane.add(area);
		DataSetPane.add(Box.createVerticalGlue());
		ValueSetDataSet cset = (ValueSetDataSet)set;		
		if(cset.stringheaders)
		{
			//Create a ItemDescriptionPane for the different Markers.
			HeaderDescription hdesc = new HeaderDescription();
			hdesc.setupItemDescription(set.getDefaultData(), DataSetLabel, Legend);
			DataSetPane.add(hdesc);
			DataSetPane.add(Box.createVerticalGlue());	
		}
		
		MarkerDataDescription desc = new MarkerDataDescription(Legend);
		desc.setupItemDescription((ValueSetDataSet)set,map);
		DataSetPane.add(desc);						
		desc.setVisibleWidth(Legend.getViewport().getWidth()-2);
		return DataSetPane;
	}
	
	/**
	 * Get the size of labels for these Properties.
	 * @return
	 */
	protected abstract int getLabelSize();
	
	@Override
	public Collection<Class<? extends DataSet>> getWorkingClassTypes()
	{
		Vector<Class<? extends DataSet>> acceptableclasses = new Vector<Class<? extends DataSet>>();
		acceptableclasses.add(ValueSetDataSet.class);
		return acceptableclasses;
	}
	
}
