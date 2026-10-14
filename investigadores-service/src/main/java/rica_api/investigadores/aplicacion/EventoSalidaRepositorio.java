package rica_api.investigadores.aplicacion;

import rica_api.investigadores.dominio.InvestigadorRegistrado;

import java.util.List;

public interface EventoSalidaRepositorio {

    void registrar(InvestigadorRegistrado evento);

    List<EventoSalidaPendiente> buscarPendientes();

    void marcarPublicado(long id);

    record EventoSalidaPendiente(long id, String tipo, String payload) {
    }

}
