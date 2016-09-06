package idare.imagenode.internal.GUI.NetworkSetup.Tasks;

import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.VisualStyle.IDAREVisualStyle;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.RequestsUIHelper;
import org.cytoscape.work.swing.TunableUIHelper;

public class NetworkSetupTask extends AbstractTask implements RequestsUIHelper{

	/**
	 * Tunables that get the desired properties.
	 */
	@Tunable
	public NetworkSetupProperties params;	

	
	public NetworkSetupTask() {
//		System.out.println("Task generated");		
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(params == null)
		{
			throw new DuplicateTypeException("Cannot use the same type twice, or an empty entry as a type identifier");
		}
		try{
//			System.out.println("Running Task");
			params.mgr.setType(IDAREProperties.NodeType.IDARE_SPECIES, params.CompoundID);
			params.mgr.setType(IDAREProperties.NodeType.IDARE_GENE, params.GeneID);
			params.mgr.setType(IDAREProperties.NodeType.IDARE_REACTION, params.InteractionID);
			params.mgr.setType(IDAREProperties.NodeType.IDARE_PROTEIN, params.ProteinID);
//			System.out.println("manager set up");
			IDARESettingsManager.SetNetworkData(params.network, params.mgr, params.TypeColID, params.IDColID, params.overwrite, params.nm);
//			System.out.println("Network set up");
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
			throw e;
		}
	}

	private class DuplicateTypeException extends Exception
	{
		public DuplicateTypeException(String Message)
		{
			super(Message);
		}
	}

	@Override
	public void setUIHelper(TunableUIHelper arg0) {
		// TODO Auto-generated method stub
		
	}
}
