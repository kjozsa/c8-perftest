package hu.dpc.phee.perftest;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Workers {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Statistics statistics;


    /**
     * determines the length of the "work" each worker does
     */
    private static final  int workerSleepTime = 10;

    @ZeebeWorker(type = "step1")
    public void step1Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        Map<String, Object> varMap =  job.getVariablesAsMap();
        logger.trace("Process instance [" + processInstanceKey + "] -> step1 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }
        long workerFinish = System.currentTimeMillis();
        varMap.put("netExTime",(workerFinish-workerStart));
        client.newCompleteCommand(job.getKey()).variables(varMap).send();
        logger.trace("Process instance [" + processInstanceKey + "] -> step1 complete: " + System.currentTimeMillis() + " in: " + (workerFinish-workerStart) + "ms");
    }

    @ZeebeWorker(type = "step2")
    public void step2Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        Map<String, Object> varMap =  job.getVariablesAsMap();
        int jobNetExTime = (int) varMap.get("netExTime");
        logger.trace("Process instance [" + processInstanceKey + "] -> step2 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }
        long workerFinish = System.currentTimeMillis();
        varMap.put("netExTime",(workerFinish-workerStart+jobNetExTime));
        client.newCompleteCommand(job.getKey()).variables(varMap).send();
        logger.trace("Process instance [" + processInstanceKey + "] -> step2 complete: " + System.currentTimeMillis() + " in: " + (workerFinish-workerStart) + "ms");
    }

    @ZeebeWorker(type = "step3")
    public void step3Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        Map<String, Object> varMap =  job.getVariablesAsMap();
        int jobNetExTime = (int) varMap.get("netExTime");
        logger.trace("Process instance [" + processInstanceKey + "] -> step3 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }
        long workerFinish = System.currentTimeMillis();
        varMap.put("netExTime",(workerFinish-workerStart+jobNetExTime));
        client.newCompleteCommand(job.getKey()).variables(varMap).send();
        logger.trace("Process instance [" + processInstanceKey + "] -> step3 complete: " + System.currentTimeMillis() + " in: " + (workerFinish-workerStart) + "ms");
    }

    @ZeebeWorker(type = "step4")
    public void step4Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        Map<String, Object> varMap =  job.getVariablesAsMap();
        int jobNetExTime = (int) varMap.get("netExTime");
        logger.trace("Process instance [" + processInstanceKey + "] -> step4 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }
        long workerFinish = System.currentTimeMillis();
        varMap.put("netExTime",(workerFinish-workerStart+jobNetExTime));
        client.newCompleteCommand(job.getKey()).variables(varMap).send();
        logger.trace("Process instance [" + processInstanceKey + "] -> step4 complete: " + System.currentTimeMillis() + " in: " + (workerFinish-workerStart) + "ms");
    }

    @ZeebeWorker(type = "step5")
    public void step5Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        logger.trace("Process instance [" + processInstanceKey + "] -> step5 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        }catch (Exception e) {

        }

        Map<String, Object> variableMap = job.getVariablesAsMap();
        int num = (int) variableMap.get("num");
        Long start = (Long) variableMap.get("start");
        int jobNetExTime = (int) variableMap.get("netExTime");

        client.newCompleteCommand(job.getKey()).send();

        long finish = System.currentTimeMillis();
        long flowRuntime = (finish-start);
        jobNetExTime += (finish - workerStart);
        long waitingTime = flowRuntime-jobNetExTime;
        logger.trace("Process instance [" + processInstanceKey + "] -> step5 complete: " + System.currentTimeMillis() + " in: " + (finish-workerStart) + "ms");
        logger.debug("Process instance [{}][num: {}] -> finished flow in {}ms: {}ms execution, {}ms waiting", processInstanceKey, num, flowRuntime, jobNetExTime, (flowRuntime - jobNetExTime) );
        statistics.recordRuntime(flowRuntime);
        statistics.recordWaitingTime(waitingTime);
        statistics.recordExecutionTime((long) jobNetExTime);

        //if all the processes of the test batch have completed
        if (statistics.completeProcessCount.incrementAndGet() == statistics.numberOfCreatedInstances) {
            statistics.endTest(finish);
        }
    }

}

