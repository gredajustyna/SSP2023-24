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

    public FlowEntry(FlowData flowData){
        this.flowData = flowData;
        isPropagated = false;
        this.byteCount = 0;
        this.currentThput = 0.0;
        this.lastThput = 0.0;
        this.match = null;
    }

    public FlowData getFlowData(){
        return this.flowData;
    } 

    public boolean getIsPropagated(){
        return this.isPropagated;
    }

    public void setIsPropagated(boolean isPropagated){
        this.isPropagated = isPropagated;
    }

    public long getFlowQueueId(){
        if (flowData.getMinBw() > 10.0){
            logger.error("Flow with id {} has min BW higher than 10.0!! Should be less or equal to 10.0");
            return 9; // Queue ID with 10 Mb/s
        }

        long queueId = Math.round(Math.ceil(flowData.getMinBw()));
        return queueId - 1;
    }

    public long getByteCount(){
        return this.byteCount;
    }

    public void setByteCount(long packetCount){
        this.byteCount = packetCount;
    }

    public double getCurrentThput(){
        return this.currentThput;
    }

    public void setCurrentThput(double currThput){
        this.currentThput = currThput;
    }

    public double getLastThput(){
        return this.lastThput;
    }

    public void setLastThput(double lastThput){
        this.lastThput = lastThput;
    }

    public Match getMatch(){
        return this.match;
    }

    public void setMatch(Match match){
        this.match = match;
    }
}
