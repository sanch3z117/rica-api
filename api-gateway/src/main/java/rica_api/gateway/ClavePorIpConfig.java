package rica_api.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class ClavePorIpConfig {

    @Bean
    public KeyResolver clavePorIp() {
        return intercambio -> {
            var remota = intercambio.getRequest().getRemoteAddress();
            if (remota == null || remota.getAddress() == null) {
                return Mono.just("desconocida");
            }
            return Mono.just(remota.getAddress().getHostAddress());
        };
    }

}
