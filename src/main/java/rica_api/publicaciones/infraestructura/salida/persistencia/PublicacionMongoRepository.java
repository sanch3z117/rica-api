package rica_api.publicaciones.infraestructura.salida.persistencia;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PublicacionMongoRepository extends MongoRepository<PublicacionDocumento, String> {

    List<PublicacionDocumento> findByInvestigadorCorreo(String investigadorCorreo);

}
