package idare.imagenode.internal.Layout.Manual.GUI;

import java.awt.Dimension;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import idare.ThirdParty.BoundsPopupMenuListener;

public class NodeIDSelectorToolBarEntry extends JPanel {
	JComboBox<String> idOptions;	
	JLabel idLabel = new JLabel("Visualised Node:");
	
	public NodeIDSelectorToolBarEntry(Vector<String> availableIDs)
	{
		idOptions = new JComboBox<>(availableIDs);
		idOptions.addPopupMenuListener(new BoundsPopupMenuListener(true,false));
		idOptions.setPreferredSize(new Dimension(200,50));
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(idLabel);
		this.add(idOptions);		
	}
	
	public void addSelectionListener(ItemListener listener)
	{
		idOptions.addItemListener(listener);
	}
		
	}
