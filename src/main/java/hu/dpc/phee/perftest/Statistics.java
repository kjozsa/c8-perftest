package hu.dpc.phee.perftest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Statistics {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private long startTime = 0;
    private long endTime = 0;
    private int initFailCount = 0;
    public int numberOfCreatedInstances = 0;

    /**
     * stores atomically the count of completed process instances, used to determine whether all instances have completed at any given time
     */
    public AtomicInteger completeProcessCount = new AtomicInteger(0);

    /**
     * stores the calculated runtime of each individual process instance
     */
    private List<Long> runtimes = new LinkedList<>();
    private List<Long> waitingTimes = new LinkedList<>();
    private List<Long> executionTimes = new LinkedList<>();

    public void recordRuntime(Long runtime) {
        runtimes.add(runtime);
    }

    public void recordWaitingtime(Long runtime) {
        waitingTimes.add(runtime);
    }

    public void recordExecutiontime(Long runtime) { executionTimes.add(runtime); }

    public void updateInitFailCount(int initFailCount) {
        this.initFailCount += initFailCount;
    }

    public void beginTest(int instanceCount, long startTime) {
        if (this.startTime == 0) {
            this.startTime = startTime;
        }
        numberOfCreatedInstances += instanceCount;
    }

    public void endTest(long endTime) {
        this.endTime = endTime;
        printResult(calculateStatistics());

        completeProcessCount.set(0);
        numberOfCreatedInstances = 0;
        initFailCount = 0;
        startTime = 0;
        endTime = 0;
        runtimes = new LinkedList<>();
        waitingTimes = new LinkedList<>();
        executionTimes = new LinkedList<>();
    }

    private void printResult(LongSummaryStatistics[] statistics) {

        long count = statistics[0].getCount();
        long sum = statistics[0].getSum();
        double average = statistics[0].getAverage();
        long min = statistics[0].getMin();
        long max =  statistics[0].getMax();

        logger.info("Process instances started with {} failed initializationsl", initFailCount);
        logger.info("Test completed in {}ms -> " + statistics[0].toString(), convertTime(endTime - startTime));
        logger.info("Average waiting time: {}ms ->" + statistics[1].toString(),statistics[1].getAverage());
        logger.info("Average execution time: {}ms ->" + statistics[2].toString(),statistics[2].getAverage());
    }

    private LongSummaryStatistics[] calculateStatistics() {
        Stream<Long> stream = runtimes.stream();
        Stream<Long> waitStream = waitingTimes.stream();
        Stream<Long> executionStream = executionTimes.stream();

        LongSummaryStatistics statistics = stream.collect(Collectors.summarizingLong(Long::longValue));
        LongSummaryStatistics waitStatistics = waitStream.collect(Collectors.summarizingLong(Long::longValue));
        LongSummaryStatistics executionStatistics = executionStream.collect(Collectors.summarizingLong(Long::longValue));

        LongSummaryStatistics[] stats = new LongSummaryStatistics[3];
        stats[0]=statistics;
        stats[1]=waitStatistics;
        stats[2]=executionStatistics;
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
