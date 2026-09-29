package rica_api.publicaciones.aplicacion;

import rica_api.publicaciones.dominio.Publicacion;

import java.util.List;
import java.util.Optional;

public interface RepositorioPublicaciones {

    List<Publicacion> listarPorInvestigador(String investigadorCorreo);

    Optional<Publicacion> buscarPorId(String id);

    Publicacion guardar(Publicacion publicacion);

}
