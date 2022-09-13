package hu.dpc.phee.perftest;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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


    public void startWorkflowInstance(int num) {
        long start;
        long finish;
        long initTime = 0;
        int attempt = 1;
        int failedCount=0;
        long retryWait;
        while (attempt > 0) {
            logger.debug("Attempting to start process [num: {}][attempt: {}]", num, attempt);
            try {
                start = System.currentTimeMillis();
                long processInstanceKey = zeebeClient.newCreateInstanceCommand()
                        .bpmnProcessId("step15")
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
                //exponential wait until retry
                retryWait = (long) (100 * Math.pow(2, attempt - 1));
                logger.warn("Process instance [num: {}][attempt: {}] start FAILED -> retrying in [{}]ms", num, attempt, retryWait);
                failedCount += 1;
                try {
                    Thread.sleep(retryWait);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                attempt++;
            }
        }
        statistics.recordInitTime(initTime);
        statistics.updateInitFailCount(failedCount);
    }
}
