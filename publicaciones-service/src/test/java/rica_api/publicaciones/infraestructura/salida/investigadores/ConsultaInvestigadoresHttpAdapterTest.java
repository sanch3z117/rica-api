package rica_api.publicaciones.infraestructura.salida.investigadores;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import rica_api.publicaciones.aplicacion.ConsultaInvestigadoresNoDisponibleException;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ConsultaInvestigadoresHttpAdapterTest {

    private static final String URL_EXISTE =
            "http://localhost:8081/api/investigadores/existe?correo=ana.torres@uptc.edu.co";

    @Test
    void devuelveVerdaderoCuandoElServicioConfirmaLaExistencia() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        ConsultaInvestigadoresHttpAdapter adaptador = crearAdaptador(builder, 1);
        servidor.expect(requestTo(URL_EXISTE))
                .andRespond(withSuccess(
                        "{\"correo\":\"ana.torres@uptc.edu.co\",\"existe\":true}",
                        MediaType.APPLICATION_JSON));

        assertThat(adaptador.existePorCorreo("ana.torres@uptc.edu.co")).isTrue();
        servidor.verify();
    }

    @Test
    void devuelveFalsoCuandoElServicioDiceQueNoExiste() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        ConsultaInvestigadoresHttpAdapter adaptador = crearAdaptador(builder, 1);
        servidor.expect(requestTo(URL_EXISTE))
                .andRespond(withSuccess(
                        "{\"correo\":\"ana.torres@uptc.edu.co\",\"existe\":false}",
                        MediaType.APPLICATION_JSON));

        assertThat(adaptador.existePorCorreo("ana.torres@uptc.edu.co")).isFalse();
    }

    @Test
    void traduceUnFalloDelServicioEnUnaExcepcionDeDependencia() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        ConsultaInvestigadoresHttpAdapter adaptador = crearAdaptador(builder, 1);
        servidor.expect(requestTo(URL_EXISTE)).andRespond(withServerError());

        assertThatThrownBy(() -> adaptador.existePorCorreo("ana.torres@uptc.edu.co"))
                .isInstanceOf(ConsultaInvestigadoresNoDisponibleException.class);
        servidor.verify();
    }

    @Test
    void reintentaAntesDeDarLaLlamadaPorFallida() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        ConsultaInvestigadoresHttpAdapter adaptador = crearAdaptador(builder, 2);
        servidor.expect(requestTo(URL_EXISTE)).andRespond(withServerError());
        servidor.expect(requestTo(URL_EXISTE))
                .andRespond(withSuccess(
                        "{\"correo\":\"ana.torres@uptc.edu.co\",\"existe\":true}",
                        MediaType.APPLICATION_JSON));

        assertThat(adaptador.existePorCorreo("ana.torres@uptc.edu.co")).isTrue();
        servidor.verify();
    }

    @Test
    void abreElCircuitoCuandoElServicioFallaRepetidamente() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        ConsultaInvestigadoresHttpAdapter adaptador = crearAdaptadorConCircuito(builder);
        servidor.expect(requestTo(URL_EXISTE)).andRespond(withServerError());
        servidor.expect(requestTo(URL_EXISTE)).andRespond(withServerError());

        assertThatThrownBy(() -> adaptador.existePorCorreo("ana.torres@uptc.edu.co"))
                .isInstanceOf(ConsultaInvestigadoresNoDisponibleException.class);
        assertThatThrownBy(() -> adaptador.existePorCorreo("ana.torres@uptc.edu.co"))
                .isInstanceOf(ConsultaInvestigadoresNoDisponibleException.class);
        assertThatThrownBy(() -> adaptador.existePorCorreo("ana.torres@uptc.edu.co"))
                .isInstanceOf(ConsultaInvestigadoresNoDisponibleException.class)
                .hasMessageContaining("circuito");

        servidor.verify();
    }

    private ConsultaInvestigadoresHttpAdapter crearAdaptador(RestClient.Builder builder, int intentos) {
        RetryRegistry retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(intentos)
                .waitDuration(Duration.ofMillis(1))
                .build());
        return new ConsultaInvestigadoresHttpAdapter(builder, "http://localhost:8081",
                CircuitBreakerRegistry.ofDefaults(), retryRegistry);
    }

    private ConsultaInvestigadoresHttpAdapter crearAdaptadorConCircuito(RestClient.Builder builder) {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .slidingWindowSize(2)
                .minimumNumberOfCalls(2)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .build());
        RetryRegistry retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(1)
                .build());
        return new ConsultaInvestigadoresHttpAdapter(builder, "http://localhost:8081",
                circuitBreakerRegistry, retryRegistry);
    }

}
