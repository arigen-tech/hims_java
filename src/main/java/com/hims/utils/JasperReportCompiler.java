
package com.hims.utils;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.stream.Stream;

public class JasperReportCompiler {

    public static void compileAllReports(String sourceDirPath) {
        compileAllReports(sourceDirPath, false);
    }

    public static void compileAllReports(
            String sourceDirPath,
            boolean forceCompile) {

        File folder = new File(sourceDirPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Invalid report directory: " + sourceDirPath);
            return;
        }

        System.out.println("\n======================================");
        System.out.println("JasperReports Compiler Started");
        System.out.println("JasperReports Version: "
                + JasperCompileManager.class.getPackage()
                .getImplementationVersion());
        System.out.println("Source Directory: " + folder.getAbsolutePath());
        System.out.println("Force Compilation: " + forceCompile);
        System.out.println("======================================\n");

        int compiledCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        try (Stream<Path> files = Files.walk(folder.toPath())) {

            var jrxmlFiles = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString()
                            .toLowerCase()
                            .endsWith(".jrxml"))
                    .toList();

            if (jrxmlFiles.isEmpty()) {
                System.out.println("No JRXML files found.");
                return;
            }

            for (Path jrxmlPath : jrxmlFiles) {

                String fileName = jrxmlPath.getFileName().toString();

                Path jasperPath = jrxmlPath.resolveSibling(
                        fileName.replaceFirst("(?i)\\.jrxml$", ".jasper")
                );

                Path tempJasperPath = jasperPath.resolveSibling(
                        jasperPath.getFileName() + ".tmp"
                );

                try {

                    // Compile only when JRXML is newer or JASPER is missing.
                    boolean compilationRequired = forceCompile
                            || !Files.exists(jasperPath)
                            || Files.getLastModifiedTime(jrxmlPath)
                            .compareTo(Files.getLastModifiedTime(jasperPath)) > 0;

                    if (!compilationRequired) {
                        skippedCount++;
                        System.out.println("SKIPPED (No Changes): " + fileName);
                        continue;
                    }

                    System.out.println("COMPILING: " + fileName);

                    // Compile into temporary file first.
                    JasperCompileManager.compileReportToFile(
                            jrxmlPath.toString(),
                            tempJasperPath.toString()
                    );

                    if (!Files.exists(tempJasperPath)
                            || Files.size(tempJasperPath) == 0) {
                        throw new JRException(
                                "Generated Jasper file is missing or empty."
                        );
                    }

                    // Replace existing JASPER only after successful compilation.
                    try {
                        Files.move(
                                tempJasperPath,
                                jasperPath,
                                StandardCopyOption.REPLACE_EXISTING,
                                StandardCopyOption.ATOMIC_MOVE
                        );
                    } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                        Files.move(
                                tempJasperPath,
                                jasperPath,
                                StandardCopyOption.REPLACE_EXISTING
                        );
                    }

                    // Match generated file timestamp with source timestamp.
                    FileTime sourceModifiedTime =
                            Files.getLastModifiedTime(jrxmlPath);

                    Files.setLastModifiedTime(
                            jasperPath,
                            sourceModifiedTime
                    );

                    compiledCount++;

                    System.out.println("SUCCESS: " + fileName);
                    System.out.println("Updated: " + jasperPath);

                } catch (JRException e) {

                    failedCount++;
                    System.err.println("COMPILATION FAILED: " + fileName);
                    e.printStackTrace();

                } catch (Exception e) {

                    failedCount++;
                    System.err.println("ERROR: " + fileName);
                    e.printStackTrace();

                } finally {

                    try {
                        Files.deleteIfExists(tempJasperPath);
                    } catch (Exception ignored) {
                        // Temporary file cleanup failure.
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error while reading report directory.");
            e.printStackTrace();
        }

        System.out.println("\n======================================");
        System.out.println("Jasper Compilation Summary");
        System.out.println("Successfully Compiled: " + compiledCount);
        System.out.println("Skipped (Unchanged): " + skippedCount);
        System.out.println("Failed: " + failedCount);
        System.out.println("======================================\n");
    }

    public static void main(String[] args) {

        String sourceDirPath = args.length > 0
                ? args[0]
                : "src/main/resources/jasperReport";

        boolean forceCompile = args.length > 1
                && "--force".equalsIgnoreCase(args[1]);

        compileAllReports(sourceDirPath, forceCompile);
    }
}
