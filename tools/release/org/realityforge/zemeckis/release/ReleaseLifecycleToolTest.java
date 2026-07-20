package org.realityforge.zemeckis.release;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.stream.Collectors;

public final class ReleaseLifecycleToolTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T00:00:00Z"), ZoneId.of("UTC"));

    private ReleaseLifecycleToolTest() {}

    public static void main(final String[] args) throws Exception {
        final Path root = Files.createTempDirectory("release-lifecycle-tool-test");
        try {
            testNextVersion(root);
            testPrepare(root);
            testFinalizeIsIdempotent(root);
            testReleaseNotes(root);
            testReadmeMissingPreviousVersionFailsWithoutMutation(root);
            testPrepareRejectsInvalidUnreleasedSections(root);
        } finally {
            deleteTree(root);
        }
    }

    private static void testNextVersion(final Path root) throws IOException {
        assertNextVersion(root, "0.25", "0.26");
        assertNextVersion(root, "0.09", "0.10");
        assertNextVersion(root, "1.9", "1.10");
        final Path invalid = write(root.resolve("next-1.2.3.md"), "### [v1.2.3]\n\n- Old.\n");
        final Result invalidResult = run("next-version", "--changelog", invalid.toString());
        assertNotEquals(0, invalidResult.exitCode(), "three-component version should fail");
        assertContains(invalidResult.err(), "MAJOR.MINOR", "three-component version failure");
    }

    private static void assertNextVersion(final Path root, final String current, final String expected)
            throws IOException {
        final Path changelog = write(root.resolve("next-" + current + ".md"), "### [v" + current + "]\n\n- Old.\n");
        final Result result = run("next-version", "--changelog", changelog.toString());
        assertEquals(0, result.exitCode(), result.err());
        assertEquals(expected + "\n", result.out(), "next-version output");
    }

    private static void testPrepare(final Path root) throws IOException {
        final Path directory = Files.createDirectories(root.resolve("prepare"));
        final Path changelog =
                write(directory.resolve("CHANGELOG.md"), unreleasedChangelog("- Added lifecycle helper.\n"));
        final Path readme = write(directory.resolve("README.md"), """
            <version>0.25</version>
            https://repo.maven.apache.org/maven2/org/realityforge/zemeckis/zemeckis-core/0.25/zemeckis-core-0.25-sources.jar
            zemeckis-core-0.25-sources.jar
            """);

        final Result result = run(
                "prepare",
                "--changelog",
                changelog.toString(),
                "--readme",
                readme.toString(),
                "--version",
                "0.26",
                "--release-date",
                "2026-06-30");
        assertEquals(0, result.exitCode(), result.err());

        assertEquals("""
            # Changelog

            ### [v0.26](https://github.com/realityforge/zemeckis/tree/v0.26) (2026-06-30)\
             · [Full Changelog](https://github.com/realityforge/zemeckis/compare/v0.25...v0.26)

            Changes in this release:

            - Added lifecycle helper.

            ### [v0.25](https://github.com/realityforge/zemeckis/tree/v0.25) (2026-05-01)

            Changes in this release:

            - Previous release.
            """, Files.readString(changelog, StandardCharsets.UTF_8), "prepared changelog");
        assertEquals("""
            <version>0.26</version>
            https://repo.maven.apache.org/maven2/org/realityforge/zemeckis/zemeckis-core/0.26/zemeckis-core-0.26-sources.jar
            zemeckis-core-0.26-sources.jar
            """, Files.readString(readme, StandardCharsets.UTF_8), "updated README");
    }

    private static void testFinalizeIsIdempotent(final Path root) throws IOException {
        final Path changelog = write(root.resolve("finalize.md"), """
            # Changelog

            ### [v0.26](https://github.com/realityforge/zemeckis/tree/v0.26) (2026-06-30)

            Changes in this release:

            - Added lifecycle helper.
            """);

        final Result first = run("finalize", "--changelog", changelog.toString(), "--version", "0.26");
        assertEquals(0, first.exitCode(), first.err());
        final String expected = """
            # Changelog

            ### Unreleased

            ### [v0.26](https://github.com/realityforge/zemeckis/tree/v0.26) (2026-06-30)

            Changes in this release:

            - Added lifecycle helper.
            """;
        assertEquals(expected, Files.readString(changelog, StandardCharsets.UTF_8), "finalized changelog");

        final Result second = run("finalize", "--changelog", changelog.toString(), "--version", "0.26");
        assertEquals(0, second.exitCode(), second.err());
        assertEquals(expected, Files.readString(changelog, StandardCharsets.UTF_8), "idempotent finalized changelog");
    }

    private static void testReleaseNotes(final Path root) throws IOException {
        final Path changelog = write(root.resolve("release-notes.md"), """
            # Changelog

            ### Unreleased

            ### [v0.26](https://github.com/realityforge/zemeckis/tree/v0.26) (2026-06-30)

            Changes in this release:

            - Added lifecycle helper.

            ### [v0.25](https://github.com/realityforge/zemeckis/tree/v0.25) (2026-05-01)

            - Previous release.
            """);
        final Path output = root.resolve("notes.md");

        final Result result = run(
                "release-notes",
                "--changelog",
                changelog.toString(),
                "--version",
                "0.26",
                "--output",
                output.toString());
        assertEquals(0, result.exitCode(), result.err());
        assertEquals(
                "Changes in this release:\n\n- Added lifecycle helper.\n",
                Files.readString(output, StandardCharsets.UTF_8),
                "release notes");
    }

    private static void testReadmeMissingPreviousVersionFailsWithoutMutation(final Path root) throws IOException {
        final Path directory = Files.createDirectories(root.resolve("missing-readme-version"));
        final String changelogContent = unreleasedChangelog("- Added lifecycle helper.\n");
        final String readmeContent = "<version>9.99</version>\n";
        final Path changelog = write(directory.resolve("CHANGELOG.md"), changelogContent);
        final Path readme = write(directory.resolve("README.md"), readmeContent);

        final Result result = run(
                "prepare",
                "--changelog",
                changelog.toString(),
                "--readme",
                readme.toString(),
                "--version",
                "0.26",
                "--release-date",
                "2026-06-30");
        assertNotEquals(0, result.exitCode(), "prepare should fail");
        assertContains(result.err(), "README", "README failure message");
        assertEquals(changelogContent, Files.readString(changelog, StandardCharsets.UTF_8), "unchanged changelog");
        assertEquals(readmeContent, Files.readString(readme, StandardCharsets.UTF_8), "unchanged README");
    }

    private static void testPrepareRejectsInvalidUnreleasedSections(final Path root) throws IOException {
        assertPrepareFails(root, "# Changelog\n\n### [v0.25]\n\n- Old.\n", "Missing ### Unreleased");
        assertPrepareFails(
                root,
                "# Changelog\n\n### Unreleased\n\n### Unreleased\n\n- Change.\n\n### [v0.25]\n\n- Old.\n",
                "Duplicate ### Unreleased");
        assertPrepareFails(root, "# Changelog\n\n### Unreleased\n\n   \n\n### [v0.25]\n\n- Old.\n", "empty");
    }

    private static void assertPrepareFails(final Path root, final String changelogContent, final String message)
            throws IOException {
        final Path directory = Files.createTempDirectory(root, "invalid-prepare");
        final Path changelog = write(directory.resolve("CHANGELOG.md"), changelogContent);
        final Path readme = write(directory.resolve("README.md"), "<version>0.25</version>\n");
        final Result result = run(
                "prepare",
                "--changelog",
                changelog.toString(),
                "--readme",
                readme.toString(),
                "--version",
                "0.26",
                "--release-date",
                "2026-06-30");
        assertNotEquals(0, result.exitCode(), "prepare should fail");
        assertContains(result.err(), message, "prepare failure message");
    }

    private static String unreleasedChangelog(final String body) {
        return "# Changelog\n\n"
                + "### Unreleased\n\n"
                + body
                + "\n"
                + "### [v0.25](https://github.com/realityforge/zemeckis/tree/v0.25) (2026-05-01)\n\n"
                + "Changes in this release:\n\n"
                + "- Previous release.\n";
    }

    private static Path write(final Path path, final String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
        return path;
    }

    private static Result run(final String... args) {
        final var out = new ByteArrayOutputStream();
        final var err = new ByteArrayOutputStream();
        final int exitCode;
        try (PrintStream outStream = new PrintStream(out, true, StandardCharsets.UTF_8);
                PrintStream errStream = new PrintStream(err, true, StandardCharsets.UTF_8)) {
            exitCode = ReleaseLifecycleTool.run(args, outStream, errStream, CLOCK);
        }
        return new Result(
                exitCode,
                new String(out.toByteArray(), StandardCharsets.UTF_8),
                new String(err.toByteArray(), StandardCharsets.UTF_8));
    }

    private static void assertEquals(final int expected, final int actual, final String message) {
        if (expected != actual) {
            throw new AssertionError(message + "\nExpected: " + expected + "\nActual: " + actual);
        }
    }

    private static void assertNotEquals(final int expected, final int actual, final String message) {
        if (expected == actual) {
            throw new AssertionError(message + "\nUnexpected: " + actual);
        }
    }

    private static void assertEquals(final String expected, final String actual, final String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + "\nExpected:\n" + expected + "\nActual:\n" + actual);
        }
    }

    private static void assertContains(final String actual, final String expected, final String message) {
        if (!actual.contains(expected)) {
            throw new AssertionError(message + "\nExpected to contain: " + expected + "\nActual:\n" + actual);
        }
    }

    private static void deleteTree(final Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        for (final Path path : paths(root)) {
            Files.deleteIfExists(path);
        }
    }

    private static Iterable<Path> paths(final Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            return stream.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        }
    }

    private static final class Result {
        private final int _exitCode;
        private final String _out;
        private final String _err;

        private Result(final int exitCode, final String out, final String err) {
            _exitCode = exitCode;
            _out = out;
            _err = err;
        }

        private int exitCode() {
            return _exitCode;
        }

        private String out() {
            return _out;
        }

        private String err() {
            return _err;
        }
    }
}
