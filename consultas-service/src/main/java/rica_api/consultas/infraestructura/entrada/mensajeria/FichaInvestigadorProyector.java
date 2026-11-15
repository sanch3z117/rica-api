package rica_api.consultas.infraestructura.entrada.mensajeria;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import rica_api.consultas.infraestructura.salida.persistencia.FichaInvestigadorRepository;
import rica_api.consultas.modelo.FichaInvestigador;

import java.util.UUID;

@Component
public class FichaInvestigadorProyector {

    private static final Logger log = LoggerFactory.getLogger(FichaInvestigadorProyector.class);

    private final FichaInvestigadorRepository fichas;
    private final ObjectMapper objectMapper;

    public FichaInvestigadorProyector(FichaInvestigadorRepository fichas, ObjectMapper objectMapper) {
        this.fichas = fichas;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "investigadores-eventos", groupId = "consultas-group")
    public void proyectar(String payload) {
        EventoInvestigadorRegistrado evento = deserializar(payload);
        String id = String.valueOf(evento.investigadorId());
        FichaInvestigador ficha = fichas.findById(id).orElseGet(() -> new FichaInvestigador(id));
        ficha.setNombreCompleto(evento.nombreCompleto());
        ficha.setCorreoInstitucional(evento.correoInstitucional());
        ficha.setEstado("REGISTRADO");
        fichas.save(ficha);
        log.info("Ficha proyectada para el investigador {}", id);
    }

    private EventoInvestigadorRegistrado deserializar(String payload) {
        try {
            return objectMapper.readValue(payload, EventoInvestigadorRegistrado.class);
        } catch (JsonProcessingException excepcion) {
            throw new IllegalStateException("El payload del evento no tiene el formato esperado: " + payload, excepcion);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EventoInvestigadorRegistrado(
            UUID eventoId,
            Long investigadorId,
            String correoInstitucional,
            String nombreCompleto) {
    }

}
