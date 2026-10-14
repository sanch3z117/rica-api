package rica_api.publicaciones.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.publicaciones.dominio.Publicacion;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicacionServiceTest {

    private static final String CORREO_ANA = "ana.torres@uptc.edu.co";

    private final RepositorioPublicaciones repositorioPublicaciones = mock(RepositorioPublicaciones.class);
    private final RegistrarPublicacionSaga registrarPublicacionSaga = mock(RegistrarPublicacionSaga.class);

    private PublicacionService publicacionService;

    @BeforeEach
    void prepararServicio() {
        publicacionService = new PublicacionService(repositorioPublicaciones, registrarPublicacionSaga);
    }

    @Test
    void registrarDelegaEnLaSaga() {
        Publicacion nueva = new Publicacion();
        when(registrarPublicacionSaga.ejecutar(nueva)).thenReturn(nueva);

        Publicacion resultado = publicacionService.registrar(nueva);

        assertThat(resultado).isSameAs(nueva);
        verify(registrarPublicacionSaga).ejecutar(nueva);
    }

    @Test
    void listarPorInvestigadorDevuelveSoloLasRegistradas() {
        Publicacion registrada = new Publicacion();
        when(repositorioPublicaciones.listarRegistradasPorInvestigador(CORREO_ANA))
                .thenReturn(List.of(registrada));

        List<Publicacion> resultado = publicacionService.listarPorInvestigador(CORREO_ANA);

        assertThat(resultado).containsExactly(registrada);
        verify(repositorioPublicaciones).listarRegistradasPorInvestigador(CORREO_ANA);
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(repositorioPublicaciones.buscarPorId("publicacion-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicacionService.buscarPorId("publicacion-inexistente"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("publicacion-inexistente");
    }

}
