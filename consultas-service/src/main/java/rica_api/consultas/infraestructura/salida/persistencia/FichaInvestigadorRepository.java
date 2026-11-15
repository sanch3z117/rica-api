package rica_api.consultas.infraestructura.salida.persistencia;

import org.springframework.data.mongodb.repository.MongoRepository;

import rica_api.consultas.modelo.FichaInvestigador;

public interface FichaInvestigadorRepository extends MongoRepository<FichaInvestigador, String> {

}
