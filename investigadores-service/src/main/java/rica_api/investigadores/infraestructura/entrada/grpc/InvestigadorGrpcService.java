package rica_api.investigadores.infraestructura.entrada.grpc;

import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import rica_api.grpc.InvestigadorGrpcGrpc;
import rica_api.grpc.InvestigadorRequest;
import rica_api.grpc.InvestigadorResponse;
import rica_api.investigadores.puertos.RepositorioInvestigadores;
import rica_api.investigadores.dominio.Investigador;

@GrpcService
public class InvestigadorGrpcService extends InvestigadorGrpcGrpc.InvestigadorGrpcImplBase {

    private final RepositorioInvestigadores repositorio;

    public InvestigadorGrpcService(RepositorioInvestigadores repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void existeInvestigador(InvestigadorRequest request, StreamObserver<InvestigadorResponse> responseObserver) {
        var encontrado = repositorio.buscarPorId(request.getId());
        var respuesta = InvestigadorResponse.newBuilder()
                .setExiste(encontrado.isPresent())
                .setNombreCompleto(encontrado.map(Investigador::getNombreCompleto).orElse(""))
                .build();
        responseObserver.onNext(respuesta);
        responseObserver.onCompleted();
    }

}
