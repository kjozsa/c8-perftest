package hu.dpc.phee.perftest;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LongSummaryStatistics;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@PropertySource("classpath:application.properties")
public class Statistics {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient zeebeClient;

    private ZeebeClientConfiguration configuration = zeebeClient.getConfiguration();

    private int threads = configuration.getNumJobWorkerExecutionThreads();
    private int maxJobs = configuration.getDefaultJobWorkerMaxJobsActive();

    @Value("${workerSleepTime:10}")
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
    private ConcurrentLinkedQueue<Long> initTimes = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Long> runtimes = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Long> waitingTimes = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Long> executionTimes = new ConcurrentLinkedQueue<>();

    public void recordInitTime(Long initTime) {
        initTimes.add(initTime);
    }
    public void recordRuntime(Long runtime) {
        runtimes.add(runtime);
    }

    public void recordWaitingTime(Long waitingTime) {
        waitingTimes.add(waitingTime);
    }

    public void recordExecutionTime(Long executionTime) {
        executionTimes.add(executionTime);
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
        printResult(calculateStatistics());

        completeProcessCount.set(0);
        numberOfCreatedInstances = 0;
        initFailCount = 0;
        startTime = 0;
        endTime = 0;
        runtimes = new ConcurrentLinkedQueue<>();
        waitingTimes = new ConcurrentLinkedQueue<>();
        executionTimes = new ConcurrentLinkedQueue<>();
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
            logger.info("Test not running ---");
        }
    }

    private void printResult(LongSummaryStatistics[] statistics) {

        long count = statistics[0].getCount();
        long sum = statistics[0].getSum();
        double average = statistics[0].getAverage();
        long min = statistics[0].getMin();
        long max =  statistics[0].getMax();

        logger.info("Process instances started with {} failed initializations", initFailCount);
        logger.info("Test completed in {}ms \t-> " + statistics[0].toString(), convertTime(endTime - startTime));
        logger.info("Average init time: {}ms \t->" + statistics[1].toString(), statistics[1].getAverage());
        logger.info("Average waiting time: {}ms \t-> " + statistics[2].toString(),statistics[2].getAverage());
        logger.info("Average execution time: {}ms \t-> " + statistics[3].toString(),statistics[3].getAverage());
    }

    private LongSummaryStatistics[] calculateStatistics() {
        Stream<Long> initStream = initTimes.stream();
        Stream<Long> runStream = runtimes.stream();
        Stream<Long> waitStream = waitingTimes.stream();
        Stream<Long> executionStream = executionTimes.stream();

        LongSummaryStatistics initStatistics = initStream.collect(Collectors.summarizingLong(Long::longValue));
        LongSummaryStatistics runStatistics = runStream.collect(Collectors.summarizingLong(Long::longValue));
        LongSummaryStatistics waitStatistics = waitStream.collect(Collectors.summarizingLong(Long::longValue));
        LongSummaryStatistics executionStatistics = executionStream.collect(Collectors.summarizingLong(Long::longValue));

        LongSummaryStatistics[] stats = new LongSummaryStatistics[4];
        stats[0]=runStatistics;
        stats[1]=initStatistics;
        stats[2]=waitStatistics;
        stats[3]=executionStatistics;
        return stats;
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
