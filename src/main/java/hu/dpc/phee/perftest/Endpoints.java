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


    @PostConstruct
    public void setup() {
        Javalin app = Javalin.create(config -> config.showJavalinBanner = false).start(8080);
        app.get("/", ctx -> ctx.result("boo"));

        app.get("/start", ctx -> {
            logger.info("starting 1 instance");
            zeebeController.startWorkflowInstance(0);
            ctx.result("ok");
        });

        app.get("/start/{num}", ctx -> {
            int num = Integer.parseInt(ctx.pathParam("num"));
            logger.info("starting {} instances", num);
            for (int i = 0; i < num; i++) {
                zeebeController.startWorkflowInstance(i);
            }
            ctx.result("ok");
        });
    }
}
