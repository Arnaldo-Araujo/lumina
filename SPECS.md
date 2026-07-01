# Lumina — Especificações do Projeto

> **Disciplina:** Tópicos de IA — IFRS
> **Base:** [Quarkus LangChain4j Workshop — Step 06](https://quarkus.io/quarkus-workshop-langchain4j/section-1/step-06/)

---

## 1. Visão Geral

O **Lumina** é um sistema de chat com Retrieval-Augmented Generation (RAG) construído
sobre Quarkus e LangChain4j. O sistema ingere documentos em múltiplos formatos,
vetoriza os conteúdos em um banco PostgreSQL com pgvector, e utiliza um LLM (GPT-4o)
para responder perguntas ancoradas exclusivamente nos documentos fornecidos.

---

## 2. Requisitos Funcionais

| ID   | Requisito | Status | Prioridade |
|------|-----------|--------|------------|
| RF01 | **Contexto do RAG** — Alterar o contexto do RAG selecionando novos documentos para o chat | 🔲 A fazer | Alta |
| RF02 | **Suporte Multiformato** — Ingerir documentos `.txt`, `.pdf` e `.docx` | 🔲 A fazer | Alta |
| RF03 | **System Message** — Alterar a System Message para adequar o comportamento do chat ao contexto escolhido | 🔲 A fazer | Alta |
| RF04 | **Logging de Chunks** — Persistir em banco de dados: pergunta do usuário, chunks recuperados, score de similaridade, documento de origem e timestamp | 🔲 A fazer | Alta |
| RF05 | **Chat via WebSocket** — Interface de chat em tempo real com streaming de respostas | ✅ Pronto | Alta |

---

## 3. Requisitos Não-Funcionais

| ID    | Requisito | Detalhes |
|-------|-----------|----------|
| RNF01 | **Framework** | Quarkus 3.37+ com Java 21 |
| RNF02 | **LLM Provider** | OpenAI GPT-4o via `quarkus-langchain4j-openai` |
| RNF03 | **Embedding** | BGE-Small-EN-Quantized (in-process ONNX, dim=384) |
| RNF04 | **Vector Store** | PostgreSQL + pgvector |
| RNF05 | **Persistência Relacional** | Hibernate ORM with Panache |
| RNF06 | **Comunicação** | WebSocket (`quarkus-websockets-next`) |
| RNF07 | **UI** | Vaadin Web Components + wc-chatbot |

---

## 4. Arquitetura

### 4.1 Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────────┐
│                        LUMINA                               │
│                                                             │
│  ┌──────────┐    ┌───────────────┐    ┌─────────────────┐   │
│  │ index.   │───▶│  WebSocket    │───▶│  AI Service     │   │
│  │ html     │    │  Endpoint     │    │  (@RegisterAi)  │   │
│  │ (UI)     │◀───│  (streaming)  │◀───│  + @System      │   │
│  └──────────┘    └───────────────┘    │    Message       │   │
│                                       └────────┬────────┘   │
│                                                │             │
│                                       ┌────────▼────────┐   │
│                                       │  Retrieval      │   │
│                                       │  Augmentor      │   │
│                                       │  (RAG Pipeline) │   │
│                                       └────────┬────────┘   │
│                         ┌──────────────────────┤             │
│                         │                      │             │
│                ┌────────▼────────┐    ┌────────▼────────┐   │
│                │  Content        │    │  Chunk Logger   │   │
│                │  Retriever      │    │  (Panache)      │   │
│                │  (pgvector)     │    │                 │   │
│                └────────┬────────┘    └────────┬────────┘   │
│                         │                      │             │
│                ┌────────▼────────────────────────▼────────┐  │
│                │            PostgreSQL                    │  │
│                │   ┌─────────────┐  ┌─────────────────┐  │  │
│                │   │  embeddings │  │    chunk_log     │  │  │
│                │   │  (pgvector) │  │  (relacional)    │  │  │
│                │   └─────────────┘  └─────────────────┘  │  │
│                └─────────────────────────────────────────┘  │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                  RAG Ingestion                        │  │
│  │  Documentos (.txt, .pdf, .docx)                       │  │
│  │  → Parser → Splitter → Embedding → PGVector           │  │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 Fluxo de Dados

```
Startup:
  Documentos (rag/) → FileSystemDocumentLoader → DocumentSplitter
  → EmbeddingModel (BGE) → EmbeddingStore (pgvector)

Runtime:
  Usuário → WebSocket → AI Service → RetrievalAugmentor
  → ContentRetriever (pgvector search) → ContentInjector (prompt + chunks)
  → LLM (GPT-4o) → Streaming response → WebSocket → Usuário
  → ChunkLogger → PostgreSQL (chunk_log table)
```

---

## 5. Especificações por Área

### 5.1 🗂️ Ingestão de Documentos

| Item | Detalhe |
|------|---------|
| **Classe** | `RagIngestion.java` |
| **Trigger** | `@Observes StartupEvent` (executa ao iniciar a aplicação) |
| **Loader** | `FileSystemDocumentLoader.loadDocumentsRecursively()` |
| **Caminho** | Configurável via `rag.location` em `application.properties` |
| **Splitter** | `DocumentSplitters.recursive(100, 25, HuggingFaceTokenCountEstimator)` |
| **Limpeza** | `store.removeAll()` antes da ingestão (modo demo) |

**Parsers por formato:**

| Formato | Parser | Dependência |
|---------|--------|-------------|
| `.txt` | `TextDocumentParser` | Incluso |
| `.pdf` | `ApachePdfBoxDocumentParser` | `langchain4j-document-parser-apache-pdfbox` |
| `.docx` | `ApachePoiDocumentParser` | `langchain4j-document-parser-apache-poi` |

### 5.2 🔍 Retrieval (Busca Vetorial)

| Item | Detalhe |
|------|---------|
| **Classe** | `RagRetriever.java` |
| **Tipo** | `@Produces @ApplicationScoped RetrievalAugmentor` |
| **Retriever** | `EmbeddingStoreContentRetriever` com `maxResults(3)` |
| **ContentInjector** | Customizado — insere chunks no prompt com instrução para usar apenas o contexto fornecido |
| **Dimensão** | 384 (BGE-Small-EN-Quantized) |

### 5.3 🤖 AI Service (Agente)

| Item | Detalhe |
|------|---------|
| **Interface** | `CustomerSupportAgent.java` |
| **Anotações** | `@SessionScoped`, `@RegisterAiService` |
| **System Message** | Deve ser alterada conforme o novo contexto escolhido |
| **Retorno** | `Multi<String>` (streaming reativo via Mutiny) |
| **Modelo** | GPT-4o (`quarkus.langchain4j.openai.chat-model.model-name`) |

### 5.4 🌐 WebSocket Endpoint

| Item | Detalhe |
|------|---------|
| **Classe** | `CustomerSupportAgentWebSocket.java` |
| **Path** | `/customer-support-agent` |
| **@OnOpen** | Mensagem de boas-vindas |
| **@OnTextMessage** | Delega para `CustomerSupportAgent.chat()` |
| **Protocolo** | WebSocket bidirecional com streaming |

### 5.5 💾 Logging de Chunks (A Implementar)

| Item | Detalhe |
|------|---------|
| **Entidade** | `ChunkLog` (extends `PanacheEntity`) |
| **Tabela** | `chunk_log` |
| **Campos** | `userQuery`, `chunkContent`, `similarityScore`, `sourceDocument`, `createdAt` |
| **Tecnologia** | Hibernate ORM with Panache |
| **Banco** | PostgreSQL (mesmo do pgvector) |

### 5.6 🎨 Frontend (UI)

| Item | Detalhe |
|------|---------|
| **Arquivo** | `index.html` |
| **Componentes** | `<demo-title>`, `<demo-chat>`, `<chat-bot>` (wc-chatbot) |
| **Import Maps** | Gerados dinamicamente por `ImportmapResource.java` |
| **Estilo** | CSS customizado com variáveis (cores rosa/coral) |
| **Fonte** | Red Hat Text |

---

## 6. Stack de Dependências

### 6.1 BOMs (Gerenciamento de Versões)

| BOM | Versão |
|-----|--------|
| `quarkus-bom` | 3.37.0 |
| `quarkus-langchain4j-bom` | 1.12.0.CR2 |

### 6.2 Dependências Atuais

| Dependência | Propósito |
|-------------|-----------|
| `quarkus-langchain4j-openai` | Integração com OpenAI |
| `langchain4j-embeddings-bge-small-en-q` | Modelo de embedding local |
| `quarkus-langchain4j-pgvector` | Vector store PostgreSQL |
| `quarkus-rest` | REST endpoints |
| `quarkus-websockets-next` | WebSocket |
| `importmap` | Import Maps dinâmicos |
| `vaadin-webcomponents` | Componentes UI |
| `es-module-shims` | Polyfill ES Modules |
| `wc-chatbot` | Web Component de chatbot |

### 6.3 Dependências a Adicionar

| Dependência | Propósito | Para Requisito |
|-------------|-----------|----------------|
| `langchain4j-document-parser-apache-pdfbox` | Parser PDF | RF02 |
| `langchain4j-document-parser-apache-poi` | Parser DOCX | RF02 |
| `quarkus-hibernate-orm-panache` | ORM com Active Record | RF04 |
| `quarkus-jdbc-postgresql` | Driver JDBC PostgreSQL | RF04 |

---

## 7. Configuração

### 7.1 `application.properties` (Atual)

```properties
quarkus.langchain4j.openai.api-key=${OPENAI_API_KEY}
quarkus.langchain4j.openai.chat-model.model-name=gpt-4o
quarkus.langchain4j.openai.chat-model.temperature=1.0
quarkus.langchain4j.openai.chat-model.max-completion-tokens=1000
quarkus.langchain4j.pgvector.dimension=384
quarkus.langchain4j.embedding-model.provider=dev.langchain4j.model.embedding.onnx.bgesmallenq.BgeSmallEnQuantizedEmbeddingModel
rag.location=src/main/resources/rag
```

### 7.2 Configurações a Adicionar (RF04)

```properties
quarkus.hibernate-orm.database.generation=update
%dev.quarkus.hibernate-orm.log.sql=true
```

---

## 8. Estrutura de Diretórios

```
lumina/
├── .agents/                          # Configuração do agente (gitignore)
│   ├── AGENTS.md                     # Regras locais do workspace
│   └── skills/                       # Skills do agente
│       ├── quarkus-langchain4j-rag/
│       ├── multiformat-document-ingestion/
│       ├── chunk-logging-panache/
│       └── quarkus-dev-build-run/
├── .mvn/wrapper/                     # Maven Wrapper
├── src/main/
│   ├── docker/                       # Dockerfiles (JVM, Native)
│   ├── java/dev/langchain4j/quarkus/workshop/
│   │   ├── CustomerSupportAgent.java       # AI Service (interface)
│   │   ├── CustomerSupportAgentWebSocket.java  # WebSocket endpoint
│   │   ├── ImportmapResource.java          # Import Maps REST
│   │   ├── RagIngestion.java               # Pipeline de ingestão
│   │   └── RagRetriever.java               # Retrieval Augmentor
│   └── resources/
│       ├── META-INF/resources/
│       │   ├── index.html                  # Frontend do chat
│       │   ├── components/                 # Web Components JS
│       │   ├── fonts/                      # Red Hat Font
│       │   └── icons/                      # Font Awesome
│       ├── application.properties          # Configurações Quarkus
│       └── rag/                            # Documentos para ingestão
│           └── miles-of-smiles-terms-of-use.txt
├── antigravity.config.json           # Config do agente (gitignore)
├── INST_TRABALHO.md                  # Requisitos do trabalho
├── pom.xml                           # Maven (Quarkus 3.37 + LangChain4j 1.12)
├── mvnw / mvnw.cmd                  # Maven Wrapper
└── README.md                         # Documentação do projeto
```
