package worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.io.File;

public class Worker {
    private String url = "";
    private String method = "GET";
    private final Map<String, String> queryMap = new HashMap<>();
    private final Map<String, String> headerMap = new HashMap<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private ObjectNode jsonObject = objectMapper.createObjectNode();
    private boolean isConsoleEnabled = false;
    private List<String> outputFiles = new LinkedList<>();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void updateQuery(String key, String value) {
        this.queryMap.put(key, value);
    }
    public void updateRequestBody(String key, String value){
        this.jsonObject.put(key, value);
    }

    public void updateHeader(String key, String value) {
        this.headerMap.put(key, value);
    }

    public void sendRequest(){
        this.updateUrl();

        String jsonString = "";

        if(!Objects.equals(method, Constants.HttpMethods.GET) && !Objects.equals(method, Constants.HttpMethods.DELETE)){
            try{
                jsonString = objectMapper.writeValueAsString(jsonObject);
            } catch ( JsonProcessingException e){
                System.out.println(e.toString());
            }
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(getUrl()))
                .method(getMethod(), HttpRequest.BodyPublishers.ofString(jsonString));

        headerMap.forEach((name, value) -> builder.header(name, value));

        HttpRequest request = builder.build();

        try {
            if (this.isConsoleEnabled)
                System.out.println("Calling: [" + getMethod() + "] " + getUrl());

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (this.isConsoleEnabled)
                this.printToConsole(response);
                    } catch (IOException | InterruptedException e){
            System.out.println(e.toString());
        }
    }

    private void printToConsole(HttpResponse<String> response) {
        System.out.println("Status: " + response.statusCode());
        System.out.println(response.body());
        System.out.println();
    }

    private void updateUrl(){
        StringBuilder stringBuilder = new StringBuilder(getUrl());
        if(!queryMap.isEmpty()){
            stringBuilder.append("?");
            queryMap.forEach((key, value) -> {
                stringBuilder.append(key).append("=").append(value);
            });
        }
        setUrl(stringBuilder.toString());
    }

    public void readRequestBody(String filename){
        File file = new File(filename);
        try{
            this.jsonObject = (ObjectNode) objectMapper.readTree(file);
        } catch ( ClassCastException | IOException e){
            if (this.isConsoleEnabled)
                System.out.println(e.toString());
        }
    }
    public void setConsoleEnabled(boolean consoleEnabled) {
        isConsoleEnabled = consoleEnabled;
    }
}
