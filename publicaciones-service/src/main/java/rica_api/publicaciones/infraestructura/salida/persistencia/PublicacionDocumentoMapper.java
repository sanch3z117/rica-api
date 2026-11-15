package rica_api.publicaciones.infraestructura.salida.persistencia;

import rica_api.publicaciones.dominio.Publicacion;

public final class PublicacionDocumentoMapper {

    private PublicacionDocumentoMapper() {
    }

    public static PublicacionDocumento aDocumento(Publicacion publicacion) {
        PublicacionDocumento documento = new PublicacionDocumento();
        documento.setId(publicacion.getId());
        documento.setInvestigadorCorreo(publicacion.getInvestigadorCorreo());
        documento.setTitulo(publicacion.getTitulo());
        documento.setTipo(publicacion.getTipo());
        documento.setAnio(publicacion.getAnio());
        documento.setDetalles(publicacion.getDetalles());
        documento.setEstado(publicacion.getEstado());
        return documento;
    }

    public static Publicacion aDominio(PublicacionDocumento documento) {
        Publicacion publicacion = new Publicacion();
        publicacion.setId(documento.getId());
        publicacion.setInvestigadorCorreo(documento.getInvestigadorCorreo());
        publicacion.setTitulo(documento.getTitulo());
        publicacion.setTipo(documento.getTipo());
        publicacion.setAnio(documento.getAnio());
        publicacion.setDetalles(documento.getDetalles());
        publicacion.setEstado(documento.getEstado());
        return publicacion;
    }

}
