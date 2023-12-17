package me.playajames.projectlife.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.amithkoujalgi.ollama4j.core.exceptions.OllamaBaseException;
import io.github.amithkoujalgi.ollama4j.core.models.*;
import io.github.amithkoujalgi.ollama4j.core.utils.Utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import me.playajames.projectlife.ProjectLife;
import me.playajames.projectlife.common.ollama.OllamaPrompt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.playajames.projectlife.ProjectLife.OLLAMA_CURL;

public class PlOllamaAPI {

    private static final Logger logger = LoggerFactory.getLogger(PlOllamaAPI.class);
    private final String host;
    private long requestTimeoutSeconds = 120;
    private boolean verbose = false;

    /**
     * Instantiates the Ollama API.
     *
     * @param host the host address of Ollama server
     */
    public PlOllamaAPI(String host) {
        if (host.endsWith("/")) {
            this.host = host.substring(0, host.length() - 1);
        } else {
            this.host = host;
        }
    }

    /**
     * Set/unset logging of responses
     *
     * @param verbose true/false
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * List available models from Ollama server.
     *
     * @return the list
     */
    public List<Model> listModels()
            throws OllamaBaseException, IOException, InterruptedException, URISyntaxException {
        String url = this.host + "/api/tags";

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest =
                HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .header("Accept", "application/json")
                        .header("Content-type", "application/json")
                        .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                        .GET()
                        .build();
        HttpResponse<String> response =
                httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String responseString = response.body();
        if (statusCode == 200) {
            return Utils.getObjectMapper()
                    .readValue(responseString, ListModelsResponse.class)
                    .getModels();
        } else {
            throw new OllamaBaseException(statusCode + " - " + responseString);
        }
    }

