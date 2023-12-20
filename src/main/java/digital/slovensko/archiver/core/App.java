package digital.slovensko.archiver.core;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.cli.*;

import digital.slovensko.archiver.server.ArchiverServer;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.x509.tsp.CompositeTSPSource;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;


public class App {
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private static final ExecutorService cachedExecutorService = Executors.newFixedThreadPool(8);

        private static final Options options = new Options().
        addOption("h", "help", false, "Print this command line help.").
        addOption(null, "tsa-server", true, "Url of TimeStamp Authority server that should be used for timestamping in signature level BASELINE_T. If provided, BASELINE_T signatures are made.").
        addOption("p", "port", true, "Port to listen on.");

    public static void start(String[] args) {
        try {
            var cmd = new DefaultParser().parse(options, args);

            if (cmd.hasOption("h")) {
                printHelp();
            } else {
                var port = Integer.parseInt(cmd.getOptionValue("port", "8720"));
                var tsaServers = cmd.getOptionValue("tsa-server", "http://tsa.belgium.be/connect,http://tsa.izenpe.com,http://ts.quovadisglobal.com/eu,http://tsa.sep.bg,http://kstamp.keynectis.com/KSign,https://timestamp.sectigo.com/qualified");

                var timestampDataLoader = new TimestampDataLoader();
                var tspSource = new CompositeTSPSource();
                var tspSources = new HashMap<String, TSPSource>();
                for (var tsaServer : tsaServers.split(","))
                    tspSources.put(tsaServer, new OnlineTSPSource(tsaServer, timestampDataLoader));

                tspSource.setTspSources(tspSources);
                run(port, tspSource);
            }
        } catch (ParseException e) {
            System.err.println("Unable to parse program args");
            System.err.println(e);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void printHelp() {
        final HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("archiver", options);
    }


    public static void run(int port, TSPSource tspSource) throws Exception {
        var archiver = new Archiver(tspSource);

        new Thread(() -> {
            archiver.initializeSignatureValidator(scheduledExecutorService, Executors.newFixedThreadPool(8));
        }).start();

        var server = new ArchiverServer(archiver, "0.0.0.0", port, cachedExecutorService);
        server.start();
    }
}
