package hu.dpc.phee.perftest;

import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ZeebeController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ZeebeClient zeebeClient;


    public void startWorkflowInstance() {
        zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("step15")
                .latestVersion()
                .variables(Map.of("start", System.currentTimeMillis()))
                .send()
                .join();
    }
}
