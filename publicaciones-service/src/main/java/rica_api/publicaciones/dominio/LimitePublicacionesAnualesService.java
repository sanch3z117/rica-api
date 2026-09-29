package rica_api.publicaciones.dominio;

import java.util.List;

public class LimitePublicacionesAnualesService {

    private final int limiteAnual;

    public LimitePublicacionesAnualesService(int limiteAnual) {
        this.limiteAnual = limiteAnual;
    }

    public void validarRegistro(List<Publicacion> publicacionesDelInvestigador, Integer anio) {
        if (anio == null) {
            return;
        }
        long publicacionesEnElAnio = publicacionesDelInvestigador.stream()
                .filter(publicacion -> anio.equals(publicacion.getAnio()))
                .count();
        if (publicacionesEnElAnio >= limiteAnual) {
            throw new LimitePublicacionesAlcanzadoException(
                    "El investigador alcanzó el límite de " + limiteAnual
                            + " publicaciones registradas en el año " + anio);
        }
    }

    public int limiteAnual() {
        return limiteAnual;
    }

}
