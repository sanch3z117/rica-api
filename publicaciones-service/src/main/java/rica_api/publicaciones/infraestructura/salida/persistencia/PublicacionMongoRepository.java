package rica_api.publicaciones.infraestructura.salida.persistencia;

import org.springframework.data.mongodb.repository.MongoRepository;

import rica_api.publicaciones.dominio.EstadoPublicacion;

import java.util.List;

public interface PublicacionMongoRepository extends MongoRepository<PublicacionDocumento, String> {

    List<PublicacionDocumento> findByInvestigadorCorreoAndEstado(String investigadorCorreo, EstadoPublicacion estado);

}
