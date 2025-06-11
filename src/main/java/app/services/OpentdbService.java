package app.services;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.dtos.QuestionResponseBody;
import app.dtos.QuestionResponseDTO;
import app.entities.Question;
import app.entities.enums.Difficulty;
import app.exceptions.ApiException;
import app.utils.ApiReader;

public class OpentdbService {
    // https://opentdb.com/api_config.php

    public List<Question> getQuestionsFromURL(String url, Integer times) {
        List<Question> questionsList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String token = getToken(objectMapper);

        // String urlForRequest = url + token;
        // String urlForRequest = url;

        boolean hasMoreQuestions = true; // Flag til at tjekke, om der stadig er spørgsmål

        Integer count = 0;
        while (count < times) {
            try {
                String json = ApiReader.getDataFromUrl(url);
                if (json != null) {
                    QuestionResponseDTO response = objectMapper.readValue(json, QuestionResponseDTO.class);

                    // Stop hvis API'et ikke returnerer flere spørgsmål
                    if (response.results().isEmpty()) {
                        System.out.println("Ingen flere spørgsmål fundet. Stopper...");
                        hasMoreQuestions = false;
                        break;
                    }
                    // Tilføj de hentede spørgsmål til listen
                    for (QuestionResponseBody b : response.results()) {
                        Difficulty difficulty = Difficulty.valueOf(b.difficulty().toUpperCase());
                        questionsList.add(new Question(b.question(), b.correct_answer(), b.incorrect_answers(), b.category(), difficulty));
                    }
                }
            } catch (Exception e) {
                throw new ApiException("Error calling api", e);
            }
            // Vent 5 sekunder før næste forsøg
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            count++;
        }
        return questionsList;
    }

    private static String getToken(ObjectMapper objectMapper) {
        // hent ny token https://opentdb.com/api_token.php?command=request
        String tokenResponse = ApiReader.getDataFromUrl("https://opentdb.com/api_token.php?command=request");
        String token;
        try {
            JsonNode tokenNode = objectMapper.readTree(tokenResponse);
            token = tokenNode.get("token").asText();
        } catch (Exception e) {
            throw new ApiException("Failed to parse token from API response", e);
        }
        return token;
    }
}