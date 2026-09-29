package rica_api.investigadores;

import org.springframework.stereotype.Component;

@Component
public class InvestigadorFactory {

    public Investigador registrar(String nombreCompleto, String correoInstitucional, String grupoInvestigacion) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }
        if (grupoInvestigacion == null || grupoInvestigacion.isBlank()) {
            throw new IllegalArgumentException("El grupo de investigación es obligatorio");
        }
        return new Investigador(
                null,
                nombreCompleto,
                new CorreoInstitucional(correoInstitucional),
                grupoInvestigacion);
    }

}
