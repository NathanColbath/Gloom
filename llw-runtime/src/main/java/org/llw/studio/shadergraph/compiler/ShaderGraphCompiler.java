package org.llw.studio.shadergraph.compiler;

import org.llw.studio.shadergraph.model.ShaderGraphDocument;
import org.llw.studio.shadergraph.model.ShaderGraphLink;
import org.llw.studio.shadergraph.model.ShaderGraphNode;
import org.llw.studio.shadergraph.model.ShaderGraphPinCatalog;
import org.llw.studio.shadergraph.model.ShaderGraphPinCatalog.PinDef;
import org.llw.studio.shadergraph.model.ShaderGraphPinRef;
import org.llw.studio.shadergraph.model.ShaderNodeType;
import org.llw.studio.shadergraph.model.ShaderPinType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compiles a fragment shader graph to GLSL 330 for the default sprite vertex pass.
 */
public final class ShaderGraphCompiler {
    private ShaderGraphCompiler() {
    }

    /**
     * Compiles the full graph using the {@link ShaderNodeType#FragmentOutput} node.
     */
    public static ShaderGraphCompileResult compileFull(ShaderGraphDocument document) {
        ShaderGraphNode output = findFirst(document, ShaderNodeType.FragmentOutput);
        if (output == null) {
            return ShaderGraphCompileResult.error("Graph needs a Fragment Output node");
        }
        return compileToRoot(document, output.id, "color", true);
    }

    /**
     * Compiles a subgraph preview rooted at {@code targetNodeId}.
     */
    public static ShaderGraphCompileResult compilePreview(ShaderGraphDocument document, String targetNodeId) {
        if (targetNodeId == null || targetNodeId.isBlank()) {
            return ShaderGraphCompileResult.error("No preview node selected");
        }
        ShaderGraphNode node = document.nodeById(targetNodeId);
        if (node == null) {
            return ShaderGraphCompileResult.error("Unknown node: " + targetNodeId);
        }
        if (node.type == ShaderNodeType.FragmentOutput) {
            return compileFull(document);
        }
        ShaderPinType outType = ShaderGraphPinCatalog.outputType(node.type);
        if (outType == null) {
            return ShaderGraphCompileResult.error("Node has no preview output");
        }
        String outPin = ShaderGraphPinCatalog.outputPinId(node.type);
        return compileToRoot(document, targetNodeId, outPin, false);
    }

    private static ShaderGraphCompileResult compileToRoot(
            ShaderGraphDocument document,
            String rootNodeId,
            String rootPinId,
            boolean fragmentOutputMode
    ) {
        try {
            List<String> order = topologicalOrder(document, rootNodeId);
            StringBuilder body = new StringBuilder();
            Map<String, Map<String, String>> nodeOutputs = new LinkedHashMap<>();

            for (String nodeId : order) {
                ShaderGraphNode node = document.nodeById(nodeId);
                if (node == null) {
                    return ShaderGraphCompileResult.error("Missing node: " + nodeId);
                }
                emitNode(document, node, body, nodeOutputs);
            }

            ShaderGraphNode rootNode = document.nodeById(rootNodeId);
            String colorExpr;
            if (rootNode != null && rootNode.type == ShaderNodeType.FragmentOutput) {
                colorExpr = inputExpr(document, rootNodeId, rootPinId, ShaderPinType.VEC4, nodeOutputs);
            } else {
                ShaderPinType rootOut = ShaderGraphPinCatalog.outputType(rootNode.type);
                colorExpr = resolveOutputExpr(document, rootNodeId, rootPinId, nodeOutputs, ShaderPinType.VEC4);
                if (!fragmentOutputMode) {
                    colorExpr = promote(colorExpr, rootOut, ShaderPinType.VEC4);
                }
            }
            body.append("    fragColor = ").append(colorExpr).append(";\n");

            return ShaderGraphCompileResult.ok(ShaderGraphTemplates.wrapFragmentBody(body.toString()));
        } catch (CompileException e) {
            return ShaderGraphCompileResult.error(e.getMessage());
        }
    }

