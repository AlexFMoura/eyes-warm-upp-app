package br.org.sistemafieg.aquecimentoapp.enums;

public enum IdentificacaoDispositivoEnum {

    DP1("Azul"),
    DP2("Vermelho"),
    DP3("Verde"),
    DP4("Rosa"),
    DP5("Roxo"),
    DP6("Amarelo"),
    DP7("Laranja");

    private String label;

    private IdentificacaoDispositivoEnum(String label) {
        this.label = label;
    }

    public String getLabel(){
        return this.label;
    }

    public static IdentificacaoDispositivoEnum findByName(String name) {
        return IdentificacaoDispositivoEnum.valueOf(name);
    }

    public static IdentificacaoDispositivoEnum findByLabel(String label) {
        for(IdentificacaoDispositivoEnum item : IdentificacaoDispositivoEnum.values()) {
            if(item.getLabel().equals(label)) {
                return item;
            }
        }

        return null;
    }

}
