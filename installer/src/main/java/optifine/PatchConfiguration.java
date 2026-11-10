package optifine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Generated mappings are quoted literal paths; only the remaining rules need regex matching.
public class PatchConfiguration {
    private final Map<String, LiteralRule> literals = new HashMap<>();
    private final List<RegexRule> patterns = new ArrayList<>();

    public PatchConfiguration(Map<String, String> configuration) {
        int index = 0;
        for (Map.Entry<String, String> entry : configuration.entrySet()) {
            String expression = entry.getKey();
            if (expression.startsWith("\\Q") && expression.endsWith("\\E")
                && expression.indexOf("\\E") == expression.length() - 2) {
                this.literals.put(expression.substring(2, expression.length() - 2), new LiteralRule(index, entry.getValue()));
            } else {
                this.patterns.add(new RegexRule(index, Pattern.compile(expression), entry.getValue()));
            }
            index++;
        }
    }

    public String getPatchBase(String name) {
        name = Utils.removePrefix(name, "/");
        LiteralRule literal = this.literals.get(name);
        int limit = literal == null ? Integer.MAX_VALUE : literal.index;
        for (RegexRule rule : this.patterns) {
            if (rule.index > limit) {
                break;
            }
            Matcher matcher = rule.pattern.matcher(name);
            if (matcher.matches()) {
                return Patcher.resolvePatchBase(name, rule.base, matcher);
            }
        }
        return literal == null ? null : Patcher.resolvePatchBase(name, literal.base, null);
    }

    private static class LiteralRule {
        final int index;
        final String base;

        LiteralRule(int index, String base) {
            this.index = index;
            this.base = base;
        }
    }

    private static class RegexRule {
        final int index;
        final Pattern pattern;
        final String base;

        RegexRule(int index, Pattern pattern, String base) {
            this.index = index;
            this.pattern = pattern;
            this.base = base;
        }
    }
}
