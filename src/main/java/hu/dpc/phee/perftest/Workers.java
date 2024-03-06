package hu.dpc.phee.perftest;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
@PropertySource("classpath:application.properties")
public class Workers {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Statistics statistics;

    @Value("${workerSleepTime}")
    private int workerSleepTime;

    @PostConstruct
    public void setup() {
        logger.info("configured to sleep at workers for {}", workerSleepTime);
    }


    @ZeebeWorker(type = "step1")
    public void step1Worker(JobClient client, ActivatedJob job) {
        internalStep(client, job, "step1");
    }

    private void internalStep(JobClient client, ActivatedJob job, String stepName) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        Map<String, Object> varMap = job.getVariablesAsMap();
        logger.trace("Process instance [{}] -> {} invoked", processInstanceKey, stepName);
        try {
            Thread.sleep(workerSleepTime);
        } catch (Exception ignored) {
        }
        long workerFinish = System.currentTimeMillis();
        varMap.put("netExTime", (workerFinish - workerStart));
        client.newCompleteCommand(job.getKey()).variables(varMap).send();
        logger.trace("Process instance [{}] -> {} complete: {} in: {}ms", processInstanceKey, stepName, System.currentTimeMillis(), (workerFinish - workerStart));
    }

    @ZeebeWorker(type = "step2")
    public void step2Worker(JobClient client, ActivatedJob job) {
        internalStep(client, job, "step2");
    }

    @ZeebeWorker(type = "step3")
    public void step3Worker(JobClient client, ActivatedJob job) {
        internalStep(client, job, "step3");
    }

    @ZeebeWorker(type = "step4")
    public void step4Worker(JobClient client, ActivatedJob job) {
        internalStep(client, job, "step4");
    }

    @ZeebeWorker(type = "step5")
    public void step5Worker(JobClient client, ActivatedJob job) {
        long processInstanceKey = job.getProcessInstanceKey();
        long workerStart = System.currentTimeMillis();
        logger.trace("Process instance [" + processInstanceKey + "] -> step5 invoked:  " + workerStart);
        try {
            Thread.sleep(workerSleepTime);
        } catch (Exception e) {

        }

        Map<String, Object> variableMap = job.getVariablesAsMap();
        int num = (int) variableMap.get("num");
        Long start = (Long) variableMap.get("start");
        int jobNetExTime = (int) variableMap.get("netExTime");

        client.newCompleteCommand(job.getKey()).send();

        long finish = System.currentTimeMillis();
        long flowRuntime = (finish - start);
        jobNetExTime += (finish - workerStart);
        long waitingTime = flowRuntime - jobNetExTime;
        logger.trace("Process instance [" + processInstanceKey + "] -> step5 complete: " + System.currentTimeMillis() + " in: " + (finish - workerStart) + "ms");
        logger.debug("Process instance [{}][num: {}] -> finished flow in {}ms: {}ms execution, {}ms waiting", processInstanceKey, num, flowRuntime, jobNetExTime, (flowRuntime - jobNetExTime));
        statistics.recordRuntime(flowRuntime);
        statistics.recordWaitingTime(waitingTime);
        statistics.recordExecutionTime((long) jobNetExTime);

        //if all the processes of the test batch have completed
        if (statistics.completeProcessCount.incrementAndGet() == statistics.numberOfCreatedInstances) {
            statistics.endTest(finish);
        }
    }

}

