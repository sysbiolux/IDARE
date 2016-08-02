package idare.metanode.internal.Data.ValueSetData;

import idare.ThirdParty.TextPaneEditorKit;
import idare.metanode.internal.Debug.PrintFDebugger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;

public abstract class SetEntryDescriptionPane extends JPanel{
	private static final long serialVersionUID = 1001;
	protected JPanel entry;
	protected JTextPane description;
	private int minimalDescriptionWidth = 260;
	private int minimalEntryWidth = 40;		

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
		//SimpleAttributeSet attribs = new SimpleAttributeSet();
		//StyleConstants.setAlignment(attribs,StyleConstants.ALIGN_CENTER);			
		description.setText(descriptionString);				
		description.setBackground(Color.white);				
		description.setFont(description.getFont().deriveFont(20f));				
		//StyledDocument doc = (StyledDocument)Description.getDocument();
		//doc.setParagraphAttributes(0, doc.getLength()-1, attribs, false);
		PrintFDebugger.Debugging(this, "Creating Entry");
		entry = getEntry(entryColor);
		PrintFDebugger.Debugging(this, "Entry created");
		entry.setBackground(Color.white);			
		PrintFDebugger.Debugging(this, "Entry background set to white");
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
			//					PrintFDebugger.Debugging(this, "Setting Size of Description to " + minimaldescriptionwidth + "/" +getDescriptionHeight(minimaldescriptionwidth) + "and Line to " + (minimalLineWidth) + "/"	+20);

		}
		else
		{
			int DescriptionWidth = (int) (width * (double)minimalDescriptionWidth / (300));					
			entry.setPreferredSize(new Dimension(width - DescriptionWidth,20));
			description.setPreferredSize(new Dimension(DescriptionWidth,getDescriptionHeight(DescriptionWidth)));
			//					PrintFDebugger.Debugging(this, "Setting Size of Description to " + DescriptionWidth + "/" +getDescriptionHeight(DescriptionWidth) + "and Line to " + (width - DescriptionWidth) + "/"	+20);
		}
		description.invalidate();
		entry.invalidate();
		revalidate();
		repaint();

	}
	/**
	 * Get the description height of this LineDescriptions textual description.
	 * @param width - the width to obtain a height for.
	 * @return - the height for the given width.
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

	public abstract SetEntryPanel getEntry(Color entrycolor);		
}

