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

/*
 * TODO: popracować jeszcze nad tracked bandwidth
 */

public class FlowManager {

    public static IRoutingService routingService = null;
    public static IOFSwitchService switchService = null;
    public static List<FlowEntry> availablePool = new ArrayList<>();
    public static List<FlowEntry> sharedPool = new ArrayList<>();
    private static boolean isCommitNeeded = false;
    private static int statsIgnoreCount = 0;

    private static final Logger logger = LoggerFactory
            .getLogger(FlowManager.class);

    public static void manageFlows() {
        if (statsIgnoreCount > 0) {
            statsIgnoreCount--;
            return;
        }
        logger.info("SDN_PROJ:: Started flows management.");
        seizeBW();
        returnBW();
        shareAvailableBW();
        if (isCommitNeeded) {
            commit();
            isCommitNeeded = false;
        }
        availablePool.clear();
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
            if (route == null)
                continue;
            Flows.insertQoSFlowsOnRoute(route, switchService, flow, true);
        }
    }

    private static void shareAvailableBW() {
        for (FlowEntry flow : FlowsDb.getFlows()) {
            if (flow.isGrowing() && flow.isOnItsQueue()) {
                for (FlowEntry availableFlow : availablePool) {
                    if (availableFlow.getTrackedBW() - 1 <= flow.getCurrentQueue()
                            && availableFlow.getCurrentQueue() > flow.getCurrentQueue()) {
                        sharedPool.add(flow);
                        long sharedQueue = availableFlow.getFlowQueueId();
                        availableFlow.setCurrentQueue(flow.getCurrentQueue());
                        flow.setCurrentQueue(sharedQueue);
                        availablePool.remove(availableFlow);
                        logger.info("SDN_PROJ:: zamiana kolejek: flow {}, availableFlow {}",
                                flow.getFlowData().toString(), availableFlow.getFlowData().toString());
                        isCommitNeeded = true;
                        statsIgnoreCount = 2;
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
                        logger.info("SDN_PROJ:: oddanie kolejek: flow {}, sharedFlow {}", flow.getFlowData().toString(),
                                sharedFlow.getFlowData().toString());
                        long sharedQueue = sharedFlow.getFlowQueueId();
                        sharedFlow.setCurrentQueue(sharedFlow.getFlowQueueId());
                        sharedFlow.setTrackedBW((double) (flow.getCurrentQueue() + 1));
                        flow.setCurrentQueue(flow.getFlowQueueId());
                        sharedPool.remove(sharedFlow);
                        flow.setTrackedBW((double) (sharedQueue + 1));
                        isCommitNeeded = true;
                        statsIgnoreCount = 2;
                        break;
                    }
                }
            }
        }
    }

    private static void seizeBW() {
        for (FlowEntry flow : FlowsDb.getFlows()) {
            double lowerBound = 0.75 * flow.getFlowData().getMinBw();
            String log = String.format(
                    "SDN_PROJ: Flow id: %d, lowerBound: %f, flow.isDecreasing(): %b, flow.isGrowing(): %b, flow.getCurrentThput(): %f",
                    flow.getFlowData().getId(), lowerBound, flow.isDecreasing(), flow.isGrowing(),
                    flow.getCurrentThput());
            logger.info(log);
            if ((flow.isDecreasing() || (!flow.isDecreasing() && !flow.isGrowing()))
                    && flow.getCurrentThput() < lowerBound && flow.isOnItsQueue()) {
                logger.info("SDN_PROJ:: added flow with id {} to availablePool", flow.getFlowData().getId());
                // double decrease = flow.getTrackedBW() - Math.ceil(flow.getCurrentThput());
                flow.setTrackedBW(Math.ceil(flow.getCurrentThput()));
                availablePool.add(flow);
            }
        }
    }
}
