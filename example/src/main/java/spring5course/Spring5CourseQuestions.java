package spring5course;

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

class Spring5CourseQuestions {

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
           soureFile = readFileToString("c:/projects/openai-java/example/resources/spring5_big_picture_outline.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(),
                "Create a summary capturing the main points and key details of the transscript described between the triple backticks \\\n" +
                        "       \n" +
                        "```" + soureFile + "```\n" +
                        //". Write a headline, a subtitle and two paragraphs. Format everything in markdown.");
                        ".Tell me all the important things provided in this text. Write a headline, a subtitle and two paragraphs." +
                        "Format everything in markdown." +
                        "After that answer a question about the original text: how can I get data from a database in Spring?"

        );
        messages.add(systemMessage);


        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-16k")
                .messages(messages)
                .n(1)
                .logitBias(new HashMap<>())
                .build();


        ChatCompletionResult result = service.createChatCompletion(chatCompletionRequest);

        String resultFromGpt = result.getChoices().stream()
                .map(choice -> choice.getMessage().getContent())
                .collect(Collectors.joining());
        System.out.println(resultFromGpt);

        try {
            writeStringToFile(resultFromGpt, "c:/projects/openai-java/example/resources/spring5_big_picture_samenvatting.md");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        service.shutdownExecutor();
    }
}
