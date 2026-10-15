package dio.budgeting.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Transaction {
    /** Limite por transação: R$ 100.000,00 (valores são guardados em centavos). */
    public static final long MAX_AMOUNT_IN_CENTS = 10_000_000L;
    public static final int MAX_DESCRIPTION_LENGTH = 255;

    private TransactionId id;
    private String description;
    private long amount;
    private Category category;

    /**
     * Construtor usado para CRIAR uma nova transação: aplica as regras de negócio.
     * O construtor com todos os campos (usado ao reconstruir do banco) não valida,
     * para não quebrar a leitura de dados já existentes.
     */
    public Transaction(String description, long amount, Category category) {
        validate(description, amount, category);
        this.id = new TransactionId();
        this.description = description.trim();
        this.amount = amount;
        this.category = category;
    }

    private static void validate(String description, long amount, Category category) {
        if (amount <= 0) {
            throw new InvalidTransactionException("O valor da transação deve ser maior que zero.");
        }
        if (amount > MAX_AMOUNT_IN_CENTS) {
            throw new InvalidTransactionException("O valor da transação excede o limite de R$ 100.000,00.");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidTransactionException("A descrição da transação é obrigatória.");
        }
        if (description.trim().length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidTransactionException(
                    "A descrição deve ter no máximo " + MAX_DESCRIPTION_LENGTH + " caracteres.");
        }
        if (category == null) {
            throw new InvalidTransactionException("A categoria da transação é obrigatória.");
        }
    }
}
