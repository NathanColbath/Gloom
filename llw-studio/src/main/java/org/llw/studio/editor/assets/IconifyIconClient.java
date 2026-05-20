package org.llw.studio.editor.assets;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Fetches SVG bytes from the public Iconify CDN ({@value #API_HOST}).
 */
public final class IconifyIconClient {
    static final String API_HOST = "https://api.iconify.design";

    private static final int DEFAULT_PIXEL_SIZE = 32;
    private static final String ICON_COLOR = "%23c8c8c8";

    private final HttpClient http;

    public IconifyIconClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    IconifyIconClient(HttpClient http) {
        this.http = http;
    }

    /**
     * @param spec   open-source icon descriptor
     * @param pixels raster target size (square)
     * @return SVG document bytes
     */
    public byte[] fetchSvg(OpenSourceIconSpec spec, int pixels) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(iconifyUri(spec, pixels))
                .timeout(Duration.ofSeconds(12))
                .GET()
                .header("Accept", "image/svg+xml")
                .build();
        HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Iconify HTTP " + response.statusCode() + " for " + spec.iconifyPath());
        }
        byte[] body = response.body();
        if (body == null || body.length == 0) {
            throw new IOException("Empty Iconify response for " + spec.iconifyPath());
        }
        return body;
    }

    static URI iconifyUri(OpenSourceIconSpec spec, int pixels) {
        String query = "width=" + pixels
                + "&height=" + pixels
                + "&color=" + ICON_COLOR;
        String path = "/" + encodeSegment(spec.prefix()) + "/" + encodeSegment(spec.name()) + ".svg?" + query;
        return URI.create(API_HOST + path);
    }

    static int defaultPixelSize() {
        return DEFAULT_PIXEL_SIZE;
    }

    private static String encodeSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
