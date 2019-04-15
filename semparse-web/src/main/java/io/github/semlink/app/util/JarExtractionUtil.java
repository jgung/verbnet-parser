package io.github.semlink.app.util;

import com.google.common.base.Stopwatch;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Copy files and directories from within jar file.
 *
 * @author jgung
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JarExtractionUtil {

    /**
     * Extract a single file for a given classpath resource.
     * Adapted from https://stackoverflow.com/a/19861668.
     *
     * @param path path on classpath
     */
    public static String resolveFile(@NonNull String path) {
        Stopwatch sw = Stopwatch.createStarted();
        Resource resource = new PathMatchingResourcePatternResolver().getResource("classpath:" + path);
        try {
            Path file = resource.getFile().toPath();
            File result = file.toFile();
            log.info("Extracted {} in {} to {}", path, sw, result.toString());
            return result.toString();
        } catch (IOException ignored) {
            // Resource must be inside JAR (not in file system)
        }
        try {
            Path file = Files.createTempFile(JarExtractionUtil.class.getSimpleName(), "");
            Files.copy(resource.getInputStream(), file, StandardCopyOption.REPLACE_EXISTING);
            file.toFile().deleteOnExit();
            File result = file.toFile();
            log.info("Extracted {} in {} to {}", path, sw, result.toString());
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extract a directory for a given classpath resource.
     * Adapted from https://stackoverflow.com/a/19861668.
     *
     * @param path path on classpath
     */
    public static String resolveDirectory(@NonNull String path) {
        if (!path.endsWith("/")) {
            path += "/";
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        try {
            Resource[] rootResourceDirs = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:" + path);
            if (rootResourceDirs.length == 0) {
                throw new RuntimeException("Directory " + path + " not found");
            }

            boolean isFile = false;
            try {
                rootResourceDirs[0].getFile();
                isFile = true;
            } catch (Exception ignored) {
            }

            Stopwatch sw = Stopwatch.createStarted();
            if (rootResourceDirs.length == 1 && isFile) {
                Path rootPath = rootResourceDirs[0].getFile().toPath().toAbsolutePath();
                log.info("Extracted resource in {} from {} to {}", sw, path, rootPath.toFile().toString());
                return rootPath.toFile().toString();
            }

            Path tmp = Files.createTempDirectory(JarExtractionUtil.class.getSimpleName());
            deleteOnApplicationExit(tmp);
            Path rootPath = tmp.resolve(path).toAbsolutePath();

            int fileCount = 0;
            for (Resource r : new PathMatchingResourcePatternResolver().getResources("classpath*:" + path + "**")) {
                String urlPath = r.getURL().getPath();

                String relativePath;
                if (urlPath.contains("!/")) {
                    relativePath = substringAfterLast(urlPath, "!/" + path);
                } else {
                    relativePath = substringAfter(urlPath, path);
                }

                Path filePath = rootPath.resolve(relativePath).toAbsolutePath();
                Files.createDirectories(filePath.getParent());

                if (!urlPath.endsWith("/")) {
                    Files.copy(r.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    fileCount++;
                }
            }

            log.info("Extracted {} resources in {} from {} to {}", fileCount, sw, path, rootPath.toFile().toString());
            return rootPath.toFile().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String substringAfterLast(String str, String separator) {
        final int pos = str.lastIndexOf(separator);
        if (pos == -1 || pos == str.length() - separator.length()) {
            return "";
        }
        return str.substring(pos + separator.length());
    }

    private static String substringAfter(String str, String separator) {
        final int pos = str.indexOf(separator);
        if (pos == -1) {
            return "";
        }
        return str.substring(pos + separator.length());
    }

    private static void deleteOnApplicationExit(Path path) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(path.toFile());
            } catch (IOException e) {
                log.warn("Failed to delete temporary directory at " + path, e);
            }
        }));
    }

}
