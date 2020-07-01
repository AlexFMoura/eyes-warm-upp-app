package br.org.sistemafieg.aquecimentoclientapp.enums;

public enum IdentificacaoDispositivoEnum {

    DP1("Azul", "#0000FF"),
    DP2("Vermelho", "#CD0000"),
    DP3("Verde", "#00CD00"),
    DP4("Rosa", "#EE1289"),
    DP5("Roxo", "#8B008B"),
    DP6("Amarelo", "#FFD700"),
    DP7("Laranja", "#FF8247");

    private final String label;
    private final String color;

    private IdentificacaoDispositivoEnum(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel(){
        return this.label;
    }

    public String getColor(){
        return this.color;
    }

    public static IdentificacaoDispositivoEnum findByName(String name) {
        return IdentificacaoDispositivoEnum.valueOf(name);
    }


}
