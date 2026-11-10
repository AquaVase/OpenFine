package net.optifine.shaders;

import com.mojang.blaze3d.opengl.GlStateManager;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import net.optifine.shaders.vulkan.VulkanTransparency;
import org.lwjgl.opengl.GL43;

public class OpenGlTransparency implements AutoCloseable {
    private static OpenGlTransparency instance;
    private final ShadersFramebuffer target;
    private final VulkanTransparency.Profile profile;
    private final int width;
    private final int height;
    private final int[] indices;
    private final List<Integer> textures = new ArrayList<>();
    private final int opaqueDepth;
    private final int opaqueLogical;
    private final int metadata;
    private final Map<Integer, Map<String, Integer>> uniformLocations = new HashMap<>();
    private final int[][] colors;
    private final int[] depths;
    private final int nearestLogical;
    private final int depthArray;
    private final int[] colorArrays;
    private final int nextDepth;
    private final int lighting;
    private final int gi;
    private final int waves;
    private final int gatherProgram;
    private final int framebuffer;
    private final int vao;
    private final int[] queries;
    private int activeLayers;
    private boolean captured;
    private boolean cameraResolve;
    private int[] lateColors;
    private int lateDepth;
    private int lateLogical;
    private boolean lateResolve;
    private int rank = -1;
    private int compositeDepthSource;

    private OpenGlTransparency(ShadersFramebuffer target, VulkanTransparency.Profile profile) {
        this.target = target;
        this.profile = profile;
        this.width = target.getWidth();
        this.height = target.getHeight();
        var drawBuffers = Shaders.ProgramWater.getDrawBuffers();
        this.indices = new int[drawBuffers.limit()];
        for (int i = 0; i < this.indices.length; i++) {
            this.indices[i] = drawBuffers.get(i) - GL43.GL_COLOR_ATTACHMENT0;
        }
        this.opaqueDepth = this.create(target.getDepthTexture(-1));
        this.opaqueLogical = this.create(target.getDepthTexture(1));
        this.nextDepth = this.create(target.getDepthTexture(1));
        this.lighting = this.create(target.getColorTexture(true, profile.color()));
        this.gi = profile.first() == 2 ? this.create(target.getColorTexture(true, profile.color()), true) : 0;
        this.waves = profile.first() == 2 ? this.create(target.getColorTexture(true, 4)) : 0;
        this.metadata = profile.first() == 1 ? this.create(target.getColorTexture(true, 4)) : 0;
        int count = VulkanTransparency.layerCount();
        this.queries = new int[count];
        GL43.glGenQueries(this.queries);
        this.colors = new int[count][this.indices.length];
        this.depths = new int[count];
        this.nearestLogical = this.create(target.getDepthTexture(0));
        this.depthArray = this.createArray(this.opaqueDepth, count + 1);
        this.colorArrays = new int[this.indices.length];
        for (int c = 0; c < this.indices.length; c++) {
            this.colorArrays[c] = this.createArray(target.getColorTexture(true, this.indices[c]), count);
        }
        for (int i = 0; i < count; i++) {
            this.depths[i] = this.createArrayView(this.depthArray, i);
            for (int c = 0; c < this.indices.length; c++) {
                this.colors[i][c] = this.createArrayView(this.colorArrays[c], i);
            }
        }
        this.framebuffer = GL43.glGenFramebuffers();
        this.vao = GL43.glGenVertexArrays();
        // Array textures keep eight surfaces below the fragment sampler limit and gather all colors in one draw.
        int vertex = compile(GL43.GL_VERTEX_SHADER, "#version 430\nvoid main() { vec2 p = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2); gl_Position = vec4(p * 2.0 - 1.0, 0.0, 1.0); }");
        int fragment = compile(GL43.GL_FRAGMENT_SHADER, gatherSource(count, this.indices.length));
        this.gatherProgram = GL43.glCreateProgram();
        GL43.glAttachShader(this.gatherProgram, vertex);
        GL43.glAttachShader(this.gatherProgram, fragment);
        GL43.glLinkProgram(this.gatherProgram);
        GL43.glDeleteShader(vertex);
        GL43.glDeleteShader(fragment);
        if (GL43.glGetProgrami(this.gatherProgram, GL43.GL_LINK_STATUS) == 0) {
            throw new IllegalStateException(GL43.glGetProgramInfoLog(this.gatherProgram));
        }
    }

