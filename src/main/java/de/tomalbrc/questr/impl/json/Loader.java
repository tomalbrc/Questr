package de.tomalbrc.questr.impl.json;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import de.tomalbrc.questr.QuestrMod;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Loader {
    public static <T> T load(Path file, Class<T> tClass) throws IOException {
        try (FileReader reader = new FileReader(file.toFile())) {
            return Json.GSON.fromJson(reader, tClass);
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IOException("Failed to load quest from " + file, e);
        }
    }

    public static <T> List<T> loadAll(Path folder, Class<T> tClass) throws IOException {
        Files.createDirectories(folder);

        List<T> list = new ArrayList<>();
        try (var stream = Files.list(folder)) {
            for (Path file : stream.toList()) {
                if (file.toString().endsWith(".json")) {
                    try {
                        list.add(load(file, tClass));
                    } catch (IOException e) {
                        QuestrMod.LOGGER.error("Could not load file {}", file, e);
                    }
                }
            }
        }
        return list;
    }
}