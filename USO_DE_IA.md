# Uso de Inteligência Artificial

**Ferramenta utilizada:** Claude (Anthropic), em formato de conversa.

**Como usei:** como um par de programação e mentor. Pedi à IA que me explicasse cada decisão e trabalhamos por etapas: para cada etapa, a IA propunha o código e eu o implementava, rodava os testes no meu ambiente (IntelliJ) e respondia perguntas de verificação sobre o que tinha sido feito. A IA corrigia as respostas antes de seguirmos. Meu objetivo era conseguir explicar e defender todo o código, não só entregá-lo.
Usei duas conversas separadas: uma para construir o código por etapas e outra como mentora, para tirar dúvidas, resolver impedimentos de ambiente e treinar a explicação técnica de cada arquivo. Nesta segunda, a IA explicava linha a linha e me fazia perguntas; eu respondia com minhas palavras e ela corrigia a precisão dos termos.

---

## Principais prompts e interações

1. **Entendimento e planejamento**
   *"Consegui passar da fase. A próxima fase é essa [PDF do case]. Vamos nos preparar."*
   → A IA analisou o enunciado, destacou os critérios de avaliação ("solução simples e justificada", "explicar e defender o código") e propôs: a modelagem (invariante `total = disponível + reservado + bloqueado`), as premissas a decidir, a arquitetura em camadas e um plano em 5 etapas com estimativa de tempo.

2. **Escolha da linguagem**
   → A IA comparou Java e Python considerando a vaga (stack Java) e o meu domínio atual (Python). Decidi por **Java**.

3. **Etapa 1: modelo de domínio**
   → Implementação de `Produto` com as regras e testes unitários. Perguntas de verificação: por que o disponível é calculado, por que usar `Math.addExact`, por que `IllegalStateException` × `EstoqueInsuficienteException`, por que as regras ficam no domínio.

4. **Etapa 2: reservas, bloqueios e liberação**
   → Entidades com UUID e status, service transacional e testes de integração. Discussão sobre atomicidade, ordem das validações e *dirty checking* do JPA.

5. **Etapa 3: API REST e tratamento de erros**
   → DTOs, controllers, `ProblemDetail` e testes com MockMvc. Discussão sobre códigos HTTP (400 × 422 × 409) e `POST /liberacao` × `DELETE`.

6. **Etapa 4: concorrência**
   → Comparação entre lock otimista, pessimista, `synchronized` e UPDATE condicional; teste com 20 threads simultâneas; experimento removendo a trava.

7. **Etapa 5: documentação**
   → A IA gerou um rascunho do README e deste arquivo a partir de tudo que foi feito, e eu revisei e ajustei com as minhas palavras.

8. **Mentoria e revisão do código**
   *“Quero aprender sobre o código e sobre Java, não posso passar a etapa sem saber do que se fala.”*
   → Revisei cada arquivo com a IA, criei post-its de conceitos (classe, encapsulamento, DTO, transação, lock) e treinei respostas no formato “o que é → o que faz → por que existe”.

---

## Onde a IA mais ajudou

- **Velocidade com uma stack que estou aprendendo:** sintaxe e anotações do Spring Boot e do JPA, e as mudanças do Spring Boot 4 (ex.: novo pacote do `@AutoConfigureMockMvc`).
- **Pensar nos casos de borda:** liberação dupla, operação inválida que não pode alterar o estado, estouro de `int`, produto inexistente.
- **Concorrência:** entender as alternativas e, principalmente, **como testar** concorrência de verdade (`CountDownLatch` para largada simultânea, sem `@Transactional` no teste).
- **Aprendizado:** as perguntas de verificação a cada etapa me obrigaram a entender o código antes de avançar.

## Sugestões que modifiquei ou não utilizei

- **Lock otimista → lock pessimista.** A primeira recomendação foi usar apenas o lock otimista (`@Version`). Ao testar com requisições simultâneas, ficou claro que, sem uma lógica de retry, muitas requisições falhariam com conflito em um cenário de alta disputa pelo mesmo produto. A decisão foi revista para **lock pessimista**, mantendo o `@Version` como rede de segurança.
- **Versão do Spring Boot.** A IA sugeriu a linha 3.5.x por ser a mais conhecida. Escolhi a **4.1.2**, a versão atual, e adaptamos o código (ex.: dependências de teste e pacotes que mudaram).
- **Sem Lombok:** optei por getters e construtores explícitos para deixar o código mais fácil de ler e explicar.
- **Comentários:** removi as anotações de estudo do código antes de subir, mantendo só os comentários que explicam decisões.

## Erros e limitações identificados nas respostas da IA

- **Contagem de testes errada:** na Etapa 1, a IA disse que seriam 12 testes, mas a execução mostrou 14, porque o teste parametrizado conta uma vez para cada valor. Pequeno, mas mostra a importância de **rodar e conferir** em vez de confiar na resposta.
- **Recomendação inicial de concorrência incompleta:** como descrito acima, o lock apenas otimista não atendia bem ao cenário sem retry. Isso só ficou evidente com o teste de concorrência.
- **Sem acesso ao meu ambiente:** a IA não conseguia ver o meu projeto. Por isso validei todo o código rodando os testes localmente a cada etapa.
- **Problemas de ambiente que a IA não previa:** o Java não era reconhecido no terminal (Git Bash); o language level do projeto estava em 26 com JDK 21; e o Maven falhou ao sincronizar no IntelliJ (Unable to create injector). Resolvi ajustando o JDK e o language level para 21 e trocando o Maven home path, depois de testar algumas alternativas sugeridas.

## Reflexão

A IA acelerou muito a implementação, mas as decisões que mais importaram (a escolha do lock, as premissas de negócio, o que fica fora do escopo) vieram de **questionar e testar** as sugestões. Usei a IA como acelerador e mentora, e a responsabilidade pelo código e pelas decisões é minha.
