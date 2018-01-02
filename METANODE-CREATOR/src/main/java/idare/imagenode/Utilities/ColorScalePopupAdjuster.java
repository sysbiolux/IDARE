package idare.imagenode.Utilities;
import idare.ThirdParty.BoundsPopupMenuListener;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboPopup;


/**
 * A PopupAdjuster for ColorScales, that updates the size of the colorscale being displayed, when selecting colors.
 * @author Thomas Pfau
 *
 */
public class ColorScalePopupAdjuster extends BoundsPopupMenuListener {

	int panecount;
	/**
	 * Default Constructor indicating how many panes to use.
	 * @param panecount the number of panes to use.
	 */
	public ColorScalePopupAdjuster(int panecount)
	{
		super(true,false);
		this.panecount = panecount;
	}
	/**
	 * Costumize the Popup, by first adjusting the height according to the number of panes set for this {@link ColorScalePopupAdjuster}
	 */
	protected void customizePopup(BasicComboPopup popup)
	{
		adjustPopupHeight(popup);
		super.customizePopup(popup);
		popup.revalidate();
	}
	/**
	 * Adjust the height of a Popup.
	 * @param popup
	 */
	private void adjustPopupHeight(BasicComboPopup popup)
	{
		
		Component comboBox = popup.getInvoker();	
		popup.setLayout(new BoxLayout(popup, BoxLayout.PAGE_AXIS));
		
		if(comboBox.isShowing())
		{
			
			int adjustedHeight = Math.max(20, popup.getPreferredSize().height/panecount);			
			popup.getList().setFixedCellHeight(adjustedHeight);		
			JScrollPane c = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, popup.getList());
			Dimension preferredDim = popup.getList().getPreferredSize();
			c.setPreferredSize(new Dimension(preferredDim.width,Math.min(200,preferredDim.height)));
			c.setPreferredSize(new Dimension(c.getWidth(),popup.getList().getPreferredSize().height));
		}
	}
	
	/**
	 * Change the number of items that are being adjusted by this adjuster
	 * @param count The number of items adjusted by this adjuster.
	 */
	public void changeItemCount(int count)
	{
		panecount = count;
	}
}
