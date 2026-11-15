package rica_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.grpc.client.ImportGrpcClients;

import rica_api.grpc.InvestigadorGrpcGrpc.InvestigadorGrpcBlockingStub;

@SpringBootApplication
@ImportGrpcClients(target = "investigadores", types = InvestigadorGrpcBlockingStub.class)
public class PublicacionesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PublicacionesServiceApplication.class, args);
    }

}
