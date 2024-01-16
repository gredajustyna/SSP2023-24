package pl.edu.agh.kt;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowsDb {
    private static Logger log = LoggerFactory.getLogger(LabRestServer.class);

    private static ArrayList<FlowEntry> flows = new ArrayList<>();

    public static void addFlowEntry(FlowEntry flowEntry){
        flows.add(flowEntry);
    }

    public static void addFlowEntries(ArrayList<FlowEntry> flowsEntriesList){
        flows.addAll(flowsEntriesList);
    }

    public static void printFlowInfoAll(){
        for(FlowEntry flowEntry: flows){
            log.info(flowEntry.toString());
        }
    }
}
