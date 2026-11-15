package rica_api.investigadores;

import rica_api.investigadores.dominio.Investigador;

public class InvestigadorMapper {

    private InvestigadorMapper() {
    }

    public static InvestigadorResponse aResponse(Investigador investigador) {
        return new InvestigadorResponse(
                investigador.getId(),
                investigador.getNombreCompleto(),
                investigador.getGrupoInvestigacion()
        );
    }

}
