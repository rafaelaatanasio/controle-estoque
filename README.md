# Controle de Estoque

API REST para controle de estoque de produtos, considerando as quantidades **total, disponível, reservada e bloqueada**, com suporte a várias reservas e bloqueios simultâneos por produto.

Desenvolvida em **Java 21 + Spring Boot 4.1.2** como case prático de Engenharia de Software.

---

## Visão geral

| Funcionalidade | Endpoint | Resposta |
|---|---|---|
| Entrada | `POST /produtos/{id}/entradas` | 200 + situação do produto |
| Saída | `POST /produtos/{id}/saidas` | 200 + situação do produto |
| Reservar | `POST /produtos/{id}/reservas` | 201 + reserva (com `id`) |
| Bloquear | `POST /produtos/{id}/bloqueios` | 201 + bloqueio (com `id`) |
| Liberar reserva | `POST /reservas/{id}/liberacao` | 200 + reserva liberada |
| Liberar bloqueio | `POST /bloqueios/{id}/liberacao` | 200 + bloqueio liberado |
| Consultar produto | `GET /produtos/{id}` | total, disponível, reservado, bloqueado |
| Consultar reserva / bloqueio | `GET /reservas/{id}` · `GET /bloqueios/{id}` | detalhes e status |

Exemplos prontos para executar estão em [`requests.http`](requests.http) (IntelliJ HTTP Client).

### Invariante central

```
total = disponível + reservado + bloqueado      (nenhum valor pode ser negativo)
```

Toda a solução foi construída para que essa regra nunca seja quebrada, inclusive com requisições simultâneas.

---

## Arquitetura

Arquitetura em camadas simples, com as regras de negócio concentradas no domínio:

```
src/main/java/com/rafaela/estoque
├── api/          → controllers REST, DTOs (records) e tratamento de erros (ProblemDetail)
├── service/      → orquestração dos casos de uso e controle de transação
├── domain/       → entidades (Produto, Reserva, Bloqueio), regras de negócio e exceções
└── repository/   → acesso a dados (Spring Data JPA)
```

- **Produto** guarda total, reservado e bloqueado. O **disponível é calculado** (`total - reservado - bloqueado`).
- **Reserva** e **Bloqueio** são registros com identificação (UUID), quantidade e status (`ATIVA` / `LIBERADA`). Uma reserva está ligada a uma solicitação (`solicitacaoId`). Um bloqueio tem um `motivo`.
- Persistência em **H2 em memória**, para rodar sem nenhuma instalação adicional.

---

## Principais decisões técnicas

