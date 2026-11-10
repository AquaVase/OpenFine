package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderPassDescriptor;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vulkan.VulkanGpuSampler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import net.optifine.Config;
import net.optifine.shaders.IShaderPack;
import net.optifine.shaders.Program;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersFramebuffer;
import net.optifine.shaders.config.ShaderType;

public class VulkanTransparency implements AutoCloseable {
    private static final Pattern MAIN = Pattern.compile("\\bvoid\\s+main\\s*\\(\\s*(?:void\\s*)?\\)");
    private static final int PREVIOUS_DEPTH_UNIT = 60;
    private static final int LAYER_DEPTH_UNIT = 61;
    private static final int OPAQUE_DEPTH_UNIT = 62;
    private static final int GATHER_TEXTURE_UNIT = 80;
    private static volatile Profile profile;
    private static VulkanTransparency instance;
    private final VulkanFramebuffer framebuffer;
    private final int[] colorIndices;
    private final GpuTexture[] opaqueColors;
    private final GpuTexture opaqueDepth;
    private final GpuTextureView opaqueDepthView;
    private final GpuTexture opaqueSampledDepth;
    private final Layer[] layers;
    private final GpuTexture lightingInput;
    private final GpuTextureView lightingInputView;
    private final GpuTexture waterWaves;
    private final GpuTextureView waterWavesView;
    private final GpuTexture[][] compositeInputs;
    private final List<GpuTexture> textures = new ArrayList<>();
    private final List<GpuTextureView> views = new ArrayList<>();
    private final GpuTexture resolvedLighting;
    private final GpuTexture nextDepth;
    private final GpuTextureView activeDepthView;
    private final int gatherProgram;
    private final VulkanTransparencyQueries queries = new VulkanTransparencyQueries();
    private int activeLayers;
    private final Map<GpuTexture, GpuTextureView> sampledViews = new IdentityHashMap<>();
    private boolean cameraResolve;
    private boolean captured;
    private Layer lateLayer;
    private boolean lateResolve;
    private int resolvingLayer = -1;

    public static int layerCount() {
        return Math.clamp(Integer.getInteger("optifine.transparencyLayers",
            Integer.getInteger("optifine.vulkan.transparencyLayers", 8)), 2, 8);
    }

    public static boolean hasLateHand() {
        return Config.getMinecraft().options.getCameraType().isFirstPerson()
            && (Shaders.isItemToRenderMainTranslucent() && Shaders.isHandRenderedMain()
                || Shaders.isItemToRenderOffTranslucent() && Shaders.isHandRenderedOff());
    }

    public static void configure(IShaderPack pack) {
        reset();
        profile = null;
        if (!Shaders.configLayeredTransparency || Boolean.getBoolean("optifine.vulkan.disableLayeredTransparency")) {
            return;
        }
        // Match the resolve implementation, so renamed archives still receive the same compatibility path.
        if (read(pack, "/shaders/programs/composite/Composite1.frag").contains("REFRACTION_STRENGTH")
            && read(pack, "/shaders/programs/gbuffers/Water.frag").contains("packUpGbufferDataSolid")) {
            profile = new Profile(1, 5, 5);
        } else if (read(pack, "/shaders/composite3.fsh").contains("GetGBufferDataTransparent()")
            && read(pack, "/shaders/gbuffers_water.fsh").contains("OutputGBufferDataTransparent")) {
            profile = new Profile(2, 3, 5);
        } else if (read(pack, "/shaders/composite3.fsh").contains("GetGbufferData()")
            && read(pack, "/shaders/composite3.fsh").contains("gaux3MipmapEnabled")
            && read(pack, "/shaders/gbuffers_water.fsh").contains("isStainedGlass")) {
            profile = new Profile(2, 3, 6);
        }
    }

