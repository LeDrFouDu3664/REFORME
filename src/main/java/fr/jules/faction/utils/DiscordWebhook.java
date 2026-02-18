package fr.jules.faction.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {
    private final String url;

    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void execute(String content) {
        if (url == null || url.isEmpty() || url.equals("votre_webhook_ici")) return;

        try {
            URL urlObj = new URL(this.url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            String json = "{\"content\": \"" + content.replace("\"", "\\\"") + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception ignored) {}
    }
}
