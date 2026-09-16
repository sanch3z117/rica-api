package rica_api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicacionServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private InvestigadorRepository investigadorRepository;

    @InjectMocks
    private PublicacionService publicacionService;

    @Test
    void registrarDevuelveLaPublicacionCuandoElInvestigadorExiste() {
        Publicacion nueva = crearPublicacion("articulo", Map.of(
                "revista", "Revista de prueba",
                "doi", "10.1234/prueba.001"));
        when(investigadorRepository.existsByCorreoInstitucional("ana.torres@uptc.edu.co"))
                .thenReturn(true);
        when(publicacionRepository.save(nueva)).thenReturn(nueva);

        Publicacion resultado = publicacionService.registrar(nueva);

        assertThat(resultado).isSameAs(nueva);
        verify(investigadorRepository).existsByCorreoInstitucional("ana.torres@uptc.edu.co");
        verify(publicacionRepository).save(nueva);
    }

    @Test
    void registrarLanzaExcepcionCuandoElInvestigadorNoExiste() {
        Publicacion nueva = crearPublicacion("libro", Map.of(
                "editorial", "Editorial de prueba",
                "isbn", "978-958-000-0001"));
        when(investigadorRepository.existsByCorreoInstitucional("ana.torres@uptc.edu.co"))
                .thenReturn(false);

        assertThatThrownBy(() -> publicacionService.registrar(nueva))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("ana.torres@uptc.edu.co");

        verify(investigadorRepository).existsByCorreoInstitucional("ana.torres@uptc.edu.co");
    }

    @Test
    void listarPorInvestigadorDevuelveSusPublicaciones() {
        Publicacion articulo = crearPublicacion("articulo", Map.of("doi", "10.1234/prueba.001"));
        Publicacion libro = crearPublicacion("libro", Map.of("isbn", "978-958-000-0001"));
        when(publicacionRepository.findByInvestigadorCorreo("ana.torres@uptc.edu.co"))
                .thenReturn(List.of(articulo, libro));

        List<Publicacion> resultado = publicacionService.listarPorInvestigador("ana.torres@uptc.edu.co");

        assertThat(resultado).containsExactly(articulo, libro);
        verify(publicacionRepository).findByInvestigadorCorreo("ana.torres@uptc.edu.co");
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(publicacionRepository.findById("publicacion-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicacionService.buscarPorId("publicacion-inexistente"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("publicacion-inexistente");
    }

    private Publicacion crearPublicacion(String tipo, Map<String, String> detalles) {
        Publicacion publicacion = new Publicacion();
        publicacion.setInvestigadorCorreo("ana.torres@uptc.edu.co");
        publicacion.setTitulo("Publicación de prueba");
        publicacion.setTipo(tipo);
        publicacion.setAnio(2025);
        publicacion.setDetalles(detalles);
        return publicacion;
    }
}