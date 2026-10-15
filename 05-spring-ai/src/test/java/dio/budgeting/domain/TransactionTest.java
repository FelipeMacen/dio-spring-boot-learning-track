package dio.budgeting.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    @Test
    void should_createTransaction_when_dataIsValid() {
        var transaction = new Transaction("  Mercado  ", 5000, Category.GROCERIES);

        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getDescription()).isEqualTo("Mercado");
        assertThat(transaction.getAmount()).isEqualTo(5000);
        assertThat(transaction.getCategory()).isEqualTo(Category.GROCERIES);
    }

    @Test
    void should_reject_when_amountIsZero() {
        assertThatThrownBy(() -> new Transaction("Mercado", 0, Category.GROCERIES))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    void should_reject_when_amountIsNegative() {
        assertThatThrownBy(() -> new Transaction("Mercado", -100, Category.GROCERIES))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    void should_reject_when_amountExceedsLimit() {
        assertThatThrownBy(() -> new Transaction("Carro", Transaction.MAX_AMOUNT_IN_CENTS + 1, Category.AUTO))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("limite");
    }

    @Test
    void should_acceptTransaction_when_amountEqualsLimit() {
        var transaction = new Transaction("Carro", Transaction.MAX_AMOUNT_IN_CENTS, Category.AUTO);

        assertThat(transaction.getAmount()).isEqualTo(Transaction.MAX_AMOUNT_IN_CENTS);
    }

    @Test
    void should_reject_when_descriptionIsBlank() {
        assertThatThrownBy(() -> new Transaction("   ", 1000, Category.PHARMA))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("descrição");
    }

    @Test
    void should_reject_when_descriptionIsNull() {
        assertThatThrownBy(() -> new Transaction(null, 1000, Category.PHARMA))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("descrição");
    }

    @Test
    void should_reject_when_descriptionIsTooLong() {
        var longDescription = "a".repeat(Transaction.MAX_DESCRIPTION_LENGTH + 1);

        assertThatThrownBy(() -> new Transaction(longDescription, 1000, Category.PHARMA))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("no máximo");
    }

    @Test
    void should_reject_when_categoryIsNull() {
        assertThatThrownBy(() -> new Transaction("Remédio", 1000, null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("categoria");
    }
}
