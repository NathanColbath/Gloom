package org.llw.studio.scripting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScriptSchemaTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void parsesFieldDefinitions() throws Exception {
    String json = """
        {
          "fields": [
            { "name": "speed", "type": "number", "default": 5 },
            { "name": "label", "type": "string", "default": "Player" },
            { "name": "target", "type": "entity", "default": null }
          ]
        }
        """;
    ScriptSchema schema = ScriptSchema.fromJson(MAPPER.readTree(json));
    assertEquals(3, schema.fields.size());
    assertEquals("speed", schema.fields.get(0).name);
    assertEquals("number", schema.fields.get(0).type);
    assertEquals(5, schema.fields.get(0).defaultValue.asInt());
    assertEquals("entity", schema.fields.get(2).type);
    assertTrue(schema.fields.get(2).defaultValue.isNull());
  }
}
