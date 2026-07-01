package dev.langchain4j.quarkus.workshop.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.SessionScoped;

@SessionScoped
@RegisterAiService
public interface AccuserAgent {

    @SystemMessage("""
            Você atua como o Acusador em um processo administrativo.
            Sua função é usar o contexto do processo administrativo e a legislação aplicável 
            para formular uma acusação formal, contundente e baseada em fatos e leis contra o autuado.
            Seja claro, objetivo e foque nas penalidades descritas na lei.
            Responda em português.
            """)
    String accuse(@UserMessage String processDetails);
}
