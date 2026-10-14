package rica_api.investigadores.infraestructura.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoOutboxJpaRepository extends JpaRepository<EventoOutboxEntidad, Long> {

    List<EventoOutboxEntidad> findByPublicadoFalseOrderByIdAsc();

}
