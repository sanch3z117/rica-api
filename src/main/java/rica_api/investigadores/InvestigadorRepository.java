package rica_api.investigadores;

import rica_api.investigadores.dominio.Investigador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigadorRepository extends JpaRepository<Investigador, Long> {

    boolean existsByCorreoInstitucionalValor(String valor);

}
