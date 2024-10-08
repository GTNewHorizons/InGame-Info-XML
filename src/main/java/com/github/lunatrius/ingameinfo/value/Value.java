package com.github.lunatrius.ingameinfo.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.lunatrius.ingameinfo.client.gui.Info;
import com.github.lunatrius.ingameinfo.reference.Reference;
import com.github.lunatrius.ingameinfo.tag.registry.TagRegistry;
import com.github.lunatrius.ingameinfo.value.registry.ValueRegistry;

public abstract class Value {

    private static final Pattern PATTERN = Pattern.compile("\\{([a-z0-9]+)\\}", Pattern.CASE_INSENSITIVE);
    private static final Matcher MATCHER = PATTERN.matcher("");
    protected static List<Info> info;

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
        MATCHER.reset(str);

        while (MATCHER.find()) {
            str = str.replace(MATCHER.group(0), getVariableValue(MATCHER.group(1)));
        }

        return str;
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
            String value = TagRegistry.INSTANCE.getValue(var);
            if (value != null) {
                return value;
            }
        } catch (Exception e) {
            Reference.logger.debug("Failed to get value!", e);
            return "null";
        }

        return "{" + var + "}";
    }

    public String getReplacedValue() {
        return replaceVariables(getValue());
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "[%s] '%s'", getClass(), this.value);
    }

    public static void setInfo(List<Info> info) {
        Value.info = info;
    }

    public static Value fromString(String str) {
        return ValueRegistry.INSTANCE.forName(str);
    }

    public static String toString(Value value) {
        return ValueRegistry.INSTANCE.forClass(value.getClass());
    }
}
