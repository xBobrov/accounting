package com.vodokanal.accounting.util;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class HttpRequestProducer {
    private final HttpClient httpClient;

    public HttpRequestProducer(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String get(String uri) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .GET()
                    .build();

            return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
