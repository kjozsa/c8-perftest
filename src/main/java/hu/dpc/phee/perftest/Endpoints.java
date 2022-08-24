package hu.dpc.phee.perftest;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class Endpoints {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ZeebeController zeebeController;

    @Autowired
    Workers workers;

    @Autowired
    Statistics statistics;

    private int failedCount;

    @PostConstruct
    public void setup() {
        Javalin app = Javalin.create(config -> config.showJavalinBanner = false).start(8080);
        app.get("/", ctx -> ctx.result("boo"));

        app.get("/start", ctx -> {
            failedCount=0;
            logger.info("Initialising 1 instance");
            statistics.beginTest(1, System.currentTimeMillis());

            failedCount+=zeebeController.startWorkflowInstance(0);

            statistics.updateInitFailCount(failedCount);
            ctx.result("Number of fails : " +String.valueOf(failedCount));
        });

        app.get("/start/{num}", ctx -> {
            int num = Integer.parseInt(ctx.pathParam("num"));
            failedCount=0;
            logger.info("Initialising {} instances", num);
            statistics.beginTest(num, System.currentTimeMillis());

            for (int i = 0; i < num; i++) {
                failedCount+=zeebeController.startWorkflowInstance(i);
            }

            statistics.updateInitFailCount(failedCount);
            ctx.result("Number of fails : " +String.valueOf(failedCount));
        });

        app.get("/start/slow/{num}", ctx -> {
            int num = Integer.parseInt(ctx.pathParam("num"));
            failedCount=0;
            logger.info("Initialising {} instances", num);
            statistics.beginTest(num, System.currentTimeMillis());

            for (int i = 0; i < num; i++) {
                failedCount+=zeebeController.startWorkflowInstance(i);

                //wait one second between instance inits
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            statistics.updateInitFailCount(failedCount);
            ctx.result("Number of fails : " +String.valueOf(failedCount));
        });
    }
}
