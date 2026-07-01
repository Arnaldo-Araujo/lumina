# Histórico de Prompts e Interações (PROMPT.md)

Este arquivo documenta o histórico de todos os comandos (prompts) fornecidos ao agente de Inteligência Artificial para a construção e configuração do projeto **Lumina**, descrevendo a intenção e o objetivo por trás de cada ação. Conforme novas instruções forem dadas, elas deverão ser adicionadas no final desta lista.

---

### 1. Sincronização Inicial
**Prompt:**
> `/goal sincronize esse projeto com o seguinte do github : https://github.com/Arnaldo-Araujo/lumina.git`

**Intenção:** 
Conectar o projeto base local (advindo do workshop) com o repositório remoto recém-criado no GitHub. Também foi necessário configurar as regras de privacidade globais do agente, como a criação e adição do `antigravity.config.json` e o diretório `.agents` no arquivo `.gitignore`.

---

### 2. Renomeação do Projeto
**Prompt:**
> `/goal modifique o nome do projeto para Lumina`

**Intenção:** 
Adequar a identidade do projeto alterando o `artifactId` no arquivo `pom.xml` e atualizando o título principal no arquivo `README.md`, garantindo coerência no repositório.

---

### 3. Configuração Básica de Agentes
**Prompt:**
> `/goal faça as referencias ao agentes no arquivo antigravity`

**Intenção:** 
Estabelecer formalmente a ligação entre as configurações de Inteligência Artificial (`antigravity.config.json`) e a base de conhecimento local do projeto (`.agents/AGENTS.md`), permitindo que as regras do workspace sejam carregadas e seguidas nas próximas interações.

---

### 4. Estruturação de Base de Conhecimento (Skills)
**Prompt:**
> `/goal pesquise na internet, as melhores skills existentes e escreva elas dentro da pasta skills e relacione onde deve ser relacionada.`

**Intenção:** 
Dotar o agente de contexto especializado em RAG, Quarkus e LangChain4j no cenário de 2025/2026. A pesquisa resultou na criação de 4 *skills* fundamentais (RAG, ingestão multiformato, logging no Panache e comandos de build/run) baseadas em documentações atualizadas, guiando os próximos passos de desenvolvimento de maneira robusta.

---

### 5. Documentação e Especificação
**Prompt:**
> `/goal a partir de INST_TRABALHO.md crie specs do projeto em todas as areas, e adicione em README.md tudo que estamos fazendo e como estamos fazendo.`

**Intenção:** 
Converter os requisitos brutos iniciais do trabalho em um guia arquitetural detalhado (`SPECS.md`). Adicionalmente, buscou estruturar o `README.md` como uma vitrine e manual profissional do projeto, mapeando o progresso de cada tarefa e explicando a abordagem de desenvolvimento com agentes autônomos.

---

### 6. Padrões Profissionais de Git e GitHub
**Prompt:**
> `/goal configure todos os agentes a trabalhar com github dentre do branchs corretos, e crie um agente especialista em github, em fazer commits em português, em fazer pull request, revisar codigo e fazer todas as tarefas necessarias no github de um projeto real.`

**Intenção:** 
Blindar a branch `main` contra alterações diretas. A regra impôs ao sistema de IA o fluxo real de trabalho de engenharia de software: criação obrigatória de *branches* (como `feature/` e `docs/`), uso do padrão Semantic Commits no idioma Português, e orientações rigorosas para a revisão de código.

---

### 7. Histórico e Rastreabilidade (Este arquivo)
**Prompt:**
> `/goal Faça referencia no README.md (um link que leve ao arquivo) a um arquivo chamado PROMTP.md e ne organize todos os prompts para chegar ao resultado atual, depois vai acrescentando conforme vou dando novos prompts. E descreva a intenção de cada prompt.`

**Intenção:** 
Garantir total rastreabilidade sobre a construção da aplicação. A criação deste documento (`PROMPT.md`) serve como um diário de bordo do par programação entre humano e IA, evidenciando as decisões tomadas e facilitando o entendimento de como o projeto evoluiu de um tutorial para a implementação atual.
