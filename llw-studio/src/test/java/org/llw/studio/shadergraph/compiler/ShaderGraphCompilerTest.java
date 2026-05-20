package org.llw.studio.shadergraph.compiler;

import org.junit.jupiter.api.Test;
import org.llw.studio.shadergraph.assets.ShaderGraphSerializer;
import org.llw.studio.shadergraph.model.ShaderGraphDocument;
import org.llw.studio.shadergraph.model.ShaderGraphLink;
import org.llw.studio.shadergraph.model.ShaderGraphNode;
import org.llw.studio.shadergraph.model.ShaderGraphPinRef;
import org.llw.studio.shadergraph.model.ShaderNodeType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShaderGraphCompilerTest {

    @Test
    void compilesDefaultTextureToOutput() {
        ShaderGraphCompileResult result = ShaderGraphCompiler.compileFull(ShaderGraphSerializer.newDefaultGraph());
        assertTrue(result.success(), result.errorMessage());
        String frag = result.fragmentSource();
        assertTrue(frag.contains("texture(uTexture, vTexCoord)"));
        assertTrue(frag.contains("void main()"));
        assertTrue(frag.contains("fragColor ="));
        assertTrue(frag.contains("sg_tex"));
    }

    @Test
    void compilesMultiplyChain() {
        ShaderGraphDocument document = new ShaderGraphDocument();
        ShaderGraphNode color = node("c", ShaderNodeType.Color, 0, 0);
        color.params.put("r", 1f);
        color.params.put("g", 0.5f);
        color.params.put("b", 0.25f);
        color.params.put("a", 1f);
        document.nodes.add(color);

        ShaderGraphNode mul = node("m", ShaderNodeType.Multiply, 200, 0);
        document.nodes.add(mul);

        ShaderGraphNode output = node("out", ShaderNodeType.FragmentOutput, 400, 0);
        document.nodes.add(output);

        document.links.add(link("c", "rgba", "m", "a"));
        document.links.add(link("c", "rgba", "m", "b"));
        document.links.add(link("m", "rgba", "out", "color"));

        ShaderGraphCompileResult result = ShaderGraphCompiler.compileFull(document);
        assertTrue(result.success(), result.errorMessage());
        assertTrue(result.fragmentSource().contains("sg_m ="));
    }

    @Test
    void detectsCycle() {
        ShaderGraphDocument document = new ShaderGraphDocument();
        document.nodes.add(node("a", ShaderNodeType.Multiply, 0, 0));
        document.nodes.add(node("b", ShaderNodeType.Multiply, 200, 0));
        document.nodes.add(node("out", ShaderNodeType.FragmentOutput, 400, 0));
        document.links.add(link("a", "rgba", "b", "a"));
        document.links.add(link("b", "rgba", "a", "a"));
        document.links.add(link("a", "rgba", "out", "color"));

        ShaderGraphCompileResult result = ShaderGraphCompiler.compileFull(document);
        assertFalse(result.success());
        assertTrue(result.errorMessage().toLowerCase().contains("cycle"));
    }

    @Test
    void previewSubgraphFromTextureNode() {
        ShaderGraphCompileResult result = ShaderGraphCompiler.compilePreview(
                ShaderGraphSerializer.newDefaultGraph(),
                "tex"
        );
        assertTrue(result.success(), result.errorMessage());
        assertTrue(result.fragmentSource().contains("fragColor ="));
    }

    @Test
    void requiresFragmentOutputForFullCompile() {
        ShaderGraphDocument document = new ShaderGraphDocument();
        document.nodes.add(node("tex", ShaderNodeType.TextureSample, 0, 0));
        ShaderGraphCompileResult result = ShaderGraphCompiler.compileFull(document);
        assertFalse(result.success());
    }

    private static ShaderGraphNode node(String id, ShaderNodeType type, float x, float y) {
        ShaderGraphNode node = new ShaderGraphNode();
        node.id = id;
        node.type = type;
        node.x = x;
        node.y = y;
        return node;
    }

    private static ShaderGraphLink link(String fromNode, String fromPin, String toNode, String toPin) {
        ShaderGraphLink link = new ShaderGraphLink();
        link.from = new ShaderGraphPinRef(fromNode, fromPin);
        link.to = new ShaderGraphPinRef(toNode, toPin);
        return link;
    }
}
