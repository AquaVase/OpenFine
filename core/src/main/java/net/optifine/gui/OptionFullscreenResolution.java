package net.optifine.gui;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;

public class OptionFullscreenResolution {
    public static OptionInstance make() {
        Window window = Minecraft.getInstance().getWindow();
        Monitor monitor = window.findBestMonitor();
        int j;
        if (monitor == null) {
            j = -1;
        } else {
            Optional<VideoMode> optional = window.getPreferredFullscreenVideoMode();
            j = optional.map(monitor::indexOfMode).orElse(-1);
        }

        OptionInstance<Integer> optioninstance = new OptionInstance<>(
            "options.fullscreen.resolution",
            OptionInstance.noTooltip(),
            (captionIn, valueIn) -> {
                if (monitor == null) {
                    return Component.translatable("options.fullscreen.unavailable");
                } else {
                    return valueIn == -1
                        ? Options.genericValueLabel(captionIn, Component.translatable("options.fullscreen.current"))
                        : Options.genericValueLabel(captionIn, Component.literal(monitor.mode(valueIn).toString()));
                }
            },
            new OptionInstance.IntRange(-1, monitor != null ? monitor.modeCount() - 1 : -1),
            j,
            p_232800_2_ -> {
                if (monitor != null) {
                    window.setPreferredFullscreenVideoMode(p_232800_2_ == -1 ? Optional.empty() : Optional.of(monitor.mode(p_232800_2_)));
                }
            }
        );
        return optioninstance;
    }
}
