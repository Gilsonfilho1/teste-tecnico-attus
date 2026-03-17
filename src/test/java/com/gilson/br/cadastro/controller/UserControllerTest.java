package com.gilson.br.cadastro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gilson.br.cadastro.model.UserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Cenário 1: Deve cadastrar um usuário válido com sucesso (Status 201)")
    public void deveCadastrarUsuarioComSucesso() throws Exception {
        UserRequest user = new UserRequest();
        user.setNome("João da Silva");
        user.setEmail("joao@email.com");
        user.setTelefone("11 99999-9999");
        user.setEndereco("Rua das Flores, 123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Cenário 2: Deve impedir cadastro sem nome (Status 400)")
    public void deveRetornarErroQuandoNomeAusente() throws Exception {
        UserRequest user = new UserRequest();
        user.setEmail("joao@email.com");
        user.setTelefone("11 99999-9999");
        user.setEndereco("Rua das Flores, 123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cenário 3: Tratar falha graciosamente se o telefone for nulo (Espera 400)")
    public void deveTratarTelefoneNuloSemDerrubarServidor() throws Exception {
        UserRequest user = new UserRequest();
        user.setNome("Maria Oliveira");
        user.setEmail("maria@email.com");
        user.setEndereco("Avenida Principal, 456");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cenário 4: Deve impedir o registo de e-mail duplicado (Status 409)")
    public void deveRetornarErroQuandoEmailDuplicado() throws Exception {
        UserRequest user1 = new UserRequest();
        user1.setNome("Carlos Silva");
        user1.setEmail("carlos@email.com");
        user1.setTelefone("11999999999");
        user1.setEndereco("Rua A, 1");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        UserRequest user2 = new UserRequest();
        user2.setNome("Carlos Clone");
        user2.setEmail("carlos@email.com");
        user2.setTelefone("11888888888");
        user2.setEndereco("Rua B, 2");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Cenário 5: Deve deletar um usuário existente pelo e-mail (Status 204)")
    public void deveDeletarUsuarioComSucesso() throws Exception {
        UserRequest user = new UserRequest();
        user.setNome("Ana Deleção");
        user.setEmail("ana@email.com");
        user.setTelefone("11988887777");
        user.setEndereco("Rua C, 3");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/users/ana@email.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Cenário 6: Deve retornar erro 404 ao tentar deletar um usuário inexistente")
    public void deveRetornarErroAoDeletarUsuarioInexistente() throws Exception {
        mockMvc.perform(delete("/api/users/fantasma@email.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cenário 7: Deve atualizar informações de um usuário existente (Status 200)")
    public void deveAtualizarUsuarioComSucesso() throws Exception {
        UserRequest user = new UserRequest();
        user.setNome("Roberto Antigo");
        user.setEmail("roberto@email.com");
        user.setTelefone("11900000000");
        user.setEndereco("Rua Velha, 123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        UserRequest novosDados = new UserRequest();
        novosDados.setNome("Roberto Atualizado");
        novosDados.setEmail("roberto@email.com");
        novosDados.setTelefone("11955554444");
        novosDados.setEndereco("Rua Nova, 456");

        mockMvc.perform(put("/api/users/roberto@email.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novosDados)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Roberto Atualizado"))
                .andExpect(jsonPath("$.endereco").value("Rua Nova, 456"));
    }

    @Test
    @DisplayName("Cenário 8: Deve retornar erro 404 ao tentar atualizar um usuário inexistente")
    public void deveRetornarErroAoAtualizarUsuarioInexistente() throws Exception {
        UserRequest novosDados = new UserRequest();
        novosDados.setNome("Usuário Fantasma");
        novosDados.setTelefone("11900000000");
        novosDados.setEndereco("Rua do Além, 0");

        mockMvc.perform(put("/api/users/naoexiste@email.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novosDados)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário com e-mail naoexiste@email.com não encontrado para atualização."));
    }

    @Test
    @DisplayName("Cenário 9: Deve retornar erro 400 ao tentar cadastrar usuário sem e-mail (POST)")
    public void deveRetornarErroQuandoEmailAusenteNoPost() throws Exception {
        UserRequest user = new UserRequest();
        user.setNome("Sem Email");
        user.setTelefone("11999999999");
        user.setEndereco("Rua X, 1");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cenário 10: Deve retornar erro 400 ao tentar cadastrar usuário sem endereço (POST)")
    public void deveRetornarErroQuandoEnderecoAusenteNoPost() throws Exception {
        UserRequest user = new UserRequest();
        user.setNome("Sem Endereço");
        user.setEmail("sem@endereco.com");
        user.setTelefone("11999999999");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cenário 11: Deve retornar erro 400 ao tentar atualizar usuário sem nome (PUT)")
    public void deveRetornarErroQuandoNomeAusenteNoPut() throws Exception {
        UserRequest user = new UserRequest();
        user.setEmail("roberto@email.com");
        user.setTelefone("11999999999");
        user.setEndereco("Rua Nova, 456");

        mockMvc.perform(put("/api/users/roberto@email.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cenário 12: Deve retornar erro 400 ao tentar atualizar usuário sem endereço (PUT)")
    public void deveRetornarErroQuandoEnderecoAusenteNoPut() throws Exception {
        UserRequest user = new UserRequest();
        user.setNome("Roberto Atualizado");
        user.setEmail("roberto@email.com");
        user.setTelefone("11999999999");

        mockMvc.perform(put("/api/users/roberto@email.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cenário 13: Deve retornar erro 400 ao tentar cadastrar com e-mail inválido (POST)")
    public void deveRetornarErroQuandoEmailInvalidoNoPost() throws Exception {
        UserRequest user = new UserRequest();
        user.setNome("Teste de Email");
        user.setEmail("email_sem_arroba.com");
        user.setTelefone("11999999999");
        user.setEndereco("Rua X, 1");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cenário 14: Deve retornar erro 400 ao tentar cadastrar usuário com nome em branco (POST)")
    public void deveRetornarErroQuandoNomeEmBrancoNoPost() throws Exception {
        UserRequest user = new UserRequest();
        user.setNome("   ");
        user.setEmail("espaco@email.com");
        user.setTelefone("11999999999");
        user.setEndereco("Rua X, 1");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }
}