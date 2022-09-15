package hu.dpc.phee.perftest;

import io.javalin.Javalin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

@Service
public class Endpoints {

    @Autowired
    ZeebeController zeebeController;

    @Autowired
    Workers workers;

    @Autowired
    Statistics statistics;

    @PostConstruct
    public void setup() {
        Javalin app = Javalin.create(config -> config.showJavalinBanner = false).start(8080);
        app.get("/", ctx -> ctx.result("ready"));

        app.get("/start", ctx -> {
            statistics.beginTest(1, System.currentTimeMillis());

            zeebeController.startWorkflowInstance(0);

            ctx.result("process flows started");
        });

        app.get("/start/{num}", ctx -> {
            int num = Integer.parseInt(ctx.pathParam("num"));

            statistics.beginTest(num, System.currentTimeMillis());

            //TODO measure PI starts per sec
            for (int i = 0; i < num; i++) {
                zeebeController.startWorkflowInstance(i);
            }
            ctx.result("process flows started");
        });

        app.get("/start/slow/{num}", ctx -> {
            int num = Integer.parseInt(ctx.pathParam("num"));

            statistics.beginTest(num, System.currentTimeMillis());

            for (int i = 0; i < num; i++) {
                zeebeController.startWorkflowInstance(i);

                //wait one second between instance inits
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            ctx.result("process flows started");
        });
    }
}
