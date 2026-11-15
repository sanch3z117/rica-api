package rica_api.consultas.infraestructura.entrada.mensajeria;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import rica_api.consultas.infraestructura.salida.persistencia.FichaInvestigadorRepository;
import rica_api.consultas.modelo.FichaInvestigador;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FichaInvestigadorProyectorTest {

    private static final String PAYLOAD = """
            {"eventoId":"11111111-1111-1111-1111-111111111111","investigadorId":7,"correoInstitucional":"ana.torres@uptc.edu.co","nombreCompleto":"Ana Torres","ocurridoEn":"2026-01-01T00:00:00Z"}
            """;

    @Mock
    private FichaInvestigadorRepository fichas;

    private FichaInvestigadorProyector proyector;

    @BeforeEach
    void prepararProyector() {
        proyector = new FichaInvestigadorProyector(fichas, new ObjectMapper());
    }

    @Test
    void proyectaUnaFichaCuandoElInvestigadorEsNuevo() {
        when(fichas.findById("7")).thenReturn(Optional.empty());
        when(fichas.save(any(FichaInvestigador.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        proyector.proyectar(PAYLOAD);

        ArgumentCaptor<FichaInvestigador> captor = ArgumentCaptor.forClass(FichaInvestigador.class);
        verify(fichas).save(captor.capture());
        FichaInvestigador ficha = captor.getValue();
        assertThat(ficha.getId()).isEqualTo("7");
        assertThat(ficha.getNombreCompleto()).isEqualTo("Ana Torres");
        assertThat(ficha.getCorreoInstitucional()).isEqualTo("ana.torres@uptc.edu.co");
        assertThat(ficha.getEstado()).isEqualTo("REGISTRADO");
    }

    @Test
    void esIdempotenteAlReproyectarElMismoEvento() {
        FichaInvestigador existente = new FichaInvestigador("7");
        when(fichas.findById("7")).thenReturn(Optional.of(existente));
        when(fichas.save(any(FichaInvestigador.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        proyector.proyectar(PAYLOAD);
        proyector.proyectar(PAYLOAD);

        assertThat(existente.getNombreCompleto()).isEqualTo("Ana Torres");
        verify(fichas, times(2)).save(existente);
    }

}
