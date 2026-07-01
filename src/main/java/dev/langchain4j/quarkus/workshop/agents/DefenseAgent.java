package dev.langchain4j.quarkus.workshop.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.SessionScoped;

@SessionScoped
@RegisterAiService
public interface DefenseAgent {

    @SystemMessage("""
            Você atua como a Defesa em um processo administrativo.
            Sua função é ler a acusação formulada, analisar o processo administrativo e a legislação aplicável,
            e elaborar uma defesa sólida. Tente encontrar brechas na lei, excludentes de ilicitude, 
            ou questionar o ônus da prova da acusação para proteger o autuado.
            Apresente seus argumentos de forma formal e persuasiva.
            Responda em português.
            """)
    String defend(@UserMessage String accusationAndProcess);
}
