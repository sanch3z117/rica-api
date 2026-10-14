package rica_api.investigadores.infraestructura.salida.persistencia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import rica_api.investigadores.aplicacion.EventoSalidaRepositorio;
import rica_api.investigadores.dominio.InvestigadorRegistrado;

import java.util.List;

@Component
public class EventoOutboxJpaAdapter implements EventoSalidaRepositorio {

    private final EventoOutboxJpaRepository eventoOutboxJpaRepository;
    private final ObjectMapper objectMapper;

    public EventoOutboxJpaAdapter(EventoOutboxJpaRepository eventoOutboxJpaRepository, ObjectMapper objectMapper) {
        this.eventoOutboxJpaRepository = eventoOutboxJpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void registrar(InvestigadorRegistrado evento) {
        eventoOutboxJpaRepository.save(new EventoOutboxEntidad(
                evento.eventoId(), "InvestigadorRegistrado", serializar(evento), evento.ocurridoEn()));
    }

    @Override
    public List<EventoSalidaPendiente> buscarPendientes() {
        return eventoOutboxJpaRepository.findByPublicadoFalseOrderByIdAsc().stream()
                .map(entidad -> new EventoSalidaPendiente(entidad.getId(), entidad.getTipo(), entidad.getPayload()))
                .toList();
    }

    @Override
    public void marcarPublicado(long id) {
        eventoOutboxJpaRepository.findById(id).ifPresent(EventoOutboxEntidad::marcarPublicado);
    }

    private String serializar(InvestigadorRegistrado evento) {
        try {
            return objectMapper.writeValueAsString(evento);
        } catch (JsonProcessingException excepcion) {
            throw new IllegalStateException("No fue posible serializar el evento " + evento.eventoId(), excepcion);
        }
    }

}
