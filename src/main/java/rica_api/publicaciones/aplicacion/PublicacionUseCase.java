package rica_api.publicaciones.aplicacion;

import rica_api.publicaciones.dominio.Publicacion;

import java.util.List;

public interface PublicacionUseCase {

    Publicacion registrar(Publicacion publicacion);

    List<Publicacion> listarPorInvestigador(String investigadorCorreo);

    Publicacion buscarPorId(String id);

}
