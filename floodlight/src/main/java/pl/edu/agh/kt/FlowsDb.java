package pl.edu.agh.kt;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowsDb {
    private static Logger log = LoggerFactory.getLogger(LabRestServer.class);

    private static ArrayList<FlowEntry> flows = new ArrayList<>();

    public static void addFlowEntry(FlowData flowData){
        flows.add(new FlowEntry(flowData));
    }

    public static void addFlowEntries(ArrayList<FlowData> flowsDataList){
        for(FlowData flowData: flowsDataList){
            flows.add(new FlowEntry(flowData));
        }
    }

    public static void printFlowInfoAll(){
        for(FlowEntry flowEntry: flows){
            log.info(flowEntry.getFlowData().toString());
        }
    }

    public static ArrayList<FlowEntry> getFlows(){
        return flows;
    }

    public static FlowEntry getFlowById(long id){
        for(FlowEntry flowEntry: flows){
            if (flowEntry.getFlowData().getId() == id){
                return flowEntry;
            }
        }

        return null;
    }
}
