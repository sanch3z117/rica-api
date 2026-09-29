package rica_api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.investigadores.InvestigadorRepository;
import rica_api.publicaciones.LimitePublicacionesAlcanzadoException;
import rica_api.publicaciones.LimitePublicacionesAnualesService;
import rica_api.publicaciones.Publicacion;
import rica_api.publicaciones.PublicacionRepository;
import rica_api.publicaciones.PublicacionService;

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
    private PublicacionRepository publicacionRepository;

    @Mock
    private InvestigadorRepository investigadorRepository;

    private PublicacionService publicacionService;

    @BeforeEach
    void prepararServicio() {
        publicacionService = new PublicacionService(
                publicacionRepository,
                investigadorRepository,
                new LimitePublicacionesAnualesService(5));
    }

    @Test
    void registrarDevuelveLaPublicacionCuandoElInvestigadorExisteYPuedeRegistrar() {
        Publicacion nueva = crearPublicacion(2025, "articulo", Map.of(
                "revista", "Revista de prueba",
                "doi", "10.1234/prueba.001"));
        when(investigadorRepository.existsByCorreoInstitucionalValor(CORREO_ANA)).thenReturn(true);
        when(publicacionRepository.findByInvestigadorCorreo(CORREO_ANA)).thenReturn(List.of());
        when(publicacionRepository.save(nueva)).thenReturn(nueva);

        Publicacion resultado = publicacionService.registrar(nueva);

        assertThat(resultado).isSameAs(nueva);
        verify(investigadorRepository).existsByCorreoInstitucionalValor(CORREO_ANA);
        verify(publicacionRepository).save(nueva);
    }

    @Test
    void registrarLanzaExcepcionCuandoElInvestigadorNoExiste() {
        Publicacion nueva = crearPublicacion(2025, "libro", Map.of(
                "editorial", "Editorial de prueba",
                "isbn", "978-958-000-0001"));
        when(investigadorRepository.existsByCorreoInstitucionalValor(CORREO_ANA)).thenReturn(false);

        assertThatThrownBy(() -> publicacionService.registrar(nueva))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining(CORREO_ANA);

        verify(publicacionRepository, never()).save(any(Publicacion.class));
    }

    @Test
    void registrarRechazaCuandoElInvestigadorAlcanzoElLimiteAnual() {
        Publicacion nueva = crearPublicacion(2025, "articulo", Map.of("doi", "10.1234/prueba.005"));
        when(investigadorRepository.existsByCorreoInstitucionalValor(CORREO_ANA)).thenReturn(true);
        when(publicacionRepository.findByInvestigadorCorreo(CORREO_ANA)).thenReturn(List.of(
                crearPublicacion(2025, "articulo", Map.of("doi", "10.1234/prueba.001")),
                crearPublicacion(2025, "articulo", Map.of("doi", "10.1234/prueba.002")),
                crearPublicacion(2025, "articulo", Map.of("doi", "10.1234/prueba.003")),
                crearPublicacion(2025, "articulo", Map.of("doi", "10.1234/prueba.004")),
                crearPublicacion(2025, "articulo", Map.of("doi", "10.1234/prueba.005"))));

        assertThatThrownBy(() -> publicacionService.registrar(nueva))
                .isInstanceOf(LimitePublicacionesAlcanzadoException.class)
                .hasMessageContaining("2025");

        verify(publicacionRepository, never()).save(any(Publicacion.class));
    }

    @Test
    void listarPorInvestigadorDevuelveSusPublicaciones() {
        Publicacion articulo = crearPublicacion(2024, "articulo", Map.of("doi", "10.1234/prueba.001"));
        Publicacion libro = crearPublicacion(2025, "libro", Map.of("isbn", "978-958-000-0001"));
        when(publicacionRepository.findByInvestigadorCorreo(CORREO_ANA))
                .thenReturn(List.of(articulo, libro));

        List<Publicacion> resultado = publicacionService.listarPorInvestigador(CORREO_ANA);

        assertThat(resultado).containsExactly(articulo, libro);
        verify(publicacionRepository).findByInvestigadorCorreo(CORREO_ANA);
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(publicacionRepository.findById("publicacion-inexistente")).thenReturn(Optional.empty());

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
