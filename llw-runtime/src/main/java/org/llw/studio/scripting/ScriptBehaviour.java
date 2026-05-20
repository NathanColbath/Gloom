package org.llw.studio.scripting;



/**

 * Base class for legacy Java scripts compiled into a project-specific class loader.

 */

public abstract class ScriptBehaviour {

    /** Runtime context for the owning entity, set by {@link #bind(ScriptContext)}. */

    protected ScriptContext context;



    /**

     * @param context runtime context for the script's entity

     */

    public void bind(ScriptContext context) {

        this.context = context;

    }



    /** Called once when the script starts. */

    public void onStart() {

    }



    /**

     * @param deltaTime elapsed seconds since the previous frame

     */

    public void onUpdate(float deltaTime) {

    }

}

