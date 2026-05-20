package org.llw.studio.editor.components;



import java.util.function.Supplier;



/**

 * Inspector and add-component metadata for one ECS component type.

 *

 * @param type                 component class

 * @param menuName             label in the add-component menu

 * @param category             menu grouping

 * @param addable              whether users may add this type from the inspector

 * @param hiddenInInspector    when true, drawer is omitted from the default inspector list

 * @param defaultFactory       creates default component data for new instances

 * @param drawer               ImGui property UI for the type

 */

public record ComponentTypeInfo(

        Class<?> type,

        String menuName,

        String category,

        boolean addable,

        boolean hiddenInInspector,

        Supplier<Object> defaultFactory,

        ComponentDrawer<?> drawer

) {

}

