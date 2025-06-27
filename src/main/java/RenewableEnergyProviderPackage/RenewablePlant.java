package RenewableEnergyProviderPackage;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class RenewablePlant {
    private final ScheduledExecutorService scheduler;

    RenewablePlant() {
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    void exec() {
        scheduler.scheduleAtFixedRate(new RequestPublisher("localhost"), 10, 10, TimeUnit.SECONDS);
    }
}
