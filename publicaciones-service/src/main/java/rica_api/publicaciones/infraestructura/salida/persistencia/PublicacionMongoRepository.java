package rica_api.publicaciones.infraestructura.salida.persistencia;

import org.springframework.data.mongodb.repository.MongoRepository;

import rica_api.publicaciones.dominio.EstadoPublicacion;
import rica_api.publicaciones.dominio.Publicacion;

import java.util.List;

public interface PublicacionMongoRepository extends MongoRepository<Publicacion, String> {

    List<Publicacion> findByInvestigadorCorreoAndEstado(String investigadorCorreo, EstadoPublicacion estado);

}
