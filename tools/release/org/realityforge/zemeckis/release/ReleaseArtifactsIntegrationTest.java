package org.realityforge.zemeckis.release;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public final class ReleaseArtifactsIntegrationTest {
    private static final String PACKAGE_PATH = "zemeckis/";
    private static final Pattern DEPENDENCY =
            Pattern.compile("<dependency>\\s*<groupId>([^<]+)</groupId>\\s*<artifactId>([^<]+)</artifactId>"
                    + "\\s*<version>([^<]+)</version>\\s*</dependency>");

    private ReleaseArtifactsIntegrationTest() {}

    public static void main(final String[] args) throws Exception {
        if (args.length != 5) {
            throw new IllegalArgumentException("Expected version file and four Maven artifact paths");
        }
        final String version = Files.readString(resolve(Path.of(args[0])), StandardCharsets.UTF_8)
                .trim();
        final Path mainJar = resolve(Path.of(args[1]));
        assertJarEntries(mainJar, mainEntries());
        assertJava17Bytecode(mainJar, PACKAGE_PATH + "Zemeckis.class");
        assertJarEntries(resolve(Path.of(args[2])), sourceEntries());
        assertJarContains(resolve(Path.of(args[3])), PACKAGE_PATH + "Zemeckis.html");
        assertPom(
                resolve(Path.of(args[4])),
                version,
                Set.of(
                        "org.jspecify:jspecify:1.0.0",
                        "com.google.jsinterop:jsinterop-annotations:2.0.0",
                        "com.google.jsinterop:base:1.0.0",
                        "org.realityforge.org.jetbrains.annotations:org.jetbrains.annotations:1.7.0",
                        "org.realityforge.akasha:akasha-gwt:0.34",
                        "org.realityforge.braincheck:braincheck-core:1.35.0",
                        "org.realityforge.grim:grim-annotations:0.09"));
    }

    private static Set<String> mainEntries() {
        final var entries = new LinkedHashSet<>(sourceEntries());
        entries.add("META-INF/LICENSE");
        for (final String grim : List.of(
                "TaskEntry",
                "TemporalScheduler$AbstractScheduler",
                "VirtualProcessorUnit",
                "Zemeckis$IdContainer",
                "Zemeckis",
                "ZemeckisConfig",
                "ZemeckisLogger$AbstractConsoleLogger",
                "ZemeckisLogger$ConsoleLogger",
                "ZemeckisLogger$NativeJsLoggerUtil",
                "ZemeckisLogger$NoopLogger",
                "ZemeckisLogger$ProxyLogger",
                "ZemeckisLogger",
                "ZemeckisUtil")) {
            entries.add("META-INF/grim/zemeckis/" + grim + ".grim.json");
        }
        for (final String type : List.of(
                "AbstractExecutor",
                "AfterFrameExecutor",
                "AnimationFrameExecutor",
                "Cancelable",
                "CircularBuffer",
                "DeadlineBasedExecutor$DeadlineFunction",
                "DeadlineBasedExecutor",
                "GwtIncompatible",
                "MacroTaskExecutor",
                "MicroTaskExecutor",
                "OnIdleExecutor",
                "RoundBasedExecutor",
                "TaskEntry",
                "TemporalScheduler$AbstractScheduler",
                "TemporalScheduler$ScheduledTask",
                "TemporalScheduler$SchedulerImpl",
                "TemporalScheduler",
                "UncaughtErrorHandler",
                "UncaughtErrorHandlerSupport",
                "VirtualProcessorUnit$ActivationFn",
                "VirtualProcessorUnit$Context",
                "VirtualProcessorUnit$Executor",
                "VirtualProcessorUnit",
                "VirtualProcessorUnitsHolder$AfterFrameVPU",
                "VirtualProcessorUnitsHolder$AnimationFrameVPU",
                "VirtualProcessorUnitsHolder$CurrentVPU",
                "VirtualProcessorUnitsHolder$MacroTaskVPU",
                "VirtualProcessorUnitsHolder$MicroTaskVPU",
                "VirtualProcessorUnitsHolder$OnIdleVPU",
                "VirtualProcessorUnitsHolder",
                "Zemeckis$IdContainer",
                "Zemeckis",
                "ZemeckisConfig$AbstractConfigProvider",
                "ZemeckisConfig$ConfigProvider",
                "ZemeckisConfig",
                "ZemeckisLogger$AbstractConsoleLogger",
                "ZemeckisLogger$ConsoleLogger",
                "ZemeckisLogger$Logger",
                "ZemeckisLogger$NativeJsLoggerUtil",
                "ZemeckisLogger$NoopLogger",
                "ZemeckisLogger$ProxyLogger",
                "ZemeckisLogger",
                "ZemeckisTestUtil$Logger",
                "ZemeckisTestUtil",
                "ZemeckisUtil",
                "package-info")) {
            entries.add(PACKAGE_PATH + type + ".class");
        }
        return entries;
    }

    private static Set<String> sourceEntries() {
        final var entries = new LinkedHashSet<String>();
        entries.add("META-INF/MANIFEST.MF");
        for (final String source : List.of(
                "AbstractExecutor.java",
                "AfterFrameExecutor.java",
                "AnimationFrameExecutor.java",
                "Cancelable.java",
                "CircularBuffer.java",
                "DeadlineBasedExecutor.java",
                "GwtIncompatible.java",
                "MacroTaskExecutor.java",
                "MicroTaskExecutor.java",
                "OnIdleExecutor.java",
                "RoundBasedExecutor.java",
                "TaskEntry.java",
                "TemporalScheduler.java",
                "UncaughtErrorHandler.java",
                "UncaughtErrorHandlerSupport.java",
                "VirtualProcessorUnit.java",
                "VirtualProcessorUnitsHolder.java",
                "Zemeckis.java",
                "ZemeckisConfig.java",
                "ZemeckisLogger.java",
                "ZemeckisTestUtil.java",
                "ZemeckisUtil.java",
                "package-info.java",
                "Zemeckis.gwt.xml",
                "ZemeckisConfig.native.js",
                "ZemeckisDev.gwt.xml",
                "zemeckis.js")) {
            entries.add(PACKAGE_PATH + source);
        }
        return entries;
    }

    private static void assertJava17Bytecode(final Path path, final String entry) throws IOException {
        try (JarFile jar = new JarFile(path.toFile());
                InputStream input = jar.getInputStream(jar.getJarEntry(entry))) {
            final byte[] header = input.readNBytes(8);
            final int major = (Byte.toUnsignedInt(header[6]) << 8) | Byte.toUnsignedInt(header[7]);
            if (major != 61) {
                throw new AssertionError("Expected Java 17 major version 61 for " + entry + ", got " + major);
            }
        }
    }

    private static void assertJarEntries(final Path path, final Set<String> expected) throws IOException {
        try (JarFile jar = new JarFile(path.toFile())) {
            final var names = new ArrayList<String>();
            Collections.list(jar.entries()).stream()
                    .filter(entry -> !entry.isDirectory())
                    .map(entry -> entry.getName())
                    .forEach(names::add);
            final Set<String> actual = new LinkedHashSet<>(names);
            if (!actual.equals(expected)) {
                throw new AssertionError(
                        "Unexpected entries in " + path + "\nExpected: " + expected + "\nActual: " + actual);
            }
            final String manifestVersion = jar.getManifest().getMainAttributes().getValue("Manifest-Version");
            if (!"1.0".equals(manifestVersion)) {
                throw new AssertionError("Unexpected manifest version in " + path + ": " + manifestVersion);
            }
        }
    }

    private static void assertJarContains(final Path path, final String entry) throws IOException {
        try (JarFile jar = new JarFile(path.toFile())) {
            if (jar.getJarEntry(entry) == null) {
                throw new AssertionError("Missing " + entry + " from " + path);
            }
        }
    }

    private static void assertPom(final Path path, final String version, final Set<String> expectedDependencies)
            throws IOException {
        final String pom = Files.readString(path, StandardCharsets.UTF_8);
        assertContains(pom, "<groupId>org.realityforge.zemeckis</groupId>", path);
        assertContains(pom, "<artifactId>zemeckis-core</artifactId>", path);
        assertContains(pom, "<version>" + version + "</version>", path);
        assertContains(pom, "<name>Zemeckis Core Library</name>", path);
        assertContains(pom, "<description>Zemeckis Core Library</description>", path);
        assertContains(pom, "<url>https://github.com/realityforge/zemeckis</url>", path);
        assertContains(pom, "<id>realityforge</id>", path);
        if (pom.contains("<scope>") || pom.contains("<optional>")) {
            throw new AssertionError("POM dependencies must use default compile scope and be non-optional: " + path);
        }
        final var dependencies = new LinkedHashSet<String>();
        final var matcher = DEPENDENCY.matcher(pom);
        while (matcher.find()) {
            dependencies.add(matcher.group(1) + ":" + matcher.group(2) + ":" + matcher.group(3));
        }
        if (!dependencies.equals(expectedDependencies)) {
            throw new AssertionError("Unexpected dependencies in " + path + "\nExpected: " + expectedDependencies
                    + "\nActual: " + dependencies);
        }
    }

    private static void assertContains(final String actual, final String expected, final Path path) {
        if (!actual.contains(expected)) {
            throw new AssertionError("Missing expected POM content in " + path + ": " + expected);
        }
    }

    private static Path resolve(final Path path) {
        if (Files.exists(path)) {
            return path.toAbsolutePath().normalize();
        }
        for (final String env : List.of("RUNFILES_DIR", "JAVA_RUNFILES", "TEST_SRCDIR")) {
            final String root = System.getenv(env);
            if (root != null) {
                final Path candidate = Path.of(root).resolve(path);
                if (Files.exists(candidate)) {
                    return candidate.toAbsolutePath().normalize();
                }
                final Path mainCandidate = Path.of(root).resolve("_main").resolve(path);
                if (Files.exists(mainCandidate)) {
                    return mainCandidate.toAbsolutePath().normalize();
                }
            }
        }
        throw new IllegalArgumentException("File does not exist: " + path);
    }
}
