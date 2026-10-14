package rica_api.publicaciones.infraestructura.salida.investigadores;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import rica_api.publicaciones.aplicacion.ConsultaInvestigadores;
import rica_api.publicaciones.aplicacion.ConsultaInvestigadoresNoDisponibleException;

import java.util.function.Supplier;

@Component
public class ConsultaInvestigadoresHttpAdapter implements ConsultaInvestigadores {

    private final RestClient restClient;
    private final CircuitBreaker circuito;
    private final Retry reintentos;

    public ConsultaInvestigadoresHttpAdapter(RestClient.Builder builder,
                                             @Value("${rica.investigadores.base-url}") String baseUrl,
                                             CircuitBreakerRegistry circuitBreakerRegistry,
                                             RetryRegistry retryRegistry) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.circuito = circuitBreakerRegistry.circuitBreaker("investigadores");
        this.reintentos = retryRegistry.retry("investigadores");
    }

    @Override
    public boolean existePorCorreo(String correoInstitucional) {
        Supplier<Boolean> conReintentos = Retry.decorateSupplier(reintentos, () -> consultar(correoInstitucional));
        Supplier<Boolean> conCircuito = CircuitBreaker.decorateSupplier(circuito, conReintentos);
        try {
            return conCircuito.get();
        } catch (CallNotPermittedException excepcion) {
            throw new ConsultaInvestigadoresNoDisponibleException(
                    "El circuito hacia investigadores-service está abierto");
        }
    }

    private boolean consultar(String correoInstitucional) {
        try {
            ExisteInvestigadorResponse respuesta = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/investigadores/existe")
                            .queryParam("correo", correoInstitucional)
                            .build())
                    .retrieve()
                    .body(ExisteInvestigadorResponse.class);
            return respuesta != null && respuesta.existe();
        } catch (RestClientException excepcion) {
            throw new ConsultaInvestigadoresNoDisponibleException(
                    "No fue posible consultar el servicio de investigadores: " + excepcion.getMessage());
        }
    }

}
