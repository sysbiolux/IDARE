package idare.metanode.internal.Data.ValueSetData.GraphData;

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

import idare.metanode.Data.BasicDataTypes.ValueSetData.SetDataDescription;
import idare.metanode.Data.BasicDataTypes.ValueSetData.ValueSetDataSet;
import idare.metanode.Data.BasicDataTypes.ValueSetData.ValueSetNodeData;
import idare.metanode.Data.BasicDataTypes.itemizedData.AbstractItemDataSet;
import idare.metanode.Interfaces.DataSets.DataContainer;
import idare.metanode.Interfaces.DataSets.DataSet;
import idare.metanode.Interfaces.DataSets.NodeData;
import idare.metanode.Interfaces.Layout.DataSetProperties;
import idare.metanode.Properties.Localisation.Position;
import idare.metanode.internal.ColorManagement.ColorMap;
import idare.metanode.internal.GUI.Legend.Utilities.TextPaneResizer;
import idare.metanode.internal.Utilities.GUIUtils;
import idare.metanode.internal.exceptions.io.WrongFormat;

public class GraphDataSetProperties extends DataSetProperties {

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
		return new GraphContainer(data.getDataSet(), (ValueSetNodeData)data);
	}

	@Override
	public DataContainer newContainerForData(NodeData data) {
		// TODO Auto-generated method stub
		return new GraphContainer(data.getDataSet(), (ValueSetNodeData)data);
	}

	@Override
	public String getTypeName() {
		// TODO Auto-generated method stub
		return "Graph";
	}

	public String toString()
	{
		return "Graph";
	}
	
	


	@Override
	public void testValidity(DataSet set) throws WrongFormat {
		// TODO Auto-generated method stub
		try{
			ValueSetDataSet vds = (ValueSetDataSet) set;
			if(!vds.numericheaders)
			{				
				throw new WrongFormat("String headers not allowed in a graph dataset must use numeric values");
			}
			
		}
		catch(ClassCastException e)
		{
			throw new WrongFormat("Cannot create a Graph Dataset on this type of data.");
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
		SetDataDescription desc = new LineDescription(Legend);
		desc.setupItemDescription((ValueSetDataSet)set,map);		
		DataSetPane.add(desc);
		desc.setVisibleWidth(Legend.getViewport().getWidth()-2);
		return DataSetPane;

	}
	@Override
	public Collection<Class<? extends DataSet>> getWorkingClassTypes()
	{
		Vector<Class<? extends DataSet>> acceptableclasses = new Vector<Class<? extends DataSet>>();
		acceptableclasses.add(ValueSetDataSet.class);
		return acceptableclasses;
	}

	
}
