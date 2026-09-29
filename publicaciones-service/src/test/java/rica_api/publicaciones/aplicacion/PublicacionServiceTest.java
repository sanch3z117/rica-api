package rica_api.publicaciones.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.publicaciones.dominio.LimitePublicacionesAlcanzadoException;
import rica_api.publicaciones.dominio.LimitePublicacionesAnualesService;
import rica_api.publicaciones.dominio.Publicacion;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicacionServiceTest {

    private static final String CORREO_ANA = "ana.torres@uptc.edu.co";

    @Mock
    private RepositorioPublicaciones repositorioPublicaciones;

    @Mock
    private ConsultaInvestigadores consultaInvestigadores;

    private PublicacionService publicacionService;

    @BeforeEach
    void prepararServicio() {
        publicacionService = new PublicacionService(
                repositorioPublicaciones,
                consultaInvestigadores,
                new LimitePublicacionesAnualesService(5));
    }

    @Test
    void registrarDevuelveLaPublicacionCuandoElInvestigadorExisteYPuedeRegistrar() {
        Publicacion nueva = crearPublicacion(2025, "articulo", Map.of(
                "revista", "Revista de prueba",
                "doi", "10.1234/prueba.001"));
        when(consultaInvestigadores.existePorCorreo(CORREO_ANA)).thenReturn(true);
        when(repositorioPublicaciones.listarPorInvestigador(CORREO_ANA)).thenReturn(List.of());
        when(repositorioPublicaciones.guardar(nueva)).thenReturn(nueva);

        Publicacion resultado = publicacionService.registrar(nueva);

        assertThat(resultado).isSameAs(nueva);
        verify(consultaInvestigadores).existePorCorreo(CORREO_ANA);
        verify(repositorioPublicaciones).guardar(nueva);
    }

    @Test
    void registrarLanzaExcepcionCuandoElInvestigadorNoExiste() {
        Publicacion nueva = crearPublicacion(2025, "libro", Map.of(
                "editorial", "Editorial de prueba",
                "isbn", "978-958-000-0001"));
        when(consultaInvestigadores.existePorCorreo(CORREO_ANA)).thenReturn(false);

        assertThatThrownBy(() -> publicacionService.registrar(nueva))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining(CORREO_ANA);

        verify(repositorioPublicaciones, never()).guardar(any(Publicacion.class));
    }

    @Test
    void registrarRechazaCuandoElInvestigadorAlcanzoElLimiteAnual() {
        Publicacion nueva = crearPublicacion(2025, "articulo", Map.of("doi", "10.1234/prueba.006"));
        when(consultaInvestigadores.existePorCorreo(CORREO_ANA)).thenReturn(true);
        when(repositorioPublicaciones.listarPorInvestigador(CORREO_ANA)).thenReturn(List.of(
                crearPublicacion(2025, "articulo", Map.of("doi", "10.1234/prueba.001")),
                crearPublicacion(2025, "articulo", Map.of("doi", "10.1234/prueba.002")),
                crearPublicacion(2025, "articulo", Map.of("doi", "10.1234/prueba.003")),
                crearPublicacion(2025, "articulo", Map.of("doi", "10.1234/prueba.004")),
                crearPublicacion(2025, "articulo", Map.of("doi", "10.1234/prueba.005"))));

        assertThatThrownBy(() -> publicacionService.registrar(nueva))
                .isInstanceOf(LimitePublicacionesAlcanzadoException.class)
                .hasMessageContaining("2025");

        verify(repositorioPublicaciones, never()).guardar(any(Publicacion.class));
    }

    @Test
    void listarPorInvestigadorDevuelveSusPublicaciones() {
        Publicacion articulo = crearPublicacion(2024, "articulo", Map.of("doi", "10.1234/prueba.001"));
        Publicacion libro = crearPublicacion(2025, "libro", Map.of("isbn", "978-958-000-0001"));
        when(repositorioPublicaciones.listarPorInvestigador(CORREO_ANA))
                .thenReturn(List.of(articulo, libro));

        List<Publicacion> resultado = publicacionService.listarPorInvestigador(CORREO_ANA);

        assertThat(resultado).containsExactly(articulo, libro);
        verify(repositorioPublicaciones).listarPorInvestigador(CORREO_ANA);
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(repositorioPublicaciones.buscarPorId("publicacion-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicacionService.buscarPorId("publicacion-inexistente"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("publicacion-inexistente");
    }

    private Publicacion crearPublicacion(Integer anio, String tipo, Map<String, String> detalles) {
        Publicacion publicacion = new Publicacion();
        publicacion.setInvestigadorCorreo(CORREO_ANA);
        publicacion.setTitulo("Publicación de prueba");
        publicacion.setTipo(tipo);
        publicacion.setAnio(anio);
        publicacion.setDetalles(detalles);
        return publicacion;
    }

}
