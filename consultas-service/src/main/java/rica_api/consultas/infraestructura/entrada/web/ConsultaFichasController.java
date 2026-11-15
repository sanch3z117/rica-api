package rica_api.consultas.infraestructura.entrada.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import rica_api.consultas.infraestructura.salida.persistencia.FichaInvestigadorRepository;
import rica_api.consultas.modelo.FichaInvestigador;

import java.util.List;

@RestController
@RequestMapping("/api/consultas/fichas")
public class ConsultaFichasController {

    private final FichaInvestigadorRepository fichas;

    public ConsultaFichasController(FichaInvestigadorRepository fichas) {
        this.fichas = fichas;
    }

    @GetMapping
    public List<FichaInvestigador> listar() {
        return fichas.findAll();
    }

    @GetMapping("/{id}")
    public FichaInvestigador buscar(@PathVariable String id) {
        return fichas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe una ficha para el investigador " + id));
    }

}
