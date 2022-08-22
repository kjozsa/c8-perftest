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


    public int startWorkflowInstance(int num) {
        int attempt = 1;
        int fail=0;
        long wait_ms = 0;
        while (attempt > 0) {
            logger.info("Attempting to start process [num: {}][attempt: {}]", num, attempt);
            try {
                long processInstanceKey = zeebeClient.newCreateInstanceCommand()
                        .bpmnProcessId("step15")
                        .latestVersion()
                        .variables(Map.of("start", System.currentTimeMillis()))
                        .send()
                        .join()
                        .getProcessInstanceKey();
                logger.info("Started process instance [" + processInstanceKey + "][num: {}]:  " + System.currentTimeMillis(), num);
                attempt = 0;
            } catch (ClientStatusException clientStatusException) {
                //exponential wait until retry
                wait_ms = (long) (100 * Math.pow(2, attempt - 1));
                logger.warn("Process instance [num: {}][attempt: {}] start FAILED -> retrying in [{}]ms", num, attempt, wait_ms);
                fail=1;
                try {
                    Thread.sleep(wait_ms);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                attempt++;
            }
        }
        return fail;
    }
}
