package rica_api.publicaciones.infraestructura.salida.investigadores;

import org.springframework.stereotype.Component;

import rica_api.investigadores.aplicacion.RepositorioInvestigadores;
import rica_api.publicaciones.aplicacion.ConsultaInvestigadores;

@Component
public class ConsultaInvestigadoresJpaAdapter implements ConsultaInvestigadores {

    private final RepositorioInvestigadores repositorioInvestigadores;

    public ConsultaInvestigadoresJpaAdapter(RepositorioInvestigadores repositorioInvestigadores) {
        this.repositorioInvestigadores = repositorioInvestigadores;
    }

    @Override
    public boolean existePorCorreo(String correoInstitucional) {
        return repositorioInvestigadores.existePorCorreo(correoInstitucional);
    }

}
