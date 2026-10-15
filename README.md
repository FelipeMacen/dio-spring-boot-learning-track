# Budgeting AI: API de orçamento controlada por voz (Spring Boot + Spring AI)

Projeto do **Desafio de Projeto com Spring AI** da DIO, baseado no repositório do expert
[thpoiani / DIO Spring Boot Learning Track](https://github.com/digitalinnovationone/dio-spring-boot-learning-track)
(pasta `05-spring-ai`), com uma melhoria própria: **validação de transações antes de salvar**.

---

## 📌 O que o projeto faz

É uma API de orçamento que entende **comandos de voz**. A pessoa envia um áudio dizendo, por exemplo,
*"gastei 50 reais no mercado"* ou *"quais foram meus gastos com farmácia?"*, e a API registra ou consulta
a transação e responde **também em áudio**.

Fluxo principal:

1. O cliente envia um arquivo de áudio para `POST /transactions/ai`.
2. O áudio é transcrito para texto (Whisper, via `TranscriptionModel`).
3. O texto é enviado ao modelo de linguagem (`ChatClient`), que conhece as *tools* da aplicação.
4. O modelo escolhe qual ferramenta chamar (`persist-transaction` ou `list-transactions-by-category`).
5. A ferramenta executa um **caso de uso real**, que salva ou consulta no banco de dados.
6. O modelo gera a resposta final em texto, convertida em MP3 (`TextToSpeechModel`) e devolvida ao cliente.

Além do fluxo por voz, a API tem endpoints REST comuns:

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/transactions` | Cria uma transação (JSON) |
| `GET` | `/transactions/{category}` | Lista transações por categoria (`GROCERIES`, `PHARMA`, `AUTO`) |
| `POST` | `/transactions/ai` | Recebe áudio e devolve áudio (fluxo de IA) |

### Organização do código

```
src/main/java/dio/budgeting
├── domain           → modelo (Transaction, Category, TransactionId) e contrato do repositório
├── application      → casos de uso (usados pelo REST e como @Tool da IA)
└── infrastructure   → controller HTTP, JPA/MySQL e integração com o Spring AI
```

A IA não acessa o banco diretamente: ela só chama os mesmos casos de uso que o REST usa.

---

## ▶️ Como executar a aplicação

**Pré-requisitos**

- Java 25 (o `build.gradle` usa toolchain 25)
- Docker em execução (o Spring Boot sobe o MySQL automaticamente pelo `compose.yml`)
- Uma chave de API da OpenAI

**Passos**

```bash
# 1. Clone o repositório e entre na pasta do projeto
git clone <URL-DO-SEU-FORK>
cd <seu-fork>/05-spring-ai

# 2. Configure a chave da OpenAI
export OPENAI_API_KEY="sua_chave_aqui"        # Linux/macOS
# $env:OPENAI_API_KEY="sua_chave_aqui"        # PowerShell (Windows)

# 3. Rode a aplicação
./gradlew bootRun
```

A API fica disponível em `http://localhost:8080`. O MySQL sobe na porta `3307` via Docker Compose.

---

## 🆕 Qual melhoria eu implementei

### Validação de transações antes de salvar

**Problema:** o modelo de IA pode errar ao transcrever o áudio ou ao extrair os parâmetros
(valor negativo, descrição vazia, categoria ausente). Sem validação, esses dados iam direto para o
banco, tanto pelo REST quanto pelo Tool Calling.

**Solução:** as regras de negócio foram colocadas no **domínio**, no construtor de criação de `Transaction`:

- o valor deve ser maior que zero;
- o valor deve ser de no máximo R$ 100.000,00 (guardado em centavos);
- a descrição é obrigatória, com no máximo 255 caracteres;
- a categoria é obrigatória.

**O que mudou no projeto**

| Arquivo | Mudança |
|---|---|
| `domain/Transaction.java` | O construtor de criação valida os dados. O construtor usado para ler do banco não valida, para não quebrar registros antigos |
| `domain/InvalidTransactionException.java` | Nova exceção de regra de negócio, com mensagem clara |
| `infrastructure/http/ApiExceptionHandler.java` | Converte a exceção em **HTTP 400** com `{"error": "..."}` |
| `prompts/system-message.st` | Instrui o modelo a enviar valores em centavos e a explicar erros de ferramenta, em vez de dizer que salvou |
| `test/.../domain/TransactionTest.java` | 9 testes unitários das regras, sem precisar de chave da OpenAI |

**Por que no domínio?** O mesmo caso de uso (`PersistTransactionUseCase`) atende o REST e a IA.
Validando em `Transaction`, as duas portas de entrada ficam protegidas pela mesma regra, sem código duplicado.

---

## 🛠️ Tecnologias utilizadas

- **Java 25**
- **Spring Boot 4**
- **Spring AI** (`ChatClient`, Tool Calling com `@Tool`, `TranscriptionModel`, `TextToSpeechModel`)
- **OpenAI**: `gpt-4o-mini` (chat), `whisper-1` (transcrição) e `gpt-4o-mini-tts` (voz)
- **Spring Web** (REST) e **Spring Data JPA**
- **MySQL 9** com **Docker Compose**
- **Gradle**, **Lombok** e **JUnit 5 / AssertJ**

---

## 🧪 Como testar o fluxo principal

### 1. Testes unitários (não precisam de OpenAI)

```bash
./gradlew test --tests "dio.budgeting.domain.TransactionTest"
```

### 2. Validação pelo REST

```bash
# Valor negativo → deve retornar 400 com mensagem de erro
curl -i -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Mercado","category":"GROCERIES","amount":-500}'

# Transação válida (R$ 50,00 = 5000 centavos) → deve retornar 201
curl -i -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Mercado","category":"GROCERIES","amount":5000}'

# Consulta por categoria
curl http://localhost:8080/transactions/GROCERIES
```

### 3. Fluxo de IA (áudio → áudio)

O projeto traz áudios de exemplo em `src/test/resources/audio/`.

```bash
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@src/test/resources/audio/recording-1.m4a" \
  --output resposta.mp3
```

Abra o `resposta.mp3` para ouvir a resposta. Depois confira o registro com
`curl http://localhost:8080/transactions/GROCERIES`.

Para testar a validação pela voz, grave um áudio dizendo algo como *"gastei zero reais no mercado"*
e verifique que a resposta explica o problema em vez de confirmar o registro.

> Os testes de integração com a OpenAI (`*IT.java`) só rodam se a variável `OPENAI_API_KEY` estiver definida.

### 📸 Prints e exemplos

<!-- Adicione aqui prints do terminal, das respostas dos curl ou do Postman/Insomnia -->

---

## 📚 O que aprendi durante o desafio

- **O modelo não executa nada sozinho.** No Tool Calling, a IA só escolhe qual função chamar e com quais
  parâmetros. Quem executa é o meu código Java, por isso as regras de negócio precisam estar no código,
  não só no prompt.
- **A IA é só mais uma porta de entrada.** Como o REST e as `@Tool` usam os mesmos casos de uso, não preciso
  duplicar lógica, e uma regra no domínio protege os dois caminhos.
- **Não dá para confiar cegamente na saída do modelo.** Ele pode errar na transcrição ou na extração dos
  dados, então validar antes de persistir é essencial.
- **Prompt e código se complementam.** Ajustei o `system-message.st` para o modelo enviar valores em centavos
  e explicar erros, mas a garantia real vem da validação em Java.
- **Entendi o pipeline completo de voz:** áudio → texto (Whisper) → intenção e ferramenta (LLM) →
  banco de dados → texto → áudio (TTS), tudo configurado com `application.properties` e beans do Spring AI.
- **Arquitetura em camadas ajuda a evoluir o projeto.** Consegui adicionar a melhoria mexendo em poucos
  arquivos, porque cada camada tem uma responsabilidade clara.
- **Detalhes de dinheiro importam.** O valor é guardado em centavos (`long`), e percebi que o
  `TransactionOutput` formata o valor sem dividir por 100, um ponto que pode ser melhorado no futuro.
- **Na prática:** aprendi a usar o Git/GitHub para entregar o projeto e a diagnosticar problemas quando o
  `git add` não funcionou como esperado.

## 🙏 Créditos

Projeto base: [digitalinnovationone/dio-spring-boot-learning-track](https://github.com/digitalinnovationone/dio-spring-boot-learning-track),
criado pelo expert Poiani para a DIO. A melhoria de validação e esta documentação foram desenvolvidas por mim
como parte do desafio, com apoio de IA como ferramenta de estudo.
