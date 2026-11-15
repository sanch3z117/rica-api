package rica_api.consultas.infraestructura.entrada.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rica_api.consultas.infraestructura.salida.persistencia.FichaInvestigadorRepository;
import rica_api.consultas.modelo.FichaInvestigador;

import java.util.List;

@RestController
@RequestMapping("/api/consultas/estadisticas")
public class EstadisticasController {

    private final FichaInvestigadorRepository fichas;

    public EstadisticasController(FichaInvestigadorRepository fichas) {
        this.fichas = fichas;
    }

    @GetMapping
    public EstadisticasResponse consultar() {
        List<FichaInvestigador> todas = fichas.findAll();
        long totalPublicaciones = todas.stream()
                .mapToLong(FichaInvestigador::getTotalPublicaciones)
                .sum();
        long registradas = todas.stream()
                .filter(ficha -> "REGISTRADO".equals(ficha.getEstado()))
                .count();
        return new EstadisticasResponse(todas.size(), totalPublicaciones, registradas);
    }

}
