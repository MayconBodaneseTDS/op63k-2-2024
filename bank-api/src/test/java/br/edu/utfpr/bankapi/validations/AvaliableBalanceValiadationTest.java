package br.edu.utfpr.bankapi.validations;

import br.edu.utfpr.bankapi.exception.WithoutBalanceException;
import br.edu.utfpr.bankapi.model.Account;
import br.edu.utfpr.bankapi.model.Transaction;
import br.edu.utfpr.bankapi.model.TransactionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AvaliableBalanceValiadationTest {

    @Autowired
    AvailableBalanceValidation availableBalanceValidation;

    @Test
    void deveriaSerValidoQuandoSaldoSuficiente() {
        // Arrange
        var sourceAccount = new Account("Tio Patinhhas", 12345, 1000, 0);
        var transaction = new Transaction(sourceAccount, null, 500, TransactionType.TRANSFER);

        // Act + Assert
        Assertions.assertDoesNotThrow(() -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaSerValidoComLimiteESaldo() {
        // Arrange
        var sourceAccount = new Account("Tio Patinhhas", 12345, 1000, 500);
        var transaction = new Transaction(sourceAccount, null, 1200, TransactionType.TRANSFER);

        // Act + Assert
        Assertions.assertDoesNotThrow(() -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaSerValidoComSaldoExato() {
        // Arrange
        var sourceAccount = new Account("Tio Patinhhas", 12345, 1000, 0);
        var transaction = new Transaction(sourceAccount, null, 1000, TransactionType.TRANSFER);

        // Act + Assert
        Assertions.assertDoesNotThrow(() -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaSerValidoComSaldoExatoELimite() {
        // Arrange
        var sourceAccount = new Account("Tio Patinhhas", 12345, 1000, 500);
        var transaction = new Transaction(sourceAccount, null, 1500, TransactionType.TRANSFER);

        // Act + Assert
        Assertions.assertDoesNotThrow(() -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaGerarExcecaoQuandoSaldoInsuficiente() {
        // Arrange
        var sourceAccount = new Account("Estudante Universitário", 12345, 1000, 0);
        var transaction = new Transaction(sourceAccount, null, 1500, TransactionType.TRANSFER);

        // Act + Assert
        Assertions.assertThrows(WithoutBalanceException.class,
                () -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaGerarExcecaoSemSaldoSuficienteComLimite() {
        // Arrange
        var sourceAccount = new Account("Estudante Universitário", 12345, 1000, 500);
        var transaction = new Transaction(sourceAccount, null, 1600, TransactionType.TRANSFER);

        // Act + Assert
        Assertions.assertThrows(WithoutBalanceException.class,
                () -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaGerarExcecaoQuandoQuantiaFor1MaiorQueSaldo() {
        // Arrange
        var sourceAccount = new Account("Estudante Universitário", 12345, 1000, 0);
        var transaction = new Transaction(sourceAccount, null, 1001, TransactionType.TRANSFER);

        // Act + Assert
        Assertions.assertThrows(WithoutBalanceException.class,
                () -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaGerarExcecaoQaundoQuantiaFor1MaiorQueSaldoComLimite() {
        // Arrange
        var sourceAccount = new Account("Estudante Universitário", 12345, 1000, 500);
        var transaction = new Transaction(sourceAccount, null, 1501, TransactionType.TRANSFER);

        // Act + Assert
        Assertions.assertThrows(WithoutBalanceException.class,
                () -> availableBalanceValidation.validate(transaction));
    }

    @Test
    void deveriaGerarExcecaoQuandoContaRemetenteForNula() {
        // Arrange
        var transaction = new Transaction(null, null, 10, TransactionType.TRANSFER);

        // Act + Assert
        Assertions.assertThrows(NullPointerException.class,
                () -> availableBalanceValidation.validate(transaction));
    }
}