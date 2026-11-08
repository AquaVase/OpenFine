package net.optifine.util;

import com.mojang.blaze3d.platform.NativeImage;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import net.optifine.Config;

public class NativeMemory {
    private static long imageAllocated = 0L;
    private static LongSupplier bufferAllocatedSupplier = makeLongSupplier(
        new String[][]{
            {"sun.misc.SharedSecrets", "getJavaNioAccess", "getDirectBufferPool", "getMemoryUsed"},
            {"jdk.internal.misc.SharedSecrets", "getJavaNioAccess", "getDirectBufferPool", "getMemoryUsed"}
        },
        makeDefaultAllocatedSupplier()
    );
    private static LongSupplier bufferMaximumSupplier = makeLongSupplier(
        new String[][]{{"sun.misc.VM", "maxDirectMemory"}, {"jdk.internal.misc.VM", "maxDirectMemory"}}, makeDefaultMaximumSupplier()
    );

    public static long getBufferAllocated() {
        return bufferAllocatedSupplier == null ? -1L : bufferAllocatedSupplier.getAsLong();
    }

    public static long getBufferMaximum() {
        return bufferMaximumSupplier == null ? -1L : bufferMaximumSupplier.getAsLong();
    }

    public static synchronized void imageAllocated(NativeImage nativeImage) {
        imageAllocated = imageAllocated + nativeImage.getSize();
    }

    public static synchronized void imageFreed(NativeImage nativeImage) {
        imageAllocated = imageAllocated - nativeImage.getSize();
    }

    public static long getImageAllocated() {
        return imageAllocated;
    }

    private static BufferPoolMXBean getDirectBufferPoolMXBean() {
        for (BufferPoolMXBean mxBean : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {
            if (Config.equals(mxBean.getName(), "direct")) {
                return mxBean;
            }
        }

        return null;
    }

    private static LongSupplier makeDefaultAllocatedSupplier() {
        final BufferPoolMXBean mxBean = getDirectBufferPoolMXBean();
        return mxBean == null ? null : new LongSupplier() {
            @Override
            public long getAsLong() {
                return mxBean.getMemoryUsed();
            }
        };
    }

    private static LongSupplier makeDefaultMaximumSupplier() {
        return new LongSupplier() {
            @Override
            public long getAsLong() {
                return Runtime.getRuntime().maxMemory();
            }
        };
    }

    private static LongSupplier makeLongSupplier(String[][] paths, LongSupplier defaultSupplier) {
        List<Throwable> exceptions = new ArrayList<>();

        for (int i = 0; i < paths.length; i++) {
            String[] path = paths[i];

            try {
                LongSupplier supplier = makeLongSupplier(path);
                if (supplier != null) {
                    return supplier;
                }
            } catch (Throwable e) {
                exceptions.add(e);
            }
        }

        for (Throwable t : exceptions) {
            Config.warn("(Reflector) " + t.getClass().getName() + ": " + t.getMessage());
        }

        return defaultSupplier;
    }

    private static LongSupplier makeLongSupplier(String[] path) throws Exception {
        if (path.length < 2) {
            return null;
        }

        Class cls = Class.forName(path[0]);
        Method method = cls.getMethod(path[1]);
        method.setAccessible(true);
        Object object = null;

        for (int i = 2; i < path.length; i++) {
            String name = path[i];
            object = method.invoke(object);
            method = object.getClass().getMethod(name);
            method.setAccessible(true);
        }

        final Object objectF = object;
        final Method methodF = method;
        return new LongSupplier() {
            private boolean disabled = false;

            @Override
            public long getAsLong() {
                if (this.disabled) {
                    return -1L;
                }

                try {
                    return (Long)methodF.invoke(objectF);
                } catch (Throwable e) {
                    Config.warn("(Reflector) " + e.getClass().getName() + ": " + e.getMessage());
                    this.disabled = true;
                    return -1L;
                }
            }
        };
    }
}
