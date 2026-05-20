package org.llw.studio.editor.commands;



/**

 * Undoable editor mutation applied to the edit scene.

 */

public interface EditorCommand {

    /** Applies the change. */

    void execute();



    /** Reverts the change. */

    void undo();

}

