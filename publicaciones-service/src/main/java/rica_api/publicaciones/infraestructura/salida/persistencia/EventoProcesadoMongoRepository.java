package rica_api.publicaciones.infraestructura.salida.persistencia;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventoProcesadoMongoRepository extends MongoRepository<EventoProcesado, String> {
}
