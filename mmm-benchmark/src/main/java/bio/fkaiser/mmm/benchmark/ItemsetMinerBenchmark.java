package bio.fkaiser.mmm.benchmark;

import bio.fkaiser.mmm.ItemsetMinerRunner;
import bio.fkaiser.mmm.model.configurations.ItemsetMinerConfiguration;
import bio.singa.core.utility.Resources;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.util.Statistics;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class ItemsetMinerBenchmark {

    @Param({"PDB", "MMTF"})
    private String parsing;

    //    @Param({"250", "500", "750", "1000", "1250", "1500", "1750", "2000"})
    @Param({"20", "40", "60", "80", "100"})
    private int datasetSize;
    private ItemsetMinerConfiguration<String> itemsetMinerConfiguration;

    public static void main(String[] args) throws RunnerException, IOException {
        Options opt = new OptionsBuilder()
                .include(ItemsetMinerBenchmark.class.getSimpleName())
                .warmupIterations(1)
                .measurementIterations(5)
                .forks(1)
                .mode(Mode.AverageTime)
                .timeout(TimeValue.hours(24))
                .timeUnit(TimeUnit.MILLISECONDS)
                .build();
        Collection<RunResult> results = new Runner(opt).run();
        StringJoiner stringJoiner = new StringJoiner("\n", "parsing,dataset_size,min,max,mean,stdev,ci95_min,ci95_max\n", "");
        for (RunResult result : results) {
            Statistics statistics = result.getPrimaryResult().getStatistics();
            double[] confidenceInterval = statistics.getConfidenceIntervalAt(0.95);
            String resultLine = result.getParams().getParam("parsing") +
                                "," +
                                result.getParams().getParam("datasetSize") +
                                "," +
                                statistics.getMin() +
                                "," +
                                statistics.getMax() +
                                "," +
                                statistics.getMean() +
                                "," +
                                statistics.getStandardDeviation() +
                                "," +
                                confidenceInterval[0] +
                                "," +
                                confidenceInterval[1];
            stringJoiner.add(resultLine);
        }
        Files.write(Paths.get("results_benchmark.csv"), stringJoiner.toString().getBytes());
    }

    @Setup
    public void setUp() throws IOException {
        itemsetMinerConfiguration = ItemsetMinerConfiguration.from(Resources.getResourceAsStream("configuration.json"));
    }

    @Benchmark
    public void benchmark() throws IOException, URISyntaxException {

        if (parsing.equals("MMTF")) {
            itemsetMinerConfiguration.getDataPointReaderConfiguration().setMmtf(true);
        } else {
            itemsetMinerConfiguration.getDataPointReaderConfiguration().setMmtf(false);
        }

        String fileName = "PF00127_chains_nrpdb_041416_BLAST_e-80_" + datasetSize + ".txt";
        Path chainListPath = Paths.get(Resources.getResourceAsFileLocation(fileName));

        itemsetMinerConfiguration.setInputListLocation(chainListPath.toString());

        new ItemsetMinerRunner(itemsetMinerConfiguration);
    }
}