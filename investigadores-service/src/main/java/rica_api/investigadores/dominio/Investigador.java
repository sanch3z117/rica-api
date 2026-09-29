package rica_api.investigadores.dominio;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "investigadores")
public class Investigador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "correo_institucional", nullable = false, unique = true, length = 150))
    private CorreoInstitucional correoInstitucional;

    @Column(name = "grupo_investigacion", length = 150)
    private String grupoInvestigacion;

    protected Investigador() {
        // exigido por Hibernate; la creación del Agregado pasa por InvestigadorFactory
    }

    public Investigador(Long id, String nombreCompleto, CorreoInstitucional correoInstitucional, String grupoInvestigacion) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.correoInstitucional = correoInstitucional;
        this.grupoInvestigacion = grupoInvestigacion;
    }

    public Long getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public CorreoInstitucional getCorreoInstitucional() {
        return correoInstitucional;
    }

    public String getGrupoInvestigacion() {
        return grupoInvestigacion;
    }

}
