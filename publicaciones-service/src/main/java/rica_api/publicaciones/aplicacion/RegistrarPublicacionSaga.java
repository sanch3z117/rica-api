package rica_api.publicaciones.aplicacion;

import org.springframework.stereotype.Component;

import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.publicaciones.dominio.EstadoPublicacion;
import rica_api.publicaciones.dominio.LimitePublicacionesAnualesService;
import rica_api.publicaciones.dominio.Publicacion;

@Component
public class RegistrarPublicacionSaga {

    private final RepositorioPublicaciones repositorioPublicaciones;
    private final ConsultaInvestigadores consultaInvestigadores;
    private final LimitePublicacionesAnualesService limitePublicacionesAnualesService;

    public RegistrarPublicacionSaga(RepositorioPublicaciones repositorioPublicaciones,
                                    ConsultaInvestigadores consultaInvestigadores,
                                    LimitePublicacionesAnualesService limitePublicacionesAnualesService) {
        this.repositorioPublicaciones = repositorioPublicaciones;
        this.consultaInvestigadores = consultaInvestigadores;
        this.limitePublicacionesAnualesService = limitePublicacionesAnualesService;
    }

    public Publicacion ejecutar(Publicacion publicacion) {
        publicacion.setEstado(EstadoPublicacion.PENDIENTE);
        Publicacion enCurso = repositorioPublicaciones.guardar(publicacion);
        try {
            confirmarInvestigador(publicacion);
            validarLimiteAnual(publicacion);
            enCurso.setEstado(EstadoPublicacion.REGISTRADA);
            return repositorioPublicaciones.guardar(enCurso);
        } catch (RuntimeException excepcion) {
            compensar(enCurso);
            throw excepcion;
        }
    }

    private void confirmarInvestigador(Publicacion publicacion) {
        if (!consultaInvestigadores.existePorCorreo(publicacion.getInvestigadorCorreo())) {
            throw new RecursoNoEncontradoException(
                    "No existe un investigador con correo " + publicacion.getInvestigadorCorreo());
        }
    }

    private void validarLimiteAnual(Publicacion publicacion) {
        limitePublicacionesAnualesService.validarRegistro(
                repositorioPublicaciones.listarRegistradasPorInvestigador(publicacion.getInvestigadorCorreo()),
                publicacion.getAnio());
    }

    private void compensar(Publicacion publicacion) {
        publicacion.setEstado(EstadoPublicacion.RECHAZADA);
        repositorioPublicaciones.guardar(publicacion);
    }

}
