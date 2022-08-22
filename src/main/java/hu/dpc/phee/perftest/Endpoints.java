package hu.dpc.phee.perftest;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class Endpoints {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ZeebeController zeebeController;
    private int failedCount;

    @PostConstruct
    public void setup() {
        Javalin app = Javalin.create(config -> config.showJavalinBanner = false).start(8080);
        app.get("/", ctx -> ctx.result("boo"));

        app.get("/start", ctx -> {
            failedCount=0;
            logger.info("starting 1 instance");
            failedCount+=zeebeController.startWorkflowInstance(0);
            ctx.result("Number of fails : " +String.valueOf(failedCount));
        });

        app.get("/start/{num}", ctx -> {
            int num = Integer.parseInt(ctx.pathParam("num"));
            logger.info("starting {} instances", num);
            failedCount=0;
            for (int i = 0; i < num; i++) {
                failedCount+=zeebeController.startWorkflowInstance(i);
            }
            logger.info("Number of fails: {}", failedCount);
            ctx.result("Number of fails : " +String.valueOf(failedCount));
        });
    }
}
