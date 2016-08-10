package idare.imagenode.internal.Data.ValueSetData.ScatterData;

import idare.imagenode.Data.BasicDataTypes.ValueSetData.ValueSetDataSet;
import idare.imagenode.Data.BasicDataTypes.itemizedData.ItemDataDescription;
import idare.imagenode.Interfaces.DataSets.NodeData;
import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.internal.Utilities.LayoutUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JScrollPane;

public class HeaderDescription extends ItemDataDescription {
	
	/**
	 * Set up the Description for a specific set of data and a given DataSetLabel.
	 * Adjust the size accoring to changes in the Legend provided.
	 * @param currentdata
	 * @param DataSetLabel
	 * @param Legend
	 */
	@Override
	public void setupItemDescription(NodeData currentdata, String DataSetLabel, JScrollPane Legend) {
		//First, get the size of the maximal Item to determine the number of rows.
		this.setBackground(Color.white);
		Legend.addComponentListener(new ItemDescriptionResizeListener(this));		
		ValueSetDataSet set = (ValueSetDataSet) currentdata.getDataSet();
		Itemdescriptions = new Vector<ItemDescriptionPane>();		
		int maxwidth = 0;
		Vector<Comparable> headers = set.getAllHeaders();
		HashMap<Comparable,String> headerlabels = LayoutUtils.getLabelsForData(headers);
		
		for(Comparable header : headers )
		{
			String Label = DataSetLabel + "." + headerlabels.get(header) + ":";
			String ItemLabel = header.toString();
			ItemDescriptionPane pane = new ItemDescriptionPane(Label,ItemLabel);
			maxwidth = Math.max(pane.getMinSize(), maxwidth);
			Itemdescriptions.add(pane);			
			maxitemwidth = maxwidth;
			//Use a maximum of 3 description columns. Any larger amount makes it hard to read.

		}
		//once we know the maximal width, we can determine the rows and columns, based on the available scrollpane viewport.		
		int cwidth = Legend.getViewport().getWidth()-2;
		Dimension dim = getRowsAndCols(cwidth);
		int columns =  dim.width; 		
		int rows = dim.height; 

		//Set up the Layout accordingly
		ComponentLayout = new GridLayout();
		ComponentLayout.setColumns(columns);
		ComponentLayout.setRows(rows);
		ComponentLayout.setVgap(VGAP);
		if(columns == 1)
		{
			ComponentLayout.setHgap(HGAP);
		}
		else{
		ComponentLayout.setHgap(Math.max(HGAP,((cwidth - columns * maxitemwidth)-10) / (columns-1)));
		}
		setLayout(ComponentLayout);
		//and add all the itemdescriptionpanes.
		for(ItemDescriptionPane pane : Itemdescriptions)
		{
			//first item left aligned, second item right aligned
			add(pane);
		}
		this.setPreferredSize(new Dimension(IMAGENODEPROPERTIES.LEGEND_DESCRIPTION_OPTIMAL_WIDTH,rows * MINIMAL_FONT_SIZE + (rows -1) * VGAP));
	}

}
