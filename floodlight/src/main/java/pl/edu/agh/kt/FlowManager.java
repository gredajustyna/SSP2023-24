package pl.edu.agh.kt;

import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Route;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class FlowManager {

    public static IRoutingService routingService = null;
    public static IOFSwitchService switchService = null;
    public static List<FlowEntry> availablePool = new ArrayList<>();
    public static List<FlowEntry> sharedPool = new ArrayList<>();

    private static final Logger logger = LoggerFactory
            .getLogger(FlowManager.class);

    public static void manageFlows() {
        seizeBW();
        returnBW();
        shareAvailableBW();
        //commit();
    }

    private static void commit() {
        for (FlowEntry flow : FlowsDb.getFlows()) {
            IPv4Address destIp = IPv4Address.of(flow.getFlowData().getDestIp());
            IPv4Address srcIp = IPv4Address.of(flow.getFlowData().getSrcIp());
            IpToPort ipToPortSrc = SdnLabListener.getSwPort(srcIp);
            IpToPort ipToPortDest = SdnLabListener.getSwPort(destIp);
            Route route = routingService.getRoute(DatapathId.of(ipToPortSrc.getSw()),
                    OFPort.of(ipToPortSrc.getPort()), DatapathId.of(ipToPortDest.getSw()),
                    OFPort.of(ipToPortDest.getPort()), U64.of(0));
            Flows.insertQoSFlowsOnRoute(route, switchService, flow);
        }
    }

    private static void shareAvailableBW() {
        for (FlowEntry flow : FlowsDb.getFlows()) {
            if (flow.isGrowing() && flow.isOnItsQueue()) {
                for (FlowEntry availableFlow : availablePool) {
                    if (availableFlow.getTrackedBW()-1 <= flow.getCurrentQueue() && availableFlow.getCurrentQueue() > flow.getCurrentQueue()) {
                        sharedPool.add(flow);
                        long sharedQueue = availableFlow.getFlowQueueId();
                        availableFlow.setCurrentQueue(flow.getCurrentQueue());
                        flow.setCurrentQueue(sharedQueue);
                        availablePool.remove(availableFlow);
                        logger.info("zamiana kolejek: flow1 {}, flow2 {}", flow, availableFlow);
                        break;
                    }
                }
            }
        }
    }

    private static void returnBW() {
        for (FlowEntry flow : FlowsDb.getFlows()) {
            if (flow.isGrowing() && flow.isQueueShared()) {
                for (FlowEntry sharedFlow : sharedPool) {
                    if (sharedFlow.getCurrentQueue() == flow.getFlowQueueId()) {
                        long sharedQueue = sharedFlow.getFlowQueueId();
                        sharedFlow.setCurrentQueue(flow.getCurrentQueue());
                        flow.setCurrentQueue(sharedQueue);
                        sharedPool.remove(sharedFlow);
                        break;
                    }
                }
            }
        }
    }

    private static void seizeBW() {
        for (FlowEntry flow : FlowsDb.getFlows()) {
            double lowerBound = 0.75*flow.getTrackedBW();
            if (flow.isDecreasing() && flow.getCurrentThput() < lowerBound) {
//                double decrease = flow.getTrackedBW() - Math.ceil(flow.getCurrentThput());
                flow.setTrackedBW(Math.ceil(flow.getCurrentThput()));
                availablePool.add(flow);
            }
        }
    }
}
