package idare.metanode.internal.Properties;

import java.util.LinkedList;

/**
 * A Class to produce continuous labels in an alphanumeric fashion. (i.e. A, B, C, ... AA,AB,AC...)
 * @author Thomas Pfau
 *
 */
public class LabelGenerator {
	LinkedList<Character> Labels = new LinkedList<>();

	public LabelGenerator()
	{
		Labels.add('A');
	}
	/**
	 * Get the next label
	 * @return the next label.
	 */
	public String getLabel()
	{
		String returnlabel = "";
		for(char c : Labels)
		{
			returnlabel+=c;
		}
		//and now update the buffer
		updateLabel(Labels.size()-1);
		return returnlabel;
	}
	/**
	 * Update the label chain. 
	 * @param pos
	 */
	private void updateLabel(int pos)
	{		
		char entry = Labels.get(pos);
		if(entry != 'Z')
		{				
			Labels.set(pos,++entry);
		}
		else
		{
			if(pos > 0)
			{
				Labels.set(pos, 'A');
				updateLabel(pos-1);
			}
			else
			{
				Labels.set(pos, 'A');
				Labels.addFirst('A');
			}
		}
	}
}

