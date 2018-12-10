package idare.imagenode.Utilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.batik.svggen.SVGGraphics2D;

import idare.imagenode.Properties.IMAGENODEPROPERTIES;
import idare.imagenode.internal.Debug.PrintFDebugger;

public class StringDrawer
{
	private String stringToDraw;
	public StringDrawer(String stringToDraw)
	{
		this.stringToDraw = stringToDraw;
	}
	
	public void drawInArea(int yoffset, int width, int drawheight, SVGGraphics2D context)
	{
		//lets assume, that increasing font size will roughly have the same scaling for all dimensions.
		//further assume, that we need to roughly place 2/3 of the text on one line
		//First, scale the font, so that it fits to the current width.
		PrintFDebugger.Debugging(this, "Scaling Font into a box of size: " + width + "/" + drawheight);
		Font font = LayoutUtils.scaleFont(new Dimension(width,drawheight),IMAGENODEPROPERTIES.IDFont,context,stringToDraw);		
		context.setColor(Color.black);		
		FontMetrics fm = LayoutUtils.getSVGFontMetrics(font);
	    int stringWidth = fm.stringWidth(stringToDraw);	    
	    int stringHeight = fm.getHeight();
	    PrintFDebugger.Debugging(this, "The original size of the label was: " + stringWidth + "/" + stringHeight);
	    boolean splitable = true;
	    int rowcount = 1;
	    PrintFDebugger.Debugging(this, "Calculating Row Count");
	    while (splitable)
	    {
	    	int cRowCount = rowcount +1;
	    	double rowheight = stringHeight * Math.pow(4./3., cRowCount) ;
	    	PrintFDebugger.Debugging(this, "Rowheight: " + rowheight + "/ cRowCount: " + cRowCount );
	    	if(drawheight < (rowheight + 2 ) * cRowCount -2)	    		
	    	{ // rows are separated by 2 pixels.	    	    
	    		splitable = false;
	    	}
	    	else
	    	{
	    		PrintFDebugger.Debugging(this, "Rowheight: " + rowheight + "/ drawheight: " + drawheight);
	    		rowcount += 1;
	    	}
	    	
	    }	    
	    PrintFDebugger.Debugging(this, "Building Strings for " + rowcount + " rows");
	    
	    // This is the applicable font	    
	    font = font.deriveFont(new Float(font.getSize() * Math.pow(4/3, rowcount)));
	    context.setFont(font);	    
	    PrintFDebugger.Debugging(this, "Calculating split positions");
	    int[] splitPositions = splitAtPositions(rowcount-1);	    	    
	    //Determine the centering
	    PrintFDebugger.Debugging(this, "Got " + splitPositions.length + " splits:" + splitPositions.toString() );
		fm = LayoutUtils.getSVGFontMetrics(font);	    
	    double maxwidth = 0;
	    double maxheight = fm.getHeight()*splitPositions.length+1;
	    PrintFDebugger.Debugging(this, "Determining elements");
	    Vector<String> toPlot = new Vector<String>();
	    int startpos = 0;
	    int currentsplit = 0;
	    while(currentsplit < splitPositions.length)	    	    	
	    {
	    	String cstring = stringToDraw.substring(startpos, splitPositions[currentsplit]+1); 
	    	toPlot.add(cstring);
	    	maxwidth = Math.max(fm.getStringBounds(cstring, context).getWidth(), maxwidth);
	    	startpos = splitPositions[currentsplit]+1;
	    	currentsplit += 1;
	    }	  
		String cstring = stringToDraw.substring(startpos, stringToDraw.length()); 
    	toPlot.add(cstring);
    	maxwidth = Math.max(fm.getStringBounds(cstring, context).getWidth(), maxwidth);    
	    double heightScale = drawheight/maxheight;
	    double widthScale = width/maxwidth;
	    double usedScale = Math.min(heightScale, widthScale);
	    Font usedFont = font.deriveFont(font.getSize()*(float)usedScale);
	    context.setFont(usedFont);
	    fm =  LayoutUtils.getSVGFontMetrics(usedFont);
	    int border = drawheight - (2 + fm.getHeight()) * rowcount - 2;
	    int offset = border / 2;
	    for(currentsplit = 0; currentsplit < toPlot.size();currentsplit++)
	    {
	    	drawSubstring(toPlot.get(currentsplit), currentsplit, width,	    
	    		offset+ yoffset , fm, context);
	    	
	    }	    
	}				
	
