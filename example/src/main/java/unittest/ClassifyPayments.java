package unittest;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

class ClassifyPayments {

    // private static final String SOURCE_PATH="c:/projects/openai-java/example/resources/payments_sample.csv/";
    private static final String SOURCE_PATH="c:/projects/openai-java/example/resources/detais-payments.json/";




    public static String readFileToString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
    }

    public static void writeStringToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        // Ensure the parent directory exists before writing the file.
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    public static void main(String... args) {
        String token = System.getenv("OPENAI_TOKEN");
        OpenAiService service = new OpenAiService(token, Duration.ofSeconds(80));

        System.out.println("\nCreating completion...");

        String soureFile = null;
        try {
            soureFile = readFileToString(SOURCE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        final List<ChatMessage> messages = new ArrayList<>();
/*        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(),
                "I will provide you a csv list that is delimited by triple backticks.\\\n" +
                        "       \n" +
                        "```" + soureFile + "```\n" +
                        "Create a classification this list with all payment detail transactions. Classify into these categories: groceries, payments, charity, insurance, interest, subscriptions, income, other.  Format the result in json. Summarize the total amount per category. show for each description to which category it belongs.");
*/
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(),
                "I will provide you with a json that contains payment transactions details with date, amount subject and name fields, these are delimited by triple backticks.\\\n" +
                        "       \n" +
                        "```" + soureFile + "```\n" +
                   "Create a classification for this list with all payment detail transactions. Classify into these categories: bank fees, entertainment, food & drink, general merchandise,"+
                        " general services, income, rent & utilities, transfer in, transfer out, mortgage, medical. "+
                        "  show for each category the description and name that belong to that category."
                //" please summarize all amounts per category and format the result in json."

        );

        messages.add(systemMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-16k")
                .messages(messages)
                .n(1)
                .temperature(0.1)
                .logitBias(new HashMap<>())
                .build();


        ChatCompletionResult result = service.createChatCompletion(chatCompletionRequest);

        String resultFile = result.getChoices().stream()
                .map(choice -> choice.getMessage().getContent())
                .collect(Collectors.joining());



        int firstIndex = resultFile.indexOf("```");
        int secondIndex = resultFile.indexOf("```", firstIndex+3);
        if  (firstIndex >= 0 && secondIndex >= 0) {
            resultFile = resultFile.substring(firstIndex+3, secondIndex);
        }


        try {
            writeStringToFile(resultFile, "c:/projects/openai-java/example/resources/classification_payments_new.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        service.shutdownExecutor();
    }
}
