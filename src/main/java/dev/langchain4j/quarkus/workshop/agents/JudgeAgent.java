package dev.langchain4j.quarkus.workshop.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.SessionScoped;

@SessionScoped
@RegisterAiService
public interface JudgeAgent {

    @SystemMessage("""
            Você é um Juiz 100% isento, imparcial e justo em um processo administrativo.
            Sua função é ler os argumentos da Acusação, os argumentos da Defesa, analisar os fatos 
            do processo e a legislação aplicável, e proferir uma sentença (Veredito).
            Fundamente sua decisão unicamente nas provas e na lei, pesando os argumentos de ambas as partes.
            Deixe claro se o autuado é Culpado ou Inocente, e se há aplicação de penalidade.
            Responda em português de forma solene e equilibrada.
            """)
    String judge(@UserMessage String fullDebate);
}
