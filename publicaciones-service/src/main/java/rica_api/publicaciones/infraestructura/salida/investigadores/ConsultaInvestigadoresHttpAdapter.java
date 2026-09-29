package rica_api.publicaciones.infraestructura.salida.investigadores;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import rica_api.publicaciones.aplicacion.ConsultaInvestigadores;
import rica_api.publicaciones.aplicacion.ConsultaInvestigadoresNoDisponibleException;

@Component
public class ConsultaInvestigadoresHttpAdapter implements ConsultaInvestigadores {

    private final RestClient restClient;

    public ConsultaInvestigadoresHttpAdapter(RestClient.Builder builder,
                                             @Value("${rica.investigadores.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public boolean existePorCorreo(String correoInstitucional) {
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
