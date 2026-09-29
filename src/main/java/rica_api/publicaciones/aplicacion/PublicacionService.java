package rica_api.publicaciones.aplicacion;

import org.springframework.stereotype.Service;

import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.publicaciones.dominio.LimitePublicacionesAnualesService;
import rica_api.publicaciones.dominio.Publicacion;

import java.util.List;

@Service
public class PublicacionService implements PublicacionUseCase {

    private final RepositorioPublicaciones repositorioPublicaciones;
    private final ConsultaInvestigadores consultaInvestigadores;
    private final LimitePublicacionesAnualesService limitePublicacionesAnualesService;

    public PublicacionService(RepositorioPublicaciones repositorioPublicaciones,
                              ConsultaInvestigadores consultaInvestigadores,
                              LimitePublicacionesAnualesService limitePublicacionesAnualesService) {
        this.repositorioPublicaciones = repositorioPublicaciones;
        this.consultaInvestigadores = consultaInvestigadores;
        this.limitePublicacionesAnualesService = limitePublicacionesAnualesService;
    }

    @Override
    public Publicacion registrar(Publicacion publicacion) {
        if (!consultaInvestigadores.existePorCorreo(publicacion.getInvestigadorCorreo())) {
            throw new RecursoNoEncontradoException(
                    "No existe un investigador con correo " + publicacion.getInvestigadorCorreo());
        }
        List<Publicacion> publicacionesDelInvestigador =
                repositorioPublicaciones.listarPorInvestigador(publicacion.getInvestigadorCorreo());
        limitePublicacionesAnualesService.validarRegistro(publicacionesDelInvestigador, publicacion.getAnio());
        return repositorioPublicaciones.guardar(publicacion);
    }

    @Override
    public List<Publicacion> listarPorInvestigador(String investigadorCorreo) {
        return repositorioPublicaciones.listarPorInvestigador(investigadorCorreo);
    }

    @Override
    public Publicacion buscarPorId(String id) {
        return repositorioPublicaciones.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una publicación con id " + id));
    }

}
