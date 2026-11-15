package rica_api.investigadores.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorreoInstitucionalTest {

    @Test
    void aceptaUnCorreoDelDominioInstitucional() {
        CorreoInstitucional correo = new CorreoInstitucional("ana.torres@uptc.edu.co");

        assertThat(correo.valor()).isEqualTo("ana.torres@uptc.edu.co");
    }

    @Test
    void dosCorreosConElMismoValorSonIguales() {
        CorreoInstitucional uno = new CorreoInstitucional("ana.torres@uptc.edu.co");
        CorreoInstitucional otro = new CorreoInstitucional("ana.torres@uptc.edu.co");

        assertThat(uno).isEqualTo(otro);
        assertThat(uno).hasSameHashCodeAs(otro);
    }

    @Test
    void rechazaUnCorreoDeOtroDominio() {
        assertThatThrownBy(() -> new CorreoInstitucional("ana.torres@universidad.edu"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@uptc.edu.co");
    }

    @Test
    void rechazaUnCorreoNulo() {
        assertThatThrownBy(() -> new CorreoInstitucional(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@uptc.edu.co");
    }

    @Test
    void rechazaUnTextoQueNoEsCorreo() {
        assertThatThrownBy(() -> new CorreoInstitucional("ana.torres"))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
