package rica_api.investigadores.aplicacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.investigadores.dominio.Investigador;
import rica_api.investigadores.dominio.InvestigadorRegistrado;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InvestigadorService implements InvestigadorUseCase {

    private final RepositorioInvestigadores repositorioInvestigadores;
    private final InvestigadorFactory investigadorFactory;
    private final EventoSalidaRepositorio eventoSalidaRepositorio;

    public InvestigadorService(RepositorioInvestigadores repositorioInvestigadores,
                               InvestigadorFactory investigadorFactory,
                               EventoSalidaRepositorio eventoSalidaRepositorio) {
        this.repositorioInvestigadores = repositorioInvestigadores;
        this.investigadorFactory = investigadorFactory;
        this.eventoSalidaRepositorio = eventoSalidaRepositorio;
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
    @Transactional
    public Investigador registrar(String nombreCompleto, String correoInstitucional, String grupoInvestigacion) {
        Investigador investigador = investigadorFactory.registrar(nombreCompleto, correoInstitucional, grupoInvestigacion);
        String correo = investigador.getCorreoInstitucional().valor();
        if (repositorioInvestigadores.existePorCorreo(correo)) {
            throw new CorreoDuplicadoException(
                    "Ya existe un investigador registrado con el correo " + correo);
        }
        Investigador guardado = repositorioInvestigadores.guardar(investigador);
        eventoSalidaRepositorio.registrar(new InvestigadorRegistrado(
                UUID.randomUUID(),
                guardado.getId(),
                correo,
                guardado.getNombreCompleto(),
                Instant.now()));
        return guardado;
    }

    @Override
    public boolean existePorCorreo(String correoInstitucional) {
        return repositorioInvestigadores.existePorCorreo(correoInstitucional);
    }

}
