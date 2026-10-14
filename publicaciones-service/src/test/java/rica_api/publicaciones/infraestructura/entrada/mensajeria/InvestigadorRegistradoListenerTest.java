package rica_api.publicaciones.infraestructura.entrada.mensajeria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rica_api.publicaciones.aplicacion.IdempotenciaEventos;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class InvestigadorRegistradoListenerTest {

    private static final String EVENTO_ID = "7f9a1d2e-3b4c-4d5e-8f90-0a1b2c3d4e5f";

    private static final String PAYLOAD = """
            {"eventoId":"7f9a1d2e-3b4c-4d5e-8f90-0a1b2c3d4e5f","investigadorId":7,
             "correoInstitucional":"ana.torres@uptc.edu.co","nombreCompleto":"Ana Torres",
             "ocurridoEn":"2026-10-13T10:00:00Z"}""";

    private final IdempotenciaEventos idempotenciaEventos = mock(IdempotenciaEventos.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private InvestigadorRegistradoListener listener;

    @BeforeEach
    void prepararListener() {
        listener = new InvestigadorRegistradoListener(idempotenciaEventos, objectMapper);
    }

    @Test
    void procesaElEventoLaPrimeraVez() {
        when(idempotenciaEventos.reclamar(EVENTO_ID)).thenReturn(true);

        listener.escuchar(PAYLOAD);

        verify(idempotenciaEventos).reclamar(EVENTO_ID);
        verifyNoMoreInteractions(idempotenciaEventos);
    }

    @Test
    void descartaElEventoCuandoYaHabiaSidoProcesado() {
        when(idempotenciaEventos.reclamar(EVENTO_ID)).thenReturn(false);

        listener.escuchar(PAYLOAD);

        verify(idempotenciaEventos).reclamar(EVENTO_ID);
        verifyNoMoreInteractions(idempotenciaEventos);
    }

}
