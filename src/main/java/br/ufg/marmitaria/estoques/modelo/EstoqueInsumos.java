package br.ufg.marmitaria.estoques.modelo;

public class EstoqueInsumos {
    private int porcoesArroz;
    private int porcoesFeijao;
    private int porcoesMacarrao;
    private int porcoesCarne;
    private int porcoesVerduras;
    private int embalagensP;
    private int embalagensM;
    private int embalagensG;

    public EstoqueInsumos() {
    }

    public EstoqueInsumos(
            int porcoesArroz,
            int porcoesFeijao,
            int porcoesMacarrao,
            int porcoesCarne,
            int porcoesVerduras,
            int embalagensP,
            int embalagensM,
            int embalagensG) {
        this.porcoesArroz = porcoesArroz;
        this.porcoesFeijao = porcoesFeijao;
        this.porcoesMacarrao = porcoesMacarrao;
        this.porcoesCarne = porcoesCarne;
        this.porcoesVerduras = porcoesVerduras;
        this.embalagensP = embalagensP;
        this.embalagensM = embalagensM;
        this.embalagensG = embalagensG;
    }

    public int getPorcoesArroz() {
        return porcoesArroz;
    }

    public void setPorcoesArroz(int porcoesArroz) {
        this.porcoesArroz = porcoesArroz;
    }

    public int getPorcoesFeijao() {
        return porcoesFeijao;
    }

    public void setPorcoesFeijao(int porcoesFeijao) {
        this.porcoesFeijao = porcoesFeijao;
    }

    public int getPorcoesMacarrao() {
        return porcoesMacarrao;
    }

    public void setPorcoesMacarrao(int porcoesMacarrao) {
        this.porcoesMacarrao = porcoesMacarrao;
    }

    public int getPorcoesCarne() {
        return porcoesCarne;
    }

    public void setPorcoesCarne(int porcoesCarne) {
        this.porcoesCarne = porcoesCarne;
    }

    public int getPorcoesVerduras() {
        return porcoesVerduras;
    }

    public void setPorcoesVerduras(int porcoesVerduras) {
        this.porcoesVerduras = porcoesVerduras;
    }

    public int getEmbalagensP() {
        return embalagensP;
    }

    public void setEmbalagensP(int embalagensP) {
        this.embalagensP = embalagensP;
    }

    public int getEmbalagensM() {
        return embalagensM;
    }

    public void setEmbalagensM(int embalagensM) {
        this.embalagensM = embalagensM;
    }

    public int getEmbalagensG() {
        return embalagensG;
    }

    public void setEmbalagensG(int embalagensG) {
        this.embalagensG = embalagensG;
    }

    public int total() {
        int totalAlimentos = Math.addExact(
                Math.addExact(Math.addExact(porcoesArroz, porcoesFeijao), porcoesMacarrao),
                Math.addExact(porcoesCarne, porcoesVerduras));
        int totalEmbalagens = Math.addExact(Math.addExact(embalagensP, embalagensM), embalagensG);
        return Math.addExact(totalAlimentos, totalEmbalagens);
    }
}
