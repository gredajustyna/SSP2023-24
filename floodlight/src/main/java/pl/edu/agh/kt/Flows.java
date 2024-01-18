package pl.edu.agh.kt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionEnqueue;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.ArpOpcode;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.types.VlanVid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.routing.Route;
import net.floodlightcontroller.topology.NodePortTuple;

public class Flows {

	private static final Logger logger = LoggerFactory.getLogger(Flows.class);

	public static short FLOWMOD_DEFAULT_IDLE_TIMEOUT = 0; // in seconds
	public static short FLOWMOD_DEFAULT_HARD_TIMEOUT = 0; // infinite
	public static short FLOWMOD_DEFAULT_PRIORITY = 100;

	protected static boolean FLOWMOD_DEFAULT_MATCH_VLAN = false;
	protected static boolean FLOWMOD_DEFAULT_MATCH_MAC = false;
	protected static boolean FLOWMOD_DEFAULT_MATCH_IP_ADDR = true;
	protected static boolean FLOWMOD_DEFAULT_MATCH_TRANSPORT = false;

	public static short idleTimeout = 5;
	public static short hardTimeout = 0;

	public static short getIdleTimeout() {
		return idleTimeout;
	}

	public static void setIdleTimeout(short idleTimeout) {
		Flows.idleTimeout = idleTimeout;
	}

	public static short getHardTimeout() {
		return hardTimeout;
	}

	public static void setHardTimeout(short hardTimeout) {
		Flows.hardTimeout = hardTimeout;
	}

	public Flows() {
		logger.info("Flows() begin/end");
	}

	public static void simpleAdd(IOFSwitch sw, OFPacketIn pin,
			FloodlightContext cntx, OFPort outPort) {
		// FlowModBuilder
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();
		// match
		Match m = createMatchFromPacket(sw, pin.getInPort(), cntx);

		// actions
		OFActionOutput.Builder aob = sw.getOFFactory().actions().buildOutput();
		List<OFAction> actions = new ArrayList<OFAction>();
		aob.setPort(outPort);
		aob.setMaxLen(Integer.MAX_VALUE);
		actions.add(aob.build());
		fmb.setMatch(m).setIdleTimeout(idleTimeout)
				.setHardTimeout(hardTimeout)
				.setBufferId(pin.getBufferId()).setOutPort(outPort)
				.setPriority(FLOWMOD_DEFAULT_PRIORITY);
		fmb.setActions(actions);
		// write flow to switch
		try {
			sw.write(fmb.build());
			logger.info(
					"Flow from port {} forwarded to port {}; match: {}",
					new Object[] { pin.getInPort().getPortNumber(),
							outPort.getPortNumber(), m.toString() });
		} catch (Exception e) {
			logger.error("error {}", e);
		}
	}

	public static void simpleAdd(IOFSwitch sw, OFPort inPort,
			OFPort outPort, FloodlightContext cntx) {
		// FlowModBuilder
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();
		// match
		Match m = createMatchFromPacket(sw, inPort, cntx);

		// actions
		OFActionOutput.Builder aob = sw.getOFFactory().actions().buildOutput();
		List<OFAction> actions = new ArrayList<OFAction>();
		aob.setPort(outPort);
		aob.setMaxLen(Integer.MAX_VALUE);
		actions.add(aob.build());
		fmb.setMatch(m).setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
				.setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
				.setOutPort(outPort)
				.setPriority(FLOWMOD_DEFAULT_PRIORITY);
		fmb.setActions(actions);
		// write flow to switch
		try {
			sw.write(fmb.build());
			logger.info(
					"Flow from port {} forwarded to port {}; match: {}",
					new Object[] { inPort.getPortNumber(),
							outPort.getPortNumber(), m.toString() });
		} catch (Exception e) {
			logger.error("error {}", e);
		}
	}

	public static void enqueue(IOFSwitch sw, OFPacketIn pin,
							   FloodlightContext cntx, OFPort outPort, long queueId) {
// FlowModBuilder
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();
// match
		Match m = createMatchFromPacket(sw, pin.getInPort(), cntx);
		List<OFAction> actions = new ArrayList<OFAction>();
		OFActionEnqueue enqueue = sw.getOFFactory().actions().buildEnqueue()
				.setPort(outPort).setQueueId(queueId).build();
		actions.add(enqueue);
		fmb.setMatch(m).setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
				.setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
				.setBufferId(pin.getBufferId()).setOutPort(outPort)
				.setPriority(FLOWMOD_DEFAULT_PRIORITY);
		fmb.setActions(actions);
// write flow to switch
		try {
			sw.write(fmb.build());
			logger.info(
					"Flow from port {} forwarded to port {}; match: {}",
					new Object[] { pin.getInPort().getPortNumber(),
							outPort.getPortNumber(), m.toString() });
		} catch (Exception e) {
			logger.error("error {}", e);
		}
	}

	public static Match createMatchFromPacket(IOFSwitch sw, OFPort inPort,
			FloodlightContext cntx) {
		// The packet in match will only contain the port number.
		// We need to add in specifics for the hosts we're routing between.
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
				IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.IN_PORT, inPort);

