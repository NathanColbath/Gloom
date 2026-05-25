package org.llw.studio.editor.commands;

import org.llw.studio.animation.AnimationClip;
import org.llw.studio.animation.AnimationTrack;

/**
 * Undo snapshot for animation clip keyframe edits.
 */
public final class AnimationClipEditCommand implements EditorCommand {
    private final AnimationClip target;
    private final AnimationClip before;
    private final AnimationClip after;

    public AnimationClipEditCommand(AnimationClip target, AnimationClip before, AnimationClip after) {
        this.target = target;
        this.before = before.copy();
        this.after = after.copy(); // Isolate undo stacks from the shared editor working clip.
    }

    @Override
    public void execute() {
        restore(target, after);
    }

    @Override
    public void undo() {
        restore(target, before);
    }

    private static void restore(AnimationClip target, AnimationClip source) {
        target.version = source.version;
        target.length = source.length;
        target.frameRate = source.frameRate;
        target.loop = source.loop;
        target.tracks.clear();
        // Keyframe edits can resort tracks or extend clip length — restore the full clip snapshot.
        for (AnimationTrack track : source.tracks) {
            target.tracks.add(track.copy());
        }
    }
}
