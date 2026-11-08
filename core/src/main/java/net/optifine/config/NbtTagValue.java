package net.optifine.config;

import java.util.regex.Pattern;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.optifine.Config;
import net.optifine.util.ArrayUtils;
import net.optifine.util.StrUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class NbtTagValue {
    private String[] path = null;
    private boolean negative = false;
    private boolean raw = false;
    private int type = 0;
    private String value = null;
    private Pattern valueRegex = null;
    private RangeListInt valueRange = null;
    private int valueFormat = 0;
    private static final int TYPE_INVALID = -1;
    private static final int TYPE_TEXT = 0;
    private static final int TYPE_PATTERN = 1;
    private static final int TYPE_IPATTERN = 2;
    private static final int TYPE_REGEX = 3;
    private static final int TYPE_IREGEX = 4;
    private static final int TYPE_RANGE = 5;
    private static final int TYPE_EXISTS = 6;
    private static final String PREFIX_PATTERN = "pattern:";
    private static final String PREFIX_IPATTERN = "ipattern:";
    private static final String PREFIX_REGEX = "regex:";
    private static final String PREFIX_IREGEX = "iregex:";
    private static final String PREFIX_RAW = "raw:";
    private static final String PREFIX_RANGE = "range:";
    private static final String PREFIX_EXISTS = "exists:";
    private static final int FORMAT_DEFAULT = 0;
    private static final int FORMAT_HEX_COLOR = 1;
    private static final String PREFIX_HEX_COLOR = "#";
    private static final Pattern PATTERN_HEX_COLOR = Pattern.compile("^#[0-9a-f]{6}+$");

    public NbtTagValue(String tag, String value) {
        String[] path = Config.tokenize(tag, ".");
        this.path = path;
        if (value.startsWith("!")) {
            this.negative = true;
            value = value.substring(1);
        }

        if (value.startsWith("raw:")) {
            this.raw = true;
            value = value.substring("raw:".length());
        }

        if (value.startsWith("pattern:")) {
            this.type = 1;
            value = value.substring("pattern:".length());
            if (value.equals("*")) {
                this.type = 6;
            }
        } else if (value.startsWith("ipattern:")) {
            this.type = 2;
            value = value.substring("ipattern:".length()).toLowerCase();
            if (value.equals("*")) {
                this.type = 6;
            }
        } else if (value.startsWith("regex:")) {
            this.type = 3;
            value = value.substring("regex:".length());
            this.valueRegex = Pattern.compile(value);
            if (value.equals(".*")) {
                this.type = 6;
            }
        } else if (value.startsWith("iregex:")) {
            this.type = 4;
            value = value.substring("iregex:".length());
            this.valueRegex = Pattern.compile(value, 2);
            if (value.equals(".*")) {
                this.type = 6;
            }
        } else if (value.startsWith("range:")) {
            this.type = 5;
            value = value.substring("range:".length());
            ConnectedParser cp = new ConnectedParser("NbtTag");
            this.valueRange = cp.parseRangeListIntNeg(value);
            if (this.valueRange == null) {
                Config.warn("Invalid range: " + value);
                this.type = -1;
                this.negative = false;
            }
        } else if (value.startsWith("exists:")) {
            this.type = 6;
            value = value.substring("exists:".length());
            Boolean valB = Config.parseBoolean(value, null);
            if (Config.isFalse(valB)) {
                this.negative = !this.negative;
            }

            if (valB == null) {
                Config.warn("Invalid exists: " + value);
                this.type = -1;
                this.negative = false;
            }
        } else {
            this.type = 0;
        }

        value = StringEscapeUtils.unescapeJava(value);
        if (this.type == 0 && PATTERN_HEX_COLOR.matcher(value).matches()) {
            this.valueFormat = 1;
        }

        this.value = value;
    }

    public boolean matches(CompoundTag nbt) {
        return this.negative ? !this.matchesTag(nbt, 0) : this.matchesTag(nbt, 0);
    }

    public boolean matchesTag(Tag tag, int level) {
        if (tag == null) {
            return false;
        }

        if (level < this.path.length) {
            String tagName = this.path[level];
            level++;
            if (tagName.equals("*")) {
                return this.matchesAnyChild(tag, level);
            }

            Tag childTag = getChildTag(tag, tagName);
            return this.matchesTag(childTag, level);
        } else {
            return this.matchesBase(tag);
        }
    }

    private boolean matchesAnyChild(Tag tag, int level) {
        if (tag instanceof CompoundTag tagCompound) {
            for (String key : tagCompound.keySet()) {
                Tag childTag = tagCompound.get(key);
                if (this.matchesTag(childTag, level)) {
                    return true;
                }
            }
        }

        if (tag instanceof ListTag tagList) {
            int count = tagList.size();

            for (int i = 0; i < count; i++) {
                Tag childTag = tagList.get(i);
                if (this.matchesTag(childTag, level)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static Tag getChildTag(Tag tagBase, String tag) {
        if (tagBase instanceof CompoundTag tagCompound) {
            return tagCompound.get(tag);
        } else if (tagBase instanceof ListTag tagList) {
            if (tag.equals("count")) {
                return IntTag.valueOf(tagList.size());
            }

            int index = Config.parseInt(tag, -1);
            return index >= 0 && index < tagList.size() ? tagList.get(index) : null;
        } else {
            return null;
        }
    }

    public boolean matchesBase(Tag nbtBase) {
        if (nbtBase == null) {
            return false;
        }

        switch (this.type) {
            case -1:
                return false;
            case 5:
                int nbtValueInt = getNbtInt(nbtBase, Integer.MIN_VALUE);
                if (nbtValueInt != Integer.MIN_VALUE) {
                    return matchesRange(nbtValueInt, this.valueRange);
                }
            case 6:
                return true;
            default:
                String nbtValue = this.raw ? String.valueOf(nbtBase) : getNbtString(nbtBase, this.valueFormat);
                return this.matchesValue(nbtValue);
        }
    }

    public boolean matchesValue(String nbtValue) {
        if (nbtValue == null) {
            return false;
        }

        switch (this.type) {
            case -1:
                return false;
            case 0:
                return nbtValue.equals(this.value);
            case 1:
                return matchesPattern(nbtValue, this.value);
            case 2:
                return matchesPattern(nbtValue.toLowerCase(), this.value);
            case 3:
            case 4:
                return matchesRegex(nbtValue, this.valueRegex);
            case 5:
                return matchesRange(nbtValue, this.valueRange);
            case 6:
                return true;
            default:
                throw new IllegalArgumentException("Unknown NbtTagValue type: " + this.type);
        }
    }

    private static boolean matchesPattern(String str, String pattern) {
        return StrUtils.equalsMask(str, pattern, '*', '?');
    }

    private static boolean matchesRegex(String str, Pattern regex) {
        return regex.matcher(str).matches();
    }

    private static boolean matchesRange(String str, RangeListInt range) {
        if (range == null) {
            return false;
        }

        int valInt = Config.parseInt(str, Integer.MIN_VALUE);
        return valInt == Integer.MIN_VALUE ? false : matchesRange(valInt, range);
    }

    private static boolean matchesRange(int valInt, RangeListInt range) {
        return range == null ? false : range.isInRange(valInt);
    }

    private static String getNbtString(Tag nbtBase, int format) {
        if (nbtBase == null) {
            return null;
        }

        if (!(nbtBase instanceof StringTag nbtString)) {
            if (nbtBase instanceof IntTag i) {
                return format == 1 ? "#" + StrUtils.fillLeft(Integer.toHexString(i.value()), 6, '0') : Integer.toString(i.value());
            } else if (nbtBase instanceof ByteTag b) {
                return Byte.toString(b.value());
            } else if (nbtBase instanceof ShortTag s) {
                return Short.toString(s.value());
            } else if (nbtBase instanceof LongTag l) {
                return Long.toString(l.value());
            } else if (nbtBase instanceof FloatTag f) {
                return Float.toString(f.value());
            } else {
                return nbtBase instanceof DoubleTag d ? Double.toString(d.value()) : nbtBase.toString();
            }
        } else {
            String text = nbtString.value();
            if (text.startsWith("{") && text.endsWith("}")) {
                text = getMergedJsonText(text);
            } else if (text.startsWith("[{") && text.endsWith("}]")) {
                text = getMergedJsonText(text);
            } else if (text.startsWith("\"") && text.endsWith("\"") && text.length() > 1) {
                text = text.substring(1, text.length() - 1);
            }

            return text;
        }
    }

    private static int getNbtInt(Tag nbtBase, int defVal) {
        if (nbtBase == null) {
            return defVal;
        } else if (nbtBase instanceof IntTag i) {
            return i.intValue();
        } else if (nbtBase instanceof ByteTag b) {
            return b.intValue();
        } else if (nbtBase instanceof ShortTag s) {
            return s.intValue();
        } else if (nbtBase instanceof LongTag l) {
            return l.intValue();
        } else if (nbtBase instanceof FloatTag f) {
            return f.intValue();
        } else {
            return nbtBase instanceof DoubleTag d ? d.intValue() : defVal;
        }
    }

    private static String getMergedJsonText(String text) {
        StringBuilder sb = new StringBuilder();
        String TOKEN_TEXT = "\"text\":\"";
        int pos = -1;

        while (true) {
            pos = text.indexOf(TOKEN_TEXT, pos + 1);
            if (pos < 0) {
                return sb.toString();
            }

            String str = parseString(text, pos + TOKEN_TEXT.length());
            if (str != null) {
                sb.append(str);
            }
        }
    }

    private static String parseString(String text, int pos) {
        StringBuilder sb = new StringBuilder();
        boolean escapeMode = false;

        for (int i = pos; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (escapeMode) {
                if (ch == 'b') {
                    sb.append('\b');
                } else if (ch == 'f') {
                    sb.append('\f');
                } else if (ch == 'n') {
                    sb.append('\n');
                } else if (ch == 'r') {
                    sb.append('\r');
                } else if (ch == 't') {
                    sb.append('\t');
                } else {
                    sb.append(ch);
                }

                escapeMode = false;
            } else if (ch == '\\') {
                escapeMode = true;
            } else {
                if (ch == '"') {
                    break;
                }

                sb.append(ch);
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return ArrayUtils.arrayToString(this.path, ".") + " = " + this.value;
    }
}
