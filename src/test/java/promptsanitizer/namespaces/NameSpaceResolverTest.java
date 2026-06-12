package promptsanitizer.namespaces;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameSpaceResolverTest {
    @Test
    public void shouldResolveWhenValidDirectoryExists() throws IOException {
        Path baseDirectory = null;
        try {
            baseDirectory = Files.createTempDirectory("gonzo");
            Path subDirs = null;
            try {
                subDirs = Files.createDirectory(baseDirectory.resolve("ab_3c"));
                Path subSubDirs = null;
                try {
                    subSubDirs = Files.createDirectory(subDirs.resolve("xyz_2"));
                    NameSpaceResolver nameSpaceResolver = new NameSpaceResolver(baseDirectory.toString());
                    assertEquals(baseDirectory.resolve("ab_3c").resolve("xyz_2").toString(), nameSpaceResolver.resolveNameSpace("ab_3c.xyz_2"));
                } finally {
                    if(subSubDirs != null) {
                        Files.delete(subSubDirs);
                    }
                }
            } finally {
                if(subDirs != null) {
                    Files.delete(subDirs);
                }
            }
        } finally {
            if(baseDirectory != null) {
                Files.delete(baseDirectory);
            }
        }
    }

    @Test
    public void shouldIllegalArgumentExceptWhenNotADirectory() throws IOException {
        Path baseDirectory = null;
        String caughtExceptionMessage = "";
        try {
            baseDirectory = Files.createTempDirectory("gonzo");
            Path subDirs = null;
            try {
                subDirs = Files.createDirectory(baseDirectory.resolve("ab_3c"));
                Path notADir = null;
                try {
                    notADir = Files.createFile(subDirs.resolve("xyz_2"));
                    NameSpaceResolver nameSpaceResolver = new NameSpaceResolver(baseDirectory.toString());
                    nameSpaceResolver.resolveNameSpace("ab_3c.xyz_2");
                } catch(IllegalArgumentException iae) {
                    caughtExceptionMessage = iae.getMessage();
                } finally {
                    if(notADir != null) {
                        Files.delete(notADir);
                    }
                }
            } finally {
                if(subDirs != null) {
                    Files.delete(subDirs);
                }
            }
        } finally {
            if(baseDirectory != null) {
                Files.delete(baseDirectory);
            }
        }
        assertTrue(caughtExceptionMessage.startsWith("Directory path based on namespace must exist and be a directory : "));
    }

    @Test
    public void shouldIllegalArgumentExceptWhenDirectoryNotExists() throws IOException {
        Path baseDirectory = null;
        String caughtExceptionMessage = "";
        try {
            baseDirectory = Files.createTempDirectory("gonzo");
            Path subDirs = null;
            try {
                subDirs = Files.createDirectory(baseDirectory.resolve("ab_3c"));
                try {
                    NameSpaceResolver nameSpaceResolver = new NameSpaceResolver(baseDirectory.toString());
                    nameSpaceResolver.resolveNameSpace("ab_3c.xyz_2"); //We didn't create "xyz_2" in any way using Files or otherwise.  So it shouldn't exist.
                } catch(IllegalArgumentException iae) {
                    caughtExceptionMessage = iae.getMessage();
                }
            } finally {
                if(subDirs != null) {
                    Files.delete(subDirs);
                }
            }
        } finally {
            if(baseDirectory != null) {
                Files.delete(baseDirectory);
            }
        }
        assertTrue(caughtExceptionMessage.startsWith("Directory path based on namespace must exist and be a directory : "));
    }

    @Test
    public void shouldResolveNullOrBlankAsBasePath() throws IOException {
        NameSpaceResolver nameSpaceResolver = new NameSpaceResolver("gonzo");
        assertEquals("gonzo", nameSpaceResolver.resolveNameSpace(null));
        assertEquals("gonzo", nameSpaceResolver.resolveNameSpace(""));
    }
}
