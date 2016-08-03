package idare.imagenode.internal.GUI.NetworkSetup;

import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.DataManagement.NodeManager;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Vector;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;

/**
 * A {@link CyAction} that sets up the Network for use in IDARE. 
 * @author Thomas Pfau
 *
 */
public class NetworkSetup extends AbstractCyAction{

	CyApplicationManager cyAppMgr;
	CySwingApplication cySwingApp;
	IDARESettingsManager mgr;
	NodeManager nm;
	/**
	 * Default Constructor requires the {@link CyApplicationManager} to obtain the node tables, the {@link CySwingApplication} to use as 
	 * parent for its Dialogs, an {@link IDARESettingsManager} to get and set the properties and a {@link NodeManager} to update the used nodes.
	 * @param cyAppMgr
	 * @param cySwingApp
	 * @param mgr
	 * @param nm
	 */
	public NetworkSetup(CyApplicationManager cyAppMgr, CySwingApplication cySwingApp, IDARESettingsManager mgr, NodeManager nm) {
		super("Setup Network for IDARE");
		setPreferredMenu("Apps.IDARE");
		
		this.mgr = mgr;
		this.cyAppMgr = cyAppMgr;
		this.cySwingApp = cySwingApp;
		this.nm = nm;
		// TODO Auto-generated constructor stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		CyNetwork network = cyAppMgr.getCurrentNetwork();
		Vector<String> columnNames = new Vector<String>();
		Collection<CyColumn> cols = network.getDefaultNodeTable().getColumns();
		for( CyColumn col : cols)
		{
			String colName = col.getName();
			if(!(columnNames.contains(colName)))
			{
				columnNames.add(colName);
			}
		}
		NetworkSetupGUI snc = new NetworkSetupGUI(columnNames, network, cySwingApp, mgr,nm);
	}

}
