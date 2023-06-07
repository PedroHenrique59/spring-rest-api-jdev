package curso.api.rest.controller;

import curso.api.rest.model.Profissao;
import curso.api.rest.model.Usuario;
import curso.api.rest.model.UsuarioChart;
import curso.api.rest.model.UsuarioReport;
import curso.api.rest.repositoy.TelefoneRepository;
import curso.api.rest.repositoy.UsuarioRepository;
import curso.api.rest.service.ImplementacaoUserDetailsSercice;
import curso.api.rest.service.RelatorioService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController /* Arquitetura REST */
@RequestMapping(value = "/usuario")
public class IndexController {

    @Autowired /* de fosse CDI seria @Inject*/
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TelefoneRepository telefoneRepository;

    @Autowired
    private ImplementacaoUserDetailsSercice implementacaoUserDetailsSercice;

    @Autowired
    private RelatorioService relatorioService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @DeleteMapping(value = "/excluirTelefone/{id}", produces = "application/text")
    public String excluirTelefone(@PathVariable("id") Long id) {
        telefoneRepository.deleteById(id);
        return "Ok";
    }

    @GetMapping(value = "/relatorio", produces = "application/text")
    public ResponseEntity<String> downloadRelatorio(HttpServletRequest request) throws Exception {
        byte[] pdf = relatorioService.gerarRelatorio("relatorio-usuario", new HashMap<>(), request.getServletContext());
        String base64Pdf = "data:application/pdf;base64," + Base64.encodeBase64String(pdf);
        return new ResponseEntity<>(base64Pdf, HttpStatus.OK);
    }

    @PostMapping(value = "/relatorio/", produces = "application/text")
    public ResponseEntity<String> downloadRelatorioParam(HttpServletRequest request, @RequestBody UsuarioReport usuarioReport) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat dateFormatParam = new SimpleDateFormat("yyyy-MM-dd");

        String dataInicio = dateFormatParam.format(dateFormat.parse(usuarioReport.getDataInicio()));
        String dataFim = dateFormatParam.format(dateFormat.parse(usuarioReport.getDataFim()));

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("DATA_INICIO", usuarioReport.getDataInicio());
        parametros.put("DATA_FIM", usuarioReport.getDataFim());

        byte[] pdf = relatorioService.gerarRelatorio("relatorio-usuario-parametro", parametros, request.getServletContext());
        String base64Pdf = "data:application/pdf;base64," + Base64.encodeBase64String(pdf);
        return new ResponseEntity<>(base64Pdf, HttpStatus.OK);
    }

    @GetMapping(value = "/grafico", produces = "application/json")
    public ResponseEntity<UsuarioChart> montarGrafico() {
        UsuarioChart usuarioChart = new UsuarioChart();

        List<String> resultado = jdbcTemplate.queryForList("select array_agg (nome) from usuario where salario > 0 union all select cast(array_agg (salario) as character varying[]) from usuario where salario > 0", String.class);

        if (!resultado.isEmpty()) {
            String nomes = resultado.get(0).replaceAll("\\{", "").replaceAll("\\}", "");
            String salario = resultado.get(1).replaceAll("\\{", "").replaceAll("\\}", "");
            usuarioChart.setNome(nomes);
            usuarioChart.setSalario(salario);
        }

        return new ResponseEntity<>(usuarioChart, HttpStatus.OK);
    }

}
