package org.llw.studio.assets;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule tile: ordered neighbor rules plus a default variant sprite.
 */
public final class RuleTileDefinition {
    /** Logical tile / fallback variant (usually the painted tile's sprite GUID). */
    public String defaultSpriteGuid = "";
    public final List<RuleTileRule> rules = new ArrayList<>();

    public RuleTileDefinition copy() {
        RuleTileDefinition copy = new RuleTileDefinition();
        copy.defaultSpriteGuid = defaultSpriteGuid;
        for (RuleTileRule rule : rules) {
            copy.rules.add(new RuleTileRule(rule.neighbors.copy(), rule.spriteGuid));
        }
        return copy;
    }

    /** @return {@code true} when this tile uses rule-based auto tiling */
    public boolean isActive() {
        return !rules.isEmpty() || (defaultSpriteGuid != null && !defaultSpriteGuid.isBlank());
    }
}
