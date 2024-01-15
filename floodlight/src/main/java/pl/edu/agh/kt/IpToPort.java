package pl.edu.agh.kt;

public class IpToPort {
	private int port;
    private String sw;
    private String ip;


	public IpToPort() {
		this.port = 0;
		this.ip = null;
		this.sw = null;
	}

	public IpToPort(int port, String sw, String ip) {
		this.port = port;
        this.sw = sw;
        this.ip = ip;
	}

	public String getIp() {
		return this.ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getSw() {
		return this.sw;
	}

	public void setSw(String sw) {
		this.sw = sw;
	}

	public int getPort(){
		return this.port;	
	}

	public void setPort(int port){
		this.port = port;
	}
}
