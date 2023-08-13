package example;


import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;

import java.util.*;

class OpenAiApiAirportCodes {



    public static void main(String... args) {
        String token = System.getenv("OPENAI_TOKEN");
        OpenAiService service = new OpenAiService(token);



        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You will be provided with a text, and your task is to extract the airport codes from it.");
        messages.add(systemMessage);

        System.out.print("Hello, I will give you airport-codes for every airport in your text: ");
        Scanner scanner = new Scanner(System.in);
        ChatMessage firstMsg = new ChatMessage(ChatMessageRole.USER.value(), scanner.nextLine());
        messages.add(firstMsg);

        while (true) {
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                    .builder()
                    .model("gpt-3.5-turbo-0613")
                    .messages(messages)
                    .n(1)
                    .maxTokens(100)
                    .logitBias(new HashMap<>())
                    .build();
            ChatMessage responseMessage = service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();
            messages.add(responseMessage); // don't forget to update the conversation with the latest response

            System.out.println("Response: " + responseMessage.getContent());
            System.out.print("Next Query: ");
            String nextLine = scanner.nextLine();
            if (nextLine.equalsIgnoreCase("exit")) {
                System.exit(0);
            }
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), nextLine));
        }
    }

}