    static String gatherSource(int count, int colors) {
        StringBuilder source = new StringBuilder("#version 430\nuniform sampler2DArray depths;\nuniform int rank;\nuniform int mode;\nuniform int capturedLayers;\n");
        for (int c = 0; c < colors; c++) {
            source.append("uniform sampler2DArray colors").append(c).append("; layout(location=").append(c).append(") out vec4 result").append(c).append(";\n");
        }
        source.append("void main() { ivec2 p = ivec2(gl_FragCoord.xy); float base = texelFetch(depths, ivec3(p, ").append(count).append("), 0).r; int total = 0;\n");
        source.append("for (int i = 0; i < capturedLayers; i++) total += int(texelFetch(depths, ivec3(p, i), 0).r > base + 1e-7);\n");
        source.append("int selected = rank - ").append(count).append(" + total; float depth = selected < 0 ? base : texelFetch(depths, ivec3(p, selected), 0).r; float next = selected < 0 || selected + 1 >= capturedLayers ? base : texelFetch(depths, ivec3(p, selected + 1), 0).r;\n");
        source.append("if (mode == 0) {\n");
        for (int c = 0; c < colors; c++) {
            source.append("result").append(c).append(" = texelFetch(colors").append(c).append(", ivec3(p, max(0, selected)), 0);\n");
        }
        source.append("}\n");
        return source.append("gl_FragDepth = mode == 0 ? depth : mode == 1 ? 1.0 - depth : 1.0 - next; }\n").toString();
    }

