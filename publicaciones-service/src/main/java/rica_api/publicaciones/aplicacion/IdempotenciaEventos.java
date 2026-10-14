package rica_api.publicaciones.aplicacion;

public interface IdempotenciaEventos {

    boolean reclamar(String eventoId);

}
