package pl.edu.agh.kt;

import org.projectfloodlight.openflow.protocol.match.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowEntry {
    private static final Logger logger = LoggerFactory.getLogger(Flows.class);
    private FlowData flowData;
    private boolean isPropagated;
    private long byteCount;
    private double currentThput;
    private double lastThput;
    private Match match;
    private double trackedBW;
    private boolean isDecreasing;
    private boolean isGrowing;
    private long currentQueue;

    public FlowEntry(FlowData flowData) {
        this.flowData = flowData;
        isPropagated = false;
        this.byteCount = 0;
        this.currentThput = 0.0;
        this.lastThput = 0.0;
        this.match = null;
        this.trackedBW = Math.round(Math.ceil(flowData.getMinBw()));
        this.currentQueue = getFlowQueueId();
    }

    public FlowData getFlowData() {
        return this.flowData;
    }

    public boolean getIsPropagated() {
        return this.isPropagated;
    }

    public void setIsPropagated(boolean isPropagated) {
        this.isPropagated = isPropagated;
    }

    public long getFlowQueueId() {
        if (flowData.getMinBw() > 10.0) {
            logger.error("Flow with id {} has min BW higher than 10.0!! Should be less or equal to 10.0");
            return 9; // Queue ID with 10 Mb/s
        }

        long queueId = Math.round(Math.ceil(flowData.getMinBw()));
        return queueId - 1;
    }

    public boolean isOnItsQueue() {
        return getFlowQueueId() == currentQueue;
    }

    public boolean isQueueShared() {
        return (getCurrentQueue() < getFlowQueueId());
    }

    public long getByteCount() {
        return this.byteCount;
    }

    public void setByteCount(long packetCount) {
        this.byteCount = packetCount;
    }

    public double getCurrentThput() {
        return this.currentThput;
    }

    public void setCurrentThput(double currThput) {
        this.currentThput = currThput;
    }

    public double getLastThput() {
        return this.lastThput;
    }

    public void setLastThput(double lastThput) {
        this.lastThput = lastThput;
    }

    public Match getMatch() {
        return this.match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public double getTrackedBW() {
        return this.trackedBW;
    }

    public void setTrackedBW(double trackedBW) {
        this.trackedBW = trackedBW;
    }

    public void setIsDecreasing(boolean isDecreasing) {
        this.isDecreasing = isDecreasing;
    }

    public boolean isDecreasing() {
        return isDecreasing;
    }

    public void setIsGrowing(boolean isGrowing) {
        this.isGrowing = isGrowing;
    }

    public boolean isGrowing() {
        return isGrowing;
    }

    public void setCurrentQueue(long currentQueue) {
        this.currentQueue = currentQueue;
    }

    public long getCurrentQueue() {
        return currentQueue;
    }

    @Override
    public String toString() {
        return String.format(
                "FlowEntry: flow data: %s, isDecreasing: %b, isGrowing: %b, currentQueueId: %d, isOnItsQueue: %b, isQueueShared: %b, currentQueueMaxBw: %f",
                this.flowData.toString(), this.isDecreasing, this.isGrowing, this.currentQueue, this.isOnItsQueue(),
                this.isQueueShared(), this.getCurrentQueue() + 1.0);
    }
}
