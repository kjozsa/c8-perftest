package hu.dpc.phee.perftest;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ZeebeController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private Statistics statistics;

    @Value("${process-instance}")
    private String processInstance;

    /**
     * starts multiple workflow process instances in a manner described by the arguments
     *
     * @param instanceCount the number of workflow instances to start
     */
    public void startInstances(int instanceCount) {
        this.startInstances(instanceCount, 0);
    }

    /**
     * starts multiple workflow process instances in a manner described by the arguments
     *
     * @param instanceCount  the number of workflow instances to start
     * @param interInitDelay the amount of time (in ms) to wait between starts
     */
    public void startInstances(int instanceCount, long interInitDelay) {
        logger.info("Starting {} instances of {} flow", instanceCount, processInstance);

        statistics.beginTest(instanceCount, System.currentTimeMillis());
        for (int i = 0; i < instanceCount; i++) {
            startWorkflowInstance(i);

            if (interInitDelay > 0) {
                try {
                    Thread.sleep(interInitDelay);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * starts a single workflow process instance
     *
     * @param num a number assigned to each PI for debugging purposes
     */
    public void startWorkflowInstance(int num) {
        long start;         //init start
        long finish;        //init finish
        long initTime = 0;  //the duration of the initiation
        int attempt = 1;    //the count of init attempts
        int failedCount = 0;  //the count of init fails
        long retryWait;     //the delay between init retries, increases with each consecutive retry

        while (attempt > 0) {
            logger.debug("Attempting to start process [num: {}][attempt: {}]", num, attempt);
            try {
                start = System.currentTimeMillis();

                long processInstanceKey = zeebeClient.newCreateInstanceCommand()
                        .bpmnProcessId(processInstance)
                        .latestVersion()
                        .variables(Map.of("start", System.currentTimeMillis(), "num", num))
                        .send()
                        .join()
                        .getProcessInstanceKey();

                finish = System.currentTimeMillis();
                initTime = finish - start;

                logger.debug("Started process instance [{}][num: {}] in: {}", processInstanceKey, num, initTime);
                attempt = 0;
            } catch (ClientStatusException clientStatusException) {
                //calculate next retry interval
                retryWait = (long) (100 * Math.pow(2, attempt - 1));

                //log init fail
                logger.warn("Process instance [num: {}][attempt: {}] start FAILED -> retrying in [{}]ms", num, attempt, retryWait);

                //exponential wait until retry
                try {
                    Thread.sleep(retryWait);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                failedCount++;
                attempt++;
            }
        }

        //record init statistics
        statistics.recordInitTime(initTime);
        statistics.updateInitFailCount(failedCount);
    }
}
