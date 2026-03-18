# Desafio Técnico QA - API de Cadastro (Spring Boot)

Este repositório contém a entrega do desafio técnico para a vaga de QA (Attus), com foco na análise de risco, estratégia de testes, automação de testes de API e qualidade de software.

## 🚀 Como Executar o Projeto

**Pré-requisitos:** Java 25 e Maven instalados.

1. **Rodar a Aplicação:**
   - Navegue até a pasta raiz do projeto via terminal.
   - Execute o comando: `mvn spring-boot:run` (ou execute a classe `CadastroApplication.main()` diretamente pela sua IDE).
   - A API estará disponível na porta `8080`.

2. **Rodar os Testes Automatizados:**
   - Execute o comando: `mvn clean test`
   - Os resultados e relatórios (HTML/TXT) serão gerados na pasta `target/surefire-reports` e disponibilizados na pasta `/evidencias` deste repositório. *Nota: Alguns testes falharão intencionalmente para expor defeitos críticos mapeados na análise de risco e documentados na seção de Bug Reports.*

---

## 📌 Endpoints Disponíveis (CRUD)

- `POST /api/users`: Cria um novo usuário.
- `PUT /api/users/{email}`: Atualiza os dados de um usuário existente.
- `DELETE /api/users/{email}`: Deleta um usuário existente.

---

## 🕵️‍♂️ Análise de Risco

Considerando o contexto de uma API de cadastro, identifiquei os seguintes pontos de maior risco funcional e técnico, listados por prioridade:

1. **Risco Crítico: Instabilidade da API por falta de tratamento de exceções (Crash do Servidor).**
   - *Cenário:* O envio de payloads incompletos gera erros internos na formatação, retornando Erro 500.
2. **Risco Alto: Inconsistência de Dados e Quebra de Contrato.**
   - *Cenário:* O sistema aceitar cadastros ou atualizações sem os campos obrigatórios, aceitar formatos inválidos ou permitir a criação de múltiplas contas com o mesmo e-mail.
   - *Prioridade Alta:* Dados sujos no banco quebram regras de negócio e corrompem a integridade do sistema para futuras integrações.

---

## 🎯 Estratégia de Testes e Automação

A estratégia baseou-se em **Testes Baseados em Risco (Risk-Based Testing)**, focando na camada de API (Integração) com JUnit 5 e MockMvc. Foram priorizados:

- **Caminho Feliz (Happy Path):** Garantir o fluxo de ponta a ponta (Criar, Atualizar, Deletar).
- **Validações de Contrato e Formato:** Testar se a API bloqueia requisições sem campos vitais, com formatos inválidos ou strings vazias em todos os verbos HTTP.
- **Regras de Negócio e Caminhos de Exceção:** Validação dos retornos HTTP adequados (como 404 Not Found e 409 Conflict).

### Cenários Automatizados (`UserControllerTest.java`)

1. ✅ `deveCadastrarUsuarioComSucesso` (Status 201)
2. ✅ `deveRetornarErroQuandoNomeAusente` (Status 400)
3. ❌ `deveTratarTelefoneNuloSemDerrubarServidor` (Status 400 vs 500) -> *Bug 1*
4. ✅ `deveRetornarErroQuandoEmailDuplicado` (Status 409)
5. ✅ `deveDeletarUsuarioComSucesso` (Status 204)
6. ✅ `deveRetornarErroAoDeletarUsuarioInexistente` (Status 404)
7. ✅ `deveAtualizarUsuarioComSucesso` (Status 200)
8. ✅ `deveRetornarErroAoAtualizarUsuarioInexistente` (Status 404)
9. ✅ `deveRetornarErroQuandoEmailAusenteNoPost` (Status 400)
10. ✅ `deveRetornarErroQuandoEnderecoAusenteNoPost` (Status 400)
11. ❌ `deveRetornarErroQuandoNomeAusenteNoPut` (Status 400 vs 200) -> *Bug 2*
12. ❌ `deveRetornarErroQuandoEnderecoAusenteNoPut` (Status 400 vs 200) -> *Bug 2*
13. ❌ `deveRetornarErroQuandoEmailInvalidoNoPost` (Status 400 vs 201) -> *Bug 3*
14. ❌ `deveRetornarErroQuandoNomeEmBrancoNoPost` (Status 400 vs 201) -> *Bug 3*

