package rica_api.publicaciones.infraestructura.salida.persistencia;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "eventos_procesados")
public class EventoProcesado {

    @Id
    private String eventoId;

    private Instant procesadoEn;

    public EventoProcesado() {
    }

    public EventoProcesado(String eventoId, Instant procesadoEn) {
        this.eventoId = eventoId;
        this.procesadoEn = procesadoEn;
    }

    public String getEventoId() {
        return eventoId;
    }

    public Instant getProcesadoEn() {
        return procesadoEn;
    }

}
