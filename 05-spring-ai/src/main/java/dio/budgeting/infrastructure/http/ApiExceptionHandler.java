package dio.budgeting.infrastructure.http;

import dio.budgeting.domain.InvalidTransactionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Traduz erros de regra de negócio em HTTP 400 para os endpoints REST.
 * (Pelo caminho da IA, a mesma mensagem volta ao modelo como resultado da tool.)
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidTransactionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidTransaction(InvalidTransactionException e) {
        return Map.of("error", e.getMessage());
    }
}
