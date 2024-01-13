package pl.edu.agh.kt;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.OFFlowStatsReply;
import org.projectfloodlight.openflow.protocol.OFFlowStatsRequest;
import org.projectfloodlight.openflow.protocol.OFPortStatsEntry;
import org.projectfloodlight.openflow.protocol.OFPortStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsRequest;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.util.concurrent.ListenableFuture;
import net.floodlightcontroller.core.IOFSwitch;

public class StatisticsCollector {
	private static final Logger logger = LoggerFactory
			.getLogger(StatisticsCollector.class);
	private IOFSwitch sw;

	public class PortStatisticsPoller extends TimerTask {
		private final Logger logger = LoggerFactory
				.getLogger(PortStatisticsPoller.class);
		private final int MAX_SUPPORTED_PORTS = 8;
		private long[] previousTxBytes = new long[MAX_SUPPORTED_PORTS];
		private long[] previousRxBytes = new long[MAX_SUPPORTED_PORTS];

		@Override
		public void run() {
			logger.debug("run() begin");
			synchronized (StatisticsCollector.this) {
				if (sw == null) { // no switch
					logger.error("run() end (no switch)");
					return;
				}
				ListenableFuture<?> future;
				List<OFStatsReply> values = null;
				OFStatsRequest<?> req = null;
				req = sw.getOFFactory().buildPortStatsRequest()
						.setPortNo(OFPort.ANY).build();
				try {
					if (req != null) {
						future = sw.writeStatsRequest(req);
						values = (List<OFStatsReply>) future.get(
								PORT_STATISTICS_POLLING_INTERVAL * 1000 / 2,
								TimeUnit.MILLISECONDS);
					}
					OFPortStatsReply psr = (OFPortStatsReply) values.get(0);
					logger.info("Switch id: {}", sw.getId());
					for (OFPortStatsEntry pse : psr.getEntries()) {
						if (pse.getPortNo().getPortNumber() > 0) {
							int portNumber = pse.getPortNo().getPortNumber();
							logger.info("\tport number: {}, txPackets: {}", pse
									.getPortNo().getPortNumber(), pse
									.getTxPackets().getValue());
							double txTput = (double)(pse.getTxBytes().getValue() - previousTxBytes[portNumber])/PORT_STATISTICS_POLLING_INTERVAL;
							double rxTput = (double)(pse.getRxBytes().getValue() - previousRxBytes[portNumber])/PORT_STATISTICS_POLLING_INTERVAL;
							previousTxBytes[portNumber] = pse.getTxBytes().getValue();
							previousRxBytes[portNumber] = pse.getRxBytes().getValue();	
							logger.info("\tport number: {}, txTput: {} [kb/s]",  pse.getPortNo().getPortNumber(), txTput);
							logger.info("\tport number: {}, rxTput: {} [kb/s]",  pse.getPortNo().getPortNumber(), rxTput);
						}
					}
				} catch (InterruptedException | ExecutionException
						| TimeoutException ex) {
					logger.error("Error during statistics polling", ex);
				}
				logger.info("###### FLOWS STATS ######");
				try{
					req = sw.getOFFactory().buildFlowStatsRequest()
			                .setMatch(sw.getOFFactory().buildMatch().build())
			                .setOutPort(OFPort.ANY)
			                .setTableId(TableId.ALL)
			                .build();
					
					future = sw.writeStatsRequest(req);
					values = (List<OFStatsReply>) future.get(
							PORT_STATISTICS_POLLING_INTERVAL * 1000 / 2,
							TimeUnit.MILLISECONDS);
					OFFlowStatsReply fsr = (OFFlowStatsReply) values.get(0);
					for (OFFlowStatsEntry fse : fsr.getEntries()){
						logger.info("\tflowId: {}", fse.getCookie().getValue());
						logger.info("\tpacketCount: {}", fse.getPacketCount().getValue());
					}
					
				} catch (InterruptedException | ExecutionException
						| TimeoutException ex) {
					logger.error("Error during statistics polling", ex);
				}
			}
			logger.debug("run() end");
		}
	}

	public static final int PORT_STATISTICS_POLLING_INTERVAL = 3000; // in ms
	private static StatisticsCollector singleton;

	private StatisticsCollector(IOFSwitch sw) {
		this.sw = sw;
		new Timer().scheduleAtFixedRate(new PortStatisticsPoller(), 0,
				PORT_STATISTICS_POLLING_INTERVAL);
	}

	public static StatisticsCollector getInstance(IOFSwitch sw) {
		logger.debug("getInstance() begin");
		synchronized (StatisticsCollector.class) {
			if (singleton == null) {
				logger.debug("Creating StatisticsCollector singleton");
				singleton = new StatisticsCollector(sw);
			}
		}
		logger.debug("getInstance() end");
		return singleton;
	}
}