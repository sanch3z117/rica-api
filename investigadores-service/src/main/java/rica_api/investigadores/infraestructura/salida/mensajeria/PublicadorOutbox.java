package rica_api.investigadores.infraestructura.salida.mensajeria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import rica_api.investigadores.aplicacion.EventoSalidaRepositorio;
import rica_api.investigadores.aplicacion.EventoSalidaRepositorio.EventoSalidaPendiente;

@Component
public class PublicadorOutbox {

    private static final Logger log = LoggerFactory.getLogger(PublicadorOutbox.class);
    private static final String TOPIC = "investigadores-eventos";

    private final EventoSalidaRepositorio eventoSalidaRepositorio;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public PublicadorOutbox(EventoSalidaRepositorio eventoSalidaRepositorio,
                            KafkaTemplate<String, String> kafkaTemplate) {
        this.eventoSalidaRepositorio = eventoSalidaRepositorio;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publicarPendientes() {
        for (EventoSalidaPendiente pendiente : eventoSalidaRepositorio.buscarPendientes()) {
            kafkaTemplate.send(TOPIC, String.valueOf(pendiente.id()), pendiente.payload()).join();
            eventoSalidaRepositorio.marcarPublicado(pendiente.id());
            log.info("Evento {} publicado en el topic {}", pendiente.tipo(), TOPIC);
        }
    }

}
