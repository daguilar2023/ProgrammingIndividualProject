package blackboard.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tiny utility for simple text/CSV persistence.
 *
 * Responsibilities:
 *  - Ensure parent directories exist before writing.
 *  - Read all lines (UTF-8) or return empty list if file is missing.
 *  - Write/append lines atomically where reasonable.
 *  - Minimal CSV helpers (escape + parse).
 */
public final class FileStore {
    private FileStore() {}

    /** Read all lines (UTF-8). Returns empty list if file doesn't exist. */
    public static List<String> readAll(Path path) throws IOException {
        if (path == null || !Files.exists(path)) return Collections.emptyList();
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    /** Overwrite file with provided lines (UTF-8). Creates parent directories if needed. */
    public static void writeAll(Path path, List<String> lines) throws IOException {
        ensureParentDirs(path);
        Files.write(path,
                lines == null ? Collections.emptyList() : lines,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    /** Append a single line (UTF-8). Creates parent directories if needed. */
    public static void append(Path path, String line) throws IOException {
        ensureParentDirs(path);
        Files.write(path,
                Arrays.asList(line == null ? "" : line),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    /** Ensure a directory exists (no-op if already present). */
    public static void ensureDir(Path dir) throws IOException {
        if (dir != null) Files.createDirectories(dir);
    }

    /** Ensure parent directory for a file exists. */
    private static void ensureParentDirs(Path path) throws IOException {
        if (path == null) return;
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);
    }

    // -------------------- CSV helpers --------------------

    /** Join fields into a CSV line (RFC4180-ish: quote when needed, escape quotes as \"\"). */
    public static String toCsv(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < (fields == null ? 0 : fields.length); i++) {
            sb.append(escapeCsv(fields[i]));
            if (i < fields.length - 1) sb.append(',');
        }
        return sb.toString();
    }

    /** Parse a single CSV line into fields (supports quoted fields and escaped quotes). */
    public static String[] parseCsvLine(String line) {
        if (line == null) return new String[0];
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++; // skip escaped quote
                    } else {
                        inQuotes = false; // closing quote
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else if (c == '"') {
                    inQuotes = true; // opening quote
                } else {
                    cur.append(c);
                }
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private static String escapeCsv(String s) {
        String v = (s == null) ? "" : s;
        boolean needQuote = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r");
        if (!needQuote) return v;
        return '"' + v.replace("\"", "\"\"") + '"';
    }
}
