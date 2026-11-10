package net.optifine.shaders.vulkan;

import java.util.regex.Pattern;

public class VulkanRectangleShaders {
    private static final Pattern TYPES = Pattern.compile("\\b([iu]?sampler2DRect|sampler2DRectShadow)\\b");
    public static final String MEMBER = "_ofRectTexture";

    // Input is preprocessed, so macros, comments and disabled branches cannot hide types.
    public static String adapt(String source) {
        var matcher = TYPES.matcher(source);
        if (!matcher.find()) {
            return source;
        }
        String adapted = matcher.replaceAll("_of_$1")
            .replaceAll("\\btexture2DRect\\s*(?=\\()", "texture")
            .replaceAll("\\btexture2DRectProj\\s*(?=\\()", "textureProj");
        StringBuilder helpers = new StringBuilder();
        for (String prefix : new String[]{"", "i", "u"}) {
            String type = "_of_" + prefix + "sampler2DRect";
            String result = prefix.isEmpty() ? "vec4" : prefix + "vec4";
            String sampler = "s." + MEMBER;
            helpers.append("struct ").append(type).append(" { ").append(prefix).append("sampler2D ").append(MEMBER).append("; };\n");
            helpers.append("ivec2 textureSize(").append(type).append(" s) { return textureSize(").append(sampler).append(", 0); }\n");
            helpers.append(result).append(" texture(").append(type).append(" s, vec2 p) { return textureLod(").append(sampler)
                .append(", p / vec2(textureSize(").append(sampler).append(", 0)), 0.0); }\n");
            helpers.append(result).append(" textureOffset(").append(type).append(" s, vec2 p, ivec2 o) { return texture(s, p + vec2(o)); }\n");
            helpers.append(result).append(" texelFetch(").append(type).append(" s, ivec2 p) { return texelFetch(").append(sampler).append(", p, 0); }\n");
            helpers.append(result).append(" texelFetchOffset(").append(type).append(" s, ivec2 p, ivec2 o) { return texelFetch(s, p + o); }\n");
            for (int size : new int[]{3, 4}) {
                String divisor = size == 3 ? "z" : "w";
                helpers.append(result).append(" textureProj(").append(type).append(" s, vec").append(size)
                    .append(" p) { return texture(s, p.xy / p.").append(divisor).append("); }\n");
                helpers.append(result).append(" textureProjOffset(").append(type).append(" s, vec").append(size)
                    .append(" p, ivec2 o) { return textureOffset(s, p.xy / p.").append(divisor).append(", o); }\n");
                helpers.append(result).append(" textureProjGrad(").append(type).append(" s, vec").append(size)
                    .append(" p, vec2 dx, vec2 dy) { return textureProj(s, p); }\n");
                helpers.append(result).append(" textureProjGradOffset(").append(type).append(" s, vec").append(size)
                    .append(" p, vec2 dx, vec2 dy, ivec2 o) { return textureProjOffset(s, p, o); }\n");
            }
            helpers.append(result).append(" textureGrad(").append(type).append(" s, vec2 p, vec2 dx, vec2 dy) { return texture(s, p); }\n");
            helpers.append(result).append(" textureGradOffset(").append(type)
                .append(" s, vec2 p, vec2 dx, vec2 dy, ivec2 o) { return textureOffset(s, p, o); }\n");
            helpers.append(result).append(" textureGather(").append(type).append(" s, vec2 p, int c) { vec2 uv = p / vec2(textureSize(")
                .append(sampler).append(", 0)); if (c == 0) return textureGather(").append(sampler).append(", uv, 0);")
                .append(" if (c == 1) return textureGather(").append(sampler).append(", uv, 1);")
                .append(" if (c == 2) return textureGather(").append(sampler).append(", uv, 2);")
                .append(" return textureGather(").append(sampler).append(", uv, 3); }\n");
            helpers.append(result).append(" textureGather(").append(type).append(" s, vec2 p) { return textureGather(s, p, 0); }\n");
            helpers.append(result).append(" textureGatherOffset(").append(type)
                .append(" s, vec2 p, ivec2 o, int c) { return textureGather(s, p + vec2(o), c); }\n");
            helpers.append(result).append(" textureGatherOffset(").append(type)
                .append(" s, vec2 p, ivec2 o) { return textureGather(s, p + vec2(o), 0); }\n");
            helpers.append(result).append(" textureGatherOffsets(").append(type)
                .append(" s, vec2 p, ivec2 o[4], int c) { return ").append(result)
                .append("(textureGatherOffset(s, p, o[0], c).w, textureGatherOffset(s, p, o[1], c).w,")
                .append(" textureGatherOffset(s, p, o[2], c).w, textureGatherOffset(s, p, o[3], c).w); }\n");
            helpers.append(result).append(" textureGatherOffsets(").append(type)
                .append(" s, vec2 p, ivec2 o[4]) { return textureGatherOffsets(s, p, o, 0); }\n");
        }
        helpers.append("""
            struct _of_sampler2DRectShadow { sampler2DShadow _ofRectTexture; };
            ivec2 textureSize(_of_sampler2DRectShadow s) { return textureSize(s._ofRectTexture, 0); }
            float texture(_of_sampler2DRectShadow s, vec3 p) {
                return textureGrad(s._ofRectTexture, vec3(p.xy / vec2(textureSize(s)), p.z), vec2(0), vec2(0));
            }
            float textureOffset(_of_sampler2DRectShadow s, vec3 p, ivec2 o) { return texture(s, p + vec3(o, 0)); }
            float textureProj(_of_sampler2DRectShadow s, vec4 p) { return texture(s, p.xyz / p.w); }
            float textureProjOffset(_of_sampler2DRectShadow s, vec4 p, ivec2 o) { return textureOffset(s, p.xyz / p.w, o); }
            float textureProjGrad(_of_sampler2DRectShadow s, vec4 p, vec2 dx, vec2 dy) { return textureProj(s, p); }
            float textureProjGradOffset(_of_sampler2DRectShadow s, vec4 p, vec2 dx, vec2 dy, ivec2 o) { return textureProjOffset(s, p, o); }
            float textureGrad(_of_sampler2DRectShadow s, vec3 p, vec2 dx, vec2 dy) { return texture(s, p); }
            float textureGradOffset(_of_sampler2DRectShadow s, vec3 p, vec2 dx, vec2 dy, ivec2 o) { return textureOffset(s, p, o); }
            vec4 textureGather(_of_sampler2DRectShadow s, vec2 p, float ref) {
                return textureGather(s._ofRectTexture, p / vec2(textureSize(s)), ref);
            }
            vec4 textureGatherOffset(_of_sampler2DRectShadow s, vec2 p, float ref, ivec2 o) { return textureGather(s, p + vec2(o), ref); }
            vec4 textureGatherOffsets(_of_sampler2DRectShadow s, vec2 p, float ref, ivec2 o[4]) {
                return vec4(textureGatherOffset(s, p, ref, o[0]).w, textureGatherOffset(s, p, ref, o[1]).w,
                    textureGatherOffset(s, p, ref, o[2]).w, textureGatherOffset(s, p, ref, o[3]).w);
            }
            """);
        // Keep the version and extension directives before helper declarations.
        int insertion = 0;
        while (insertion < adapted.length()) {
            int end = adapted.indexOf('\n', insertion);
            end = end < 0 ? adapted.length() : end + 1;
            String line = adapted.substring(insertion, end).strip();
            if (!line.isEmpty() && !line.startsWith("#")) {
                break;
            }
            insertion = end;
        }
        return adapted.substring(0, insertion) + helpers + "\n" + adapted.substring(insertion);
    }

    public static String uniformName(String reflected) {
        String suffix = "." + MEMBER;
        return reflected.endsWith(suffix) ? reflected.substring(0, reflected.length() - suffix.length()) : reflected;
    }
}
