package curso.api.rest.controller;

import curso.api.rest.model.Profissao;
import curso.api.rest.repositoy.ProfissaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/profissao")
public class ProfissaoController {

    @Autowired
    private ProfissaoRepository profissaoRepository;

    @GetMapping(value = "/", produces = "application/json")
    public ResponseEntity<List<Profissao>> obterTodos() {
        List<Profissao> lista = profissaoRepository.findAll();
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

}
