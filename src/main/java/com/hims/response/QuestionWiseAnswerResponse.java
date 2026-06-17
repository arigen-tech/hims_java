package com.hims.response;

import lombok.Data;

import java.util.List;

@Data
public class QuestionWiseAnswerResponse {
    private  Long questionId;
    private String question;
    private List<AnswerResponse> answerResponse ;

     @Data
     public static class AnswerResponse{
         private  Long answerId;
         private String answerCode;
         private String answerValue;
         private Integer answerScore;


     }
}
