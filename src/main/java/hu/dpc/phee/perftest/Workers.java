package hu.dpc.phee.perftest;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Workers {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ZeebeWorker(type = "step1")
    public void step1Worker(JobClient client, ActivatedJob job) {
        logger.trace("step1 invoked");
        client.newCompleteCommand(job.getKey()).send();
    }

    @ZeebeWorker(type = "step2")
    public void step2Worker(JobClient client, ActivatedJob job) {
        logger.trace("step2 invoked");
        client.newCompleteCommand(job.getKey()).send();
    }

    @ZeebeWorker(type = "step3")
    public void step3Worker(JobClient client, ActivatedJob job) {
        logger.trace("step3 invoked");
        client.newCompleteCommand(job.getKey()).send();
    }

    @ZeebeWorker(type = "step4")
    public void step4Worker(JobClient client, ActivatedJob job) {
        logger.trace("step4 invoked");
        client.newCompleteCommand(job.getKey()).send();
    }

    @ZeebeWorker(type = "step5", fetchVariables = "start")
    public void step5Worker(JobClient client, ActivatedJob job) {
        logger.trace("step5 invoked");

        Long start = (Long) job.getVariablesAsMap().get("start");
        client.newCompleteCommand(job.getKey()).send();
        long finish = System.currentTimeMillis();
        logger.info("finished flow in {} ms", (finish-start));
    }

}

