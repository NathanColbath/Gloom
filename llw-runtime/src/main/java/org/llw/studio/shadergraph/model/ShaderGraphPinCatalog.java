package org.llw.studio.shadergraph.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Static pin layout per {@link ShaderNodeType} for UI and codegen.
 */
public final class ShaderGraphPinCatalog {
    public record PinDef(String id, String label, ShaderPinType type, boolean input) {
    }

    private ShaderGraphPinCatalog() {
    }

    public static List<PinDef> pinsFor(ShaderNodeType type) {
        return switch (type) {
            case FragmentOutput -> List.of(
                    new PinDef("color", "Color", ShaderPinType.VEC4, true)
            );
            case TextureSample, VertexColor, Uv, Time -> List.of();
            case Color -> List.of();
            case FloatConstant -> List.of();
            case Multiply, Add -> List.of(
                    new PinDef("a", "A", ShaderPinType.VEC4, true),
                    new PinDef("b", "B", ShaderPinType.VEC4, true)
            );
            case Lerp -> List.of(
                    new PinDef("a", "A", ShaderPinType.VEC4, true),
                    new PinDef("b", "B", ShaderPinType.VEC4, true),
                    new PinDef("t", "T", ShaderPinType.FLOAT, true)
            );
            case SplitRgba -> List.of(
                    new PinDef("rgba", "RGBA", ShaderPinType.VEC4, true)
            );
            case CombineRgba -> List.of(
                    new PinDef("r", "R", ShaderPinType.FLOAT, true),
                    new PinDef("g", "G", ShaderPinType.FLOAT, true),
                    new PinDef("b", "B", ShaderPinType.FLOAT, true),
                    new PinDef("a", "A", ShaderPinType.FLOAT, true)
            );
        };
    }

    public static ShaderPinType outputType(ShaderNodeType type) {
        return switch (type) {
            case Uv -> ShaderPinType.VEC2;
            case FloatConstant, Time -> ShaderPinType.FLOAT;
            case SplitRgba -> ShaderPinType.FLOAT;
            case FragmentOutput -> null;
            default -> ShaderPinType.VEC4;
        };
    }

    public static String outputPinId(ShaderNodeType type) {
        return switch (type) {
            case Uv -> "uv";
            case FloatConstant, Time -> "value";
            case SplitRgba -> "r";
            case FragmentOutput -> null;
            default -> "rgba";
        };
    }

    public static List<PinDef> outputPinsFor(ShaderNodeType type) {
        if (type == ShaderNodeType.FragmentOutput) {
            return List.of();
        }
        if (type == ShaderNodeType.SplitRgba) {
            return List.of(
                    new PinDef("r", "R", ShaderPinType.FLOAT, false),
                    new PinDef("g", "G", ShaderPinType.FLOAT, false),
                    new PinDef("b", "B", ShaderPinType.FLOAT, false),
                    new PinDef("a", "A", ShaderPinType.FLOAT, false)
            );
        }
        String outId = outputPinId(type);
        ShaderPinType outType = outputType(type);
        if (outId == null) {
            return List.of();
        }
        return List.of(new PinDef(outId, "Out", outType, false));
    }

    public static List<PinDef> allPinsFor(ShaderNodeType type) {
        List<PinDef> all = new ArrayList<>(pinsFor(type));
        all.addAll(outputPinsFor(type));
        return all;
    }
}
