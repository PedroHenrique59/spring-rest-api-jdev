package curso.api.rest.controller;

import curso.api.rest.ObjetoError;
import curso.api.rest.model.Usuario;
import curso.api.rest.repositoy.UsuarioRepository;
import curso.api.rest.service.ServiceEnviaEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@RestController
@RequestMapping(value = "/recuperar")
public class RecuperaController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServiceEnviaEmail serviceEnviaEmail;

    @ResponseBody
    @PostMapping(value = "/")
    public ResponseEntity<ObjetoError> recuperar(@RequestBody Usuario usuario) throws Exception {
        ObjetoError objetoError = new ObjetoError();

        Usuario user = usuarioRepository.findUsuarioByLogin(usuario.getLogin());

        if (user == null) {
            objetoError.setCode("404");
            objetoError.setError("Usuário não encontrado!");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String senhaNova = sdf.format(Calendar.getInstance().getTime());

            String senhaCriptografada = new BCryptPasswordEncoder().encode(senhaNova);
            usuarioRepository.updateSenha(senhaCriptografada, user.getId());

            serviceEnviaEmail.enviarEmail("Recuperação de senha", user.getLogin(), "Sua nova senha é: " + senhaNova);

            objetoError.setCode("200");
            objetoError.setError("Acesso enviado para seu e-mail!");
        }

        return new ResponseEntity<>(objetoError, HttpStatus.OK);
    }
}
