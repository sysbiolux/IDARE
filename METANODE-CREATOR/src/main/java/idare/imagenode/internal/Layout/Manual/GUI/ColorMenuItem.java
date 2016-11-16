package idare.imagenode.internal.Layout.Manual.GUI;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import idare.imagenode.ColorManagement.ColorScalePane;

public class ColorMenuItem extends JMenuItem {

	private ColorScalePane pane;
	
	
	public ColorMenuItem(ColorScalePane pane)
	{
		super(new ImageIcon(pane.getImage(100, 20)));
		this.pane = pane;
		this.setContentAreaFilled(true);
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.drawImage(pane.getImage(getWidth()-4, getHeight()-2), 1, 1, null);				
	}
	public ColorScalePane getPane()
	{
		return pane;
	}
}
