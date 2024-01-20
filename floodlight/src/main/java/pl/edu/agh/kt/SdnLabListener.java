package pl.edu.agh.kt;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPAddress;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.python.antlr.ast.boolopType;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.web.serializers.IPv4Serializer;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Route;
import net.floodlightcontroller.topology.ITopologyService;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.agh.kt.StatisticsCollector;

public class SdnLabListener implements IFloodlightModule, IOFMessageListener {

	protected IFloodlightProviderService floodlightProvider;
	protected static Logger logger;
	protected IRestApiService restApiService;
	protected ITopologyService topologyService;
	protected IRoutingService routingService;
	protected static Routing routing;
	protected IOFSwitchService switchService;
	protected static List<DatapathId> swList = new ArrayList<DatapathId>();
	private static HashMap<String, IpToPort> ipToPortMap = new HashMap<>();
	private boolean isStatisticsCollectorActivated = false;

	@Override
	public String getName() {
		return SdnLabListener.class.getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {

		if (!isStatisticsCollectorActivated) {
			IOFSwitch switch4 = switchService.getSwitch(DatapathId.of("00:00:00:00:00:00:00:04"));
			StatisticsCollector.getInstance(switch4);
			isStatisticsCollectorActivated = true;
		}

		OFPacketIn pin = (OFPacketIn) msg;
		PacketExtractor packetExtractor = new PacketExtractor(cntx, msg);
		packetExtractor.packetExtract(cntx);
		IPv4Address destIp = packetExtractor.extractIp();
		if (destIp != null) {
			logger.debug("CINUS:: got destIp: {}", destIp.toString());
			if (ipToPortMap.containsKey(destIp.toString())) {
				IpToPort ipToPort = ipToPortMap.get(destIp.toString());
				logger.debug("CINUS:: resolved dest ip to port: {} on switch: {}", ipToPort.getPort(),
						ipToPort.getSw());
				logger.debug("CINUS:: sw.getId(): {}", sw.getId());
				Route route = routingService.getRoute(sw.getId(), pin.getInPort(), DatapathId.of(ipToPort.getSw()),
						OFPort.of(ipToPort.getPort()), U64.of(0));
				if (route != null) {
					logger.debug("CINUS:: resolved route: {}", route.toString());
					Flows.insertFlowsOnRoute(route, switchService, cntx);
				}
			}

			// check if there's a flow in FlowDB that has not been propagated
			// wrzucilem to tutaj zeby sie rozpropagowaly od razu jak poleci pierwszy ARP
			for (FlowEntry flow : FlowsDb.getFlows()) {
				if (!flow.getIsPropagated()) {
					destIp = IPv4Address.of(flow.getFlowData().getDestIp());
					IPv4Address srcIp = IPv4Address.of(flow.getFlowData().getSrcIp());
					if (ipToPortMap.containsKey(destIp.toString())) {
						IpToPort ipToPortDest = ipToPortMap.get(destIp.toString());
						IpToPort ipToPortSrc = ipToPortMap.get(srcIp.toString());
						logger.debug("SDN_PROJ:: resolved flow dest ip to port: {} on switch: {}",
								ipToPortDest.getPort(), ipToPortDest.getSw());
						logger.debug("SDN_PROJ:: sw.getId(): {}", sw.getId());
						Route route = routingService.getRoute(DatapathId.of(ipToPortSrc.getSw()),
								OFPort.of(ipToPortSrc.getPort()), DatapathId.of(ipToPortDest.getSw()),
								OFPort.of(ipToPortDest.getPort()), U64.of(0));
						if (route != null) {
							logger.debug("SDN_PROJ:: resolved flow (id: {}) route: {}", flow.getFlowData().getId(),
									route.toString());
							Flows.insertQoSFlowsOnRoute(route, switchService, cntx, flow);
							logger.debug("SDN_PROJ:: Propagated QoS flow with id: {}", flow.getFlowData().getId());
							flow.setIsPropagated(true);
						}
					}
				}
			}
		}

		return Command.STOP;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IRestApiService.class);
		l.add(IRoutingService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider = context
				.getServiceImpl(IFloodlightProviderService.class);
		logger = LoggerFactory.getLogger(SdnLabListener.class);
		restApiService = context.getServiceImpl(IRestApiService.class);
		topologyService = context.getServiceImpl(ITopologyService.class);
		routingService = context.getServiceImpl(IRoutingService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
	}

	public static Routing getRouting() {
		return routing;
	}

	@Override
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		restApiService.addRestletRoutable(new RestLab());
		topologyService.addListener(new SdnLabTopologyListener());
		routing = new Routing(routingService);
		logger.info("******************* START **************************");
	}

	public static void updateSwList(DatapathId sw) {
		swList.add(sw);
		logger.debug("CINUS:: current state of swList: ");
		for (DatapathId s : swList) {
			logger.debug("CINUS:: sw: {}", s.toString());
		}
	}

	public static void deleteFromSwList(DatapathId sw) {
		swList.remove(sw);
		logger.debug("CINUS:: current state of swList: ");
		for (DatapathId s : swList) {
			logger.debug("CINUS:: sw: {}", s.toString());
		}
	}

	public static void updateIpToPortMapping(List<IpToPort> ipToPortList) {
		for (IpToPort ipToPort : ipToPortList) {
			ipToPortMap.put(ipToPort.getIp(), ipToPort);
			logger.debug("CINUS:: added to ipToPortMap: ip: {} sw: {}", ipToPort.getIp(), ipToPort.getSw());
		}
	}
}
