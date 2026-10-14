package rica_api.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "eureka.client.enabled=false")
class RutasDelGatewayTest {

    @Value("${local.server.port}")
    private int puerto;

    @Test
    void registraLasRutasDeLosDosServicios() throws Exception {
        HttpResponse<String> respuesta = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/actuator/gateway/routes"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.body())
                .contains("investigadores-route")
                .contains("publicaciones-route")
                .contains("estado-route");
    }

}
