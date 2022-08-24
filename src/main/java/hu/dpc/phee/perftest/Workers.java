package hu.dpc.phee.perftest;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Workers {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Statistics statistics;


    /**
     * determines the length of the "work" each worker does
     */
    private static final  int workerSleepTime = 0;

    @ZeebeWorker(type = "step1")
    public void step1Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        logger.trace("Process instance [" + processInstanceKey + "] -> step1 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }
        client.newCompleteCommand(job.getKey()).send();
        long workerFinish = System.currentTimeMillis();
        logger.trace("Process instance [" + processInstanceKey + "] -> step1 complete: " + System.currentTimeMillis() + " in: " + (workerFinish-workerStart) + "ms");
    }

    @ZeebeWorker(type = "step2")
    public void step2Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        logger.trace("Process instance [" + processInstanceKey + "] -> step2 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }
        client.newCompleteCommand(job.getKey()).send();
        long workerFinish = System.currentTimeMillis();
        logger.trace("Process instance [" + processInstanceKey + "] -> step2 complete: " + System.currentTimeMillis() + " in: " + (workerFinish-workerStart) + "ms");
    }

    @ZeebeWorker(type = "step3")
    public void step3Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        logger.trace("Process instance [" + processInstanceKey + "] -> step3 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }
        client.newCompleteCommand(job.getKey()).send();
        long workerFinish = System.currentTimeMillis();
        logger.trace("Process instance [" + processInstanceKey + "] -> step3 complete: " + System.currentTimeMillis() + " in: " + (workerFinish-workerStart) + "ms");
    }

    @ZeebeWorker(type = "step4")
    public void step4Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        logger.trace("Process instance [" + processInstanceKey + "] -> step4 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }
        client.newCompleteCommand(job.getKey()).send();
        long workerFinish = System.currentTimeMillis();
        logger.trace("Process instance [" + processInstanceKey + "] -> step4 complete: " + System.currentTimeMillis() + " in: " + (workerFinish-workerStart) + "ms");
    }

    @ZeebeWorker(type = "step5", fetchVariables = "start")
    public void step5Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        logger.trace("Process instance [" + processInstanceKey + "] -> step5 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }

        Long start = (Long) job.getVariablesAsMap().get("start");
        Long num = (Long) job.getVariablesAsMap().get("num");
        client.newCompleteCommand(job.getKey()).send();
        long finish = System.currentTimeMillis();
        long flowRuntime = (finish-start);
        logger.trace("Process instance [" + processInstanceKey + "] -> step5 complete: " + System.currentTimeMillis() + " in: " + (finish-workerStart) + "ms");
        logger.debug("Process instance [{}][num: {}] -> finished flow in {}ms", processInstanceKey, num, flowRuntime);
        statistics.recordRuntime(flowRuntime);

        //if all of the processes of the test batch have completed
        if (statistics.completeProcessCount.incrementAndGet() == statistics.numberOfCreatedInstances) {
            statistics.endTest(finish);
        }
    }

}