    /**
     * Pull a model on the Ollama server from the list of <a
     * href="https://ollama.ai/library">available models</a>.
     *
     * @param model the name of the model
     */
    public void pullModel(String model)
            throws OllamaBaseException, IOException, URISyntaxException, InterruptedException {
        String url = this.host + "/api/pull";
        String jsonData = String.format("{\"name\": \"%s\"}", model);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                        .header("Accept", "application/json")
                        .header("Content-type", "application/json")
                        .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                        .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<InputStream> response =
                client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        int statusCode = response.statusCode();
        InputStream responseBodyStream = response.body();
        String responseString = "";
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(responseBodyStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ModelPullResponse modelPullResponse =
                        Utils.getObjectMapper().readValue(line, ModelPullResponse.class);
                if (verbose) {
                    logger.info(modelPullResponse.getStatus());
                }
            }
        }
        if (statusCode != 200) {
            throw new OllamaBaseException(statusCode + " - " + responseString);
        }
    }

    /**
     * Gets model details from the Ollama server.
     *
     * @param modelName the model
     * @return the model details
     */
    public ModelDetail getModelDetails(String modelName)
            throws IOException, OllamaBaseException, InterruptedException {
        String url = this.host + "/api/show";
        String jsonData = String.format("{\"name\": \"%s\"}", modelName);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Accept", "application/json")
                        .header("Content-type", "application/json")
                        .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                        .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String responseBody = response.body();
        if (statusCode == 200) {
            return Utils.getObjectMapper().readValue(responseBody, ModelDetail.class);
        } else {
            throw new OllamaBaseException(statusCode + " - " + responseBody);
        }
    }

    /**
     * Create a custom model from a model file. Read more about custom model file creation <a
     * href="https://github.com/jmorganca/ollama/blob/main/docs/modelfile.md">here</a>.
     *
     * @param modelName the name of the custom model to be created.
     * @param modelFilePath the path to model file that exists on the Ollama server.
     */
    public void createModel(String modelName, String modelFilePath)
            throws IOException, InterruptedException, OllamaBaseException {
        String url = this.host + "/api/create";
        String jsonData =
                String.format("{\"name\": \"%s\", \"path\": \"%s\"}", modelName, modelFilePath);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonData, StandardCharsets.UTF_8))
                        .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String responseString = response.body();
        if (statusCode != 200) {
            throw new OllamaBaseException(statusCode + " - " + responseString);
        }
        // FIXME: Ollama API returns HTTP status code 200 for model creation failure cases. Correct this
        // if the issue is fixed in the Ollama API server.
        if (responseString.contains("error")) {
            throw new OllamaBaseException(responseString);
        }
        if (verbose) {
            logger.info(responseString);
        }
    }

    /**
     * Delete a model from Ollama server.
     *
     * @param name the name of the model to be deleted.
     * @param ignoreIfNotPresent - ignore errors if the specified model is not present on Ollama
     *     server.
     */
    public void deleteModel(String name, boolean ignoreIfNotPresent)
            throws IOException, InterruptedException, OllamaBaseException {
        String url = this.host + "/api/delete";
        String jsonData = String.format("{\"name\": \"%s\"}", name);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonData, StandardCharsets.UTF_8))
                        .header("Accept", "application/json")
                        .header("Content-type", "application/json")
                        .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                        .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String responseBody = response.body();
        if (statusCode == 404 && responseBody.contains("model") && responseBody.contains("not found")) {
            return;
        }
        if (statusCode != 200) {
            throw new OllamaBaseException(statusCode + " - " + responseBody);
        }
    }

    /**
     * Generate embeddings for a given text from a model
     *
     * @param model name of model to generate embeddings from
     * @param prompt text to generate embeddings for
     * @return embeddings
     */
    public List<Double> generateEmbeddings(String model, String prompt)
            throws IOException, InterruptedException, OllamaBaseException {
        String url = this.host + "/api/embeddings";
        String jsonData = String.format("{\"model\": \"%s\", \"prompt\": \"%s\"}", model, prompt);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Accept", "application/json")
                        .header("Content-type", "application/json")
                        .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                        .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String responseBody = response.body();
        if (statusCode == 200) {
            EmbeddingResponse embeddingResponse =
                    Utils.getObjectMapper().readValue(responseBody, EmbeddingResponse.class);
            return embeddingResponse.getEmbedding();
        } else {
            throw new OllamaBaseException(statusCode + " - " + responseBody);
        }
    }

    /**
     * Ask a question to a model running on Ollama server. This is a sync/blocking call.
     *
     * @param ollamaModelType the ollama model to ask the question to
     * @param promptText the prompt/question text
     * @return OllamaResult - that includes response text and time taken for response
     */
    public OllamaResult ask(String ollamaModelType, String promptText)
            throws OllamaBaseException, IOException, InterruptedException {
        OllamaRequestModel ollamaRequestModel = new OllamaRequestModel(ollamaModelType, promptText);
        long startTime = System.currentTimeMillis();
        HttpClient httpClient = HttpClient.newHttpClient();
        URI uri = URI.create(this.host + "/api/generate");
        HttpRequest request =
                HttpRequest.newBuilder(uri)
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        Utils.getObjectMapper().writeValueAsString(ollamaRequestModel)))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                        .build();
        HttpResponse<InputStream> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        int statusCode = response.statusCode();
        InputStream responseBodyStream = response.body();
        StringBuilder responseBuffer = new StringBuilder();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(responseBodyStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (statusCode == 404) {
                    OllamaErrorResponseModel ollamaResponseModel =
                            Utils.getObjectMapper().readValue(line, OllamaErrorResponseModel.class);
                    responseBuffer.append(ollamaResponseModel.getError());
                } else {
                    OllamaResponseModel ollamaResponseModel =
                            Utils.getObjectMapper().readValue(line, OllamaResponseModel.class);
                    if (!ollamaResponseModel.isDone()) {
                        responseBuffer.append(ollamaResponseModel.getResponse());
                    }
                }
            }
        }
        if (statusCode != 200) {
            throw new OllamaBaseException(responseBuffer.toString());
        } else {
            long endTime = System.currentTimeMillis();
            return new OllamaResult(responseBuffer.toString().trim(), endTime - startTime, statusCode);
        }
    }

    /**
     * Ask a question to a model running on Ollama server and get a callback handle that can be used
     * to check for status and get the response from the model later. This would be an
     * async/non-blocking call.
     *
     * @param ollamaModelType the ollama model to ask the question to
     * @param promptText the prompt/question text
     * @return the ollama async result callback handle
     */
    public OllamaAsyncResultCallback askAsync(String ollamaModelType, String promptText) {
        OllamaRequestModel ollamaRequestModel = new OllamaRequestModel(ollamaModelType, promptText);
        HttpClient httpClient = HttpClient.newHttpClient();
        URI uri = URI.create(this.host + "/api/generate");
        OllamaAsyncResultCallback ollamaAsyncResultCallback =
                new OllamaAsyncResultCallback(httpClient, uri, ollamaRequestModel, requestTimeoutSeconds);
        ollamaAsyncResultCallback.start();
        return ollamaAsyncResultCallback;
    }

    private String makeRequest(OllamaPrompt ollamaPrompt, String message) {
        String prompt = ollamaPrompt.generate();
        ProjectLife.getPlugin(ProjectLife.class).getLogger().info("\n\n***Prompt: \n" + prompt + "\n\n");
        String response;
        try {
            response =  String.valueOf(ask(ollamaPrompt.getModel(), prompt.replace("\\\\{\\user*\\\\}", from)));
        } catch (OllamaBaseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private String parseResponse(String from, String characterId, String response) {
        if (response.equalsIgnoreCase("null")) {
            Bukkit.getLogger().warning(ChatColor.RED + "Response was null.");
        }
        JsonObject jsonResponseObject = JsonParser.parseString(response).getAsJsonObject();

        String trimmedResponse = StringUtils.trimStringAfterChar(jsonResponseObject.get("response").getAsString(), from + ":");


        if (trimmedResponse.contains(characterId + ":"))
            trimmedResponse = trimmedResponse.replace(characterId + ":", "");

        if (trimmedResponse.contains("\\\\{\\" + characterId + "*\\\\}"))
            trimmedResponse = trimmedResponse.replace("\\\\{\\" + characterId + "*\\\\}", "");

        if (trimmedResponse.contains("\\\\{\\user*\\\\}"))
            trimmedResponse = trimmedResponse.replace("\\\\{\\user*\\\\}", from);

        if (trimmedResponse.contains("\\\\{\\" + characterId + "*\\\\}:"))
            trimmedResponse = trimmedResponse.replace("\\\\{\\" + characterId + "*\\\\}:", "");

        if (trimmedResponse.contains("\n"))
            trimmedResponse = trimmedResponse.replace("\n", "");

        if (trimmedResponse.startsWith(" "))
            trimmedResponse = trimmedResponse.replaceFirst(" ", "");

        return trimmedResponse;
    }
}
