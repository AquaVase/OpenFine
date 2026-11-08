package net.optifine;

import java.util.concurrent.ExecutorService;
import net.minecraft.client.Minecraft;
import net.optifine.util.ExecutorProxy;

public class SmartExecutorService extends ExecutorProxy {
    private ExecutorService executor;

    public SmartExecutorService(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    protected ExecutorService delegate() {
        return this.executor;
    }

    @Override
    public void execute(final Runnable command) {
        Runnable smartCommand = new Runnable() {
            @Override
            public void run() {
                long timeStartMs = System.currentTimeMillis();
                command.run();
                long timeEndMs = System.currentTimeMillis();
                long runMs = timeEndMs - timeStartMs;
                Minecraft mc = Minecraft.getInstance();
                if (mc != null && (mc.isLocalServer() || mc.level != null)) {
                    Config.sleep(10L * runMs);
                }
            }
        };
        super.execute(smartCommand);
    }
}