1. **Modelo de domínio rico.** As regras (`reservar`, `bloquear`, `registrarSaida`...) ficam dentro de `Produto`, que valida antes de alterar. Nenhum caminho (API, fila, job) consegue burlar a regra, e o domínio é testado sem Spring nem banco.
2. **Disponível calculado, não armazenado.** É uma única fonte da verdade e elimina o risco de campos dessincronizados.
3. **Concorrência: lock pessimista + `@Version`.**
    - As operações de escrita leem o produto com `SELECT ... FOR UPDATE` (`PESSIMISTIC_WRITE`): operações no mesmo produto entram em fila no banco. Funciona com várias instâncias da aplicação, ao contrário de um `synchronized`.
    - O `@Version` (lock otimista) fica como **rede de segurança**: se algum caminho futuro esquecer a trava, o conflito é detectado e retorna **409**, sem corromper o estoque.
    - Escolhi o pessimista porque o cenário de estoque tem **alta disputa no mesmo produto** (ex.: promoções). Com lock só otimista, muitas requisições falhariam e precisariam de retry. Ver o experimento em [Testes](#testes).
4. **Transações atômicas.** Cada operação é `@Transactional`: na liberação, mudar o status da reserva e devolver a quantidade ao produto acontecem juntos ou não acontecem.
5. **Liberar não apaga.** Reservas e bloqueios mudam para `LIBERADA` e registram a data, o que mantém histórico e impede liberar duas vezes (409). Por isso a liberação é `POST /{id}/liberacao` e não `DELETE`.
6. **Validação em duas camadas (defesa em profundidade).** O DTO valida o formato na entrada (`@NotNull`, `@Positive`) e o domínio garante a regra para qualquer chamador.
7. **Erros padronizados com ProblemDetail (RFC 9457):**

| Situação | HTTP |
|---|---|
| Dado inválido (quantidade ≤ 0, campo ausente) | 400 |
| Produto, reserva ou bloqueio inexistente | 404 |
| Reserva/bloqueio já liberado, conflito de concorrência | 409 |
| Regra de negócio violada (estoque insuficiente) | 422 |

8. **DTOs separados das entidades.** A API não expõe o modelo interno (ex.: campo `versao`), e um pode mudar sem quebrar o outro.
9. **`IllegalStateException` para estados impossíveis.** Liberar mais do que está reservado indica bug ou dado corrompido, não erro do cliente, e por isso é tratado diferente de "estoque insuficiente".
10. **`Math.addExact` na entrada**, para falhar em vez de estourar o `int` silenciosamente.
11. **Sem Lombok**, para manter o código explícito e fácil de ler.
12. **`spring.jpa.open-in-view=false`**, para evitar consultas ao banco escondidas na camada web.

---

## Premissas

- A **saída** consome apenas unidades **disponíveis**. Unidades reservadas ou bloqueadas não saem pela operação de saída.
- A **entrada** em um produto inexistente **cria o produto**. As demais operações em produto inexistente retornam 404.
- A **liberação** devolve a quantidade **integral** da reserva/bloqueio ao **disponível**.
- Quantidades são **inteiras e maiores que zero**.
- A identificação do produto (ex.: SKU) é informada pelo cliente. Reservas e bloqueios recebem um UUID gerado pelo sistema.

---

## Limitações conhecidas

- **Banco em memória (H2):** os dados são perdidos ao reiniciar a aplicação.
- **Sem autenticação**, então não há registro de **quem** fez cada operação.
- **Sem idempotência:** se o cliente reenviar uma requisição (ex.: após timeout), ela é executada de novo.
- **Liberação apenas total** (sem liberação parcial) e **sem expiração** automática de reservas.
- Não há operação para **efetivar uma reserva** (transformar a reserva em saída).
- Não há listagem de reservas/bloqueios por produto.
- Duas **entradas simultâneas para um produto que ainda não existe** podem tentar criá-lo ao mesmo tempo, e uma delas falha.
- Reserva e Bloqueio guardam o `produtoId` como texto, sem chave estrangeira no banco.

---

## Como executar

**Pré-requisito:** Java 21 (o projeto usa o Maven Wrapper, então não é preciso instalar o Maven).

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Exemplo:

```bash
curl -X POST http://localhost:8080/produtos/SKU-1/entradas -H "Content-Type: application/json" -d "{\"quantidade\": 10}"
curl http://localhost:8080/produtos/SKU-1
```

Ou use o arquivo [`requests.http`](requests.http) no IntelliJ.

### Testes

```bash
mvnw.cmd test        # Windows
./mvnw test          # Linux / macOS
```

| Tipo | Classe | O que garante |
|---|---|---|
| Unitário (domínio) | `ProdutoTest` | Todas as regras e o invariante; estado intacto após operação inválida. Sem Spring, roda em milissegundos |
| Integração (serviço) | `EstoqueServiceTest` | Várias reservas e bloqueios simultâneos, liberação, liberação dupla, recursos inexistentes |
| Integração (API) | `EstoqueApiTest` | Fluxo HTTP completo e códigos de erro (400, 404, 409, 422) |
| Concorrência | `ConcorrenciaTest` | 20 requisições simultâneas disputando 10 unidades: exatamente 10 aceitas, 10 recusadas, estoque nunca negativo |

**Experimento:** removendo a anotação `@Lock` do `ProdutoRepository`, o `ConcorrenciaTest` falha com `ObjectOptimisticLockingFailureException`. Sem a trava, as threads leem o mesmo estoque ao mesmo tempo, e o `@Version` impede a gravação inconsistente, mas as requisições falham. Com a trava, todas são atendidas corretamente, em fila. Isso confirma as duas camadas de proteção.

---

## O que eu faria diferente em produção

1. **Banco relacional real (PostgreSQL)** com migrações versionadas (Flyway) e chave estrangeira entre reserva/bloqueio e produto.
2. **Idempotência:** header `Idempotency-Key` nas operações de escrita, para que reenvios não dupliquem entradas, saídas ou reservas.
3. **Histórico de movimentações (auditoria):** tabela com cada operação (tipo, quantidade, data, usuário, origem), que permite rastrear e reconstruir o saldo.
4. **Autenticação e autorização** (OAuth2/JWT), registrando quem fez cada operação.
5. **Observabilidade:** logs estruturados com correlation id, métricas (Micrometer: operações por tipo, recusas por estoque insuficiente, tempo de espera por lock, conflitos 409), tracing distribuído (OpenTelemetry) e alertas, com atenção especial a `IllegalStateException`, que indica inconsistência. Health checks via Spring Boot Actuator.
6. **Expiração de reservas** (TTL) com um job que libera reservas vencidas, e a operação de **efetivar reserva**.
7. **Eventos de domínio** (ex.: `EstoqueReservado`) publicados com o padrão Outbox em uma fila (SQS/Kafka), para outros sistemas reagirem sem acoplamento.
8. **Escala:** se a disputa por lock virar gargalo, avaliar um UPDATE condicional atômico (`... WHERE disponivel >= :qtd`) ou particionamento por produto.
9. **Entrega segura:** containers, pipeline CI/CD com os testes, deploy Blue/Green com validação de saúde antes de virar o tráfego, e rollback automatizado.

---

## Visão de engenharia: os 5 aspectos mais importantes

1. **Consistência garantida no domínio.** O invariante `total = disponível + reservado + bloqueado` é a principal regra do negócio. Concentrar as regras na entidade, validando antes de alterar, garante que nenhum caminho deixe o estoque inconsistente.
2. **Controle de concorrência.** Estoque é um recurso disputado: sem controle, duas requisições simultâneas vendem a mesma unidade. A trava no banco funciona com várias instâncias, e o `@Version` é a rede de segurança.
3. **Testes em camadas, incluindo concorrência.** Os testes de domínio são rápidos e cobrem as regras, os de integração validam o contrato HTTP, e o de concorrência prova o comportamento sob disputa real. Isso dá segurança para evoluir.
4. **Observabilidade.** Em produção, é preciso saber o que está acontecendo antes do cliente perceber: logs, métricas e alertas sobre recusas, conflitos e estados impossíveis. Essa é uma prática que trago da minha experiência com Release Management e resposta a incidentes.
5. **Rastreabilidade e idempotência.** Em estoque, cada unidade precisa ter origem e destino explicáveis. O histórico de movimentações permite auditar e corrigir, e a idempotência evita duplicidade em retentativas de rede, que são comuns em sistemas distribuídos.

---

## Uso de IA

Veja [`USO_DE_IA.md`](USO_DE_IA.md).

---

## Tempo de desenvolvimento

Cerca de 5 horas, divididas em duas sessões (08/11 e 09/11). Como estou em transição para Java, dediquei parte do tempo a entender, testar e documentar cada decisão, para conseguir explicá-las.