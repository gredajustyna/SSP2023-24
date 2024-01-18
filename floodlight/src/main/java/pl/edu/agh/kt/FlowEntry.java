package pl.edu.agh.kt;

public class FlowEntry {
    private FlowData flowData;
    private boolean isPropagated;

    public FlowEntry(FlowData flowData){
        this.flowData = flowData;
        isPropagated = false;
    }

    public FlowData geFlowData(){
        return this.flowData;
    } 

    public boolean getIsPropagated(){
        return this.isPropagated;
    }

    public void setIsPropagated(boolean isPropagated){
        this.isPropagated = isPropagated;
    }
}
