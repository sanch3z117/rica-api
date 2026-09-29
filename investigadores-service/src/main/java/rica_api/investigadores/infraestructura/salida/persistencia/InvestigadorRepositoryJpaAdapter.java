package rica_api.investigadores.infraestructura.salida.persistencia;

import org.springframework.stereotype.Component;

import rica_api.investigadores.aplicacion.RepositorioInvestigadores;
import rica_api.investigadores.dominio.Investigador;

import java.util.List;
import java.util.Optional;

@Component
public class InvestigadorRepositoryJpaAdapter implements RepositorioInvestigadores {

    private final InvestigadorJpaRepository investigadorJpaRepository;

    public InvestigadorRepositoryJpaAdapter(InvestigadorJpaRepository investigadorJpaRepository) {
        this.investigadorJpaRepository = investigadorJpaRepository;
    }

    @Override
    public List<Investigador> listarTodos() {
        return investigadorJpaRepository.findAll();
    }

    @Override
    public Optional<Investigador> buscarPorId(Long id) {
        return investigadorJpaRepository.findById(id);
    }

    @Override
    public boolean existePorCorreo(String correoInstitucional) {
        return investigadorJpaRepository.existsByCorreoInstitucionalValor(correoInstitucional);
    }

    @Override
    public Investigador guardar(Investigador investigador) {
        return investigadorJpaRepository.save(investigador);
    }

}
