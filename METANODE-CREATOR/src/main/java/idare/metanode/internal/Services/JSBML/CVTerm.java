package idare.metanode.internal.Services.JSBML;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class CVTerm {
	
	private Object SBMLCVTerm;
	
	private Method BiologicalQualifierType;
	private Method Resources;
	
	private Qualifier quali;
	public CVTerm(Object o)
	{
		SBMLCVTerm = o;
		try{
			BiologicalQualifierType = o.getClass().getMethod("getBiologicalQualifierType");			
			Resources = o.getClass().getMethod("getResources");
		}
		catch(NoSuchMethodException e)
		{
			
		}
	}
	
	public Qualifier getBiologicalQualifierType()
	{
		try{
			//Class enumclass = BiologicalQualifierType.invoke(SBMLCVTerm).getClass();
			String enumString = BiologicalQualifierType.invoke(SBMLCVTerm).toString();
			quali = Enum.valueOf(Qualifier.class, enumString);
		}		
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
		return quali;
	}
	
	public List<String> getResources()
	{
		try{
			List ressourcelist = (List)Resources.invoke(SBMLCVTerm);
			List<String> result = new LinkedList<String>();
			for(Object o : ressourcelist)
			{
				result.add(o.toString());
			}
			return result;
		}
		catch(InvocationTargetException | IllegalAccessException e)
		{
			return null;
		}
	}
	
	
	public static enum Qualifier {
		BQB_ENCODES,
		BQB_HAS_PART,
		BQB_HAS_PROPERTY,
		BQB_HAS_TAXON,
		BQB_HAS_VERSION,
		BQB_IS,
		BQB_IS_DESCRIBED_BY,
		BQB_IS_ENCODED_BY,
		BQB_IS_HOMOLOG_TO,
		BQB_IS_PART_OF,
		BQB_IS_PROPERTY_OF,
		BQB_IS_VERSION_OF,
		BQB_OCCURS_IN,
		BQB_UNKNOWN,
		BQM_HAS_INSTANCE,
		BQM_IS,
		BQM_IS_DERIVED_FROM,
		BQM_IS_DESCRIBED_BY,
		BQM_IS_INSTANCE_OF,
		BQM_UNKNOWN
	}
}
