package com.github.lunatrius.ingameinfo.value.registry;

import java.util.HashMap;
import java.util.Map;

import com.github.lunatrius.ingameinfo.reference.Reference;
import com.github.lunatrius.ingameinfo.value.Value;
import com.github.lunatrius.ingameinfo.value.ValueComplex;
import com.github.lunatrius.ingameinfo.value.ValueLogic;
import com.github.lunatrius.ingameinfo.value.ValueMath;
import com.github.lunatrius.ingameinfo.value.ValueSimple;

public class ValueRegistry {

    public static final ValueRegistry INSTANCE = new ValueRegistry();

    private final Map<String, Value> stringValueMap = new HashMap<>();
    private final Map<String, Class<? extends Value>> stringClassMap = new HashMap<>();
    private final Map<Class<? extends Value>, String> classStringMap = new HashMap<>();

    private void register(String name, Value value, boolean isAlias) {
        if (this.stringValueMap.containsKey(name)) {
            Reference.logger.error("Duplicate value key '" + name + "'!");
            return;
        }

        if (name == null) {
            Reference.logger.error("Value name cannot be null!");
            return;
        }

        this.stringValueMap.put(name.toLowerCase(), value);
        this.stringClassMap.put(name.toLowerCase(), value.getClass());
        if (!isAlias) {
            this.classStringMap.put(value.getClass(), name.toLowerCase());
        }
    }

    public void register(Value value) {
        register(value.getName(), value, false);

        for (String name : value.getAliases()) {
            register(name, value, true);
        }
    }

    public Value forName(String name) {
        name = name.toLowerCase();
        try {
            final Class<? extends Value> clazz = this.stringClassMap.get(name);
            if (clazz != null) {
                return clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            Reference.logger.error(String.format("Failed to create an instance for %s!", name), e);
            return new ValueSimple.ValueInvalid();
        }

        Reference.logger.error(String.format("Failed to create an instance for %s!", name));
        return new ValueSimple.ValueInvalid();
    }

    public String forClass(Class<? extends Value> clazz) {
        final String str = this.classStringMap.get(clazz);
        return str != null ? str : "invalid";
    }

    public void init() {
        ValueComplex.register();
        ValueLogic.register();
        ValueMath.register();
        ValueSimple.register();
    }
}
