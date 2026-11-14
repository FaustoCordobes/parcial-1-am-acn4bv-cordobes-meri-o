package com.example.barista_app.data;

public final class MethodImages {

    private MethodImages() {
    }

    public static String urlFor(String methodId) {
        if (methodId == null) {
            return "";
        }

        // Por ahora ACA se cambian las imagenes de las distintas cafeteras que vienen por default.
        // Lo ideal seria en caso de que estas imagenes no cambien seria que esten en local, y en caso de
        // que fueran a cambiar en el tiempo, deberian estarn en firebase noSQL para que desde ese panel
        // poder cambiar la url, y en caso de que se agreguen constantemente mas CARDS/metodos/cafeteras crear
        // un panel donde se pueda modificar eso con un permiso para asignar a una cuenta.

        switch (methodId) {
            case "prensa":
                return "https://images.unsplash.com/photo-1639906512494-dd4a536abc4e";
            case "moka":
                return "https://images.unsplash.com/photo-1659425757191-35d28eff25c9";
            case "filtro":
                return "https://images.unsplash.com/photo-1678275713046-1b92f108c165";
            case "v60":
                return "https://images.unsplash.com/photo-1732870695254-b2718d0796ff";
            case "chemex":
                return "https://images.unsplash.com/photo-1579895146085-8445603b1b01";
            case "expreso":
                return "https://images.unsplash.com/photo-1775050703667-9a42451dbe0b";
            default:
                return "";
        }
    }
}
