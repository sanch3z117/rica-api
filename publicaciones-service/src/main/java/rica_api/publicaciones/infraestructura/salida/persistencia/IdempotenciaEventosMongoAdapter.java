package rica_api.publicaciones.infraestructura.salida.persistencia;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import rica_api.publicaciones.aplicacion.IdempotenciaEventos;

import java.time.Instant;

@Component
public class IdempotenciaEventosMongoAdapter implements IdempotenciaEventos {

    private final EventoProcesadoMongoRepository eventoProcesadoMongoRepository;

    public IdempotenciaEventosMongoAdapter(EventoProcesadoMongoRepository eventoProcesadoMongoRepository) {
        this.eventoProcesadoMongoRepository = eventoProcesadoMongoRepository;
    }

    @Override
    public boolean reclamar(String eventoId) {
        try {
            eventoProcesadoMongoRepository.save(new EventoProcesado(eventoId, Instant.now()));
            return true;
        } catch (DuplicateKeyException excepcion) {
            return false;
        }
    }

}
