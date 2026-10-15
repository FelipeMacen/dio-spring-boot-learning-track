package dio.budgeting.domain;

/**
 * Lançada quando os dados de uma transação violam as regras de negócio.
 * A mensagem é escrita para ser entendida tanto por quem usa a API REST
 * quanto pelo modelo de IA (que a repassa à pessoa usuária).
 */
public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
