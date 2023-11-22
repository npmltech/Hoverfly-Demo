package org.example.intercept;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

public class ConsumingWorldClock {

    private final OkHttpClient client;
    private final Gson gson;

    public ConsumingWorldClock() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .build();
        //
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
        //
    }

    public LocalTime getTime(@NotNull String timezone, String callback) {
        LocalTime localTime;
        assert callback != null;

        if (callback.isEmpty() || callback.isBlank())
            callback = "";

        Request request = new Request.Builder()
            .url(
                String.format(
                    "http://worldclockapi.com/api/json/%s/now%s",
                    timezone,
                    callback
                )
            )
            .build();
        //
        out.printf("URLs: %s%n", request.url());
        //
        try (Response response = this.client.newCall(request).execute()) {
            if (response.body() == null) throw new AssertionError();

            String responseBody = response.body().string();

            prettyPrintJson(responseBody);

            String currentDateTime = getValueResponseUsingKeyJson(responseBody, "currentDateTime");

            localTime = LocalDateTime.parse(
                currentDateTime,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            ).toLocalTime();
            //
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        //
        return localTime;
    }

    private void prettyPrintJson(String json) {
        out.println(gson.toJson(JsonParser.parseString(json)));
    }

    private String getValueResponseUsingKeyJson(@NotNull String responseBody, @NotNull String key) {
        return String.valueOf(
            JsonParser.parseString(responseBody)
                .getAsJsonObject()
                .get(key)
            //
        )
        .replace("\"", "");
    }

    public static void main(String[] args) {
        //
        ConsumingWorldClock worldClock = new ConsumingWorldClock();
        //
        out.printf("<< Current DateTime EST: %s >>%n", worldClock.getTime("est", ""));
        out.printf("<< Current DateTime UTC: %s >>%n", worldClock.getTime("utc", ""));
        out.printf("<< Current DateTime CET: %s >>", worldClock.getTime("cet", "?callback=mycallback"));
    }
}
