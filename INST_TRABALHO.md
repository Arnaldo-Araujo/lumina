### O trabalho final da disciplina consiste em desenvolver um sistema RAG baseando-se no exemplo 6 do tutorial: https://quarkus.io/quarkus-workshop-langchain4j/section-1/step-06/

### Requisitos
**Contexto do RAG:** alterar o contexto do RAG, ou seja, selecionar documentos para que um novo chat possa ser implementado.

**Suporte multiformato:** o sistema deve suportar arquivos .txt, .docx e .pdf. 

**Dica:** utilizar as próprias bibliotecas do Quarkus LangChain4j.

**System Message:** alterar a System Message para que o chat se comporte adequadamente ao contexto escolhido.
Logging dos chunks recuperados em banco de dados: implementar a persistência, em banco de dados, dos trechos (chunks) recuperados pelo retriever a cada interação do usuário. Para cada consulta, devem ser registrados ao menos: pergunta do usuário, chunks recuperados, score de similaridade, documento de origem e timestamp. Sugere-se utilizar Hibernate ORM with Panache e um banco relacional (PostgreSQL ou H2).
