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
            zeebeController.startInstances(1);

            while (statistics.isTestRunning()) {
                Thread.sleep(5000);
            }

            ctx.result("test complete");
        });

        app.get("/start/{num}", ctx -> {
            int num = Integer.parseInt(ctx.pathParam("num"));
            zeebeController.startInstances(num);

            while (statistics.isTestRunning()) {
                Thread.sleep(5000);
            }

            ctx.result("test complete");
        });

        app.get("/start/slow/{num}", ctx -> {
            int num = Integer.parseInt(ctx.pathParam("num"));
            zeebeController.startInstances(num, 1000);

            while (statistics.isTestRunning()) {
                Thread.sleep(5000);
            }

            ctx.result("test complete");
        });
    }
}