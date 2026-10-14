package rica_api.publicaciones.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rica_api.compartido.RecursoNoEncontradoException;
import rica_api.publicaciones.dominio.EstadoPublicacion;
import rica_api.publicaciones.dominio.LimitePublicacionesAlcanzadoException;
import rica_api.publicaciones.dominio.LimitePublicacionesAnualesService;
import rica_api.publicaciones.dominio.Publicacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrarPublicacionSagaTest {

    private static final String CORREO_ANA = "ana.torres@uptc.edu.co";

    private final RepositorioPublicaciones repositorioPublicaciones = mock(RepositorioPublicaciones.class);
    private final ConsultaInvestigadores consultaInvestigadores = mock(ConsultaInvestigadores.class);
    private final List<EstadoPublicacion> estadosGuardados = new ArrayList<>();

    private RegistrarPublicacionSaga saga;

    @BeforeEach
    void prepararSaga() {
        saga = new RegistrarPublicacionSaga(repositorioPublicaciones, consultaInvestigadores,
                new LimitePublicacionesAnualesService(5));
        when(repositorioPublicaciones.guardar(any(Publicacion.class))).thenAnswer(invocacion -> {
            Publicacion publicacion = invocacion.getArgument(0);
            estadosGuardados.add(publicacion.getEstado());
            return publicacion;
        });
    }

    @Test
    void confirmaLaPublicacionCuandoElInvestigadorExisteYPuedeRegistrar() {
        Publicacion nueva = crearPublicacion(2025);
        when(consultaInvestigadores.existePorCorreo(CORREO_ANA)).thenReturn(true);
        when(repositorioPublicaciones.listarRegistradasPorInvestigador(CORREO_ANA)).thenReturn(List.of());

        Publicacion resultado = saga.ejecutar(nueva);

        assertThat(resultado.getEstado()).isEqualTo(EstadoPublicacion.REGISTRADA);
        assertThat(estadosGuardados).containsExactly(
                EstadoPublicacion.PENDIENTE, EstadoPublicacion.REGISTRADA);
    }

    @Test
    void compensaMarcandoRechazadaCuandoElInvestigadorNoExiste() {
        Publicacion nueva = crearPublicacion(2025);
        when(consultaInvestigadores.existePorCorreo(CORREO_ANA)).thenReturn(false);

        assertThatThrownBy(() -> saga.ejecutar(nueva))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining(CORREO_ANA);

        assertThat(estadosGuardados).containsExactly(
                EstadoPublicacion.PENDIENTE, EstadoPublicacion.RECHAZADA);
        verify(repositorioPublicaciones, never()).listarRegistradasPorInvestigador(any());
    }

    @Test
    void compensaMarcandoRechazadaCuandoSeAlcanzoElLimiteAnual() {
        Publicacion nueva = crearPublicacion(2025);
        when(consultaInvestigadores.existePorCorreo(CORREO_ANA)).thenReturn(true);
        when(repositorioPublicaciones.listarRegistradasPorInvestigador(CORREO_ANA)).thenReturn(List.of(
                crearPublicacion(2025), crearPublicacion(2025), crearPublicacion(2025),
                crearPublicacion(2025), crearPublicacion(2025)));

        assertThatThrownBy(() -> saga.ejecutar(nueva))
                .isInstanceOf(LimitePublicacionesAlcanzadoException.class)
                .hasMessageContaining("2025");

        assertThat(estadosGuardados).containsExactly(
                EstadoPublicacion.PENDIENTE, EstadoPublicacion.RECHAZADA);
    }

    @Test
    void compensaMarcandoRechazadaCuandoElServicioDeInvestigadoresNoEstaDisponible() {
        Publicacion nueva = crearPublicacion(2025);
        when(consultaInvestigadores.existePorCorreo(CORREO_ANA))
                .thenThrow(new ConsultaInvestigadoresNoDisponibleException("sin conexión"));

        assertThatThrownBy(() -> saga.ejecutar(nueva))
                .isInstanceOf(ConsultaInvestigadoresNoDisponibleException.class);

        assertThat(estadosGuardados).containsExactly(
                EstadoPublicacion.PENDIENTE, EstadoPublicacion.RECHAZADA);
    }

    private Publicacion crearPublicacion(Integer anio) {
        Publicacion publicacion = new Publicacion();
        publicacion.setInvestigadorCorreo(CORREO_ANA);
        publicacion.setTitulo("Publicación de prueba");
        publicacion.setAnio(anio);
        publicacion.setDetalles(Map.of("doi", "10.1234/prueba.001"));
        return publicacion;
    }

}
