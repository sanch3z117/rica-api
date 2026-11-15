package rica_api.consultas.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "fichas")
public class FichaInvestigador {

    @Id
    private String id;

    private String nombreCompleto;
    private String correoInstitucional;
    private String estado;
    private int totalPublicaciones;

    public FichaInvestigador() {
    }

    public FichaInvestigador(String id) {
        this.id = id;
        this.estado = "REGISTRADO";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getTotalPublicaciones() {
        return totalPublicaciones;
    }

    public void setTotalPublicaciones(int totalPublicaciones) {
        this.totalPublicaciones = totalPublicaciones;
    }

}
