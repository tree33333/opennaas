package org.opennaas.extensions.router.capability.ip;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opennaas.core.resources.ActivatorException;
import org.opennaas.core.resources.action.IAction;
import org.opennaas.core.resources.action.IActionSet;
import org.opennaas.core.resources.capability.AbstractCapability;
import org.opennaas.core.resources.capability.CapabilityException;
import org.opennaas.core.resources.descriptor.CapabilityDescriptor;
import org.opennaas.core.resources.descriptor.ResourceDescriptorConstants;
import org.opennaas.extensions.queuemanager.IQueueManagerCapability;
import org.opennaas.extensions.router.model.IPProtocolEndpoint;
import org.opennaas.extensions.router.model.LogicalDevice;
import org.opennaas.extensions.router.model.LogicalPort;
import org.opennaas.extensions.router.model.NetworkPort;

public class IPCapability extends AbstractCapability implements IIPCapability {

	public static final String	CAPABILITY_TYPE	= "ipv4";

	public final static String	IPv4			= CAPABILITY_TYPE;

	Log							log				= LogFactory.getLog(IPCapability.class);

	private String				resourceId		= "";

	public IPCapability(CapabilityDescriptor descriptor, String resourceId) {
		super(descriptor);
		this.resourceId = resourceId;
		log.debug("Built new IP Capability");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opennaas.extensions.router.capability.ip.IIPCapability#setIPv4(org.opennaas.extensions.router.model.LogicalPort)
	 */
	@Override
	public void setIPv4(LogicalDevice iface, IPProtocolEndpoint ipProtocolEndpoint) throws CapabilityException {
		NetworkPort param = new NetworkPort();
		param.setName(iface.getName());
		if (iface instanceof NetworkPort) {
			param.setPortNumber(((NetworkPort) iface).getPortNumber());
			param.setLinkTechnology(((NetworkPort) iface).getLinkTechnology());
		}
		IPProtocolEndpoint ip = new IPProtocolEndpoint();
		ip.setIPv4Address(ipProtocolEndpoint.getIPv4Address());
		ip.setSubnetMask(ipProtocolEndpoint.getSubnetMask());
		param.addProtocolEndpoint(ipProtocolEndpoint);

		IAction action = createActionAndCheckParams(IPActionSet.SET_IPv4, param);
		queueAction(action);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opennaas.extensions.router.capability.ip.IIPCapability#setInterfaceDescription(org.opennaas.extensions.router.model.LogicalPort)
	 */
	@Override
	public void setInterfaceDescription(LogicalPort iface) throws CapabilityException {
		IAction action = createActionAndCheckParams(IPActionSet.SET_INTERFACE_DESCRIPTION, iface);
		queueAction(action);
	}

	/**
	 * 
	 * @return QueuemanagerService this capability is associated to.
	 * @throws CapabilityException
	 *             if desired queueManagerService could not be retrieved.
	 */
	private IQueueManagerCapability getQueueManager(String resourceId) throws CapabilityException {
		try {
			return Activator.getQueueManagerService(resourceId);
		} catch (ActivatorException e) {
			throw new CapabilityException("Failed to get QueueManagerService for resource " + resourceId, e);
		}
	}

	@Override
	public IActionSet getActionSet() throws CapabilityException {
		String name = this.descriptor.getPropertyValue(ResourceDescriptorConstants.ACTION_NAME);
		String version = this.descriptor.getPropertyValue(ResourceDescriptorConstants.ACTION_VERSION);

		try {
			return Activator.getIPActionSetService(name, version);
		} catch (ActivatorException e) {
			throw new CapabilityException(e);
		}

	}

	@Override
	public String getCapabilityName() {
		return CAPABILITY_TYPE;
	}

	@Override
	public void queueAction(IAction action) throws CapabilityException {
		getQueueManager(resourceId).queueAction(action);
	}

}