	private void drawSubstring(String cstring, int position, int width, int offset, FontMetrics fm, SVGGraphics2D context)
	{    	
		PrintFDebugger.Debugging(this, "Printing string: " + cstring);
    	Rectangle2D bounds = fm.getStringBounds(cstring, context);
    	int xpos = (int) ((width - bounds.getWidth())/2);
		int ypos = offset + // offset from top. 
				2*position + //separation of string lines
				position * fm.getHeight() + //Line
				fm.getAscent();			
		context.drawString(cstring, xpos, ypos);
	}
	
	int[] splitAtPositions(int splitcount)
	{
		//This is a very crude implementation of a string splitting algorithm.
		//Once a better (using propery Hyphenation) approach is found that approach will be used. 
		int[] splitpositions = new int[splitcount];		
		Pattern nonAlphaNumericPattern = Pattern.compile("[^\\w]");
		Matcher mat = nonAlphaNumericPattern.matcher(stringToDraw);
		//we start at the 0 position
		int csplit = 0;
		int splitposition = 0;
		//If we can split, we will split at positions in between 1 / number of splitted elements (i.e. splits + 1)
		// and  1.333 * 1/ number of splitted elements;
		float splitminfraction = 1/(float)(splitcount+1);
		float splitmaxfraction = 4/(3*(float)((splitcount+1)*2));
		int stringLength = stringToDraw.length();
		PrintFDebugger.Debugging(this, "Trying to determine the row count for the current ID");
		PrintFDebugger.Debugging(this, "The String is: " + stringToDraw);
		PrintFDebugger.Debugging(this, "the minimal fraction is: " + splitminfraction);
		PrintFDebugger.Debugging(this, "the maximal fraction is: " + splitmaxfraction);
		while(mat.find() && csplit < splitcount)			
		{ 
			// while we have additional potential split positions and not enough splits.			
			if(mat.start() > splitposition + splitminfraction*stringLength )
			{								
				if(mat.start() < splitposition + splitmaxfraction*stringLength)
				{
					PrintFDebugger.Debugging(this, "Found a separator at position: " + mat.start());
					PrintFDebugger.Debugging(this, "The substring would be: " + stringToDraw.substring(splitposition, mat.start()));
					// We found a usable split position, we'll use it 
					splitpositions[csplit] = mat.start();
					splitposition = mat.start();
				}
				else
				{
					PrintFDebugger.Debugging(this, "Start position was too far in. Using default.");					
					// we cannot find a usable split position, just use the one closest to the cut position
					splitpositions[csplit] = Math.min(splitposition + (int)Math.ceil(splitminfraction*stringLength), stringLength-1);
					PrintFDebugger.Debugging(this, "The substring would be: " + stringToDraw.substring(splitposition, splitpositions[csplit]));
					splitposition = splitpositions[csplit];					
				}
				csplit += 1;
			}
		}
		while(csplit < splitcount)
		{
			//We actually did not find enough splits
			PrintFDebugger.Debugging(this, "Not enough separators found. Creating split points manually.");					
			// we cannot find a usable split position, just use the one closest to the cut position			
			splitpositions[csplit] = Math.min(splitposition + (int)Math.ceil(splitminfraction*stringLength), stringLength-1);
			PrintFDebugger.Debugging(this, "The substring would be: " + stringToDraw.substring(splitposition, splitpositions[csplit]));
			splitposition = splitpositions[csplit];
			csplit += 1;
		}			
		
		return splitpositions;
	}
	
}
