package idare.imagenode.internal.VisualStyle;

import java.util.HashMap;

public class DefaultMap<S> extends HashMap<String,S> {

	S defaultValue;
	
	public DefaultMap(S defaultValue)
	{
		super();
		this.defaultValue = defaultValue;
	}
	@Override
	public S get(Object key)
	{
		if(containsKey(key))
		{
			return super.get(key);
		}
		else
		{
			return defaultValue;
		}
	}
}
