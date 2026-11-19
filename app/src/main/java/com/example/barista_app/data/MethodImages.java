package com.example.barista_app.data;

public final class MethodImages {

    private MethodImages() {
    }

    public static String urlFor(String methodId) {
        if (methodId == null) {
            return "";
        }

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