		// TODO Detect switch type and match to create hardware-implemented flow
		if (eth.getEtherType() == EthType.IPv4) { /*
													 * shallow check for equality is
													 * okay for EthType
													 */
			IPv4 ip = (IPv4) eth.getPayload();
			IPv4Address srcIp = ip.getSourceAddress();
			IPv4Address dstIp = ip.getDestinationAddress();

			if (FLOWMOD_DEFAULT_MATCH_IP_ADDR) {
				mb.setExact(MatchField.ETH_TYPE, EthType.IPv4)
						.setExact(MatchField.IPV4_SRC, srcIp)
						.setExact(MatchField.IPV4_DST, dstIp);
			}

		} else if (eth.getEtherType() == EthType.ARP) { /*
														 * shallow check for
														 * equality is okay for
														 * EthType
														 */
			ARP arp = (ARP) eth.getPayload();
			if (eth.getEtherType() == EthType.ARP) {
				arp = (ARP) eth.getPayload();
				mb.setExact(MatchField.ETH_TYPE, EthType.ARP)
						.setExact(MatchField.ARP_TPA, arp.getTargetProtocolAddress());
			}
		}

		return mb.build();
	}

	public static void simpleQoSAdd(IOFSwitch sw, OFPort inPort,
			OFPort outPort, FloodlightContext cntx, FlowEntry flow) {

		// FlowModBuilder
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();

		// match
		// The packet in match will only contain the port number.
		// We need to add in specifics for the hosts we're routing between.

		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.IN_PORT, inPort);

		IPv4Address srcIp = IPv4Address.of(flow.geFlowData().getSrcIp());
		IPv4Address dstIp = IPv4Address.of(flow.geFlowData().getDestIp());

		mb.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IPV4_SRC, srcIp)
				.setExact(MatchField.IPV4_DST, dstIp);

		TransportPort srcPort = TransportPort.of(flow.geFlowData().getSrcPort());
		TransportPort destPort = TransportPort.of(flow.geFlowData().getDestPort());
		mb.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				//.setExact(MatchField.UDP_SRC, srcPort)
				.setExact(MatchField.UDP_DST, destPort);

		Match m = mb.build();

		// actions
		OFActionOutput.Builder aob = sw.getOFFactory().actions().buildOutput();
		List<OFAction> actions = new ArrayList<OFAction>();
		OFActionEnqueue enqueue = sw.getOFFactory().actions().buildEnqueue()
				.setPort(outPort).setQueueId(1).build();
		actions.add(enqueue);
		aob.setPort(outPort);
		aob.setMaxLen(Integer.MAX_VALUE);
		actions.add(aob.build());
		fmb.setMatch(m).setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
				.setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
				.setOutPort(outPort)
				.setPriority(FLOWMOD_DEFAULT_PRIORITY)
				.setCookie(U64.of(flow.geFlowData().getId()));
		fmb.setActions(actions);
		// write flow to switch
		try {
			sw.write(fmb.build());
			logger.info(
					"SDN_PROJ:: Flow from UDP port {} forwarded to UDP port {}; match: {}",
					new Object[] { srcPort.getPort(),
						destPort.getPort(), m.toString() });
		} catch (Exception e) {
			logger.error("SDN_PROJ:: error {}", e);
		}

	}

	public static void insertFlowsOnRoute(Route route, IOFSwitchService switchService, FloodlightContext cntx) {
		List<NodePortTuple> switchPortList = route.getPath();
		for (int indx = switchPortList.size() - 1; indx > 0; indx -= 2) {
			// indx and indx-1 will always have the same switch DPID.
			DatapathId switchDPID = switchPortList.get(indx).getNodeId();
			IOFSwitch sw = switchService.getSwitch(switchDPID);

			if (sw == null) {
				logger.warn("CINUS:: Unable to push route, switch at DPID {} " + "not available", switchDPID);
				return;
			}
			logger.info("CINUS:: switch info: " + sw.toString());
			OFPort outPort = switchPortList.get(indx).getPortId();
			OFPort inPort = switchPortList.get(indx - 1).getPortId();
			simpleAdd(sw, inPort, outPort, cntx);
		}
	}

	public static void insertQoSFlowsOnRoute(Route route, IOFSwitchService switchService, FloodlightContext cntx,
			FlowEntry flow) {
		List<NodePortTuple> switchPortList = route.getPath();
		for (int indx = switchPortList.size() - 1; indx > 0; indx -= 2) {
			// indx and indx-1 will always have the same switch DPID.
			DatapathId switchDPID = switchPortList.get(indx).getNodeId();
			IOFSwitch sw = switchService.getSwitch(switchDPID);

			if (sw == null) {
				logger.warn("SDN_PROJ:: Unable to push route, switch at DPID {} " + "not available", switchDPID);
				return;
			}
			logger.info("SDN_PROJ:: switch info: " + sw.toString());
			OFPort outPort = switchPortList.get(indx).getPortId();
			OFPort inPort = switchPortList.get(indx - 1).getPortId();
			simpleQoSAdd(sw, inPort, outPort, cntx, flow);
		}
	}
}
