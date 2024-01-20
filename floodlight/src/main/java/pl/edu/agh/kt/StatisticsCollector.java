package pl.edu.agh.kt;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.OFFlowStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsRequest;
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
		private long elapsedTime = 0;

		@Override
		public void run() {
			logger.debug("run() begin");
			synchronized (StatisticsCollector.this) {
				if (sw == null) { // no switch
					logger.error("run() end (no switch)");
					return;
				}

				logger.info("###### FLOWS STATS ######");
				this.elapsedTime += PORT_STATISTICS_POLLING_INTERVAL;
				logger.info("SDN_PROJ: elapsed time: {} [s]", this.elapsedTime / 1000);
				OFStatsRequest<?> req = null;
				ListenableFuture<?> future;
				List<OFStatsReply> values = null;

				try {
					req = sw.getOFFactory().buildFlowStatsRequest().build();

					future = sw.writeStatsRequest(req);
					values = (List<OFStatsReply>) future.get(
							PORT_STATISTICS_POLLING_INTERVAL * 1000 / 2,
							TimeUnit.MILLISECONDS);
					OFFlowStatsReply fsr = (OFFlowStatsReply) values.get(0);
					for (OFFlowStatsEntry fse : fsr.getEntries()) {
						FlowEntry flow = FlowsDb.getFlowById(fse.getCookie().getValue());

						if (flow == null) {
							continue;
						}

						logger.info("\t SDN_PROJ: flowId: {}", fse.getCookie().getValue());
						logger.info("\t SDN_PROJ: byteCount: {}", fse.getByteCount().getValue());
						long byteDiff = fse.getByteCount().getValue() - flow.getByteCount();
						double currThput = (byteDiff * 8 / (PORT_STATISTICS_POLLING_INTERVAL / 1000.0)) / 1000000.0; // in
																														// Mb/s
						flow.setLastThput(flow.getCurrentThput());
						flow.setCurrentThput(currThput);
						flow.setByteCount(fse.getByteCount().getValue());
						logger.info("\t SDN_PROJ: lastThput: {} [Mb/s]", flow.getLastThput());
						logger.info("\t SDN_PROJ: currentThput: {} [Mb/s]", flow.getCurrentThput());
					}

				} catch (InterruptedException | ExecutionException
						| TimeoutException ex) {
					logger.error("Error during statistics polling: {}", ex);
				}
			}
			logger.debug("run() end");
		}
	}

	public static final int PORT_STATISTICS_POLLING_INTERVAL = 1000; // in ms
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