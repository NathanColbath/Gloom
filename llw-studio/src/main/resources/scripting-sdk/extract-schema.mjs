#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import ts from "typescript";

const [sourcePath, outputPath] = process.argv.slice(2);
if (!sourcePath || !outputPath) {
  console.error("Usage: extract-schema.mjs <source.ts> <output.json>");
  process.exit(1);
}

const source = fs.readFileSync(sourcePath, "utf8");
const sourceFile = ts.createSourceFile(
  sourcePath,
  source,
  ts.ScriptTarget.ES2020,
  true,
  sourcePath.endsWith(".tsx") ? ts.ScriptKind.TSX : ts.ScriptKind.TS
);

const EXCLUDED = new Set(["entity", "transform", "enabled"]);
const fields = [];
let hasDrawGizmos = false;
let hasDrawGizmosSelected = false;

const VECTOR2_TYPE_NAMES = new Set(["Vec2", "Vector2", "Vector2f"]);

function isVec2Type(typeNode) {
  if (!typeNode) {
    return false;
  }
  if (ts.isTypeReferenceNode(typeNode)) {
    const name = typeNode.typeName.getText(sourceFile);
    return VECTOR2_TYPE_NAMES.has(name);
  }
  return false;
}

function vector2CalleeName(expression) {
  if (ts.isIdentifier(expression)) {
    return expression.text;
  }
  if (ts.isPropertyAccessExpression(expression)) {
    return expression.name.text;
  }
  return null;
}

function isVec2Create(initializer) {
  if (!initializer || !ts.isCallExpression(initializer)) {
    return false;
  }
  const expr = initializer.expression;
  if (!ts.isPropertyAccessExpression(expr)) {
    return false;
  }
  return VECTOR2_TYPE_NAMES.has(expr.expression.getText(sourceFile)) && expr.name.text === "create";
}

function isNewVector2(initializer) {
  if (!initializer || !ts.isNewExpression(initializer)) {
    return false;
  }
  const name = vector2CalleeName(initializer.expression);
  if (name && VECTOR2_TYPE_NAMES.has(name)) {
    return true;
  }
  if (ts.isPropertyAccessExpression(initializer.expression)) {
    const root = initializer.expression.expression.getText(sourceFile);
    return root === "core" && VECTOR2_TYPE_NAMES.has(initializer.expression.name.text);
  }
  return false;
}

function vec2LiteralFromCall(initializer) {
  const args = initializer.arguments ?? [];
  const x =
    args.length > 0 && ts.isNumericLiteral(args[0]) ? Number(args[0].text) : 0;
  const y =
    args.length > 1 && ts.isNumericLiteral(args[1]) ? Number(args[1].text) : 0;
  return { x, y };
}

function isEntityType(typeNode) {
  if (!typeNode) {
    return false;
  }
  if (ts.isUnionTypeNode(typeNode)) {
    return typeNode.types.some((t) => isEntityType(t));
  }
  if (ts.isTypeReferenceNode(typeNode)) {
    const name = typeNode.typeName.getText(sourceFile);
    return name === "Entity";
  }
  return false;
}

function fieldType(typeNode, initializer) {
  if (isEntityType(typeNode)) {
    return "entity";
  }
  if (
    isVec2Type(typeNode) ||
    (initializer && (isVec2Create(initializer) || isNewVector2(initializer)))
  ) {
    return "vector2";
  }
  if (typeNode) {
    const text = typeNode.getText(sourceFile);
    if (text === "number") return "number";
    if (text === "boolean") return "boolean";
    if (text === "string") return "string";
  }
  if (initializer) {
    if (ts.isNumericLiteral(initializer)) return "number";
    if (initializer.kind === ts.SyntaxKind.TrueKeyword || initializer.kind === ts.SyntaxKind.FalseKeyword) {
      return "boolean";
    }
    if (ts.isStringLiteral(initializer) || ts.isNoSubstitutionTemplateLiteral(initializer)) {
      return "string";
    }
    if (initializer.kind === ts.SyntaxKind.NullKeyword) {
      return "entity";
    }
    if (ts.isPropertyAccessExpression(initializer)) {
      return "number";
    }
    if (ts.isPrefixUnaryExpression(initializer) && ts.isNumericLiteral(initializer.operand)) {
      return "number";
    }
  }
  return "string";
}

function defaultValue(type, initializer) {
  if (type === "vector2") {
    if (initializer && isVec2Create(initializer)) {
      return vec2LiteralFromCall(initializer);
    }
    if (initializer && isNewVector2(initializer)) {
      return vec2LiteralFromCall(initializer);
    }
    return { x: 0, y: 0 };
  }
  if (initializer) {
    if (ts.isNumericLiteral(initializer)) {
      return Number(initializer.text);
    }
    if (initializer.kind === ts.SyntaxKind.TrueKeyword) return true;
    if (initializer.kind === ts.SyntaxKind.FalseKeyword) return false;
    if (ts.isStringLiteral(initializer) || ts.isNoSubstitutionTemplateLiteral(initializer)) {
      return initializer.text;
    }
    if (initializer.kind === ts.SyntaxKind.NullKeyword) {
      return null;
    }
  }
  switch (type) {
    case "number":
      return 0;
    case "boolean":
      return false;
    case "entity":
      return null;
    default:
      return "";
  }
}

function isInspectorField(member) {
  if (!ts.isPropertyDeclaration(member) || !member.name) {
    return false;
  }
  // ECMAScript private fields (#name) are never inspector-visible.
  if (ts.isPrivateIdentifier(member.name)) {
    return false;
  }
  if (!ts.isIdentifier(member.name)) {
    return false;
  }
  if (EXCLUDED.has(member.name.text)) {
    return false;
  }
  const modifiers = ts.getModifiers(member) ?? [];
  for (const modifier of modifiers) {
    switch (modifier.kind) {
      case ts.SyntaxKind.PrivateKeyword:
      case ts.SyntaxKind.ProtectedKeyword:
      case ts.SyntaxKind.StaticKeyword:
      case ts.SyntaxKind.AbstractKeyword:
        return false;
      default:
        break;
    }
  }
  return true;
}

function visitClass(node) {
  for (const member of node.members) {
    if (ts.isMethodDeclaration(member) && member.name && ts.isIdentifier(member.name)) {
      const name = member.name.text;
      if (name === "onDrawGizmos") {
        hasDrawGizmos = true;
      }
      if (name === "onDrawGizmosSelected") {
        hasDrawGizmosSelected = true;
      }
    }
    if (!isInspectorField(member)) {
      continue;
    }
    const name = member.name.text;
    const type = fieldType(member.type, member.initializer);
    fields.push({
      name,
      type,
      default: defaultValue(type, member.initializer),
    });
  }
}

function visit(node) {
  if (ts.isClassDeclaration(node) && node.modifiers?.some((m) => m.kind === ts.SyntaxKind.DefaultKeyword)) {
    visitClass(node);
    return;
  }
  if (ts.isExportAssignment(node) && ts.isClassExpression(node.expression)) {
    visitClass(node.expression);
    return;
  }
  ts.forEachChild(node, visit);
}

visit(sourceFile);

const schema = { fields, hasDrawGizmos, hasDrawGizmosSelected };
fs.mkdirSync(path.dirname(outputPath), { recursive: true });
fs.writeFileSync(outputPath, JSON.stringify(schema, null, 2));
