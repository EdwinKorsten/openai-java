package example;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class ChatCompletionExample {
    public static void main(String... args) {
        String token = System.getenv("OPENAI_TOKEN");
        OpenAiService service = new OpenAiService(token, Duration.ofSeconds(30));

        System.out.println("\nCreating completion...");

        String textToSummarize="You should express what you want a model to do by \\ \n" +
                "providing instructions that are as clear and \\ \n" +
                "specific as you can possibly make them. \\ \n" +
                "This will guide the model towards the desired output, \\ \n" +
                "and reduce the chances of receiving irrelevant \\ \n" +
                "or incorrect responses. Don't confuse writing a \\ \n" +
                "clear prompt with writing a short prompt. \\ \n" +
                "In many cases, longer prompts provide more clarity \\ \n" +
                "and context for the model, which can lead to \\ \n" +
                "more detailed and relevant outputs.";


                final List<ChatMessage> messages = new ArrayList<>();
                final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(),
                        "Summarize the text delimited by triple backticks \\\n" +
                                "        into a single sentence.\n" +
                                "```"+textToSummarize+"```\n" +
                                "");
                messages.add(systemMessage);
                ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                        .builder()
                        .model("gpt-3.5-turbo")
                        .messages(messages)
                        .n(1)
                        .maxTokens(50)
                        .logitBias(new HashMap<>())
                        .build();

                service.streamChatCompletion(chatCompletionRequest)
                        .doOnError(Throwable::printStackTrace)
                        .blockingForEach(System.out::println);

                service.shutdownExecutor();
            }
        }
