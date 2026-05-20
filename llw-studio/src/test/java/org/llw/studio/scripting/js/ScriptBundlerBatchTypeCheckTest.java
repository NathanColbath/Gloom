package org.llw.studio.scripting.js;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScriptBundlerBatchTypeCheckTest {
    @TempDir
    Path projectRoot;

    @BeforeEach
    void resetCounters() {
        ScriptBundler.resetTypeCheckInvocationCount();
    }

    @Test
    void bundleAllRunsTypeCheckOnceForMultipleStaleTypeScriptSources() throws Exception {
        Path scriptsDir = projectRoot.resolve("Assets/Scripts");
        Files.createDirectories(scriptsDir);
        Files.writeString(projectRoot.resolve("tsconfig.json"), """
                {
                  "compilerOptions": {
                    "target": "ES2020",
                    "module": "ES2020",
                    "strict": false,
                    "skipLibCheck": true
                  },
                  "include": ["Assets/Scripts/**/*.ts"]
                }
                """);

        Path first = scriptsDir.resolve("First.ts");
        Path second = scriptsDir.resolve("Second.ts");
        String template = """
                import { Script } from "@llw/studio";
                export default class %s extends Script {
                  update() {}
                }
                """;
        Files.writeString(first, template.formatted("First"));
        Files.writeString(second, template.formatted("Second"));

        List<ScriptBundler.ScriptSource> sources = List.of(
                new ScriptBundler.ScriptSource("guid-1", first),
                new ScriptBundler.ScriptSource("guid-2", second)
        );

        ScriptBundler.bundleAll(projectRoot, sources, null);

        assertEquals(1, ScriptBundler.typeCheckInvocationCount());
    }
}
