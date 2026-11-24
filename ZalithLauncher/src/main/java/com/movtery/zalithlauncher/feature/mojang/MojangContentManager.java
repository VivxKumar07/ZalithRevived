package com.movtery.zalithlauncher.feature.mojang;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.movtery.zalithlauncher.task.TaskExecutors;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MojangContentManager {
    private static final String URL_PATCH_NOTES = "https://launchercontent.mojang.com/v2/javaPatchNotes.json";
    private static final String URL_NEWS = "https://launchercontent.mojang.com/v2/news.json";
    private static final Gson GSON = new Gson();

    public static class PatchNoteItem {
        public String title;
        public String version;
        public String summary;
        public String badge;

        public PatchNoteItem(String title, String version, String summary, String badge) {
            this.title = title;
            this.version = version;
            this.summary = summary;
            this.badge = badge;
        }
    }

    public static class NewsItem {
        public String title;
        public String category;
        public String imageUrl;
        public String readMoreUrl;

        public NewsItem(String title, String category, String imageUrl, String readMoreUrl) {
            this.title = title;
            this.category = category;
            this.imageUrl = imageUrl;
            this.readMoreUrl = readMoreUrl;
        }
    }

    public interface Callback<T> {
        void onLoaded(List<T> items);
    }

    public static void loadPatchNotes(Context context, Callback<PatchNoteItem> callback) {
        TaskExecutors.getDefault().execute(() -> {
            List<PatchNoteItem> result = fetchPatchNotesOnline(context);
            if (result == null || result.isEmpty()) {
                result = loadPatchNotesOffline(context);
            }
            final List<PatchNoteItem> finalResult = result;
            TaskExecutors.runInUIThread(() -> callback.onLoaded(finalResult));
        });
    }

    public static void loadNews(Context context, Callback<NewsItem> callback) {
        TaskExecutors.getDefault().execute(() -> {
            List<NewsItem> result = fetchNewsOnline(context);
            if (result == null || result.isEmpty()) {
                result = loadNewsOffline(context);
            }
            final List<NewsItem> finalResult = result;
            TaskExecutors.runInUIThread(() -> callback.onLoaded(finalResult));
        });
    }

    private static String httpGet(String urlStr) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; ZalithRevived)");
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
                return sb.toString();
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    private static List<PatchNoteItem> fetchPatchNotesOnline(Context context) {
        try {
            String json = httpGet(URL_PATCH_NOTES);
            if (json != null && !json.trim().isEmpty()) {
                File cache = new File(context.getCacheDir(), "mojang_patch_notes.json");
                FileUtils.writeStringToFile(cache, json, StandardCharsets.UTF_8);
                return parsePatchNotes(json);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static List<PatchNoteItem> loadPatchNotesOffline(Context context) {
        try {
            File cache = new File(context.getCacheDir(), "mojang_patch_notes.json");
            if (cache.exists()) {
                String json = FileUtils.readFileToString(cache, StandardCharsets.UTF_8);
                return parsePatchNotes(json);
            }
        } catch (Exception ignored) {
        }
        // Fallback default list
        List<PatchNoteItem> list = new ArrayList<>();
        list.add(new PatchNoteItem("Minecraft: Java Edition - The Garden Awakens", "1.21.4", "Adds the Pale Garden biome, the Creaking mob, Creaking Heart, Resin clumps, Eyeblossom, and Pale Oak wood set.", "RELEASE"));
        list.add(new PatchNoteItem("Minecraft: Java Edition - Bundles of Bravery", "1.21.2", "Adds dyeable Bundles, Hardcore mode for Realms, and improved wolf armor mechanics.", "RELEASE"));
        list.add(new PatchNoteItem("Minecraft: Java Edition - Tricky Trials", "1.21", "Adds Trial Chambers, the Breeze mob, the Mace, Crafter block, heavy core, and ominous trials.", "RELEASE"));
        list.add(new PatchNoteItem("Minecraft: Java Edition - Armored Paws", "1.20.5", "Adds the Armadillo mob, Wolf Armor, 8 new Wolf variants, and revised breeding mechanics.", "RELEASE"));
        list.add(new PatchNoteItem("Minecraft: Java Edition - Trails & Tales", "1.20", "Adds Camels, Sniffers, Cherry Blossom biomes, Archaeology, Armor Trims, and Bamboo wood sets.", "RELEASE"));
        return list;
    }

    private static List<PatchNoteItem> parsePatchNotes(String json) {
        List<PatchNoteItem> list = new ArrayList<>();
        try {
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            JsonArray entries = root.getAsJsonArray("entries");
            if (entries != null) {
                for (JsonElement el : entries) {
                    JsonObject obj = el.getAsJsonObject();
                    String title = obj.has("title") ? obj.get("title").getAsString() : "Minecraft Update";
                    String version = obj.has("version") ? obj.get("version").getAsString() : "";
                    String shortText = obj.has("shortText") ? obj.get("shortText").getAsString() : "";
                    String body = obj.has("body") ? obj.get("body").getAsString() : shortText;
                    if (body == null || body.trim().isEmpty()) body = shortText;
                    // strip html tags
                    body = body.replaceAll("<[^>]*>", "").trim();
                    if (body.length() > 200) body = body.substring(0, 197) + "...";

                    boolean isSnapshot = version.toLowerCase().contains("snapshot") || version.toLowerCase().contains("pre") || version.toLowerCase().contains("rc");
                    String badge = isSnapshot ? "SNAPSHOT" : "RELEASE";

                    list.add(new PatchNoteItem(title, version, body, badge));
                }
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    private static List<NewsItem> fetchNewsOnline(Context context) {
        try {
            String json = httpGet(URL_NEWS);
            if (json != null && !json.trim().isEmpty()) {
                File cache = new File(context.getCacheDir(), "mojang_news.json");
                FileUtils.writeStringToFile(cache, json, StandardCharsets.UTF_8);
                return parseNews(json);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static List<NewsItem> loadNewsOffline(Context context) {
        try {
            File cache = new File(context.getCacheDir(), "mojang_news.json");
            if (cache.exists()) {
                String json = FileUtils.readFileToString(cache, StandardCharsets.UTF_8);
                return parseNews(json);
            }
        } catch (Exception ignored) {
        }
        List<NewsItem> list = new ArrayList<>();
        list.add(new NewsItem("Minecraft Java Edition: 1.21.4 The Garden Awakens Available Now", "MINECRAFT JAVA EDITION", "", "https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-4"));
        list.add(new NewsItem("Zalith Revived: Legacy ZL1 PvP touch engine updated with full Cubiq UI", "COMMUNITY", "", "https://github.com"));
        list.add(new NewsItem("Bundles of Bravery Drop Released for Java & Bedrock", "GAME UPDATE", "", "https://www.minecraft.net/en-us/article/bundles-bravery-drop-released"));
        return list;
    }

    private static List<NewsItem> parseNews(String json) {
        List<NewsItem> list = new ArrayList<>();
        try {
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            JsonArray entries = root.getAsJsonArray("entries");
            if (entries != null) {
                for (JsonElement el : entries) {
                    JsonObject obj = el.getAsJsonObject();
                    String title = obj.has("title") ? obj.get("title").getAsString() : "";
                    String category = obj.has("category") ? obj.get("category").getAsString() : "MINECRAFT";
                    String readMore = obj.has("readMoreLink") ? obj.get("readMoreLink").getAsString() : "https://www.minecraft.net";
                    String imgUrl = "";
                    if (obj.has("newsPageImage") && obj.getAsJsonObject("newsPageImage").has("url")) {
                        imgUrl = obj.getAsJsonObject("newsPageImage").get("url").getAsString();
                    } else if (obj.has("playPageImage") && obj.getAsJsonObject("playPageImage").has("url")) {
                        imgUrl = obj.getAsJsonObject("playPageImage").get("url").getAsString();
                    }
                    if (imgUrl != null && !imgUrl.isEmpty() && !imgUrl.startsWith("http")) {
                        imgUrl = "https://launchercontent.mojang.com" + imgUrl;
                    }

                    list.add(new NewsItem(title, category, imgUrl, readMore));
                }
            }
        } catch (Exception ignored) {
        }
        return list;
    }
}
