package optifine;

import java.lang.reflect.Field;
import java.util.Map;

public class LaunchUtils {
   private static Boolean forgeServer = null;

   public static boolean isForgeServer() {
      if (forgeServer == null) {
         try {
            Class cls = Class.forName("net.minecraft.launchwrapper.Launch");
            Field fieldBlackboard = cls.getField("blackboard");
            Map<String, Object> blackboard = (Map<String, Object>)fieldBlackboard.get(null);
            Map<String, String> launchArgs = (Map<String, String>)blackboard.get("launchArgs");
            String accessToken = launchArgs.get("--accessToken");
            String version = launchArgs.get("--version");
            boolean onServer = accessToken == null && Utils.equals(version, "UnknownFMLProfile");
            forgeServer = onServer;
         } catch (Throwable e) {
            System.out.println("Error checking Forge server: " + e.getClass().getName() + ": " + e.getMessage());
            forgeServer = Boolean.FALSE;
         }
      }

      return forgeServer;
   }
}
