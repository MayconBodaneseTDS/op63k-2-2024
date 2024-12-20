package br.edu.utfpr.bankapi.controller;

import br.edu.utfpr.bankapi.model.Account;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@Transactional
class TransferControllerTest {

    @Autowired
    MockMvc mvc;

    // Gerenciador de persistência para os testes des classe
    @Autowired
    TestEntityManager entityManager;

    Account account; // Conta para os testes

    @BeforeEach
    void setup() {
        account = new Account("Jhon Doe",
                12346, 1000, 0);
        entityManager.persist(account); // salvando uma conta
    }

    // POST /transfer/withdraw

    @Test
    void deveriaRetornar400ParaRequisicaoInvalida() throws Exception {
        // ARRANGE
        var json = "{}"; // Body inválido

        // ACT
        var res = mvc.perform(
                MockMvcRequestBuilders.post("/transaction/withdraw")
                        .content(json).contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // ASSERT
        Assertions.assertEquals(400, res.getStatus());
    }

    @Test
    void deveriaRetornar201ParaRequisicaoOK() throws Exception {
        // ARRANGE
        var json = """
                {
                    "sourceAccountNumber": 12346,
                    "amount": 200
                }
                    """;

        // ACT
        var res = mvc.perform(
                MockMvcRequestBuilders.post("/transaction/withdraw")
                        .content(json).contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // ASSERT
        Assertions.assertEquals(201, res.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, res.getContentType());
        var responseBody = res.getContentAsString();
        Assertions.assertTrue(responseBody.contains("\"amount\":200"));
        Assertions.assertTrue(responseBody.contains("\"balance\":800")); // 1000 - 200
        Assertions.assertTrue(responseBody.contains("\"number\":12346"));
    }

    @Test
    void deveriaRetornar400ParaQuantiaMaiorQueSaldo() throws Exception {
        // ARRANGE
        var json = """
                {
                    "sourceAccountNumber": 12346,
                    "amount": 2000
                }
                    """;

        // ACT
        var res = mvc.perform(
                MockMvcRequestBuilders.post("/transaction/withdraw")
                        .content(json).contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // ASSERT
        Assertions.assertEquals(400, res.getStatus());
    }

    @Test
    void deveriaRetornar400ParaContaNaoExistente() throws Exception {
        // ARRANGE
        var json = """
                {
                    "sourceAccountNumber": 11111,
                    "amount": 200
                }
                    """;

        // ACT
        var res = mvc.perform(
                MockMvcRequestBuilders.post("/transaction/withdraw")
                        .content(json).contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // ASSERT
        Assertions.assertEquals(400, res.getStatus());
    }

    // POST /transfer/transfer

    @Test
    void deveriaRetornar201PraRequisicaoOK() throws Exception {
        // ARRANGE
        entityManager.persist(new Account("Pix da Silva", 12345, 500, 0));
        var json = """
                {
                    "sourceAccountNumber": 12345,
                    "receiverAccountNumber": 12346,
                    "amount": 200
                }""";

        // ACT + ASSERT
        mvc.perform(
                MockMvcRequestBuilders.post("/transaction/transfer")
                        .content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.amount", Matchers.equalTo(200.0)))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.sourceAccount.number", Matchers.equalTo(12345)))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.receiverAccount.number", Matchers.equalTo(12346)))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.sourceAccount.balance", Matchers.equalTo(300.0)))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.receiverAccount.balance", Matchers.equalTo(1200.0)));
    }

    @Test
    void deveriaRetornar400QuandoBodyVazio() throws Exception {
        // ARRANGE
        var json = "{}";

        // ACT
        var res = mvc.perform(
                MockMvcRequestBuilders.post("/transaction/transfer")
                        .content(json).contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // ASSERT
        Assertions.assertEquals(400, res.getStatus());
    }

    @Test
    void deveriaRetornar400QuandoContaRemetenteNaoExistir() throws Exception {
        // ARRANGE
        var json = """
                {
                    "sourceAccountNumber": 11111,
                    "receiverAccountNumber": 12346,
                    "amount": 200
                }""";

        // ACT
        var res = mvc.perform(
                MockMvcRequestBuilders.post("/transaction/transfer")
                        .content(json).contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // ASSERT
        Assertions.assertEquals(400, res.getStatus());
    }

    @Test
    void deveriaRetornar400QuandoContaBeneficiariaNaoExistir() throws Exception {
        // ARRANGE
        var json = """
                {
                    "sourceAccountNumber": 12346,
                    "receiverAccountNumber": 11111,
                    "amount": 200
                }""";

        // ACT
        var res = mvc.perform(
                MockMvcRequestBuilders.post("/transaction/transfer")
                        .content(json).contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // ASSERT
        Assertions.assertEquals(400, res.getStatus());
    }

    @Test
    void deveriaRetornar400QuandoValorForMaiorQueSaldo() throws Exception {
        // ARRANGE
        var json = """
                {
                    "sourceAccountNumber": 12346,
                    "receiverAccountNumber": 12345,
                    "amount": 2000
                }""";

        // ACT
        var res = mvc.perform(
                MockMvcRequestBuilders.post("/transaction/transfer")
                        .content(json).contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // ASSERT
        Assertions.assertEquals(400, res.getStatus());
    }
}