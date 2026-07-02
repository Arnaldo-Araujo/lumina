package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.quarkus.workshop.agents.MultiAgentService;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import io.smallrye.common.annotation.Blocking;

@WebSocket(path = "/customer-support-agent")
public class MultiAgentWebSocket {

    private final MultiAgentService multiAgentService;
    private final WebSocketConnection connection;

    public MultiAgentWebSocket(MultiAgentService multiAgentService, WebSocketConnection connection) {
        this.multiAgentService = multiAgentService;
        this.connection = connection;
    }

    @OnOpen
    public String onOpen() {
        return "👋 Olá! Antes de tudo, quero agradeçer e me despedir temporariamento do professor Rodrigo e Fabio e agradecer aos dois pelos ensinamentos passados, acredito que cresci bastante Agora pode testar fazendo perguntas sobre o processo anexado, nos 3 tipos de agentes selecionados ao lado. ";
    }

    @OnTextMessage
    @Blocking
    public void onTextMessage(String message) {
        String persona = "Especialista";
        String userQuestion = message;

        if (message.startsWith("[PERSONA:")) {
            int closeBracket = message.indexOf("]");
            if (closeBracket != -1) {
                persona = message.substring(9, closeBracket);
                userQuestion = message.substring(closeBracket + 1).trim();
            }
        }

        multiAgentService.chat(connection.id(), persona, userQuestion)
                .subscribe().with(
                        item -> connection.sendTextAndAwait(item),
                        failure -> connection.sendTextAndAwait("❌ Ocorreu um erro: " + failure.getMessage())
                );
    }
}
