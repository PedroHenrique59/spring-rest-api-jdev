package curso.api.rest;

import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.util.List;

@RestControllerAdvice
@ControllerAdvice
public class ControleExcecoes extends ResponseEntityExceptionHandler {

    /* Tratamento da maioria dos erros */
    @Override
    @ExceptionHandler({Exception.class, RuntimeException.class, Throwable.class})
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String msg = "";

        if (ex instanceof MethodArgumentNotValidException) {
            List<ObjectError> list = ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors();
            for (ObjectError objectError : list) {
                msg += objectError.getDefaultMessage() + "\n";
            }
        }
        ObjetoError objetoError = new ObjetoError();
        objetoError.setCode(status.value() + " ==> " + status.getReasonPhrase());
        objetoError.setError(msg);

        return new ResponseEntity<>(objetoError, headers, status);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ConstraintViolationException.class, PSQLException.class, SQLException.class})
    protected ResponseEntity<Object> handleExceptionDataIntegry(Exception ex) {
        String msg = "";

        if (ex instanceof DataIntegrityViolationException) {
            msg = ex.getCause().getCause().getMessage();
        } else if (ex instanceof ConstraintViolationException) {
            msg = ex.getCause().getCause().getMessage();
        } else if (ex instanceof PSQLException) {
            msg = ex.getCause().getCause().getMessage();
        } else if (ex instanceof SQLException) {
            msg = ex.getCause().getCause().getMessage();
        } else {
            msg = ex.getMessage();
        }

        ObjetoError objetoError = new ObjetoError();
        objetoError.setCode(HttpStatus.INTERNAL_SERVER_ERROR + " ==> " + HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        objetoError.setError(msg);

        return new ResponseEntity<>(objetoError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
