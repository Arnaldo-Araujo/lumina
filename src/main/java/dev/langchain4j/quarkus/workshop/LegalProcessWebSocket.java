package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.quarkus.workshop.agents.AccuserAgent;
import dev.langchain4j.quarkus.workshop.agents.DefenseAgent;
import dev.langchain4j.quarkus.workshop.agents.JudgeAgent;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import io.smallrye.common.annotation.Blocking;

@WebSocket(path = "/legal-process")
public class LegalProcessWebSocket {

    private final AccuserAgent accuserAgent;
    private final DefenseAgent defenseAgent;
    private final JudgeAgent judgeAgent;
    private final WebSocketConnection connection;

    public LegalProcessWebSocket(AccuserAgent accuserAgent, DefenseAgent defenseAgent, JudgeAgent judgeAgent, WebSocketConnection connection) {
        this.accuserAgent = accuserAgent;
        this.defenseAgent = defenseAgent;
        this.judgeAgent = judgeAgent;
        this.connection = connection;
    }

    @OnOpen
    public String onOpen() {
        return "⚖️ Tribunal Digital iniciado. Forneça o fato inicial ou prova para iniciarmos o julgamento.";
    }

    @OnTextMessage
    @Blocking
    public void onTextMessage(String message) {
        connection.sendTextAndAwait("👨‍⚖️ **TRIBUNAL INICIADO**\nAnalisando o fato: " + message);
        
        connection.sendTextAndAwait("\n\n---\n**ACUSAÇÃO (Formulando argumentos...)**\n");
        String accusation = accuserAgent.accuse(message);
        connection.sendTextAndAwait(accusation);

        connection.sendTextAndAwait("\n\n---\n**DEFESA (Analisando acusação...)**\n");
        String defense = defenseAgent.defend("Fato original: " + message + "\n\nAcusação: " + accusation);
        connection.sendTextAndAwait(defense);

        connection.sendTextAndAwait("\n\n---\n**JUIZ (Avaliando o caso para veredito...)**\n");
        String debate = "Fato: " + message + "\n\nAcusação:\n" + accusation + "\n\nDefesa:\n" + defense;
        String verdict = judgeAgent.judge(debate);
        connection.sendTextAndAwait(verdict);
        
        connection.sendTextAndAwait("\n\n---\n⚖️ **SESSÃO ENCERRADA** ⚖️");
    }
}
