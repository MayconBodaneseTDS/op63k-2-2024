package br.edu.utfpr.bankapi.controller;

import br.edu.utfpr.bankapi.model.Account;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@Transactional
class AccountControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    TestEntityManager entityManager;

    // GET /account/{number} tests
    @Test
    void deveriaRetornar404ParaContaNaoEncontrada() throws Exception {
        // Arrange
        long accountNumber = 11111;

        // Act
        var res = mvc.perform(MockMvcRequestBuilders.get("/account/" + accountNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Assert
        Assertions.assertEquals(404, res.getStatus());
        Assertions.assertEquals("", res.getContentAsString());
    }

    @Test
    void deveriaRetornar200ParaContaValida() throws Exception {
        // Arrange
        long accountNumber = 12345;
        double balance = 1000;
        var account = new Account("Sobjak Querido", accountNumber, balance, 0);
        entityManager.persist(account);

        // Act + Assert
        mvc.perform(MockMvcRequestBuilders.get("/account/" + accountNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status().isOk())
                .andExpect(MockMvcResultMatchers
                        .content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("number").value(accountNumber))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("balance").value(balance))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("name").value(account.getName()));
    }

    // GET /account tests
    @Test
    void deveriaRetornarTodasAsContas() throws Exception {
        // Arrange
        var account1 = new Account("Sobjak Querido", 12345, 200, 0);
        var account2 = new Account("Lauro Lima", 12346, 1000, 0);
        entityManager.persist(account1);
        entityManager.persist(account2);

        // Act + Assert
        mvc.perform(MockMvcRequestBuilders.get("/account")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status().isOk())
                .andExpect(MockMvcResultMatchers
                        .content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[0].number").value(account1.getNumber()))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[0].balance").value(account1.getBalance()))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[0].name").value(account1.getName()))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[1].number").value(account2.getNumber()))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[1].balance").value(account2.getBalance()))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[1].name").value(account2.getName()));
    }

    @Test
    void deveriaRetornar200ComArrayVazioNaoTiverContas() throws Exception {
        // Act + Assert
        mvc.perform(MockMvcRequestBuilders.get("/account")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status().isOk())
                .andExpect(MockMvcResultMatchers
                        .content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$").isEmpty());
    }

    // POST /account tests
    @Test
    void deveriaRetornar400ParaRequisicaoInvalida() throws Exception {
        // Arrange
        var json = "{}"; // Invalid body

        // Act + Arrange
        mvc.perform(MockMvcRequestBuilders.post("/account")
                .content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status().isBadRequest());
    }

    @Test
    void deveriaRetornar201ParaRequisicaoValida() throws Exception {
        // Arrange
        String name = "Sobjak Querido";
        long number = 12345;
        var json = """
                {
                    "name": "Sobjak Querido",
                    "number": 12345,
                    "balance": 200,
                    "specialLimit": 0
                }
                """;

        // Act + Assert
        mvc.perform(MockMvcRequestBuilders.post("/account")
                .content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status().isCreated())
                .andExpect(MockMvcResultMatchers
                        .content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("number").value(number))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("name").value(name))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("specialLimit").value(0))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("balanceWithLimit").value(0));
    }

    // PUT /account/{id} tests

    @Test
    void deveriaRetornar404QuandoNaoEncontrarContaNoUpdate() throws Exception {
        // Arrange
        long accountId = 1;
        var json = """
                {
                    "name": "Sobjak Querido",
                    "number": 12345,
                    "balance": 200,
                    "specialLimit": 0
                }
                """;

        // Act + Assert
        mvc.perform(MockMvcRequestBuilders.put("/account/" + accountId)
                .content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status().isNotFound())
                .andExpect(MockMvcResultMatchers
                        .content().contentType("text/plain;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers
                        .content().string("Not found"));
    }

    @Test
    void deveriaRetornar400ParaRequisicaoDeUpdateInvalida() throws Exception {
        // Arrange
        long accountId = 1;
        var json = "{}"; // Invalid body

        // Act
        var res = mvc.perform(MockMvcRequestBuilders.put("/account/" + accountId)
                .content(json).contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Assert
        Assertions.assertEquals(400, res.getStatus());
    }

}
