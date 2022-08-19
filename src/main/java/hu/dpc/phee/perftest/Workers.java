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

    private static final  int workerSleepTime = 0;

    @ZeebeWorker(type = "step1")
    public void step1Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        logger.info("Process instance [" + processInstanceKey + "] -> step1 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }
        client.newCompleteCommand(job.getKey()).send();
        long workerFinish = System.currentTimeMillis();
        logger.info("Process instance [" + processInstanceKey + "] -> step1 complete: " + System.currentTimeMillis() + " in: " + (workerFinish-workerStart) + "ms");
    }

    @ZeebeWorker(type = "step2")
    public void step2Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        logger.info("Process instance [" + processInstanceKey + "] -> step2 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }
        client.newCompleteCommand(job.getKey()).send();
        long workerFinish = System.currentTimeMillis();
        logger.info("Process instance [" + processInstanceKey + "] -> step2 complete: " + System.currentTimeMillis() + " in: " + (workerFinish-workerStart) + "ms");
    }

    @ZeebeWorker(type = "step3")
    public void step3Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        logger.info("Process instance [" + processInstanceKey + "] -> step3 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }
        client.newCompleteCommand(job.getKey()).send();
        long workerFinish = System.currentTimeMillis();
        logger.info("Process instance [" + processInstanceKey + "] -> step3 complete: " + System.currentTimeMillis() + " in: " + (workerFinish-workerStart) + "ms");
    }

    @ZeebeWorker(type = "step4")
    public void step4Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        logger.info("Process instance [" + processInstanceKey + "] -> step4 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }
        client.newCompleteCommand(job.getKey()).send();
        long workerFinish = System.currentTimeMillis();
        logger.info("Process instance [" + processInstanceKey + "] -> step4 complete: " + System.currentTimeMillis() + " in: " + (workerFinish-workerStart) + "ms");
    }

    @ZeebeWorker(type = "step5", fetchVariables = "start")
    public void step5Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        logger.info("Process instance [" + processInstanceKey + "] -> step5 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }

        Long start = (Long) job.getVariablesAsMap().get("start");
        client.newCompleteCommand(job.getKey()).send();
        long finish = System.currentTimeMillis();
        logger.info("Process instance [" + processInstanceKey + "] -> step5 complete: " + System.currentTimeMillis() + " in: " + (finish-workerStart) + "ms");
        logger.info("Process instance [" + processInstanceKey + "] -> finished flow in {} ms", (finish-start));
    }

}

