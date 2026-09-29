package rica_api;

import org.junit.jupiter.api.Test;

import rica_api.publicaciones.LimitePublicacionesAlcanzadoException;
import rica_api.publicaciones.LimitePublicacionesAnualesService;
import rica_api.publicaciones.Publicacion;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LimitePublicacionesAnualesServiceTest {

    private final LimitePublicacionesAnualesService servicio = new LimitePublicacionesAnualesService(2);

    @Test
    void permiteRegistrarCuandoEstaPorDebajoDelLimite() {
        assertThatCode(() -> servicio.validarRegistro(List.of(publicacion(2025)), 2025))
                .doesNotThrowAnyException();
    }

    @Test
    void rechazaCuandoYaSeAlcanzoElLimite() {
        assertThatThrownBy(() -> servicio.validarRegistro(List.of(publicacion(2025), publicacion(2025)), 2025))
                .isInstanceOf(LimitePublicacionesAlcanzadoException.class)
                .hasMessageContaining("2025");
    }

    @Test
    void soloCuentaLasPublicacionesDelAnioQueSeEstaRegistrando() {
        assertThatCode(() -> servicio.validarRegistro(
                List.of(publicacion(2023), publicacion(2024), publicacion(2024)), 2025))
                .doesNotThrowAnyException();
    }

    @Test
    void ignoraLasPublicacionesSinAnio() {
        assertThatCode(() -> servicio.validarRegistro(
                List.of(publicacion(null), publicacion(null), publicacion(null)), 2025))
                .doesNotThrowAnyException();
    }

    private Publicacion publicacion(Integer anio) {
        Publicacion publicacion = new Publicacion();
        publicacion.setInvestigadorCorreo("ana.torres@uptc.edu.co");
        publicacion.setTitulo("Publicación de prueba");
        publicacion.setAnio(anio);
        return publicacion;
    }

}
