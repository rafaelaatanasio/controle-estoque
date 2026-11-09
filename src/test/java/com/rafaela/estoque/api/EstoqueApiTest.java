package com.rafaela.estoque.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EstoqueApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fluxoCompletoDeEntradaReservaBloqueioELiberacao() throws Exception {
        postJson("/produtos/SKU-1/entradas", "{\"quantidade\": 10}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeDisponivel").value(10));

        String resposta = postJson("/produtos/SKU-1/reservas", "{\"solicitacaoId\": \"PEDIDO-1\", \"quantidade\": 4}")
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andReturn().getResponse().getContentAsString();
        String reservaId = JsonPath.read(resposta, "$.id");

        postJson("/produtos/SKU-1/bloqueios", "{\"motivo\": \"AVARIA\", \"quantidade\": 2}")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/produtos/SKU-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeTotal").value(10))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(4))
                .andExpect(jsonPath("$.quantidadeReservada").value(4))
                .andExpect(jsonPath("$.quantidadeBloqueada").value(2));

        mockMvc.perform(post("/reservas/{id}/liberacao", reservaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIBERADA"));

        mockMvc.perform(get("/produtos/SKU-1"))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(8))
                .andExpect(jsonPath("$.quantidadeReservada").value(0));
    }

    @Test
    void saidaMaiorQueDisponivelRetorna422() throws Exception {
        postJson("/produtos/SKU-1/entradas", "{\"quantidade\": 5}");

        postJson("/produtos/SKU-1/saidas", "{\"quantidade\": 6}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Estoque insuficiente"));
    }

    @Test
    void quantidadeInvalidaRetorna400() throws Exception {
        postJson("/produtos/SKU-1/entradas", "{\"quantidade\": 0}")
                .andExpect(status().isBadRequest());
        postJson("/produtos/SKU-1/entradas", "{}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void produtoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/produtos/NAO-EXISTE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"));
    }

    @Test
    void liberarReservaDuasVezesRetorna409() throws Exception {
        postJson("/produtos/SKU-1/entradas", "{\"quantidade\": 10}");
        String resposta = postJson("/produtos/SKU-1/reservas", "{\"solicitacaoId\": \"PEDIDO-1\", \"quantidade\": 4}")
                .andReturn().getResponse().getContentAsString();
        String reservaId = JsonPath.read(resposta, "$.id");

        mockMvc.perform(post("/reservas/{id}/liberacao", reservaId)).andExpect(status().isOk());
        mockMvc.perform(post("/reservas/{id}/liberacao", reservaId))
                .andExpect(status().isConflict());
    }

    private ResultActions postJson(String url, String json) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }
}