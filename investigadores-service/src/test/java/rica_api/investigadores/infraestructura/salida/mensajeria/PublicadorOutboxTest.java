package rica_api.investigadores.infraestructura.salida.mensajeria;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import rica_api.investigadores.aplicacion.EventoSalidaRepositorio;
import rica_api.investigadores.aplicacion.EventoSalidaRepositorio.EventoSalidaPendiente;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PublicadorOutboxTest {

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);

    private final EventoSalidaRepositorio eventoSalidaRepositorio = mock(EventoSalidaRepositorio.class);

    private final PublicadorOutbox publicadorOutbox =
            new PublicadorOutbox(eventoSalidaRepositorio, kafkaTemplate);

    @Test
    void publicaLosEventosPendientesYLosMarcaComoPublicados() {
        EventoSalidaPendiente pendiente = new EventoSalidaPendiente(7L, "InvestigadorRegistrado",
                "{\"eventoId\":\"d0f1a2b3\",\"investigadorId\":7}");
        when(eventoSalidaRepositorio.buscarPendientes()).thenReturn(List.of(pendiente));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        publicadorOutbox.publicarPendientes();

        verify(kafkaTemplate).send("investigadores-eventos", "7", pendiente.payload());
        verify(eventoSalidaRepositorio).marcarPublicado(7L);
    }

    @Test
    void noPublicaNadaCuandoNoHayPendientes() {
        when(eventoSalidaRepositorio.buscarPendientes()).thenReturn(List.of());

        publicadorOutbox.publicarPendientes();

        verifyNoInteractions(kafkaTemplate);
        verify(eventoSalidaRepositorio, never()).marcarPublicado(anyLong());
    }

}
