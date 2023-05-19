package curso.api.rest.repositoy;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import curso.api.rest.model.Usuario;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

    @Query("select u from Usuario u where u.login = ?1")
    Usuario findUsuarioByLogin(String login);

    List<Usuario> findUsuariosByNomeContainsIgnoreCase(String nome);

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

}
