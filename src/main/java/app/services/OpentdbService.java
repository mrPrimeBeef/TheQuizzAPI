package app.services;

import app.dtos.QuestionBody;
import app.dtos.QuestionDTO;
import app.entities.Question;
import app.entities.enums.Difficulty;
import app.exceptions.ApiException;
import app.utils.ApiReader;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class OpentdbService {
    // https://opentdb.com/api_config.php

    public List<Question> getComputerSienceQuestions() {
        List<Question> questionsList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // hent ny token https://opentdb.com/api_token.php?command=request
        String tokenResponse = ApiReader.getDataFromUrl("https://opentdb.com/api_token.php?command=request");
        String token;
        try {
            JsonNode tokenNode = objectMapper.readTree(tokenResponse);
            token = tokenNode.get("token").asText();
        } catch (Exception e) {
            throw new ApiException("Failed to parse token from API response", e);
        }

        String url = "https://opentdb.com/api.php?amount=50&category=18&token=" + token;

        try {
            for (int i = 0; i < 4; i++) {
                String json = ApiReader.getDataFromUrl(url);
                if (json == null) {
                    throw new ApiException("Failed to fetch data from API");
                }

                QuestionDTO response = objectMapper.readValue(json, QuestionDTO.class);
                for (QuestionBody b : response.results()) {
                    Difficulty difficulty = Difficulty.valueOf(b.difficulty().toUpperCase());
                    questionsList.add(new Question(b.question(), b.correct_answer(), b.incorrect_answers(), b.category(), difficulty));
                }
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("There is an error in getting the API in getComputerSienceQuestions()");
        }
        return questionsList;
    }
}