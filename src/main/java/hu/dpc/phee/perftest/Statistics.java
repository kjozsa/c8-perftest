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

    public void recordRuntime(Long runtime) {
        runtimes.add(runtime);
    }

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
    }

    private void printResult(LongSummaryStatistics statistics) {
        long count = statistics.getCount();
        long sum = statistics.getSum();
        double average = statistics.getAverage();
        long min = statistics.getMin();
        long max =  statistics.getMax();

        logger.info("Process instances started with {} failed initializations", initFailCount);
        logger.info("Test completed in {}ms -> " + statistics.toString(), (endTime - startTime));
    }

    private LongSummaryStatistics calculateStatistics() {
        Stream<Long> stream = runtimes.stream();

        LongSummaryStatistics statistics = stream.collect(Collectors.summarizingLong(Long::longValue));

        return statistics;
    }
}
