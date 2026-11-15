package rica_api.publicaciones.infraestructura.salida.investigadores;

import org.springframework.stereotype.Component;

import rica_api.grpc.InvestigadorGrpcGrpc.InvestigadorGrpcBlockingStub;
import rica_api.grpc.InvestigadorRequest;

@Component
public class VerificadorInvestigadorGrpc {

    private final InvestigadorGrpcBlockingStub stub;

    public VerificadorInvestigadorGrpc(InvestigadorGrpcBlockingStub stub) {
        this.stub = stub;
    }

    public boolean existe(long investigadorId) {
        var request = InvestigadorRequest.newBuilder().setId(investigadorId).build();
        return stub.existeInvestigador(request).getExiste();
    }

}
