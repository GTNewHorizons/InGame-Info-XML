package com.github.lunatrius.ingameinfo.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.github.lunatrius.ingameinfo.client.gui.InfoText;
import com.github.lunatrius.ingameinfo.tag.Tag;
import com.github.lunatrius.ingameinfo.tag.registry.TagRegistry;
import com.github.lunatrius.ingameinfo.value.registry.ValueRegistry;

public abstract class Value {

    protected InfoText parent;
    private String name = null;
    private String[] aliases = new String[0];
    protected String value = "";
    public final List<Value> values = new ArrayList<>();

    public Value setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Value setAliases(String... aliases) {
        this.aliases = aliases;
        return this;
    }

    public String[] getAliases() {
        return this.aliases;
    }

    public String getType() {
        return ValueRegistry.INSTANCE.forClass(getClass());
    }

    public Value setRawValue(String value, boolean isText) {
        this.value = "";
        return this;
    }

    public String getRawValue(boolean isText) {
        return this.value;
    }

    protected String replaceVariables(String str) {
        int tagAmount = StringUtils.countMatches(str, "{");
        if (tagAmount == 0 || tagAmount == 1 && str.contains("ICON")) {
            return str;
        }
        StringBuilder builder = new StringBuilder(str);

        int nextStart = 0;
        for (int i = 0; i < tagAmount; i++) {
            int start = builder.indexOf("{", nextStart);
            if (start == -1) break;

            int end = builder.indexOf("}", start);
            if (end == -1) break;
            String var = builder.substring(start + 1, end);
            if (var.startsWith("ICON")) {
                nextStart = end + 1;
                continue;
            }
            String replacement = getVariableValue(var);
            builder.replace(start, end + 1, replacement);
        }

        return builder.toString();
    }

    public abstract boolean isSimple();

    public abstract boolean isValidSize();

    public abstract String getValue();

    public boolean isValid() {
        return true;
    }

    protected String getValue(int index) {
        return this.values.get(index).getReplacedValue();
    }

    protected int getIntValue() {
        return Integer.parseInt(getReplacedValue());
    }

    protected int getIntValue(int index) {
        return Integer.parseInt(getValue(index));
    }

    protected double getDoubleValue() {
        return Double.parseDouble(getReplacedValue());
    }

    protected double getDoubleValue(int index) {
        return Double.parseDouble(getValue(index));
    }

    protected boolean getBooleanValue() {
        return Boolean.parseBoolean(getReplacedValue());
    }

    protected boolean getBooleanValue(int index) {
        return Boolean.parseBoolean(getValue(index));
    }

    protected String getVariableValue(String var) {
        try {
            Tag tag = TagRegistry.INSTANCE.getTag(var);
            String value = tag.getValue();
            if (value == null) {
                return "{" + var + "}";
            }
            if (value.isEmpty()) {
                value = tag.getValue(this.parent);
            }

            return value;
        } catch (Exception e) {
            return "<ERROR>";
        }
    }

    public String getReplacedValue() {
        return replaceVariables(getValue());
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "[%s] '%s'", getClass(), this.value);
    }

    public void setParent(InfoText parent) {
        this.parent = parent;
        for (Value value : values) {
            value.setParent(parent);
        }
    }

    public static Value fromString(String str) {
        return ValueRegistry.INSTANCE.forName(str);
    }

    public static String toString(Value value) {
        return ValueRegistry.INSTANCE.forClass(value.getClass());
    }
}
