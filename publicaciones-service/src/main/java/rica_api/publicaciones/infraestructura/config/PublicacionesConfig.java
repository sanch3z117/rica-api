package rica_api.publicaciones.infraestructura.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import rica_api.publicaciones.dominio.LimitePublicacionesAnualesService;

@Configuration
public class PublicacionesConfig {

    @Bean
    public LimitePublicacionesAnualesService limitePublicacionesAnualesService(
            @Value("${rica.publicaciones.limite-anual:5}") int limiteAnual) {
        return new LimitePublicacionesAnualesService(limiteAnual);
    }

}
