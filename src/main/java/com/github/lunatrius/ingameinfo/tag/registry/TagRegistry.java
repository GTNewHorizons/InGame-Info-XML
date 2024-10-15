package com.github.lunatrius.ingameinfo.tag.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.lunatrius.ingameinfo.reference.Reference;
import com.github.lunatrius.ingameinfo.tag.Tag;
import com.github.lunatrius.ingameinfo.tag.TagFormatting;
import com.github.lunatrius.ingameinfo.tag.TagMisc;
import com.github.lunatrius.ingameinfo.tag.TagMouseOver;
import com.github.lunatrius.ingameinfo.tag.TagNearbyPlayer;
import com.github.lunatrius.ingameinfo.tag.TagPlayerEquipment;
import com.github.lunatrius.ingameinfo.tag.TagPlayerGeneral;
import com.github.lunatrius.ingameinfo.tag.TagPlayerPosition;
import com.github.lunatrius.ingameinfo.tag.TagPlayerPotion;
import com.github.lunatrius.ingameinfo.tag.TagRiding;
import com.github.lunatrius.ingameinfo.tag.TagTime;
import com.github.lunatrius.ingameinfo.tag.TagWorld;

public class TagRegistry {

    public static final TagRegistry INSTANCE = new TagRegistry();

    private final Map<String, Tag> stringTagMap = new HashMap<>();

    private void register(String name, Tag tag) {
        if (this.stringTagMap.containsKey(name)) {
            Reference.logger.error("Duplicate tag key '{}'!", name);
            return;
        }

        if (name == null) {
            Reference.logger.error("Tag name cannot be null!");
            return;
        }

        this.stringTagMap.put(name.toLowerCase(), tag);
    }

    public void register(Tag tag) {
        register(tag.getName(), tag);

        for (String name : tag.getAliases()) {
            register(name, tag);
        }
    }

    public String getValue(String name) {
        Tag tag = stringTagMap.get(name.toLowerCase());
        return tag != null ? tag.getValue() : null;
    }

    public Tag getTag(String name) {
        return stringTagMap.get(name.toLowerCase());
    }

    public List<Tag> getRegisteredTags() {
        return new ArrayList<>(stringTagMap.values());
    }

    public void init() {
        TagFormatting.register();
        TagMisc.register();
        TagMouseOver.register();
        TagNearbyPlayer.register();
        TagPlayerEquipment.register();
        TagPlayerGeneral.register();
        TagPlayerPosition.register();
        TagPlayerPotion.register();
        TagRiding.register();
        TagTime.register();
        TagWorld.register();

        Reference.logger.info("Registered {} tags.", stringTagMap.size());
    }
}
