package rica_api.investigadores.aplicacion;

import org.springframework.stereotype.Service;

import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.investigadores.dominio.Investigador;

import java.util.List;

@Service
public class InvestigadorService implements InvestigadorUseCase {

    private final RepositorioInvestigadores repositorioInvestigadores;
    private final InvestigadorFactory investigadorFactory;

    public InvestigadorService(RepositorioInvestigadores repositorioInvestigadores,
                               InvestigadorFactory investigadorFactory) {
        this.repositorioInvestigadores = repositorioInvestigadores;
        this.investigadorFactory = investigadorFactory;
    }

    @Override
    public List<Investigador> listarTodos() {
        return repositorioInvestigadores.listarTodos();
    }

    @Override
    public Investigador buscarPorId(Long id) {
        return repositorioInvestigadores.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un investigador con id " + id));
    }

    @Override
    public Investigador registrar(String nombreCompleto, String correoInstitucional, String grupoInvestigacion) {
        Investigador investigador = investigadorFactory.registrar(nombreCompleto, correoInstitucional, grupoInvestigacion);
        String correo = investigador.getCorreoInstitucional().valor();
        if (repositorioInvestigadores.existePorCorreo(correo)) {
            throw new CorreoDuplicadoException(
                    "Ya existe un investigador registrado con el correo " + correo);
        }
        return repositorioInvestigadores.guardar(investigador);
    }

}