    private int createArray(int source, int count) {
        GlStateManager._activeTexture(GL43.GL_TEXTURE0);
        int previous = GL43.glGetInteger(GL43.GL_TEXTURE_BINDING_2D);
        GlStateManager._bindTexture(source);
        int format = GL43.glGetTexLevelParameteri(GL43.GL_TEXTURE_2D, 0, GL43.GL_TEXTURE_INTERNAL_FORMAT);
        GlStateManager._bindTexture(previous);
        int array = GlStateManager._genTexture();
        int previousArray = GL43.glGetInteger(GL43.GL_TEXTURE_BINDING_2D_ARRAY);
        GL43.glBindTexture(GL43.GL_TEXTURE_2D_ARRAY, array);
        GL43.glTexStorage3D(GL43.GL_TEXTURE_2D_ARRAY, 1, format, this.width, this.height, count);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D_ARRAY, GL43.GL_TEXTURE_MIN_FILTER, GL43.GL_NEAREST);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D_ARRAY, GL43.GL_TEXTURE_MAG_FILTER, GL43.GL_NEAREST);
        GL43.glBindTexture(GL43.GL_TEXTURE_2D_ARRAY, previousArray);
        this.textures.add(array);
        return array;
    }

    private void copyArray(int source, int destination, int layer) {
        GL43.glCopyImageSubData(source, GL43.GL_TEXTURE_2D, 0, 0, 0, 0, destination, GL43.GL_TEXTURE_2D_ARRAY, 0, 0, 0, layer, this.width, this.height, 1);
    }

    private int createArrayView(int array, int layer) {
        GlStateManager._activeTexture(GL43.GL_TEXTURE0);
        int previous = GL43.glGetInteger(GL43.GL_TEXTURE_BINDING_2D_ARRAY);
        GL43.glBindTexture(GL43.GL_TEXTURE_2D_ARRAY, array);
        int format = GL43.glGetTexLevelParameteri(GL43.GL_TEXTURE_2D_ARRAY, 0, GL43.GL_TEXTURE_INTERNAL_FORMAT);
        GL43.glBindTexture(GL43.GL_TEXTURE_2D_ARRAY, previous);
        int texture = GlStateManager._genTexture();
        GL43.glTextureView(texture, GL43.GL_TEXTURE_2D, array, format, 0, 1, layer, 1);
        int previousTexture = GL43.glGetInteger(GL43.GL_TEXTURE_BINDING_2D);
        GlStateManager._bindTexture(texture);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MIN_FILTER, GL43.GL_NEAREST);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MAG_FILTER, GL43.GL_NEAREST);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_WRAP_S, GL43.GL_CLAMP_TO_EDGE);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_WRAP_T, GL43.GL_CLAMP_TO_EDGE);
        GlStateManager._bindTexture(previousTexture);
        this.textures.add(texture);
        return texture;
    }

    private static int compile(int type, String source) {
        int shader = GL43.glCreateShader(type);
        GL43.glShaderSource(shader, source);
        GL43.glCompileShader(shader);
        if (GL43.glGetShaderi(shader, GL43.GL_COMPILE_STATUS) == 0) {
            throw new IllegalStateException(GL43.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    private int create(int source) {
        return this.create(source, false);
    }

    private int create(int source, boolean mipmaps) {
        GlStateManager._activeTexture(GL43.GL_TEXTURE0);
        int previous = GL43.glGetInteger(GL43.GL_TEXTURE_BINDING_2D);
        GlStateManager._bindTexture(source);
        int format = GL43.glGetTexLevelParameteri(GL43.GL_TEXTURE_2D, 0, GL43.GL_TEXTURE_INTERNAL_FORMAT);
        int texture = GlStateManager._genTexture();
        GlStateManager._bindTexture(texture);
        GL43.glTexStorage2D(GL43.GL_TEXTURE_2D, mipmaps ? 1 + (int)(Math.log(Math.max(this.width, this.height)) / Math.log(2)) : 1, format, this.width, this.height);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MIN_FILTER, GL43.GL_NEAREST);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_MAG_FILTER, GL43.GL_NEAREST);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_WRAP_S, GL43.GL_CLAMP_TO_EDGE);
        GL43.glTexParameteri(GL43.GL_TEXTURE_2D, GL43.GL_TEXTURE_WRAP_T, GL43.GL_CLAMP_TO_EDGE);
        GlStateManager._bindTexture(previous);
        this.textures.add(texture);
        return texture;
    }

    private void copy(int source, int destination) {
        if (source != destination) {
            GL43.glCopyImageSubData(source, GL43.GL_TEXTURE_2D, 0, 0, 0, 0, destination, GL43.GL_TEXTURE_2D, 0, 0, 0, 0, this.width, this.height, 1);
        }
    }

    private int uniformLocation(int program, String name) {
        return this.uniformLocations.computeIfAbsent(program, key -> new HashMap<>())
            .computeIfAbsent(name, key -> GL43.glGetUniformLocation(program, key));
    }

    private void uniform(int program, String name, int value) {
        GL43.glProgramUniform1i(program, this.uniformLocation(program, name), value);
    }

    private void bind(int program, String name, int unit, int texture) {
        this.uniform(program, name, unit);
        GlStateManager._activeTexture(GL43.GL_TEXTURE0 + unit);
        GlStateManager._bindTexture(texture);
        GL43.glBindSampler(unit, 0);
        GlStateManager._activeTexture(GL43.GL_TEXTURE0);
    }

    public static void render(ShadersFramebuffer target, VulkanTransparency.Profile profile, Runnable geometry) {
        if (instance == null || instance.target != target) {
            reset();
            instance = new OpenGlTransparency(target, profile);
        }
        instance.capture(geometry);
    }

    private void capture(Runnable geometry) {
        int nextPoll = Math.clamp(this.activeLayers + 1, 2, this.depths.length);
        this.copy(this.target.getDepthTexture(-1), this.opaqueDepth);
        this.copy(this.target.getDepthTexture(1), this.opaqueLogical);
        int program = Shaders.ProgramWater.getId();
        try {
            for (int i = 0; i < this.depths.length; i++) {
                if (i > 0) {
                    this.copy(this.opaqueDepth, this.target.getDepthTexture(-1));
                }
                bind(program, "ofPeelPrevious", 28, this.depths[Math.max(0, i - 1)]);
                uniform(program, "ofPeelLayer", i);
                GL43.glBeginQuery(GL43.GL_ANY_SAMPLES_PASSED, this.queries[i]);
                try {
                    geometry.run();
                } finally {
                    GL43.glEndQuery(GL43.GL_ANY_SAMPLES_PASSED);
                }
                if (i + 1 == nextPoll) {
                    this.activeLayers = 0;
                    for (int q = 0; q <= i; q++) {
                        if (GL43.glGetQueryObjecti(this.queries[q], GL43.GL_QUERY_RESULT) != 0) {
                            this.activeLayers = q + 1;
                        }
                    }
                    if (this.activeLayers <= i) {
                        break;
                    }
                    nextPoll = Math.min(nextPoll + 2, this.depths.length);
                }
                this.copy(this.target.getDepthTexture(-1), this.depths[i]);
                for (int c = 0; c < this.indices.length; c++) {
                    this.copy(this.target.getColorTexture(true, this.indices[c]), this.colors[i][c]);
                }
                if (i == 0) {
                    Shaders.refreshTransparencyDepth(this.target);
                    this.copy(this.target.getDepthTexture(0), this.nearestLogical);
                }
                GlStateManager._enableDepthTest();
                GlStateManager._depthFunc(GL43.GL_GEQUAL);
            }
            this.load(0);
            this.target.bindFramebuffer();
            this.target.setColorTextures(true);
            this.target.setDepthTexture();
            this.captured = true;
        } finally {
            uniform(program, "ofPeelLayer", 0);
        }
    }

    private void load(int index) {
        this.copy(this.depths[index], this.target.getDepthTexture(-1));
        this.copy(this.nearestLogical, this.target.getDepthTexture(0));
        for (int c = 0; c < this.indices.length; c++) {
            this.copy(this.colors[index][c], this.target.getColorTexture(true, this.indices[c]));
        }
    }

    private void loadCamera(boolean lateHand) {
        if (!lateHand) {
            this.load(0);
            return;
        }
        this.copy(this.lateDepth, this.target.getDepthTexture(-1));
        this.copy(this.lateLogical, this.target.getDepthTexture(0));
        for (int c = 0; c < this.indices.length; c++) {
            this.copy(this.lateColors[c], this.target.getColorTexture(true, this.indices[c]));
        }
    }

    public static boolean isResolving() {
        return instance != null && instance.rank >= 0;
    }

    public static boolean shouldResolve(Program[] programs, int index) {
        return instance != null && instance.captured && programs == Shaders.ProgramsComposite && index == instance.profile.first();
    }

    private void gather(int rank) {
        int[] previousArrays = new int[this.indices.length + 1];
        int[] previousSamplers = new int[previousArrays.length];
        int previousProgram = GL43.glGetInteger(GL43.GL_CURRENT_PROGRAM);
        int previousVao = GL43.glGetInteger(GL43.GL_VERTEX_ARRAY_BINDING);
        GL43.glUseProgram(this.gatherProgram);
        GL43.glBindVertexArray(this.vao);
        GlStateManager._glBindFramebuffer(GL43.GL_FRAMEBUFFER, this.framebuffer);
        GL43.glViewport(0, 0, this.width, this.height);
        GlStateManager._disableBlend();
        GlStateManager._enableDepthTest();
        GlStateManager._depthFunc(GL43.GL_ALWAYS);
        GlStateManager._depthMask(true);
        for (int unit = 0; unit < previousArrays.length; unit++) {
            GlStateManager._activeTexture(GL43.GL_TEXTURE0 + unit);
            previousArrays[unit] = GL43.glGetInteger(GL43.GL_TEXTURE_BINDING_2D_ARRAY);
            previousSamplers[unit] = GL43.glGetIntegeri(GL43.GL_SAMPLER_BINDING, unit);
            GL43.glBindTexture(GL43.GL_TEXTURE_2D_ARRAY, unit == 0 ? this.depthArray : this.colorArrays[unit - 1]);
            GL43.glBindSampler(unit, 0);
            uniform(this.gatherProgram, unit == 0 ? "depths" : "colors" + (unit - 1), unit);
        }
        uniform(this.gatherProgram, "rank", rank);
        uniform(this.gatherProgram, "capturedLayers", this.activeLayers);
        int[] drawBuffers = new int[this.indices.length];
        for (int c = 0; c < this.indices.length; c++) {
            drawBuffers[c] = GL43.GL_COLOR_ATTACHMENT0 + c;
            GL43.glFramebufferTexture2D(GL43.GL_FRAMEBUFFER, drawBuffers[c], GL43.GL_TEXTURE_2D, this.target.getColorTexture(true, this.indices[c]), 0);
        }
        GL43.glFramebufferTexture2D(GL43.GL_FRAMEBUFFER, GL43.GL_DEPTH_ATTACHMENT, GL43.GL_TEXTURE_2D, this.target.getDepthTexture(-1), 0);
        GL43.glDrawBuffers(drawBuffers);
        uniform(this.gatherProgram, "mode", 0);
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, 3);
        for (int attachment : drawBuffers) {
            GL43.glFramebufferTexture2D(GL43.GL_FRAMEBUFFER, attachment, GL43.GL_TEXTURE_2D, 0, 0);
        }
        GL43.glDrawBuffer(GL43.GL_NONE);
        for (int mode = 1; mode <= 2; mode++) {
            GL43.glFramebufferTexture2D(GL43.GL_FRAMEBUFFER, GL43.GL_DEPTH_ATTACHMENT, GL43.GL_TEXTURE_2D, mode == 1 ? this.target.getDepthTexture(0) : this.nextDepth, 0);
            uniform(this.gatherProgram, "mode", mode);
            GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, 3);
        }
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);
        GL43.glBindVertexArray(previousVao);
        GL43.glUseProgram(previousProgram);
        for (int unit = 0; unit < previousArrays.length; unit++) {
            GlStateManager._activeTexture(GL43.GL_TEXTURE0 + unit);
            GL43.glBindTexture(GL43.GL_TEXTURE_2D_ARRAY, previousArrays[unit]);
            GL43.glBindSampler(unit, previousSamplers[unit]);
        }
        GlStateManager._activeTexture(GL43.GL_TEXTURE0);
        this.target.bindFramebuffer();
        this.target.bindColorTextures(0);
        this.target.setColorTextures(false);
        this.target.unbindDepthTexture();
        this.target.setDrawBuffers();
    }

    public static void resolve(Runnable programs, Runnable camera) {
        var current = instance;
        current.compositeDepthSource = 0;
        int[] history = current.profile.first() == 1 ? new int[]{
            current.target.getColorTexture(true, 3), current.target.getColorTexture(true, 7)
        } : null;
        try {
            boolean lateHand = VulkanTransparency.hasLateHand();
            if (lateHand) {
                if (current.lateColors == null) {
                    current.lateColors = new int[current.indices.length];
                    for (int c = 0; c < current.indices.length; c++) {
                        current.lateColors[c] = current.create(current.target.getColorTexture(true, current.indices[c]));
                    }
                    current.lateDepth = current.create(current.opaqueDepth);
                    current.lateLogical = current.create(current.opaqueLogical);
                }
                current.copy(current.target.getDepthTexture(-1), current.lateDepth);
                current.copy(current.target.getDepthTexture(0), current.lateLogical);
                for (int c = 0; c < current.indices.length; c++) {
                    current.copy(current.target.getColorTexture(true, current.indices[c]), current.lateColors[c]);
                }
            }
            current.copyArray(current.opaqueDepth, current.depthArray, current.depths.length);
            if (current.metadata != 0) {
                current.copy(current.target.getColorTexture(true, 4), current.metadata);
            }
            for (int i = current.depths.length - 1; i >= current.depths.length - current.activeLayers; i--) {
                current.rank = i;
                current.gather(i);
                programs.run();
                current.restoreHistory(history);
            }
            if (lateHand) {
                current.lateResolve = true;
                current.rank = 0;
                current.loadCamera(true);
                programs.run();
                current.restoreHistory(history);
                current.lateResolve = false;
            }
            current.copy(current.target.getColorTexture(true, current.profile.color()), current.lighting);
            current.loadCamera(lateHand);
            current.cameraResolve = true;
            current.rank = 0;
            if (current.profile.first() == 1) {
                programs.run();
            } else {
                camera.run();
            }
        } finally {
            current.rank = -1;
            current.captured = false;
            current.cameraResolve = false;
            current.lateResolve = false;
            current.setCompositeDepth(current.opaqueLogical);
        }
    }

    private void restoreHistory(int[] history) {
        if (history != null) {
            for (int i = 0; i < history.length; i++) {
                int color = i == 0 ? 3 : 7;
                if (this.target.getColorTexture(true, color) != history[i]) {
                    this.target.flipColorTexture(color);
                }
            }
        }
    }

    private void setCompositeDepth(int source) {
        if (this.compositeDepthSource != source) {
            this.copy(source, this.target.getDepthTexture(1));
            this.compositeDepthSource = source;
        }
    }

    public static void beforeComposite(Program program) {
        var current = instance;
        if (current == null) {
            return;
        }
        int id = program.getId();
        if (current.uniformLocation(id, "ofLayerResolve") < 0) {
            return;
        }
        if (current.rank < 0) {
            if (program == Shaders.ProgramsComposite[2] && current.gi != 0) {
                current.copy(current.target.getColorTexture(true, current.profile.color()), current.gi);
                current.bind(id, "ofLayerGi", 27, current.gi);
                GlStateManager._activeTexture(GL43.GL_TEXTURE0 + 27);
                GL43.glGenerateMipmap(GL43.GL_TEXTURE_2D);
                GlStateManager._activeTexture(GL43.GL_TEXTURE0);
            }
            if (current.waves != 0) {
                current.copy(current.target.getColorTexture(true, 4), current.waves);
            }
        } else {
            if (current.cameraResolve && program == Shaders.ProgramsComposite[current.profile.last()]) {
                current.copy(current.lighting, current.target.getColorTexture(true, current.profile.color()));
            }
            boolean opaqueDepth = current.cameraResolve || program == Shaders.ProgramsComposite[2]
                || current.profile.first() == 1 && program != Shaders.ProgramsComposite[1];
            if (!opaqueDepth && !current.lateResolve) {
                current.compositeDepthSource = 0;
            }
            current.setCompositeDepth(opaqueDepth ? current.opaqueLogical : current.lateResolve ? current.nearestLogical : current.nextDepth);
            var buffers = program.getDrawBuffers();
            for (int i = 0; i < buffers.limit(); i++) {
                int c = buffers.get(i) - GL43.GL_COLOR_ATTACHMENT0;
                if (c >= 0 && current.profile.first() != 1) {
                    current.copy(current.target.getColorTexture(true, c), current.target.getColorTexture(false, c));
                }
            }
        }
        if (current.waves != 0) {
            current.bind(id, "ofWaterWaves", 26, current.waves);
        }
        if (current.gi != 0) {
            current.bind(id, "ofLayerGi", 27, current.gi);
        }
        if (current.metadata != 0) {
            current.bind(id, "ofLayerMetadata", 25, current.metadata);
        }
        current.bind(id, "ofLayerDepth", 29, current.target.getDepthTexture(-1));
        current.bind(id, "ofOpaqueDepth", 30, current.lateResolve ? current.depths[0] : current.opaqueDepth);
        current.uniform(id, "ofLayerResolve", current.rank < 0 || current.cameraResolve ? 0 : 1);
        current.uniform(id, "ofLayerIntermediate", current.rank < 0 || current.cameraResolve ? 0 : 1);
        current.uniform(id, "ofLayerCameraOnly", current.cameraResolve ? 1 : 0);
    }

    public static void reset() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    @Override
    public void close() {
        GL43.glDeleteProgram(this.gatherProgram);
        GL43.glDeleteFramebuffers(this.framebuffer);
        GL43.glDeleteVertexArrays(this.vao);
        GL43.glDeleteQueries(this.queries);
        this.textures.forEach(GlStateManager::_deleteTexture);
    }
}
