package rica_api.investigadores.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.investigadores.dominio.CorreoInstitucional;
import rica_api.investigadores.dominio.Investigador;

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

    private InvestigadorService investigadorService;

    @BeforeEach
    void prepararServicio() {
        investigadorService = new InvestigadorService(repositorioInvestigadores, new InvestigadorFactory());
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
    void registrarGuardaElInvestigadorConstruidoPorLaFactory() {
        when(repositorioInvestigadores.existePorCorreo("ana.torres@uptc.edu.co")).thenReturn(false);
        when(repositorioInvestigadores.guardar(any(Investigador.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Investigador resultado = investigadorService.registrar(
                "Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");

        assertThat(resultado.getNombreCompleto()).isEqualTo("Ana Torres");
        assertThat(resultado.getCorreoInstitucional().valor()).isEqualTo("ana.torres@uptc.edu.co");
        assertThat(resultado.getGrupoInvestigacion()).isEqualTo("GIT-UPTC");
        verify(repositorioInvestigadores).guardar(resultado);
    }

    @Test
    void registrarRechazaCorreoInstitucionalDuplicado() {
        when(repositorioInvestigadores.existePorCorreo("carlos.ruiz@uptc.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> investigadorService.registrar(
                "Carlos Ruiz", "carlos.ruiz@uptc.edu.co", "GIT-UPTC"))
                .isInstanceOf(CorreoDuplicadoException.class);

        verify(repositorioInvestigadores).existePorCorreo("carlos.ruiz@uptc.edu.co");
        verify(repositorioInvestigadores, never()).guardar(any(Investigador.class));
    }

    @Test
    void registrarRechazaUnCorreoFueraDelDominioInstitucional() {
        assertThatThrownBy(() -> investigadorService.registrar(
                "Carlos Ruiz", "carlos.ruiz@universidad.edu", "GIT-UPTC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@uptc.edu.co");
    }

}
