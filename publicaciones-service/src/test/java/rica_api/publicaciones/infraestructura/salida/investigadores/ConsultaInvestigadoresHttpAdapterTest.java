package rica_api.publicaciones.infraestructura.salida.investigadores;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

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
        ConsultaInvestigadoresHttpAdapter adaptador =
                new ConsultaInvestigadoresHttpAdapter(builder, "http://localhost:8081");
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
        ConsultaInvestigadoresHttpAdapter adaptador =
                new ConsultaInvestigadoresHttpAdapter(builder, "http://localhost:8081");
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
        ConsultaInvestigadoresHttpAdapter adaptador =
                new ConsultaInvestigadoresHttpAdapter(builder, "http://localhost:8081");
        servidor.expect(requestTo(URL_EXISTE)).andRespond(withServerError());

        assertThatThrownBy(() -> adaptador.existePorCorreo("ana.torres@uptc.edu.co"))
                .isInstanceOf(rica_api.publicaciones.aplicacion.ConsultaInvestigadoresNoDisponibleException.class);
    }

}
