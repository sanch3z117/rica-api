package rica_api.publicaciones.aplicacion;

import org.springframework.stereotype.Service;

import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.publicaciones.dominio.Publicacion;

import java.util.List;

@Service
public class PublicacionService implements PublicacionUseCase {

    private final RepositorioPublicaciones repositorioPublicaciones;
    private final RegistrarPublicacionSaga registrarPublicacionSaga;

    public PublicacionService(RepositorioPublicaciones repositorioPublicaciones,
                              RegistrarPublicacionSaga registrarPublicacionSaga) {
        this.repositorioPublicaciones = repositorioPublicaciones;
        this.registrarPublicacionSaga = registrarPublicacionSaga;
    }

    @Override
    public Publicacion registrar(Publicacion publicacion) {
        return registrarPublicacionSaga.ejecutar(publicacion);
    }

    @Override
    public List<Publicacion> listarPorInvestigador(String investigadorCorreo) {
        return repositorioPublicaciones.listarRegistradasPorInvestigador(investigadorCorreo);
    }

    @Override
    public Publicacion buscarPorId(String id) {
        return repositorioPublicaciones.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una publicación con id " + id));
    }

}