    private static String read(IShaderPack pack, String path) {
        try (var stream = pack.getResourceAsStream(path)) {
            return stream == null ? "" : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    public static String adapt(String name, ShaderType type, String source) {
        Profile current = profile;
        if (current == null) {
            return source;
        }
        if (current.first() == 2 && type == ShaderType.VERTEX && name.equals("gbuffers_water")) {
            return MAIN.matcher(source).replaceFirst("void ofTransparencyVertex()")
                + "\nvoid main() { ofTransparencyVertex(); if (mc_Entity.x == 20.0 || mc_Entity.x == 102.0) isStainedGlass = 2.0; }\n";
        }
        if (type != ShaderType.FRAGMENT) {
            return source;
        }
        boolean water = name.equals("gbuffers_water");
        int composite = name.equals("composite") ? 0 : name.startsWith("composite") ? Config.parseInt(name.substring(9), -1) : -1;
        boolean resolve = composite >= current.first() && composite <= current.last();
        if (!water && !resolve) {
            return source;
        }
        var main = MAIN.matcher(source);
        if (!main.find()) {
            throw new IllegalArgumentException("Missing transparency shader entry point: " + name);
        }
        source = main.replaceFirst("void ofTransparencyMain()");
        if (water) {
            if (current.first() == 2) {
                // Plain glass needs the glass material and normal, while retaining its opaque border texels.
                source = source.replace("isice + isStainedGlass", "min(isice + isStainedGlass, 1.0)")
                    .replaceAll("iswater > 0.5 \\|\\| isice > 0.5 \\|\\| isStainedGlass > 0.5(\\s*\\)\\s*\\{\\s*tex\\s*=)",
                        "iswater > 0.5 || isice > 0.5 || (isStainedGlass > 0.5 && isStainedGlass < 1.5)$1");
            }
            return source + "\nuniform sampler2D ofPeelPrevious;\nuniform int ofPeelLayer;\n"
                + "void main() {\n"
                + "  if (ofPeelLayer > 0 && gl_FragCoord.z >= texelFetch(ofPeelPrevious, ivec2(gl_FragCoord.xy), 0).r - 1e-7) discard;\n"
                + "  ofTransparencyMain();\n}\n";
        }
        // Intermediate SEUS layers shade/refraction/reflect without applying camera fog repeatedly.
        if (current.first() == 2) {
            if (composite == 2) {
                source = source.replaceAll(current.color() == 5 ? "\\bgaux2\\b" : "\\bgaux3\\b", "ofLayerGi");
            }
            source = source.replaceAll("(uniform\\s+sampler2D\\s+gaux1\\s*;)", "$1\nuniform sampler2D ofWaterWaves;")
                .replaceAll("(texture(?:2D)?Lod\\s*\\()gaux1(,\\s*coord\\s*,\\s*0\\s*\\)\\.zw)", "$1ofWaterWaves$2");
            if (composite == 3 && current.color() == 5) {
                source = source.replace("vec4 transparentAlbedo = texture(gaux2, texcoord.st);", "vec4 transparentAlbedo = GetGBufferDataTransparent().albedo;");
            }
            source = source.replace("color *= transparentAlbedo.rgb * 2.0;", "color *= mix(vec3(1.0), transparentAlbedo.rgb, transparentAlbedo.a);");
            if (composite == 3) {
                source = source.replaceAll("(?m)^(\\s*)(WaterRefraction|TransparentAbsorption|CalculateSpecularReflections)(\\s*\\()", "$1if (ofLayerCameraOnly == 0) $2$3");
                source = source.replace("color *= 1.0 - gbuffer.rainMask * 0.2;", "if (ofLayerCameraOnly != 0) color *= 1.0 - gbuffer.rainMask * 0.2;")
                    .replace("color += gbuffer.rainMask * colorSkylight * 0.025;", "if (ofLayerCameraOnly != 0) color += gbuffer.rainMask * colorSkylight * 0.025;");
            }
            source = source.replace("LandAtmosphericScattering(color,", "if (ofLayerIntermediate == 0) LandAtmosphericScattering(color,")
                .replace("RainFog(color,", "if (ofLayerIntermediate == 0) RainFog(color,")
                .replace("CrepuscularRays(color,", "if (ofLayerIntermediate == 0) CrepuscularRays(color,")
                .replace("BlindnessFog(color,", "if (ofLayerIntermediate == 0) BlindnessFog(color,");
            // The declaration must precede the existing main function using it.
            source = source.replace("void ofTransparencyMain()", "uniform int ofLayerIntermediate;\nuniform int ofLayerCameraOnly;\nvoid ofTransparencyMain()");
        } else {
            if (composite == 1) {
                source = source.replace("texBuffer0 = vec4(0.0, 0.0, texelFetch(colortex4, texel, 0).zw);",
                    "texBuffer0 = vec4(0.0, 0.0, texelFetch(colortex4, texel, 0).zw);\n"
                        + "if (ofLayerCameraOnly != 0) { if (waterDepth < solidDepth) texBuffer0.b = gbufferData.materialID / 255.0; texBuffer5 = solidColor; return; }");
            } else if (composite == 2) {
                source = source.replace("vec4 diffuseData = texelFetch(colortex5, texel, 0);",
                    "vec4 diffuseData = texelFetch(colortex5, texel, 0);\n"
                        + "if (ofLayerCameraOnly != 0) { texBuffer1 = vec4(0.0); texBuffer2 = vec4(0.0, 0.0, 0.0, gbufferData.parallaxOffset); texBuffer4 = vec4(0.0); texBuffer5 = diffuseData; return; }");
            } else if (composite == 3 || composite == 4) {
                source = source.replace("void ofTransparencyMain() {", "void ofTransparencyMain() { if (ofLayerCameraOnly != 0) { texBuffer4 = vec4(0.0); return; }");
                if (composite == 4) {
                    source = source.replaceAll("(\\belse\\s*\\{\\s*)discard;", "$1texBuffer4 = texelFetch(colortex4, ivec2(gl_FragCoord.xy), 0);");
                }
            }
            if (composite != 5) {
                source = source.replace("void ofTransparencyMain()", "uniform int ofLayerCameraOnly;\nvoid ofTransparencyMain()");
            }
        }
        if (current.first() == 1 && composite == 5) {
            source = source.replace("if (waterDepth < texelFetch(depthtex1, texel, 0).r)", "if (ofLayerCameraOnly == 0 && waterDepth < texelFetch(depthtex1, texel, 0).r)");
            source = source.replace("vec3 absorption = vec3(1.0);", "if (ofLayerIntermediate != 0) { texBuffer5 = solidColor; return; }\nvec3 absorption = vec3(1.0);");
            source = source.replace("void ofTransparencyMain()", "uniform int ofLayerIntermediate;\nuniform int ofLayerCameraOnly;\nvoid ofTransparencyMain()");
        }
        String waterMask = current.first() == 2 && composite == 2
            ? current.color() == 5
                ? "  if (ofLayerResolve != 0 && CalculateMasks(GetGBufferDataTransparent().materialID).water < 0.5 && !(CalculateMasks(GetGBufferDataTransparent().materialID).stainedGlass > 0.5 && GetGBufferDataTransparent().albedo.a >= 0.9)) discard;\n"
                : "  if (ofLayerResolve != 0 && CalculateMasks(GetGbufferData().materialID).water < 0.5 && !(CalculateMasks(GetGbufferData().materialID).stainedGlass > 0.5 && texture(gaux2, texcoord.st).a >= 0.9)) discard;\n"
            : "";
        String emptyLayer = "discard;";
        if (current.first() == 1) {
            int[] outputs = switch (composite) {
                case 1 -> new int[]{0, 5};
                case 2 -> new int[]{1, 2, 4, 5};
                case 3, 4 -> new int[]{4};
                case 5 -> new int[]{5};
                default -> throw new IllegalArgumentException("Unexpected transparency composite: " + composite);
            };
            StringBuilder preserve = new StringBuilder();
            for (int output : outputs) {
                preserve.append("texBuffer").append(output).append(" = texelFetch(colortex").append(output).append(", pixel, 0); ");
            }
            emptyLayer = preserve.append("return;").toString();
        }
        return source + "\nuniform sampler2D ofLayerDepth;\nuniform sampler2D ofOpaqueDepth;\nuniform int ofLayerResolve;\n"
            + "void main() {\n"
            + "  ivec2 pixel = ivec2(gl_FragCoord.xy);\n"
            + "  if (ofLayerResolve != 0 && texelFetch(ofLayerDepth, pixel, 0).r <= texelFetch(ofOpaqueDepth, pixel, 0).r + 1e-7) { " + emptyLayer + " }\n"
            + waterMask
            + "  ofTransparencyMain();\n"
            + "}\n";
    }

    public static void render(ShadersFramebuffer target, Runnable geometry) {
        if (profile == null || Shaders.isShadowPass) {
            geometry.run();
            return;
        }
        if (!Config.isVulkan()) {
            net.optifine.shaders.OpenGlTransparency.render(target, profile, geometry);
            return;
        }
        VulkanFramebuffer framebuffer = target.getVulkanFramebuffer();
        if (instance == null || instance.framebuffer != framebuffer) {
            reset();
            instance = new VulkanTransparency(framebuffer);
        }
        instance.capture(target, geometry);
    }

    private VulkanTransparency(VulkanFramebuffer framebuffer) {
        this.framebuffer = framebuffer;
        var buffers = Shaders.ProgramWater.getDrawBuffers();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < buffers.limit(); i++) {
            int index = buffers.get(i) - 36064;
            if (index >= 0 && !indices.contains(index)) {
                indices.add(index);
            }
        }
        this.colorIndices = indices.stream().mapToInt(Integer::intValue).toArray();
        this.opaqueColors = new GpuTexture[this.colorIndices.length];
        this.opaqueDepth = this.create(framebuffer.getDepthTarget().texture(), "opaque depth");
        this.opaqueDepthView = this.view(this.opaqueDepth);
        this.opaqueSampledDepth = this.create(framebuffer.getDepth(1).texture(), "opaque sampled depth");
        for (int i = 0; i < this.colorIndices.length; i++) {
            this.opaqueColors[i] = framebuffer.getColor(true, this.colorIndices[i]).texture();
        }
        if (profile.first() == 2) {
            var source = framebuffer.getColor(true, profile.color()).texture();
            this.lightingInput = RenderSystem.getDevice().createTexture(() -> "OptiFine transparency GI input", 15,
                source.getFormat(), source.getWidth(0), source.getHeight(0), 1, source.getMipLevels());
            this.textures.add(this.lightingInput);
            this.lightingInputView = this.view(this.lightingInput);
        } else {
            this.lightingInput = null;
            this.lightingInputView = null;
        }
        this.waterWaves = profile.first() == 2 ? this.create(framebuffer.getColor(true, 4).texture(), "filtered water normals") : null;
        this.waterWavesView = this.waterWaves == null ? null : this.view(this.waterWaves);
        this.compositeInputs = new GpuTexture[2][framebuffer.getColorCount()];
        for (int side = 0; side < 2; side++) {
            for (int c = 0; c < framebuffer.getColorCount(); c++) {
                if (profile.first() == 1 && side == 0 && c == 4) {
                    this.compositeInputs[side][c] = this.create(framebuffer.getColor(side == 0, c).texture(), "composite input " + side + "/" + c);
                }
            }
        }
        this.resolvedLighting = this.create(framebuffer.getColor(true, profile.color()).texture(), "resolved lighting");
        this.nextDepth = this.create(this.opaqueSampledDepth, "next surface depth");
        this.activeDepthView = this.view(framebuffer.getDepthTarget().texture());
        int count = layerCount();
        this.layers = new Layer[count];
        for (int i = 0; i < count; i++) {
            GpuTexture[] colors = new GpuTexture[this.colorIndices.length];
            for (int c = 0; c < colors.length; c++) {
                colors[c] = this.create(this.opaqueColors[c], "layer " + i + " color " + c);
            }
            GpuTexture depth = this.create(this.opaqueDepth, "layer " + i + " depth");
            this.layers[i] = new Layer(colors, depth, this.view(depth), i == 0 ? this.create(this.opaqueSampledDepth, "nearest sampled depth") : null);
        }
        this.gatherProgram = this.createGatherProgram();
    }

    private int createGatherProgram() {
        var runtime = VulkanShaderRuntime.getInstance();
        int vertex = runtime.createShader(ShaderType.VERTEX, "transparency_gather.vsh");
        int fragment = runtime.createShader(ShaderType.FRAGMENT, "transparency_gather.fsh");
        try {
            runtime.setShaderSource(vertex, """
                #version 450
                void main() {
                    vec2 corners[4] = vec2[4](vec2(0, 0), vec2(1, 0), vec2(1, 1), vec2(0, 1));
                    gl_Position = vec4(corners[gl_VertexID] * 2.0 - 1.0, 0.0, 1.0);
                }
                """);
            runtime.setShaderSource(fragment, gatherSource(this.layers.length, this.colorIndices.length));
            return runtime.linkProgram("transparency_gather", vertex, fragment);
        } finally {
            runtime.deleteShader(vertex);
            runtime.deleteShader(fragment);
        }
    }

    public static String gatherSource(int layers, int colors) {
        StringBuilder source = new StringBuilder("#version 450\nuniform sampler2D opaque;\nuniform int rank;\nuniform int mode;\nuniform int capturedLayers;\n");
        for (int i = 0; i < layers; i++) {
            source.append("uniform sampler2D depth").append(i).append(";\n");
            for (int c = 0; c < colors; c++) {
                source.append("uniform sampler2D color").append(i).append('_').append(c).append(";\n");
            }
        }
        for (int c = 0; c < colors; c++) {
            source.append("layout(location=").append(c).append(") out vec4 result").append(c).append(";\n");
        }
        source.append("void main() { ivec2 p = ivec2(gl_FragCoord.xy); float base = texelFetch(opaque, p, 0).r; int count = 0;\n");
        for (int i = 0; i < layers; i++) {
            source.append("float d").append(i).append(" = ").append(i).append(" < capturedLayers ? texelFetch(depth").append(i).append(", p, 0).r : base; count += int(d").append(i).append(" > base + 1e-7);\n");
        }
        source.append("int selected = rank - ").append(layers).append(" + count; float depth = base; float next = base;\n");
        for (int i = 0; i < layers; i++) {
            source.append("if (selected == ").append(i).append(") { depth = d").append(i).append(";\n");
            if (i + 1 < layers) {
                source.append("next = d").append(i + 1).append(";\n");
            }
            source.append("}\n");
            // Empty ranks retain nearest-surface metadata for the final full-screen camera fog.
            source.append("if (mode == 0 && max(selected, 0) == ").append(i).append(") {\n");
            for (int c = 0; c < colors; c++) {
                source.append("result").append(c).append(" = texelFetch(color").append(i).append('_').append(c).append(", p, 0);\n");
            }
            source.append("}\n");
        }
        source.append("gl_FragDepth = mode == 0 ? depth : mode == 1 ? 1.0 - depth : 1.0 - next; }\n");
        return source.toString();
    }

    private void gather(int rank) {
        var runtime = VulkanShaderRuntime.getInstance();
        var uniforms = runtime.getProgram(this.gatherProgram).uniforms();
        bind(uniforms, "opaque", GATHER_TEXTURE_UNIT, this.opaqueDepthView);
        int unit = GATHER_TEXTURE_UNIT + 1;
        for (int i = 0; i < this.layers.length; i++) {
            bind(uniforms, "depth" + i, unit++, this.layers[i].view());
            for (int c = 0; c < this.colorIndices.length; c++) {
                bind(uniforms, "color" + i + "_" + c, unit++, this.viewFor(this.layers[i].colors()[c]));
            }
        }
        uniforms.setInt("rank", rank);
        uniforms.setInt("capturedLayers", this.activeLayers);
        uniforms.setInt("mode", 0);
        var area = new RenderPass.RenderArea(0, 0, this.opaqueDepth.getWidth(0), this.opaqueDepth.getHeight(0));
        var target = RenderPassDescriptor.create(() -> "OptiFine transparent surface gather").withRenderArea(area);
        for (int color : this.colorIndices) {
            target.withColorAttachment(this.framebuffer.getColor(true, color).attachment());
        }
        target.withDepthAttachment(this.framebuffer.getDepthTarget().attachment());
        runtime.drawComposite(this.gatherProgram, target, true);
        uniforms.setInt("mode", 1);
        runtime.drawComposite(this.gatherProgram, RenderPassDescriptor.create(() -> "OptiFine gathered logical depth")
            .withDepthAttachment(this.framebuffer.getDepth(0).attachment()).withRenderArea(area), true);
        uniforms.setInt("mode", 2);
        runtime.drawComposite(this.gatherProgram, RenderPassDescriptor.create(() -> "OptiFine next transparent depth")
            .withDepthAttachment(this.viewFor(this.nextDepth)).withRenderArea(area), true);
    }

    private GpuTextureView viewFor(GpuTexture texture) {
        return this.sampledViews.computeIfAbsent(texture, this::view);
    }

    private GpuTexture create(GpuTexture source, String label) {
        GpuTexture texture = RenderSystem.getDevice().createTexture(() -> "OptiFine transparency " + label, 15,
            source.getFormat(), source.getWidth(0), source.getHeight(0), 1, 1);
        this.textures.add(texture);
        return texture;
    }

    private GpuTextureView view(GpuTexture texture) {
        GpuTextureView view = RenderSystem.getDevice().createTextureView(texture);
        this.views.add(view);
        return view;
    }

    private static void copy(GpuTexture source, GpuTexture destination) {
        if (source != destination) {
            RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(source, destination, 0, 0, 0, 0, 0,
                source.getWidth(0), source.getHeight(0));
        }
    }

    private static void bind(VulkanProgramUniforms uniforms, String name, int unit, GpuTextureView texture) {
        uniforms.setInt(name, unit);
        var sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        VulkanShaderRuntime.getInstance().bindTexture(unit, texture, ((VulkanGpuSampler)sampler).vkSampler());
    }

    private void capture(ShadersFramebuffer target, Runnable geometry) {
        int nextPoll = Math.clamp(this.activeLayers + 1, 2, this.layers.length);
        this.queries.reset();
        copy(this.framebuffer.getDepthTarget().texture(), this.opaqueDepth);
        copy(this.framebuffer.getDepth(1).texture(), this.opaqueSampledDepth);
        var uniforms = VulkanShaderRuntime.getInstance().getProgram(Shaders.ProgramWater.getId()).uniforms();
        try {
            for (int i = 0; i < this.layers.length; i++) {
                if (i > 0) {
                    copy(this.opaqueDepth, this.framebuffer.getDepthTarget().texture());
                }
                bind(uniforms, "ofPeelPrevious", PREVIOUS_DEPTH_UNIT, this.layers[Math.max(0, i - 1)].view());
                uniforms.setInt("ofPeelLayer", i);
                this.queries.beginLayer(i);
                try {
                    geometry.run();
                } finally {
                    this.queries.endLayer();
                }
                Layer layer = this.layers[i];
                copy(this.framebuffer.getDepthTarget().texture(), layer.depth());
                for (int c = 0; c < this.colorIndices.length; c++) {
                    copy(this.framebuffer.getColor(true, this.colorIndices[c]).texture(), layer.colors()[c]);
                }
                if (i == 0) {
                    Shaders.refreshTransparencyDepth(target);
                    copy(this.framebuffer.getDepth(0).texture(), layer.sampledDepth());
                }
                if (i + 1 == nextPoll) {
                    this.activeLayers = this.queries.activeLayers(this.layers.length);
                    if (this.activeLayers <= i) {
                        break;
                    }
                    nextPoll = Math.min(nextPoll + 2, this.layers.length);
                }
            }
            this.load(0);
            target.bindFramebuffer();
            target.setColorTextures(true);
            target.setDepthTexture();
            this.captured = true;
        } finally {
            uniforms.setInt("ofPeelLayer", 0);
        }
    }

    private void load(int index) {
        this.load(this.layers[index]);
    }

    private void load(Layer layer) {
        copy(layer.depth(), this.framebuffer.getDepthTarget().texture());
        copy(layer.sampledDepth(), this.framebuffer.getDepth(0).texture());
        for (int c = 0; c < this.colorIndices.length; c++) {
            copy(layer.colors()[c], this.framebuffer.getColor(true, this.colorIndices[c]).texture());
        }
    }

    public static boolean shouldResolve(Program[] programs, int index) {
        if (!Config.isVulkan()) return net.optifine.shaders.OpenGlTransparency.shouldResolve(programs, index);
        return instance != null && instance.captured && programs == Shaders.ProgramsComposite && index == profile.first();
    }

    public static boolean isResolving() {
        return Config.isVulkan() ? instance != null && instance.resolvingLayer >= 0
            : net.optifine.shaders.OpenGlTransparency.isResolving();
    }

    public static boolean needsOpaqueResolve() {
        return profile.first() == 2;
    }

    public static int lastComposite() {
        return profile.last();
    }

    public static void resolve(Runnable programs, Runnable camera) {
        if (!Config.isVulkan()) {
            net.optifine.shaders.OpenGlTransparency.resolve(programs, camera);
            return;
        }
        VulkanTransparency current = instance;
        GpuTexture[] history = profile.first() == 1 ? new GpuTexture[]{
            current.framebuffer.getColor(true, 3).texture(), current.framebuffer.getColor(true, 7).texture()
        } : null;
        try {
            boolean lateHand = hasLateHand();
            if (lateHand) {
                if (current.lateLayer == null) {
                    GpuTexture[] colors = new GpuTexture[current.colorIndices.length];
                    for (int c = 0; c < colors.length; c++) {
                        colors[c] = current.create(current.opaqueColors[c], "late color " + c);
                    }
                    GpuTexture depth = current.create(current.opaqueDepth, "late depth");
                    current.lateLayer = new Layer(colors, depth, current.view(depth), current.create(current.opaqueSampledDepth, "late sampled depth"));
                }
                copy(current.framebuffer.getDepthTarget().texture(), current.lateLayer.depth());
                copy(current.framebuffer.getDepth(0).texture(), current.lateLayer.sampledDepth());
                for (int c = 0; c < current.colorIndices.length; c++) {
                    copy(current.framebuffer.getColor(true, current.colorIndices[c]).texture(), current.lateLayer.colors()[c]);
                }
            }
            copy(current.opaqueSampledDepth, current.framebuffer.getDepth(1).texture());
            if (current.waterWaves != null) {
                copy(current.framebuffer.getColor(true, 4).texture(), current.waterWaves);
            }
            // Only scratch inputs that are written need a copy. Read-only history
            // textures retain their storage and return to their original ping-pong side.
            for (int side = 0; side < 2; side++) {
                for (int c = 0; c < current.compositeInputs[side].length; c++) {
                    if (current.compositeInputs[side][c] != null) {
                        copy(current.framebuffer.getColor(side == 0, c).texture(), current.compositeInputs[side][c]);
                    }
                }
            }
            // Align surfaces from the back per pixel. A water surface split between ranks by foreground
            // glass must be fully shaded before the glass samples neighboring pixels for refraction.
            for (int i = current.layers.length - 1; i >= current.layers.length - current.activeLayers; i--) {
                current.resolvingLayer = i;
                for (int side = 0; side < 2; side++) {
                    for (int c = 0; c < current.compositeInputs[side].length; c++) {
                        if (current.compositeInputs[side][c] != null) {
                            copy(current.compositeInputs[side][c], current.framebuffer.getColor(side == 0, c).texture());
                        }
                    }
                }
                current.gather(i);
                programs.run();
                current.restoreHistory(history);
            }
            if (lateHand) {
                current.lateResolve = true;
                current.resolvingLayer = 0;
                for (int side = 0; side < 2; side++) {
                    for (int c = 0; c < current.compositeInputs[side].length; c++) {
                        if (current.compositeInputs[side][c] != null) {
                            copy(current.compositeInputs[side][c], current.framebuffer.getColor(side == 0, c).texture());
                        }
                    }
                }
                current.load(current.lateLayer);
                programs.run();
                current.restoreHistory(history);
                current.lateResolve = false;
            }
            copy(current.framebuffer.getColor(true, profile.color()).texture(), current.resolvedLighting);
            current.load(lateHand ? current.lateLayer : current.layers[0]);
            copy(current.opaqueSampledDepth, current.framebuffer.getDepth(1).texture());
            current.cameraResolve = true;
            current.resolvingLayer = 0;
            // Rebuild postprocess metadata from the same history inputs as a normal frame.
            // The last peeled layer has already replaced normals, reflection data and history sides.
            if (profile.first() == 1) {
                for (int side = 0; side < 2; side++) {
                    for (int c = 0; c < current.compositeInputs[side].length; c++) {
                        if (current.compositeInputs[side][c] != null) {
                            copy(current.compositeInputs[side][c], current.framebuffer.getColor(side == 0, c).texture());
                        }
                    }
                }
                current.load(lateHand ? current.lateLayer : current.layers[0]);
                programs.run();
            } else {
                camera.run();
            }
        } finally {
            current.cameraResolve = false;
            current.lateResolve = false;
            current.resolvingLayer = -1;
            current.captured = false;
            copy(current.opaqueSampledDepth, current.framebuffer.getDepth(1).texture());
        }
    }

    private void restoreHistory(GpuTexture[] history) {
        if (history != null) {
            for (int i = 0; i < history.length; i++) {
                int color = i == 0 ? 3 : 7;
                if (this.framebuffer.getColor(true, color).texture() != history[i]) {
                    this.framebuffer.flipColor(color);
                }
            }
        }
    }

    public static void beforeComposite(Program program) {
        if (!Config.isVulkan()) {
            net.optifine.shaders.OpenGlTransparency.beforeComposite(program);
            return;
        }
        VulkanTransparency current = instance;
        if (current == null) {
            return;
        }
        var uniforms = VulkanShaderRuntime.getInstance().getProgram(program.getId()).uniforms();
        if (current.resolvingLayer < 0) {
            if (!uniforms.contains("ofLayerResolve")) {
                return;
            }
            if (program == Shaders.ProgramsComposite[2] && current.lightingInput != null) {
                copy(current.framebuffer.getColor(true, profile.color()).texture(), current.lightingInput);
                VulkanMipmaps.generateColor((com.mojang.blaze3d.vulkan.VulkanDevice)RenderSystem.getDevice().getBackend(), current.lightingInput);
                bind(uniforms, "ofLayerGi", 64, current.lightingInputView);
            }
            if (uniforms.contains("ofLayerResolve")) uniforms.setInt("ofLayerResolve", 0);
            if (uniforms.contains("ofLayerIntermediate")) uniforms.setInt("ofLayerIntermediate", 0);
            if (uniforms.contains("ofLayerCameraOnly")) uniforms.setInt("ofLayerCameraOnly", 0);
            if (uniforms.contains("ofWaterWaves")) {
                copy(current.framebuffer.getColor(true, 4).texture(), current.waterWaves);
                bind(uniforms, "ofWaterWaves", 63, current.waterWavesView);
            }
            bind(uniforms, "ofLayerDepth", LAYER_DEPTH_UNIT, current.layers[0].view());
            bind(uniforms, "ofOpaqueDepth", OPAQUE_DEPTH_UNIT, current.opaqueDepthView);
            return;
        }
        if (current.cameraResolve && program == Shaders.ProgramsComposite[profile.last()]) {
            copy(current.resolvedLighting, current.framebuffer.getColor(true, profile.color()).texture());
        }
        if (current.cameraResolve || program == Shaders.ProgramsComposite[2] || profile.first() == 1 && program != Shaders.ProgramsComposite[1]) {
            // Reflection history still describes opaque geometry; refraction uses the next surface.
            copy(current.opaqueSampledDepth, current.framebuffer.getDepth(1).texture());
        } else {
            copy(current.lateResolve ? current.layers[0].sampledDepth() : current.nextDepth, current.framebuffer.getDepth(1).texture());
        }
        if (uniforms.contains("ofLayerGi")) {
            bind(uniforms, "ofLayerGi", 64, current.lightingInputView);
        }
        if (uniforms.contains("ofWaterWaves")) {
            bind(uniforms, "ofWaterWaves", 63, current.waterWavesView);
        }
        bind(uniforms, "ofLayerDepth", LAYER_DEPTH_UNIT, current.activeDepthView);
        bind(uniforms, "ofOpaqueDepth", OPAQUE_DEPTH_UNIT, current.lateResolve ? current.layers[0].view() : current.opaqueDepthView);
        uniforms.setInt("ofLayerResolve", current.cameraResolve ? 0 : 1);
        if (uniforms.contains("ofLayerCameraOnly")) uniforms.setInt("ofLayerCameraOnly", current.cameraResolve ? 1 : 0);
        if (uniforms.contains("ofLayerIntermediate")) {
            uniforms.setInt("ofLayerIntermediate", current.cameraResolve ? 0 : 1);
        }
        var buffers = program.getDrawBuffers();
        for (int i = 0; i < buffers.limit(); i++) {
            int index = buffers.get(i) - 36064;
            if (index >= 0 && profile.first() != 1) {
                copy(current.framebuffer.getColor(true, index).texture(), current.framebuffer.getColor(false, index).texture());
            }
        }
    }

    public static void reset() {
        net.optifine.shaders.OpenGlTransparency.reset();
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    @Override
    public void close() {
        this.queries.close();
        VulkanShaderRuntime.getInstance().deleteProgram(this.gatherProgram);
        this.views.forEach(GpuTextureView::close);
        this.textures.forEach(GpuTexture::close);
    }

    private record Layer(GpuTexture[] colors, GpuTexture depth, GpuTextureView view, GpuTexture sampledDepth) {
    }

    public record Profile(int first, int last, int color) {
    }
}
