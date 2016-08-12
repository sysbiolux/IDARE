package idare.imagenode.internal.Utilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIDefaults;
import javax.swing.text.JTextComponent;

/**
 * Class proviing static utilitits for multiple GUI classes.
 * @author Thomas Pfau
 *
 */
public class GUIUtils {
	/**
	 * Create a JTextPane that is uneditable and uses background and font as desired.
	 * @param DescriptionString
	 * @param background
	 * @param font
	 * @return An uneditable Textpane with the given DescriptionString, background color and font.
	 */
	public static JTextPane createSelectionDescription(String DescriptionString, Color background, Font font)
	{

		UIDefaults defaults = new UIDefaults();
		defaults.put("TextPane[Enabled].backgroundPainter", background);
		JTextPane selectionDesc = new JTextPane();
		selectionDesc.putClientProperty("Nimbus.Overrides", defaults);
		selectionDesc.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		selectionDesc.setAlignmentY(JTextPane.CENTER_ALIGNMENT);
		selectionDesc.setBackground(null);
		selectionDesc.setEditable(false);
		selectionDesc.setBorder(BorderFactory.createEmptyBorder());			
		selectionDesc.setOpaque(true);				
		selectionDesc.setText(DescriptionString);		
		selectionDesc.setFont(font);			
		selectionDesc.setPreferredSize(new Dimension(200,45));
		selectionDesc.setBackground(background);		
		return selectionDesc;		
	}
	/**
	 * Get the preferred size of a JTextComponent given a specific minimal width and its current dimension
	 * @param comp - 
	 * @param origDimension
	 * @param minwidth
	 * @return the preferred height for the provided {@link JTextComponent} assuming the given minimum width and the actual width from the dimension.
	 */
	public static Dimension getPreferredSize(JTextComponent comp, Dimension origDimension, int minwidth )
	{
		JTextPane dummyPane=new JTextPane();
		dummyPane.setFont(comp.getFont());
		int cwidth = origDimension.width;
		dummyPane.setSize(Math.max(minwidth,cwidth),Short.MAX_VALUE);
		dummyPane.setText(comp.getText());		        		        			
		int preferredHeight = dummyPane.getPreferredSize().height;
		//PrintFDebugger.Debugging(new GUIUtils(), "Setting new preferred size for textpane to" + new Dimension(Math.max(minwidth,origDimension.width),preferredHeight));
		return new Dimension(Math.max(300,cwidth),preferredHeight);
	}

	/**
	 * Create a SelecionPanel, with a given String and a JCombobox as selector along with a given backrgound color.
	 * @param SelectionName - The Name for the panel 
	 * @param selector - the {@link JComboBox} that represents the selection options.
	 * @param background The Color to use as background.
	 * @return a JPanel that contains the Descriptor and selector properly aligned.
	 */

	public static JPanel createSelectionPanel(String SelectionName, JComboBox selector, Color background)
	{
		JPanel resultingPanel = new JPanel();
		resultingPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 1;		
		resultingPanel.add(GUIUtils.createSelectionDescription(SelectionName,background,new Font(Font.SANS_SERIF, Font.BOLD, 16)),gbc);
		gbc.gridx++;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		resultingPanel.add(selector,gbc);
		selector.setPreferredSize(new Dimension(100,selector.getPreferredSize().height));
		selector.setMinimumSize(new Dimension(100,selector.getMinimumSize().height));
		return resultingPanel;
	}
}
