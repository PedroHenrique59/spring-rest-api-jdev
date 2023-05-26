package curso.api.rest.controller;

import curso.api.rest.model.Profissao;
import curso.api.rest.model.Usuario;
import curso.api.rest.repositoy.TelefoneRepository;
import curso.api.rest.repositoy.UsuarioRepository;
import curso.api.rest.service.ImplementacaoUserDetailsSercice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController /* Arquitetura REST */
@RequestMapping(value = "/usuario")
public class IndexController {

    @Autowired /* de fosse CDI seria @Inject*/
    private UsuarioRepository usuarioRepository;

    @Autowired /* de fosse CDI seria @Inject*/
    private TelefoneRepository telefoneRepository;

    @Autowired
    private ImplementacaoUserDetailsSercice implementacaoUserDetailsSercice;

    /* Serviço RESTful */
    @GetMapping(value = "/{id}/codigovenda/{venda}", produces = "application/json")
    public ResponseEntity<Usuario> relatorio(@PathVariable(value = "id") Long id, @PathVariable(value = "venda") Long venda) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        /*o retorno seria um relatorio*/
        return new ResponseEntity<>(usuario.get(), HttpStatus.OK);
    }

    /* Serviço RESTful */
    @GetMapping(value = "/{id}", produces = "application/json")
    @CacheEvict(value = "cacheuser", allEntries = true)
    @CachePut("cacheuser")
    public ResponseEntity<Usuario> init(@PathVariable(value = "id") Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            if (usuario.get().getProfissao() == null) {
                usuario.get().setProfissao(new Profissao());
            }
            return new ResponseEntity<>(usuario.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/{id}", produces = "application/text")
    public String delete(@PathVariable("id") Long id) {
        usuarioRepository.deleteById(id);
        return "ok";
    }

    @DeleteMapping(value = "/{id}/venda", produces = "application/text")
    public String deletevenda(@PathVariable("id") Long id) {
        usuarioRepository.deleteById(id);
        return "ok";
    }

    @GetMapping(value = "/", produces = "application/json")
    @CacheEvict(value = "cacheusuarios", allEntries = true)
    @CachePut("cacheusuarios")
    public ResponseEntity<Page<Usuario>> usuario() throws InterruptedException {

        PageRequest page = PageRequest.of(0, 2, Sort.by("nome"));

        Page<Usuario> lista = usuarioRepository.findAll(page);

        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    @GetMapping(value = "/page/{pagina}", produces = "application/json")
    @CacheEvict(value = "cacheusuarios", allEntries = true)
    @CachePut("cacheusuarios")
    public ResponseEntity<Page<Usuario>> usuarioPagina(@PathVariable("pagina") int pagina) throws InterruptedException {

        PageRequest page = PageRequest.of(pagina, 2, Sort.by("nome"));

        Page<Usuario> lista = usuarioRepository.findAll(page);

        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    @GetMapping(value = "/usuarioPorNome/{nome}", produces = "application/json")
    @CachePut("cacheusuarios")
    public ResponseEntity<Page<Usuario>> obterUsuarioPorNome(@PathVariable("nome") String nome) throws InterruptedException {

        PageRequest page;
        Page<Usuario> list;

        if (nome == null || nome.trim().isEmpty() || nome.equalsIgnoreCase("undefined")) {
            page = PageRequest.of(0, 2, Sort.by("nome"));
            list = usuarioRepository.findAll(page);
        } else {
            page = PageRequest.of(0, 2, Sort.by("nome"));
            list = usuarioRepository.findUsuariosByNomeContainsIgnoreCase(nome, page);
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping(value = "/usuarioPorNome/{nome}/page/{page}", produces = "application/json")
    @CachePut("cacheusuarios")
    public ResponseEntity<Page<Usuario>> obterUsuarioPorNomePage(@PathVariable("nome") String nome, @PathVariable("page") int page) throws InterruptedException {

        PageRequest pageRequest;
        Page<Usuario> list;

        if (nome == null || nome.trim().isEmpty() || nome.equalsIgnoreCase("undefined")) {
            pageRequest = PageRequest.of(page, 2, Sort.by("nome"));
            list = usuarioRepository.findAll(pageRequest);
        } else {
            pageRequest = PageRequest.of(page, 2, Sort.by("nome"));
            list = usuarioRepository.findUsuariosByNomeContainsIgnoreCase(nome, pageRequest);
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping(value = "/", produces = "application/json")
    public ResponseEntity<Usuario> cadastrar(@RequestBody @Valid Usuario usuario) {

        for (int pos = 0; pos < usuario.getTelefones().size(); pos++) {
            usuario.getTelefones().get(pos).setUsuario(usuario);
        }

        String senhacriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
        usuario.setSenha(senhacriptografada);
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        implementacaoUserDetailsSercice.insereAcessoPadrao(usuario.getId());

        return new ResponseEntity<>(usuarioSalvo, HttpStatus.OK);
    }

    @PutMapping(value = "/", produces = "application/json")
    public ResponseEntity<Usuario> atualizar(@RequestBody Usuario usuario) {

        for (int pos = 0; pos < usuario.getTelefones().size(); pos++) {
            usuario.getTelefones().get(pos).setUsuario(usuario);
        }

        Usuario userTemporario = usuarioRepository.findById(usuario.getId()).get();

        if (!userTemporario.getSenha().equals(usuario.getSenha())) { /*Senhas diferentes*/
            String senhacriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
            usuario.setSenha(senhacriptografada);
        }

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return new ResponseEntity<>(usuarioSalvo, HttpStatus.OK);
    }

    @PutMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
    public ResponseEntity updateVenda(@PathVariable Long iduser,
                                      @PathVariable Long idvenda) {
        /*outras rotinas antes de atualizar*/

        //Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return new ResponseEntity("Venda atualzada", HttpStatus.OK);
    }


    @PostMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
    public ResponseEntity cadastrarvenda(@PathVariable Long iduser,
                                         @PathVariable Long idvenda) {

        /*Aqui seria o processo de venda*/
        //Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return new ResponseEntity("id user :" + iduser + " idvenda :" + idvenda, HttpStatus.OK);

    }

    @DeleteMapping(value = "/excluirTelefone/{id}", produces = "application/text")
    public String excluirTelefone(@PathVariable("id") Long id) {
        telefoneRepository.deleteById(id);
        return "Ok";
    }

}
