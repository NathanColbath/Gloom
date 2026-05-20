package org.llw.studio.scripting.js.bindings;



import org.graalvm.polyglot.HostAccess;



/**

 * Play-mode {@link org.llw.studio.ecs.World} API: reference to a prefab asset (not a scene instance).

 */

public final class PrefabTemplateBinding {

    private final String prefabGuid;



    /**

     * @param prefabGuid prefab asset GUID

     */

    public PrefabTemplateBinding(String prefabGuid) {

        this.prefabGuid = prefabGuid == null ? "" : prefabGuid;

    }



    /**

     * @return prefab asset GUID

     */

    public String prefabGuid() {

        return prefabGuid;

    }



    /**

     * @return prefab asset GUID for JavaScript access

     */

    @HostAccess.Export

    public String getPrefabGuid() {

        return prefabGuid;

    }

}

