package curso.api.rest.repositoy;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import curso.api.rest.model.Usuario;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("select u from Usuario u where u.login = ?1")
    Usuario findUsuarioByLogin(String login);

    @Query(value = "select constraint_name \n" +
            "from information_schema.constraint_column_usage\n" +
            "where table_name = 'usuarios_role'\n" +
            "and column_name = 'role_id'\n" +
            "and constraint_name <> 'unique_role_user';", nativeQuery = true)
    String consultaConstraintRole();

    @Modifying
    @Transactional
    @Query(value = "insert into usuarios_role (usuario_id, role_id) values (?1, (select id from role where nome_role = 'ROLE_USER'));", nativeQuery = true)
    void insereAcessoPadrao(Long idUser);

    Page<Usuario> findUsuariosByNomeContainsIgnoreCase(String nome, Pageable page);

    /* Método para buscar um usuário do banco que contenha parte do nome passado como parametro */
    default Page<Usuario> findUsuarioByNamePage(String nome, PageRequest pageRequest) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);

        ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny().
                withMatcher("nome", ExampleMatcher.GenericPropertyMatchers
                        .contains().ignoreCase());

        Example<Usuario> example = Example.of(usuario, exampleMatcher);

        Page<Usuario> retorno = findAll(example, pageRequest);

        return retorno;

    }

}