### Cenários Não Cobertos e Justificativa

Em alinhamento com a estratégia de focar nos maiores riscos de negócio e integração, os seguintes cenários ficaram fora do escopo desta automação inicial:

- **Testes de Carga/Performance:** Não automatizados, pois o escopo atual é validar regras de negócio e a estabilidade funcional básica de uma API.
- **Testes de Integração com Banco de Dados Real:** Como a aplicação utiliza persistência em memória (Lista via Java), testes de transação SQL e concorrência no banco não se aplicam neste momento.
- **Validações de Limite Extremo (Boundary Testing de Carga Máxima):** Testar o envio de strings com milhares de caracteres no payload foi omitido para priorizar a entrega de testes funcionais e falhas de contrato mais comuns e críticas no dia a dia.

---

## 🐛 Defeitos Encontrados (Bug Reports)

Durante a execução da estratégia, os seguintes defeitos foram identificados e mantidos no código para avaliação da automação (evidenciados nas falhas dos cenários 3, 11, 12, 13 e 14):

### Bug 1: Erro 500 (Internal Server Error) ao enviar payload nulo no campo `telefone`
- **Passos para reproduzir:** Enviar um `POST` para `/api/users` com os campos obrigatórios, omitindo o `telefone`.
- **Resultado esperado:** A API deveria validar a ausência do campo opcional e retornar `201 Created` ignorando a formatação.
- **Resultado obtido:** A aplicação lança um `NullPointerException` interno no `UserService.java` ao tentar aplicar `.replace()`, resultando em resposta HTTP `500`.
- **Impacto:** **Alto**. Derruba a requisição e expõe a stacktrace.

### Bug 2: Falha de Validação de Contrato no Endpoint de Atualização (PUT)
- **Passos para reproduzir:** Enviar um `PUT` para `/api/users/{email}` omitindo campos obrigatórios como `nome` ou `endereco`.
- **Resultado esperado:** A API deveria bloquear a requisição e retornar `400 Bad Request` (ausência da anotação `@Valid` no controller).
- **Resultado obtido:** A API aceita a requisição, atualiza o usuário com valores nulos e retorna `200 OK`.
- **Impacto:** **Alto**. Corrompe a integridade dos dados já existentes no sistema.

### Bug 3: Falha de Validação de Formato e Falsos Nulos (POST)
- **Passos para reproduzir:** Enviar um `POST` para `/api/users` com o campo e-mail fora do padrão (ex: sem `@`) ou o campo nome preenchido apenas com espaços em branco `"   "`.
- **Resultado esperado:** A API deveria rejeitar o formato incorreto e as strings em branco, retornando `400 Bad Request` (utilizando validações avançadas como `@Email` e `@NotBlank`).
- **Resultado obtido:** A API aceita a requisição, cadastrando e-mails inválidos e usuários sem nome no sistema, retornando `201 Created`.
- **Impacto:** **Médio/Alto**. Permite a inserção de lixo na base de dados, impactando regras de negócio e integrações futuras (ex: falha no envio de e-mails).

*(As evidências de execução e logs encontram-se na pasta `/evidencias` na raiz do repositório).*

---

## 🤖 Declaração de Uso de IA

Em conformidade com as diretrizes do desafio, declaro o uso ético de Inteligência Artificial (ChatGPT/Gemini) durante este processo:

- **Finalidade:** Setup do boilerplate inicial (Spring Boot MVC e Entidades) e consulta de sintaxe.
- **Validação:** A IA funcionou estritamente como copiloto. A concepção da Estratégia de Testes, a Análise de Risco, a engenharia dos 14 testes de API (`MockMvc`), a arquitetura de tratamento de exceções, a implementação das rotas e a documentação técnica dos Bugs foram elaboradas, validadas e integradas inteiramente por mim, demonstrando minha visão crítica e prática em Qualidade de Software.
