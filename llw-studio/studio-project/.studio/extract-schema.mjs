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
  }
  return "string";
}

function defaultValue(type, initializer) {
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

function visitClass(node) {
  for (const member of node.members) {
    if (!ts.isPropertyDeclaration(member) || !member.name || !ts.isIdentifier(member.name)) {
      continue;
    }
    const name = member.name.text;
    if (EXCLUDED.has(name)) {
      continue;
    }
    const modifiers = ts.getModifiers(member);
    const isPrivate =
      modifiers?.some((m) => m.kind === ts.SyntaxKind.PrivateKeyword || m.kind === ts.SyntaxKind.ProtectedKeyword) ??
      false;
    if (isPrivate) {
      continue;
    }
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

const schema = { fields };
fs.mkdirSync(path.dirname(outputPath), { recursive: true });
fs.writeFileSync(outputPath, JSON.stringify(schema, null, 2));
