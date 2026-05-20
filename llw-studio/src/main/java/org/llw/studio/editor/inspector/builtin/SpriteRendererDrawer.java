package org.llw.studio.editor.inspector.builtin;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.SpriteDefinition;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.panels.ShaderGraphPanel;
import org.llw.studio.editor.widgets.fields.ShaderGraphReferenceField;
import org.llw.studio.editor.widgets.fields.SpriteReferenceField;
import org.llw.studio.editor.widgets.fields.ColorField;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.ecs.components.SpriteRendererComponent;

/** Inspector fields for {@link SpriteRendererComponent} (texture, sorting order, tint). */
public final class SpriteRendererDrawer implements ComponentDrawer<SpriteRendererComponent> {
  @Override
  public void draw(SpriteRendererComponent component, InspectorContext context) {
    component.spriteGuid = SpriteReferenceField.draw("Sprite", component.spriteGuid, context.assets());
    syncTextureGuid(component, context.assets());
    ShaderGraphPanel shaderGraphPanel = context.editorSession().shaderGraphPanel();
    component.shaderGraphGuid = ShaderGraphReferenceField.draw(
            "Shader",
            component.shaderGraphGuid,
            context.assets(),
            shaderGraphPanel
    );
    component.sortingOrder = (int) FloatField.draw("Sorting Order", component.sortingOrder);
    float[] tint = ColorField.draw("Color", component.r, component.g, component.b, component.a);
    component.r = tint[0];
    component.g = tint[1];
    component.b = tint[2];
    component.a = tint[3];
    context.markDirty();
  }

  private static void syncTextureGuid(SpriteRendererComponent component, AssetDatabase assets) {
    if (component.spriteGuid == null || component.spriteGuid.isBlank()) {
      component.textureGuid = "";
      return;
    }
    SpriteDefinition slice = assets.sprite(component.spriteGuid);
    if (slice != null) {
      component.textureGuid = slice.textureGuid();
    }
  }
}
