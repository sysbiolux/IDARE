package idare.imagenode.internal.Utilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
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

}
