package org.llw.studio.build;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Assets grouped by output pack category after a build scan.
 */
public final class BuildAssetSet {
    private final Set<String> referencedGuids;
    private final Map<BuildPackCategory, List<StudioAsset>> byCategory;
    private final Map<String, StudioAsset> supplementalByGuid;
    private final List<String> scanLog;

    public BuildAssetSet(
            Set<String> referencedGuids,
            Map<BuildPackCategory, List<StudioAsset>> byCategory,
            Map<String, StudioAsset> supplementalByGuid,
            List<String> scanLog
    ) {
        this.referencedGuids = Collections.unmodifiableSet(new LinkedHashSet<>(referencedGuids));
        this.byCategory = Collections.unmodifiableMap(new EnumMap<>(byCategory));
        this.supplementalByGuid = Collections.unmodifiableMap(new LinkedHashMap<>(supplementalByGuid));
        this.scanLog = List.copyOf(scanLog);
    }

    public Set<String> referencedGuids() {
        return referencedGuids;
    }

    public List<StudioAsset> assets(BuildPackCategory category) {
        return byCategory.getOrDefault(category, List.of());
    }

    public Map<BuildPackCategory, List<StudioAsset>> byCategory() {
        return byCategory;
    }

    public List<String> scanLog() {
        return scanLog;
    }

    public Set<String> scriptGuids() {
        Set<String> guids = new LinkedHashSet<>();
        for (StudioAsset asset : assets(BuildPackCategory.SCRIPTS)) {
            guids.add(asset.guid());
        }
        return guids;
    }

    /**
     * @param guid asset GUID
     * @param assets live project database
     * @return indexed or supplemental asset, or {@code null}
     */
    public StudioAsset resolve(String guid, AssetDatabase assets) {
        if (guid == null || guid.isBlank()) {
            return null;
        }
        StudioAsset supplemental = supplementalByGuid.get(guid);
        if (supplemental != null) {
            return supplemental;
        }
        return assets == null ? null : assets.get(guid);
    }

    public static BuildAssetSet empty() {
        return new BuildAssetSet(Set.of(), new EnumMap<>(BuildPackCategory.class), Map.of(), List.of());
    }
}
