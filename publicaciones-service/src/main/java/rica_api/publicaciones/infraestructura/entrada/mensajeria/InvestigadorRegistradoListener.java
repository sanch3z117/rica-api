package rica_api.publicaciones.infraestructura.entrada.mensajeria;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import rica_api.publicaciones.aplicacion.IdempotenciaEventos;

import java.time.Instant;
import java.util.UUID;

@Component
public class InvestigadorRegistradoListener {

    private static final Logger log = LoggerFactory.getLogger(InvestigadorRegistradoListener.class);

    private final IdempotenciaEventos idempotenciaEventos;
    private final ObjectMapper objectMapper;

    public InvestigadorRegistradoListener(IdempotenciaEventos idempotenciaEventos, ObjectMapper objectMapper) {
        this.idempotenciaEventos = idempotenciaEventos;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "investigadores-eventos", groupId = "publicaciones-group")
    public void escuchar(String payload) {
        InvestigadorRegistradoMensaje mensaje = deserializar(payload);
        if (!idempotenciaEventos.reclamar(mensaje.eventoId().toString())) {
            log.info("El evento {} ya había sido procesado; se descarta el duplicado", mensaje.eventoId());
            return;
        }
        log.info("InvestigadorRegistrado: {} ({})", mensaje.nombreCompleto(), mensaje.correoInstitucional());
    }

    private InvestigadorRegistradoMensaje deserializar(String payload) {
        try {
            return objectMapper.readValue(payload, InvestigadorRegistradoMensaje.class);
        } catch (JsonProcessingException excepcion) {
            throw new IllegalStateException("El payload del evento no tiene el formato esperado: " + payload, excepcion);
        }
    }

    public record InvestigadorRegistradoMensaje(
            UUID eventoId,
            Long investigadorId,
            String correoInstitucional,
            String nombreCompleto,
            Instant ocurridoEn) {
    }

}
