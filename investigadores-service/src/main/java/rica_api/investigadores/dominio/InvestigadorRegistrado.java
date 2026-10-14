package rica_api.investigadores.dominio;

import java.time.Instant;
import java.util.UUID;

public record InvestigadorRegistrado(
        UUID eventoId,
        Long investigadorId,
        String correoInstitucional,
        String nombreCompleto,
        Instant ocurridoEn) {
}
