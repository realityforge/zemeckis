package zemeckis.test;

import static org.testng.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;
import jdepend.framework.DependencyConstraint;
import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;
import jdepend.framework.PackageFilter;
import org.testng.annotations.Test;

public class JDependTest {
    @Test
    public void dependencyAnalysis() throws Exception {
        final var jdepend = new JDepend(PackageFilter.all().excluding("java.*", "javax.*"));
        jdepend.addDirectory(compileTargetDir());
        jdepend.analyze();

        final var constraint = new DependencyConstraint();

        final var zemeckis = constraint.addPackage("zemeckis");
        final var braincheck = constraint.addPackage("org.realityforge.braincheck");
        final var jsinterop = constraint.addPackage("jsinterop.annotations");
        final var jsinteropBase = constraint.addPackage("jsinterop.base");
        final var jspecify = constraint.addPackage("org.jspecify.annotations");
        zemeckis.dependsUpon(jsinterop);
        zemeckis.dependsUpon(jsinteropBase);
        zemeckis.dependsUpon(jspecify);
        zemeckis.dependsUpon(braincheck);
        final var result = jdepend.analyzeDependencies(constraint);

        final var undefinedPackages = result.getUndefinedPackages();
        if (!undefinedPackages.isEmpty()) {
            fail("Undefined Packages: "
                    + undefinedPackages.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }

        final var nonMatchingPackages = result.getNonMatchingPackages();
        if (!nonMatchingPackages.isEmpty()) {
            final var sb = new StringBuilder();
            sb.append("Discovered packages where relationships do not align.\n");
            for (final JavaPackage[] packages : nonMatchingPackages) {
                final var expected = packages[0];
                final var actual = packages[1];

                final var oldAfferents = new ArrayList<>(expected.getAfferents());
                oldAfferents.removeAll(actual.getAfferents());

                oldAfferents.forEach(p -> sb.append("Package ")
                        .append(p.getName())
                        .append(" no longer depends upon ")
                        .append(expected.getName())
                        .append("\n"));

                final var newAfferents = new ArrayList<>(actual.getAfferents());
                newAfferents.removeAll(expected.getAfferents());

                newAfferents.forEach(p -> sb.append("Package ")
                        .append(p.getName())
                        .append(" now depends upon ")
                        .append(expected.getName())
                        .append("\n"));

                final var oldEfferents = new ArrayList<>(expected.getEfferents());
                oldEfferents.removeAll(actual.getEfferents());

                oldEfferents.forEach(p -> sb.append("Package ")
                        .append(expected.getName())
                        .append(" no longer depends depends upon ")
                        .append(p.getName())
                        .append("\n"));

                final var newEfferents = new ArrayList<>(actual.getEfferents());
                newEfferents.removeAll(expected.getEfferents());

                newEfferents.forEach(p -> sb.append("Package ")
                        .append(expected.getName())
                        .append(" now depends upon ")
                        .append(p.getName())
                        .append("\n"));
            }
            fail(sb.toString());
        }
    }

    private String compileTargetDir() {
        final var fixtureDir = System.getProperty("zemeckis.core.compile_target");
        assertNotNull(
                fixtureDir, "Expected System.getProperty( \"zemeckis.core.compile_target\" ) to return directory");
        return new File(fixtureDir).getAbsolutePath();
    }
}
