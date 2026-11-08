package net.optifine.shaders;

import com.google.common.base.Charsets;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class DefaultShaders {
    public static InputStream getResourceAsStream(String filename) {
        if (filename.endsWith("/final.vsh")) {
            return getFinalVsh();
        } else if (filename.endsWith("/final.fsh")) {
            return getFinalFsh();
        } else if (filename.endsWith("/invert_depth.vsh")) {
            return getInvertDepthVsh();
        } else {
            return filename.endsWith("/invert_depth.fsh") ? getInvertDepthFsh() : null;
        }
    }

    private static InputStream getFinalVsh() {
        String str = "#version 150\nuniform mat4 modelViewMatrix;\nuniform mat4 projectionMatrix;\nin vec2 vaUV0;\nin vec3 vaPosition;\nout vec2 texcoord;\nvoid main()\n{\n  gl_Position = (projectionMatrix * modelViewMatrix * vec4(vaPosition, 1.0));\n  texcoord = vec4(vaUV0, 0.0, 1.0).st;\n}\n";
        return getInputStream(str);
    }

    private static InputStream getFinalFsh() {
        String str = "#version 150\nuniform sampler2D colortex0;\nin vec2 texcoord;\n/* DRAWBUFFERS:0 */\nout vec4 outColor0;\nvoid main()\n{\n  outColor0 = texture(colortex0, texcoord);\n}\n";
        return getInputStream(str);
    }

    private static InputStream getInvertDepthVsh() {
        return getFinalVsh();
    }

    private static InputStream getInvertDepthFsh() {
        String str = "#version 150\n#COMMENT_COMPATIBILITY\nuniform sampler2D depthtex0;\nin vec2 texcoord;\n/* DRAWBUFFERS:0 */\nvoid main()\n{\n  gl_FragDepth = 1.0 - texture(depthtex0, texcoord).r;\n}\n";
        str = str.replace("#COMMENT_COMPATIBILITY", "// Compatibility (auto-generated)");
        return getInputStream(str);
    }

    private static InputStream getInputStream(String str) {
        byte[] bytes = str.getBytes(Charsets.US_ASCII);
        return new ByteArrayInputStream(bytes);
    }
}
