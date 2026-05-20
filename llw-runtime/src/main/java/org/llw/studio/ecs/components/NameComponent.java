package org.llw.studio.ecs.components;



/**

 * Identity metadata for a game object: hierarchy name and shared gameplay tag.

 * <p>

 * Accessed via {@link org.llw.studio.scene.GameObject#name()}, {@link org.llw.studio.scene.GameObject#tag()},

 * and related APIs—not through the add-component catalog. Blank names default to {@code "GameObject"};

 * blank tags are stored as {@code ""}.

 *

 * @param name hierarchy label for the owning entity

 * @param tag  shared gameplay label; many entities may use the same tag

 */

public record NameComponent(String name, String tag) {

    /**

     * @param name proposed display name; tag defaults to empty

     */

    public NameComponent(String name) {

        this(name, "");

    }



    /**

     * Normalizes blank names to {@code "GameObject"} and null tags to {@code ""}.

     */

    public NameComponent {

        if (name == null || name.isBlank()) {

            name = "GameObject";

        }

        if (tag == null) {

            tag = "";

        }

    }



    /**

     * @return copy with the same normalized name and tag

     */

    public NameComponent copy() {

        return new NameComponent(name, tag);

    }

}

