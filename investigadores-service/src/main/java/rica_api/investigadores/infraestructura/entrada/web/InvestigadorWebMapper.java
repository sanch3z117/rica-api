package rica_api.investigadores.infraestructura.entrada.web;

import rica_api.investigadores.dominio.Investigador;

public class InvestigadorWebMapper {

    private InvestigadorWebMapper() {
    }

    public static InvestigadorResponse aResponse(Investigador investigador) {
        return new InvestigadorResponse(
                investigador.getId(),
                investigador.getNombreCompleto(),
                investigador.getGrupoInvestigacion()
        );
    }

}
