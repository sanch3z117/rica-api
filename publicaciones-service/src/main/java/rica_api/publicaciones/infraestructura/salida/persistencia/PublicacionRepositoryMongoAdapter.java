package rica_api.publicaciones.infraestructura.salida.persistencia;

import org.springframework.stereotype.Component;

import rica_api.publicaciones.aplicacion.RepositorioPublicaciones;
import rica_api.publicaciones.dominio.EstadoPublicacion;
import rica_api.publicaciones.dominio.Publicacion;

import java.util.List;
import java.util.Optional;

@Component
public class PublicacionRepositoryMongoAdapter implements RepositorioPublicaciones {

    private final PublicacionMongoRepository publicacionMongoRepository;

    public PublicacionRepositoryMongoAdapter(PublicacionMongoRepository publicacionMongoRepository) {
        this.publicacionMongoRepository = publicacionMongoRepository;
    }

    @Override
    public List<Publicacion> listarRegistradasPorInvestigador(String investigadorCorreo) {
        return publicacionMongoRepository.findByInvestigadorCorreoAndEstado(
                investigadorCorreo, EstadoPublicacion.REGISTRADA);
    }

    @Override
    public Optional<Publicacion> buscarPorId(String id) {
        return publicacionMongoRepository.findById(id);
    }

    @Override
    public Publicacion guardar(Publicacion publicacion) {
        return publicacionMongoRepository.save(publicacion);
    }

}
