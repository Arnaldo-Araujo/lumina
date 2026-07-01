# ✨ Lumina

> **Sistema de Chat com RAG (Retrieval-Augmented Generation)**
> Projeto final da disciplina de Tópicos de IA — IFRS

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/projects/jdk/21/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.37-blue)](https://quarkus.io/)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.12-green)](https://docs.langchain4j.dev/)

---

## 📋 Sobre o Projeto

O **Lumina** é um assistente de chat inteligente que utiliza Retrieval-Augmented Generation (RAG) para responder perguntas com base em documentos fornecidos. Construído sobre **Quarkus** e **LangChain4j**, o sistema ingere documentos em múltiplos formatos (`.txt`, `.pdf`, `.docx`), vetoriza os conteúdos e utiliza busca semântica para fornecer respostas fundamentadas exclusivamente nos documentos.

O projeto é baseado no [Step 06 do Quarkus LangChain4j Workshop](https://quarkus.io/quarkus-workshop-langchain4j/section-1/step-06/) e foi estendido com funcionalidades adicionais.

---

## 🏗️ Arquitetura

```
┌────────────────────────────────────────────────────────┐
│                       LUMINA                           │
│                                                        │
│   Usuário ──▶ WebSocket ──▶ AI Service (GPT-4o)       │
│                                  │                     │
│                          RetrievalAugmentor             │
│                           ┌──────┴──────┐              │
│                     ContentRetriever   ChunkLogger      │
│                        (pgvector)      (Panache)        │
│                           │                │            │
│                    ┌──────┴────────────────┴──────┐     │
│                    │         PostgreSQL           │     │
│                    │  embeddings  │  chunk_log    │     │
│                    └─────────────────────────────┘     │
│                                                        │
│   RAG Ingestion: .txt/.pdf/.docx → Chunks → Embeddings │
└────────────────────────────────────────────────────────┘
```

### Fluxo do Sistema

1. **Startup** — Documentos são carregados, divididos em chunks, vetorizados e armazenados no pgvector
2. **Usuário envia mensagem** — Via interface de chat (WebSocket)
3. **Busca semântica** — Os 3 chunks mais relevantes são recuperados do pgvector
4. **Injeção de contexto** — Chunks são inseridos no prompt com instrução para usar apenas eles
5. **LLM responde** — GPT-4o gera resposta em streaming baseada no contexto
6. **Logging** — Chunks recuperados, scores e metadata são persistidos no banco

---

## 🚀 Stack Tecnológica

| Componente | Tecnologia | Versão |
|---|---|---|
| **Framework** | Quarkus | 3.37.0 |
| **Linguagem** | Java | 21 |
| **AI Framework** | Quarkus LangChain4j | 1.12.0.CR2 |
| **LLM** | OpenAI GPT-4o | — |
| **Embedding** | BGE-Small-EN-Quantized (ONNX, local) | dim=384 |
| **Vector Store** | PostgreSQL + pgvector | — |
| **ORM** | Hibernate ORM with Panache | — |
| **Comunicação** | WebSocket (`quarkus-websockets-next`) | — |
| **Frontend** | Vaadin Web Components + wc-chatbot | — |

---

## ✅ Requisitos do Trabalho e Status

| # | Requisito | Status | Detalhes |
|---|-----------|--------|----------|
| 1 | **Contexto do RAG** — Alterar documentos e contexto do chat | 🔲 A fazer | Selecionar novos documentos para o RAG |
| 2 | **Suporte Multiformato** — `.txt`, `.pdf`, `.docx` | 🔲 A fazer | Parsers Apache PDFBox e POI |
| 3 | **System Message** — Adequar ao contexto escolhido | 🔲 A fazer | Modificar `@SystemMessage` |
| 4 | **Logging de Chunks** — Persistir em banco com Panache | 🔲 A fazer | Entidade `ChunkLog`, Hibernate ORM |
| 5 | **Chat via WebSocket** (base do workshop) | ✅ Pronto | Streaming com `Multi<String>` |

---

## 📁 Estrutura do Projeto

```
lumina/
├── src/main/
│   ├── docker/                          # Dockerfiles
│   ├── java/.../workshop/
│   │   ├── CustomerSupportAgent.java    # AI Service + @SystemMessage
│   │   ├── CustomerSupportAgentWebSocket.java  # WebSocket endpoint
│   │   ├── ImportmapResource.java       # Import Maps dinâmicos
│   │   ├── RagIngestion.java            # Pipeline de ingestão de docs
│   │   └── RagRetriever.java            # Retrieval Augmentor + ContentInjector
│   └── resources/
│       ├── META-INF/resources/          # Frontend (HTML, CSS, JS)
│       ├── application.properties       # Configuração Quarkus
│       └── rag/                         # Documentos para o RAG
├── SPECS.md                             # Especificações detalhadas
├── INST_TRABALHO.md                     # Requisitos do trabalho
├── pom.xml                              # Maven config
└── README.md                            # Este arquivo
```

---

## ⚙️ Como Executar

### Pré-requisitos

- **Java 21+**
- **Docker** (ou Podman) — para o PostgreSQL via Dev Services
- **Chave da API OpenAI**

### 1. Clonar o repositório

```bash
git clone https://github.com/Arnaldo-Araujo/lumina.git
cd lumina
```

### 2. Configurar variáveis de ambiente

```bash
export OPENAI_API_KEY=sk-sua-chave-aqui
```

Ou crie um arquivo `.env` na raiz do projeto:
```
OPENAI_API_KEY=sk-sua-chave-aqui
```

### 3. Executar em modo desenvolvimento

```bash
./mvnw quarkus:dev
```

> **Nota:** O Quarkus Dev Services cria automaticamente um container PostgreSQL com pgvector.
> Certifique-se de que o Docker esteja rodando.

### 4. Acessar a aplicação

- **Chat UI:** [http://localhost:8080](http://localhost:8080)
- **Dev UI:** [http://localhost:8080/q/dev](http://localhost:8080/q/dev)

---

## 🔧 Configuração

As configurações principais ficam em `src/main/resources/application.properties`:

```properties
# LLM (OpenAI)
quarkus.langchain4j.openai.api-key=${OPENAI_API_KEY}
quarkus.langchain4j.openai.chat-model.model-name=gpt-4o
quarkus.langchain4j.openai.chat-model.temperature=1.0
quarkus.langchain4j.openai.chat-model.max-completion-tokens=1000

# Embedding Model (local, in-process)
quarkus.langchain4j.embedding-model.provider=dev.langchain4j.model.embedding.onnx.bgesmallenq.BgeSmallEnQuantizedEmbeddingModel

# Vector Store (PGVector — dimensão DEVE ser 384)
quarkus.langchain4j.pgvector.dimension=384

# Caminho dos documentos RAG
rag.location=src/main/resources/rag
```

---

## 📐 Como Estamos Desenvolvendo

### Metodologia

O desenvolvimento é assistido por um **agente de IA** (Antigravity) configurado com skills especializadas para o projeto. As regras e instruções do agente ficam no diretório `.agents/` (protegido no `.gitignore`).

### Skills do Agente

O projeto utiliza 4 skills customizadas que guiam o desenvolvimento:

| Skill | Propósito |
|-------|-----------|
| `quarkus-langchain4j-rag` | Referência da arquitetura RAG, padrões de código e checklist de qualidade |
| `multiformat-document-ingestion` | Guia para implementação de parsers PDF/DOCX/TXT |
| `chunk-logging-panache` | Modelo de entidade, integração Panache e padrão de logging |
| `quarkus-dev-build-run` | Comandos de execução, resolução de problemas, Dockerfiles |

### Pipeline de Desenvolvimento

1. **Specs** → Requisitos extraídos do `INST_TRABALHO.md` e documentados em `SPECS.md`
2. **Skills** → Guias de implementação criados em `.agents/skills/`
3. **Implementação** → Código Java seguindo os padrões do Quarkus LangChain4j
4. **Verificação** → Build com Maven, testes com Dev Services
5. **Sincronização** → Git push para o repositório GitHub

---

## 📜 Histórico de Interações (Prompts)

Toda a trilha de desenvolvimento deste repositório, realizada em co-criação com um agente autônomo de Inteligência Artificial, está rastreada de forma transparente. Você pode consultar todos os comandos solicitados e o motivo de cada decisão técnica acessando o nosso **[Histórico de Prompts (PROMPT.md)](PROMPT.md)**.

---

## 📚 Referências

- [Quarkus LangChain4j Workshop](https://quarkus.io/quarkus-workshop-langchain4j/)
- [Quarkus LangChain4j Extension](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html)
- [LangChain4j Documentation](https://docs.langchain4j.dev/)
- [Quarkus Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [pgvector Extension](https://github.com/pgvector/pgvector)

---

## 📄 Licença

Este projeto está sob a licença definida no arquivo [LICENSE](LICENSE).
