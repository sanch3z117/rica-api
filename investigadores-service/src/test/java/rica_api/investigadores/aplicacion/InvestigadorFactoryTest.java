package rica_api.investigadores.aplicacion;

import org.junit.jupiter.api.Test;

import rica_api.investigadores.dominio.Investigador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvestigadorFactoryTest {

    private final InvestigadorFactory investigadorFactory = new InvestigadorFactory();

    @Test
    void construyeUnInvestigadorCompleto() {
        Investigador investigador = investigadorFactory.registrar(
                "Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");

        assertThat(investigador.getId()).isNull();
        assertThat(investigador.getNombreCompleto()).isEqualTo("Ana Torres");
        assertThat(investigador.getCorreoInstitucional().valor()).isEqualTo("ana.torres@uptc.edu.co");
        assertThat(investigador.getGrupoInvestigacion()).isEqualTo("GIT-UPTC");
    }

    @Test
    void rechazaUnNombreVacio() {
        assertThatThrownBy(() -> investigadorFactory.registrar(
                "  ", "ana.torres@uptc.edu.co", "GIT-UPTC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    void rechazaUnGrupoVacio() {
        assertThatThrownBy(() -> investigadorFactory.registrar(
                "Ana Torres", "ana.torres@uptc.edu.co", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("grupo");
    }

    @Test
    void noEntregaUnInvestigadorAMediasSiElCorreoEsInvalido() {
        assertThatThrownBy(() -> investigadorFactory.registrar(
                "Ana Torres", "ana.torres@universidad.edu", "GIT-UPTC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@uptc.edu.co");
    }

}
