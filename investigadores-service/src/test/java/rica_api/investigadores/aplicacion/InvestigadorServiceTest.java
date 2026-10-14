package rica_api.investigadores.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.investigadores.dominio.CorreoInstitucional;
import rica_api.investigadores.dominio.Investigador;
import rica_api.investigadores.dominio.InvestigadorRegistrado;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestigadorServiceTest {

    @Mock
    private RepositorioInvestigadores repositorioInvestigadores;

    @Mock
    private EventoSalidaRepositorio eventoSalidaRepositorio;

    private InvestigadorService investigadorService;

    @BeforeEach
    void prepararServicio() {
        investigadorService = new InvestigadorService(
                repositorioInvestigadores, new InvestigadorFactory(), eventoSalidaRepositorio);
    }

    @Test
    void buscarPorIdDevuelveElInvestigadorCuandoExiste() {
        Investigador investigador = new Investigador(
                1L, "Ana Torres", new CorreoInstitucional("ana.torres@uptc.edu.co"), "GIT-UPTC");
        when(repositorioInvestigadores.buscarPorId(1L)).thenReturn(Optional.of(investigador));

        Investigador resultado = investigadorService.buscarPorId(1L);

        assertThat(resultado.getNombreCompleto()).isEqualTo("Ana Torres");
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(repositorioInvestigadores.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> investigadorService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void registrarGuardaElInvestigadorYSuEventoDeDominio() {
        when(repositorioInvestigadores.existePorCorreo("ana.torres@uptc.edu.co")).thenReturn(false);
        when(repositorioInvestigadores.guardar(any(Investigador.class))).thenAnswer(invocacion -> {
            Investigador sinId = invocacion.getArgument(0);
            return new Investigador(7L, sinId.getNombreCompleto(), sinId.getCorreoInstitucional(),
                    sinId.getGrupoInvestigacion());
        });

        Investigador resultado = investigadorService.registrar(
                "Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");

        assertThat(resultado.getId()).isEqualTo(7L);
        assertThat(resultado.getCorreoInstitucional().valor()).isEqualTo("ana.torres@uptc.edu.co");
        assertThat(resultado.getGrupoInvestigacion()).isEqualTo("GIT-UPTC");

        ArgumentCaptor<InvestigadorRegistrado> captor = ArgumentCaptor.forClass(InvestigadorRegistrado.class);
        verify(eventoSalidaRepositorio).registrar(captor.capture());
        InvestigadorRegistrado evento = captor.getValue();
        assertThat(evento.eventoId()).isNotNull();
        assertThat(evento.investigadorId()).isEqualTo(7L);
        assertThat(evento.correoInstitucional()).isEqualTo("ana.torres@uptc.edu.co");
        assertThat(evento.nombreCompleto()).isEqualTo("Ana Torres");
        assertThat(evento.ocurridoEn()).isNotNull();
    }

    @Test
    void registrarRechazaCorreoInstitucionalDuplicado() {
        when(repositorioInvestigadores.existePorCorreo("carlos.ruiz@uptc.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> investigadorService.registrar(
                "Carlos Ruiz", "carlos.ruiz@uptc.edu.co", "GIT-UPTC"))
                .isInstanceOf(CorreoDuplicadoException.class);

        verify(repositorioInvestigadores).existePorCorreo("carlos.ruiz@uptc.edu.co");
        verify(repositorioInvestigadores, never()).guardar(any(Investigador.class));
        verify(eventoSalidaRepositorio, never()).registrar(any(InvestigadorRegistrado.class));
    }

    @Test
    void registrarRechazaUnCorreoFueraDelDominioInstitucional() {
        assertThatThrownBy(() -> investigadorService.registrar(
                "Carlos Ruiz", "carlos.ruiz@universidad.edu", "GIT-UPTC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@uptc.edu.co");
    }

    @Test
    void existePorCorreoDelegaEnElRepositorio() {
        when(repositorioInvestigadores.existePorCorreo("ana.torres@uptc.edu.co")).thenReturn(true);

        assertThat(investigadorService.existePorCorreo("ana.torres@uptc.edu.co")).isTrue();

        verify(repositorioInvestigadores).existePorCorreo("ana.torres@uptc.edu.co");
    }

}
