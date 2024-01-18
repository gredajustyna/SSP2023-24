package pl.edu.agh.kt;

import javax.annotation.Nonnull;

public class FlowData {

	private int id;
	private String srcIp;
	private String destIp;
	private int srcPort;
	private int destPort;
	private float duration;
	private float minBw;
	private int prio;

	public FlowData() {
		this.srcIp = null;
		this.destIp = null;
		this.srcPort = 0;
		this.destPort = 0;
		this.duration = 0;
		this.minBw = 0;
		this.prio = 0;
		this.id = 0;
	}

	public FlowData(String srcIp, String destIp, int srcPort, int destPort, float duration, float minBw, int prio,
			int id) {
		this.srcIp = srcIp;
		this.destIp = destIp;
		this.srcPort = srcPort;
		this.destPort = destPort;
		this.duration = duration;
		this.minBw = minBw;
		this.prio = prio;
		this.id = id;
	}

	@Nonnull
	public String getSrcIp() {
		return this.srcIp;
	}

	public void setSrcIp(String ip) {
		this.srcIp = ip;
	}

	@Nonnull
	public String getDestIp() {
		return this.destIp;
	}

	public void setDestIp(String ip) {
		this.destIp = ip;
	}

	public int getSrcPort() {
		return this.srcPort;
	}

	public void setSrcPort(int port) {
		this.srcPort = port;
	}

	public int getDestPort() {
		return this.destPort;
	}

	public void setDestPort(int port) {
		this.destPort = port;
	}

	public float getDuration() {
		return this.duration;
	}

	public void setDuration(float duration) {
		this.duration = duration;
	}

	public float getMinBw() {
		return this.minBw;
	}

	public void setMinBw(float minBw) {
		this.minBw = minBw;
	}

	public int getPrio() {
		return this.prio;
	}

	public void setPrio(int prio) {
		this.prio = prio;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String toString() {
		return "Flow info: id: " + this.id + ", srcIp: " + this.srcIp + ", destIp: " + this.destIp + ", srcPort: " + this.srcPort
				+ ", destPort: " + this.destPort + ", duration " + this.duration + " [s], minBw: " + this.minBw + " [Mb/s], priority: "
				+ this.prio;
	}
}
