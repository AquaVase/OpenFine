package net.optifine.shaders.uniform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.optifine.expr.IExpression;
import net.optifine.expr.IExpressionCached;
import net.optifine.shaders.vulkan.VulkanProgramUniforms;

public class CustomUniforms {
    private CustomUniform[] uniforms;
    private IExpressionCached[] expressionsCached;

    public CustomUniforms(CustomUniform[] uniforms, Map<String, IExpression> mapExpressions) {
        this.uniforms = uniforms;
        List<IExpressionCached> list = new ArrayList<>();

        for (String key : mapExpressions.keySet()) {
            IExpression expr = mapExpressions.get(key);
            if (expr instanceof IExpressionCached exprCached) {
                list.add(exprCached);
            }
        }

        this.expressionsCached = list.toArray(new IExpressionCached[list.size()]);
    }

    public void setProgram(int program) {
        this.setProgram(program, null);
    }

    public void setProgram(int program, VulkanProgramUniforms vulkanUniforms) {
        for (int i = 0; i < this.uniforms.length; i++) {
            CustomUniform uniform = this.uniforms[i];
            uniform.getShaderUniform().setProgram(program, vulkanUniforms);
        }
    }

    public void update() {
        this.resetCache();

        for (int i = 0; i < this.uniforms.length; i++) {
            CustomUniform uniform = this.uniforms[i];
            uniform.update();
        }
    }

    private void resetCache() {
        for (int i = 0; i < this.expressionsCached.length; i++) {
            IExpressionCached exprCached = this.expressionsCached[i];
            exprCached.reset();
        }
    }

    public void reset() {
        for (int i = 0; i < this.uniforms.length; i++) {
            CustomUniform cu = this.uniforms[i];
            cu.reset();
        }
    }
}
