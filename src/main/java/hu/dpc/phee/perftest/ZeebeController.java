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


    public void startWorkflowInstance(int num) {
        try {
            long processInstanceKey = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("step15")
                    .latestVersion()
                    .variables(Map.of("start", System.currentTimeMillis()))
                    .send()
                    .join()
                    .getProcessInstanceKey();
            logger.info("Started process instance [" + processInstanceKey + "][num: {}]:  " + System.currentTimeMillis(), num);

        }
        catch (ClientStatusException clientStatusException) {
            logger.info("Process instance [num: {}] start FAILED", num);
        }
    }
}
