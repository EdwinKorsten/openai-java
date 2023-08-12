package unittest;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.stream.Collectors;

class GenerateUnitTest {

    private static final String SOURCE_PATH_PART1="C:/projects/ecs-portals/src/";
    private static final String SOURCE_PATH_PART2="main/java/nl/cargonaut/ecs/portals/service/company/CompanyServiceImpl.java";

    private static final String DEST_PATH_PART2="test/java/nl/cargonaut/ecs/portals/service/company/CompanyServiceImplTest.java";



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
        OpenAiService service = new OpenAiService(token, Duration.ofSeconds(30));

        System.out.println("\nCreating completion...");

        String soureFile = null;
        try {
           // soureFile = readFileToString("c:/projects/openai-java/example/src/main/java/unittest/GenerateUnitTest.java");
            soureFile = readFileToString(SOURCE_PATH_PART1 + SOURCE_PATH_PART2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(),
                "Create a junit unit test for the java code delimited by triple backticks \\\n" +
                        "       \n" +
                        "```" + soureFile + "```\n" +
                        "");
        messages.add(systemMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .n(1)
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
            writeStringToFile(resultFile, "c:/projects/openai-java/example/src/test/java/unittest/GeneratedUnitTest.java");
            writeStringToFile(resultFile, SOURCE_PATH_PART1 + DEST_PATH_PART2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        service.shutdownExecutor();
    }
}