    private static void emitNode(
            ShaderGraphDocument document,
            ShaderGraphNode node,
            StringBuilder body,
            Map<String, Map<String, String>> nodeOutputs
    ) {
        String base = safeVar(node.id);
        Map<String, String> outs = new LinkedHashMap<>();
        nodeOutputs.put(node.id, outs);

        switch (node.type) {
            case TextureSample -> {
                body.append("    vec4 ").append(base).append(";\n");
                body.append("    if (uUseTexture == 1) {\n");
                body.append("        ").append(base).append(" = texture(uTexture, vTexCoord) * vColor;\n");
                body.append("    } else {\n");
                body.append("        ").append(base).append(" = vColor;\n");
                body.append("    }\n");
                outs.put("rgba", base);
            }
            case VertexColor -> {
                body.append("    vec4 ").append(base).append(" = vColor;\n");
                outs.put("rgba", base);
            }
            case Uv -> {
                body.append("    vec2 ").append(base).append(" = vTexCoord;\n");
                outs.put("uv", base);
            }
            case Color -> {
                float r = node.param("r", 1f);
                float g = node.param("g", 1f);
                float b = node.param("b", 1f);
                float a = node.param("a", 1f);
                body.append("    vec4 ").append(base).append(" = vec4(")
                        .append(r).append(", ").append(g).append(", ").append(b).append(", ").append(a).append(");\n");
                outs.put("rgba", base);
            }
            case FloatConstant -> {
                float v = node.param("value", 0f);
                body.append("    float ").append(base).append(" = ").append(v).append(";\n");
                outs.put("value", base);
            }
            case Time -> {
                body.append("    float ").append(base).append(" = uTime;\n");
                outs.put("value", base);
            }
            case Multiply -> {
                String a = inputExpr(document, node.id, "a", ShaderPinType.VEC4, nodeOutputs);
                String b = inputExpr(document, node.id, "b", ShaderPinType.VEC4, nodeOutputs);
                body.append("    vec4 ").append(base).append(" = ").append(a).append(" * ").append(b).append(";\n");
                outs.put("rgba", base);
            }
            case Add -> {
                String a = inputExpr(document, node.id, "a", ShaderPinType.VEC4, nodeOutputs);
                String b = inputExpr(document, node.id, "b", ShaderPinType.VEC4, nodeOutputs);
                body.append("    vec4 ").append(base).append(" = ").append(a).append(" + ").append(b).append(";\n");
                outs.put("rgba", base);
            }
            case Lerp -> {
                String a = inputExpr(document, node.id, "a", ShaderPinType.VEC4, nodeOutputs);
                String b = inputExpr(document, node.id, "b", ShaderPinType.VEC4, nodeOutputs);
                String t = inputExpr(document, node.id, "t", ShaderPinType.FLOAT, nodeOutputs);
                body.append("    vec4 ").append(base).append(" = mix(").append(a).append(", ").append(b).append(", ").append(t).append(");\n");
                outs.put("rgba", base);
            }
            case SplitRgba -> {
                String rgba = inputExpr(document, node.id, "rgba", ShaderPinType.VEC4, nodeOutputs);
                body.append("    float ").append(base).append("_r = ").append(rgba).append(".r;\n");
                body.append("    float ").append(base).append("_g = ").append(rgba).append(".g;\n");
                body.append("    float ").append(base).append("_b = ").append(rgba).append(".b;\n");
                body.append("    float ").append(base).append("_a = ").append(rgba).append(".a;\n");
                outs.put("r", base + "_r");
                outs.put("g", base + "_g");
                outs.put("b", base + "_b");
                outs.put("a", base + "_a");
            }
            case CombineRgba -> {
                String r = inputExpr(document, node.id, "r", ShaderPinType.FLOAT, nodeOutputs);
                String g = inputExpr(document, node.id, "g", ShaderPinType.FLOAT, nodeOutputs);
                String b = inputExpr(document, node.id, "b", ShaderPinType.FLOAT, nodeOutputs);
                String a = inputExpr(document, node.id, "a", ShaderPinType.FLOAT, nodeOutputs);
                body.append("    vec4 ").append(base).append(" = vec4(").append(r).append(", ").append(g).append(", ")
                        .append(b).append(", ").append(a).append(");\n");
                outs.put("rgba", base);
            }
            case FragmentOutput -> {
                // No locals; fragColor assigned after all nodes
            }
            default -> throw new CompileException("Unsupported node type: " + node.type);
        }
    }

    private static String inputExpr(
            ShaderGraphDocument document,
            String nodeId,
            String pinId,
            ShaderPinType expected,
            Map<String, Map<String, String>> nodeOutputs
    ) {
        ShaderGraphLink link = findLinkTo(document, nodeId, pinId);
        if (link == null) {
            return defaultLiteral(expected);
        }
        return resolveOutputExpr(document, link.from.nodeId, link.from.pinId, nodeOutputs, expected);
    }

