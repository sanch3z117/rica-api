package rica_api.investigadores.aplicacion;

import rica_api.investigadores.dominio.Investigador;

import java.util.List;
import java.util.Optional;

public interface RepositorioInvestigadores {

    List<Investigador> listarTodos();

    Optional<Investigador> buscarPorId(Long id);

    boolean existePorCorreo(String correoInstitucional);

    Investigador guardar(Investigador investigador);

}
