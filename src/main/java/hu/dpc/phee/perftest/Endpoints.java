package hu.dpc.phee.perftest;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class Endpoints {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void setup() {
        Javalin app = Javalin.create().start(8080);
        app.get("/", ctx -> ctx.result("boo"));

    }
}
