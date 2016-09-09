package idare.subnetwork.internal.Tasks.propertySelection;

import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;
import idare.subnetwork.internal.NetworkViewSwitcher;
import idare.subnetwork.internal.SubNetworkUtils;
import idare.subnetwork.internal.Tasks.InvalidSelectionException;
import idare.subnetwork.internal.Tasks.SubsystemGeneration.SubnetworkCreationTask;
import idare.subnetwork.internal.Tasks.SubsystemGeneration.SubnetworkCreationGUIHandlerFactory;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.RequestsUIHelper;
import org.cytoscape.work.swing.TunableUIHelper;

/**
 * A Task that sets up the network according to user selected columns and identifiers and calls the subnetworkpropertyselection after successful completion
 * @author Thomas Pfau
 *
 */
public class SubnetworkColumnPropertiesSelectionTask extends AbstractTask
		implements RequestsUIHelper {

	@Tunable 
	public SubnetworkColumnProperties properties;
	
	
	private CyServiceRegistrar reg;
	private NetworkViewSwitcher nvs;
	private IDARESettingsManager ism;
	private SubnetworkCreationGUIHandlerFactory sncghf;
	public SubnetworkColumnPropertiesSelectionTask(
			CyServiceRegistrar reg,	NetworkViewSwitcher nvs, IDARESettingsManager ism, SubnetworkCreationGUIHandlerFactory sncghf) {
		super();
		this.reg = reg;
		this.nvs = nvs;
		this.ism = ism;
		this.sncghf = sncghf;
	}

	@Override
	public void setUIHelper(TunableUIHelper arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		if(properties == null)
		{
			insertTasksAfterCurrentTask(new AbstractTask(){

				@Override
				public void run(TaskMonitor taskMonitor) throws Exception {
					throw new Exception("No properties for columns found. Most likely no network was selected.");					
				}				
			});
		}
		if(properties.ErrorMessage != null)
		{
			throw new InvalidSelectionException(properties.ErrorMessage);			
		}
		else
		{
			ism.resetSubNetworkTypes();
			ism.setSubNetworkType(IDAREProperties.NodeType.IDARE_REACTION, properties.ReactionID);
			ism.setSubNetworkType(IDAREProperties.NodeType.IDARE_SPECIES, properties.CompoundID);
			SubNetworkUtils.setupNetworkForSubNetworkCreation(reg.getService(CyApplicationManager.class).getCurrentNetwork(), ism, properties.TypeCol);
			sncghf.setIDCol(properties.IDCol);
			insertTasksAfterCurrentTask(new SubnetworkCreationTask(reg, nvs, ism));
		}
	}

}
