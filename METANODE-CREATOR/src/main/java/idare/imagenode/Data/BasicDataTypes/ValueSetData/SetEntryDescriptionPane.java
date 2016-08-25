package idare.imagenode.Data.BasicDataTypes.ValueSetData;

import idare.ThirdParty.TextPaneEditorKit;
import idare.imagenode.internal.GUI.Legend.IDARELegend;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;
/**
 * Abstract description of an Entry (i.e. one Sheet or set of data) from a {@link ValueSetDataSet} useable for a representation in a {@link IDARELegend}.
 * @author Thomas Pfau
 *
 */
public abstract class SetEntryDescriptionPane extends JPanel{
	private static final long serialVersionUID = 1001;
	protected JPanel entry;
	protected JTextPane description;
	private int minimalDescriptionWidth = 260;
	private int minimalEntryWidth = 40;		
	/**
	 * Default constructor receiving the color for this entry along with a descriptive String.
	 * @param entryColor
	 * @param descriptionString
	 */
	public SetEntryDescriptionPane(Color entryColor, String descriptionString)
	{
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 0.1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		setBackground(Color.WHITE);
		description = new JTextPane();
		DefaultCaret caret = (DefaultCaret)description.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		TextPaneEditorKit tek = new TextPaneEditorKit();
		description.setEditorKit(tek);
		description.setText(descriptionString);				
		description.setBackground(Color.white);				
		description.setFont(description.getFont().deriveFont(20f));				
		entry = getEntry(entryColor);
		entry.setBackground(Color.white);			
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(entry,gbc);
		gbc.weightx = 1;
		gbc.gridx = 1;
		add(description,gbc);
	}
	/**
	 * Update the preferredsize of this Line according to a provided width.
	 * 
	 * @param width the width to which adjust the height of the Component.
	 */
	public void updatePreferredSize(int width)
	{		
		if(width < 300)
		{
			entry.setPreferredSize(new Dimension(minimalEntryWidth,20));
			description.setPreferredSize(new Dimension(minimalDescriptionWidth,getDescriptionHeight(minimalDescriptionWidth)));

		}
		else
		{
			int DescriptionWidth = (int) (width * (double)minimalDescriptionWidth / (300));					
			entry.setPreferredSize(new Dimension(width - DescriptionWidth,20));
			description.setPreferredSize(new Dimension(DescriptionWidth,getDescriptionHeight(DescriptionWidth)));
		}
		description.invalidate();
		entry.invalidate();
		revalidate();
		repaint();

	}
	/**
	 * Get the description height of this LineDescriptions textual description.
	 * @param width the width to obtain a height for.
	 * @return the height for the given width.
	 */
	private int getDescriptionHeight(int width)
	{
		JTextPane dummyPane=new JTextPane();
		dummyPane.setFont(description.getFont());

		dummyPane.setSize(Math.max(minimalDescriptionWidth,width),Short.MAX_VALUE);
		dummyPane.setText(description.getText());		        		        			
		int preferredHeight = dummyPane.getPreferredSize().height;
		return preferredHeight;
	}
	/**
	 * Depending on the implementation, this function should return a SetEntryPanel that represents 
	 * the Entry using a color (e.g. the color of a line, the color and shape of the representing marker, the color used for a bar etc).  
	 * @param entrycolor
	 * @return A {@link SetEntryPanel} that represents the Layouted shape/color.
	 */
	public abstract SetEntryPanel getEntry(Color entrycolor);		
}

