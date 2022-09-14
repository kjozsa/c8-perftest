package hu.dpc.phee.perftest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LongSummaryStatistics;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

@Component
@PropertySource("classpath:application.properties")
public class Statistics {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${zeebe.client.worker.threads}")
    private int threads;
    @Value("${zeebe.client.worker.max-jobs-active}")
    private int maxJobs;
    @Value("${workerSleepTime}")
    private int workerSleepTime;

    private long startTime = 0;
    private long endTime = 0;
    private int initFailCount = 0;
    public int numberOfCreatedInstances = 0;

    private boolean testRunning = false;

    /**
     * stores atomically the count of completed process instances, used to determine whether all instances have completed at any given time
     */
    public AtomicInteger completeProcessCount = new AtomicInteger(0);

    /**
     * stores the calculated runtime of each individual process instance
     */
    private DescriptiveStatistics initStats = new SynchronizedDescriptiveStatistics();
    private DescriptiveStatistics runStats = new SynchronizedDescriptiveStatistics();
    private DescriptiveStatistics waitingStats = new SynchronizedDescriptiveStatistics();
    private DescriptiveStatistics executionStats = new SynchronizedDescriptiveStatistics();

    public void recordInitTime(long initTime) {
        initStats.addValue(initTime);
    }

    public void recordRuntime(long runtime) {
        runStats.addValue(runtime);
    }

    public void recordWaitingTime(long waitingTime) {
        waitingStats.addValue(waitingTime);
    }

    public void recordExecutionTime(long executionTime) {
        executionStats.addValue(executionTime);
    }

    public void updateInitFailCount(int initFailCount) {
        this.initFailCount += initFailCount;
    }

    public void beginTest(int instanceCount, long startTime) {
        logger.info("Initialising {} instances", instanceCount);
        logger.info("thread-count={}, max-active-jobs={}, worker-sleep={}", threads, maxJobs, workerSleepTime);

        if (!testRunning) {
            testRunning = true;
            this.startTime = startTime;
        }

        numberOfCreatedInstances += instanceCount;
    }

    public void endTest(long endTime) {
        testRunning = false;

        this.endTime = endTime;

        printStatistics();

        completeProcessCount.set(0);
        numberOfCreatedInstances = 0;
        initFailCount = 0;
        startTime = 0;
        endTime = 0;

        initStats.clear();
        runStats.clear();
        waitingStats.clear();
        executionStats.clear();
    }

    /**
     * logs scheduled status updates while test program is running
     */
    @Scheduled(fixedRate = 60 * 1000, initialDelay = 10 * 1000)
    private void printUpdate() {
        if (testRunning) {
            logger.info("Test is running -> [{}] out of [{}] process instances completed in {}", completeProcessCount.get(), numberOfCreatedInstances, convertTime(System.currentTimeMillis() - startTime));
        }
        else {
            logger.info("Test not running [X]");
        }
    }

    private void printStatistics() {
        logger.info("Test completed -> [{}] instances in [{}] with [{}] retried initialisations", completeProcessCount.get(), convertTime(endTime - startTime), initFailCount);

        logger.info("PI initiation statistics  \t-> [n={}][average: {}ms, standard-deviation: {}ms, variance: {}ms]", initStats.getN(), initStats.getMean(), initStats.getStandardDeviation(), initStats.getVariance());
        logger.info("PI initiation percentiles \t-> [min: {}][25th: {}][50th: {}][75th: {}][max: {}]", initStats.getMin(), initStats.getPercentile(25), initStats.getPercentile(50), initStats.getPercentile(75), initStats.getMax());

        logger.info("PI runtime statistics  \t-> [n={}][average: {}ms, standard-deviation: {}ms, variance: {}ms]", runStats.getN(), runStats.getMean(), runStats.getStandardDeviation(), runStats.getVariance());
        logger.info("PI runtime percentiles \t-> [min: {}][25th: {}][50th: {}][75th: {}][max: {}]", runStats.getMin(), runStats.getPercentile(25), runStats.getPercentile(50), runStats.getPercentile(75), runStats.getMax());

        logger.info("PI idle time statistics  \t-> [n={}][average: {}ms, standard-deviation: {}ms, variance: {}ms]", waitingStats.getN(), waitingStats.getMean(), waitingStats.getStandardDeviation(), waitingStats.getVariance());
        logger.info("PI idle time percentiles \t-> [min: {}][25th: {}][50th: {}][75th: {}][max: {}]", waitingStats.getMin(), waitingStats.getPercentile(25), waitingStats.getPercentile(50), waitingStats.getPercentile(75), waitingStats.getMax());

        logger.info("PI execution time statistics  \t-> [n={}][average: {}ms, standard-deviation: {}ms, variance: {}ms]", executionStats.getN(), executionStats.getMean(), executionStats.getStandardDeviation(), executionStats.getVariance());
        logger.info("PI execution time percentiles \t-> [min: {}][25th: {}][50th: {}][75th: {}][max: {}]", executionStats.getMin(), executionStats.getPercentile(25), executionStats.getPercentile(50), executionStats.getPercentile(75), executionStats.getMax());
    }

    public String convertTime(long sum){
        long millis = sum % 1000;
        long second = (sum / 1000) % 60;
        long minute = (sum / (1000 * 60)) % 60;
        long hour = (sum / (1000 * 60 * 60)) % 24;
        String time = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
        return time;
    }

}
