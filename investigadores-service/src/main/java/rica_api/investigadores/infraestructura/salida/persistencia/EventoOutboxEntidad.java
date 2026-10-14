package rica_api.investigadores.infraestructura.salida.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
public class EventoOutboxEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false, unique = true)
    private UUID eventoId;

    @Column(name = "tipo", nullable = false, length = 100)
    private String tipo;

    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "ocurrido_en", nullable = false)
    private Instant ocurridoEn;

    @Column(name = "publicado", nullable = false)
    private boolean publicado;

    protected EventoOutboxEntidad() {
    }

    public EventoOutboxEntidad(UUID eventoId, String tipo, String payload, Instant ocurridoEn) {
        this.eventoId = eventoId;
        this.tipo = tipo;
        this.payload = payload;
        this.ocurridoEn = ocurridoEn;
        this.publicado = false;
    }

    public Long getId() {
        return id;
    }

    public UUID getEventoId() {
        return eventoId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getOcurridoEn() {
        return ocurridoEn;
    }

    public boolean isPublicado() {
        return publicado;
    }

    public void marcarPublicado() {
        this.publicado = true;
    }

}
