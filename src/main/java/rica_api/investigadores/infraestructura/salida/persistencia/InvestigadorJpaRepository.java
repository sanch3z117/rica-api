package rica_api.investigadores.infraestructura.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import rica_api.investigadores.dominio.Investigador;

public interface InvestigadorJpaRepository extends JpaRepository<Investigador, Long> {

    boolean existsByCorreoInstitucionalValor(String valor);

}
