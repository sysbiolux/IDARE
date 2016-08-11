package idare.imagenode.internal.Data.ValueSetData.GraphData;

import idare.imagenode.Data.BasicDataTypes.ValueSetData.SetDataDescription;
import idare.imagenode.Data.BasicDataTypes.ValueSetData.ValueSetDataProperties;
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
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;

import org.apache.poi.ss.usermodel.Header;

public class GraphDataSetProperties extends ValueSetDataProperties {

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
			else
			{
				for(String sheet : vds.getSetNames())
				{
					Vector<Comparable> header = vds.getHeadersForSheet(sheet);
					Set<Comparable> uniqueHeaders = new HashSet<Comparable>(header);
					if(uniqueHeaders.size() < header.size())
					{
						throw new WrongFormat("Graph visualisation is incompatible with non unique headers in a single sheet.");		
					}					
				}
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