    private static String resolveOutputExpr(
            ShaderGraphDocument document,
            String nodeId,
            String pinId,
            Map<String, Map<String, String>> nodeOutputs,
            ShaderPinType expected
    ) {
        Map<String, String> outs = nodeOutputs.get(nodeId);
        if (outs == null) {
            ShaderGraphNode node = document.nodeById(nodeId);
            if (node == null) {
                throw new CompileException("Missing node output: " + nodeId);
            }
            String defaultPin = ShaderGraphPinCatalog.outputPinId(node.type);
            pinId = pinId == null || pinId.isBlank() ? defaultPin : pinId;
            if (pinId == null) {
                throw new CompileException("Node has no output: " + nodeId);
            }
            throw new CompileException("Node not compiled before use: " + nodeId);
        }
        String expr = outs.get(pinId);
        if (expr == null) {
            ShaderGraphNode node = document.nodeById(nodeId);
            String fallback = ShaderGraphPinCatalog.outputPinId(node.type);
            expr = outs.get(fallback);
        }
        if (expr == null) {
            throw new CompileException("Unknown output pin " + pinId + " on " + nodeId);
        }
        ShaderGraphNode node = document.nodeById(nodeId);
        ShaderPinType sourceType = pinTypeForOutput(node, pinId);
        return promote(expr, sourceType, expected);
    }

    private static ShaderPinType pinTypeForOutput(ShaderGraphNode node, String pinId) {
        for (PinDef pin : ShaderGraphPinCatalog.outputPinsFor(node.type)) {
            if (pin.id().equals(pinId)) {
                return pin.type();
            }
        }
        return ShaderGraphPinCatalog.outputType(node.type);
    }

    private static String promote(String expr, ShaderPinType from, ShaderPinType to) {
        if (from == null || to == null || from == to) {
            return expr;
        }
        return switch (to) {
            case FLOAT -> switch (from) {
                case VEC2 -> "(" + expr + ").x";
                case VEC3 -> "(" + expr + ").x";
                case VEC4 -> "(" + expr + ").r";
                default -> expr;
            };
            case VEC2 -> switch (from) {
                case FLOAT -> "vec2(" + expr + ")";
                case VEC4 -> "(" + expr + ").xy";
                default -> expr;
            };
            case VEC3 -> switch (from) {
                case FLOAT -> "vec3(" + expr + ")";
                case VEC2 -> "vec3(" + expr + ", 0.0)";
                case VEC4 -> "(" + expr + ").xyz";
                default -> expr;
            };
            case VEC4 -> switch (from) {
                case FLOAT -> "vec4(" + expr + ")";
                case VEC2 -> "vec4(" + expr + ", 0.0, 1.0)";
                case VEC3 -> "vec4(" + expr + ", 1.0)";
                default -> expr;
            };
        };
    }

    private static String defaultLiteral(ShaderPinType type) {
        return switch (type) {
            case FLOAT -> "0.0";
            case VEC2 -> "vec2(0.0)";
            case VEC3 -> "vec3(0.0)";
            case VEC4 -> "vec4(0.0)";
        };
    }

    private static List<String> topologicalOrder(ShaderGraphDocument document, String rootNodeId) {
        List<String> order = new ArrayList<>();
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        visit(document, rootNodeId, order, visiting, visited);
        return order;
    }

    private static void visit(
            ShaderGraphDocument document,
            String nodeId,
            List<String> order,
            Set<String> visiting,
            Set<String> visited
    ) {
        if (visited.contains(nodeId)) {
            return;
        }
        if (visiting.contains(nodeId)) {
            throw new CompileException("Cycle detected in shader graph");
        }
        visiting.add(nodeId);
        ShaderGraphNode node = document.nodeById(nodeId);
        if (node != null) {
            for (PinDef pin : ShaderGraphPinCatalog.pinsFor(node.type)) {
                ShaderGraphLink link = findLinkTo(document, nodeId, pin.id());
                if (link != null) {
                    visit(document, link.from.nodeId, order, visiting, visited);
                }
            }
        }
        visiting.remove(nodeId);
        visited.add(nodeId);
        order.add(nodeId);
    }

    private static ShaderGraphLink findLinkTo(ShaderGraphDocument document, String nodeId, String pinId) {
        for (ShaderGraphLink link : document.links) {
            if (nodeId.equals(link.to.nodeId) && pinId.equals(link.to.pinId)) {
                return link;
            }
        }
        return null;
    }

    private static ShaderGraphNode findFirst(ShaderGraphDocument document, ShaderNodeType type) {
        for (ShaderGraphNode node : document.nodes) {
            if (node.type == type) {
                return node;
            }
        }
        return null;
    }

    private static String safeVar(String nodeId) {
        String safe = nodeId.replaceAll("[^a-zA-Z0-9_]", "_");
        if (safe.isEmpty() || Character.isDigit(safe.charAt(0))) {
            safe = "n_" + safe;
        }
        return "sg_" + safe;
    }

    private static final class CompileException extends RuntimeException {
        CompileException(String message) {
            super(message);
        }
    }
}
