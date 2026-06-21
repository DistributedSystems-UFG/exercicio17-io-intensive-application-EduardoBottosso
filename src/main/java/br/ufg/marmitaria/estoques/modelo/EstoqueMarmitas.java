package br.ufg.marmitaria.estoques.modelo;

public class EstoqueMarmitas {
    private int quantidadeP;
    private int quantidadeM;
    private int quantidadeG;

    public EstoqueMarmitas() {
    }

    public EstoqueMarmitas(int quantidadeP, int quantidadeM, int quantidadeG) {
        this.quantidadeP = quantidadeP;
        this.quantidadeM = quantidadeM;
        this.quantidadeG = quantidadeG;
    }

    public int getQuantidadeP() {
        return quantidadeP;
    }

    public void setQuantidadeP(int quantidadeP) {
        this.quantidadeP = quantidadeP;
    }

    public int getQuantidadeM() {
        return quantidadeM;
    }

    public void setQuantidadeM(int quantidadeM) {
        this.quantidadeM = quantidadeM;
    }

    public int getQuantidadeG() {
        return quantidadeG;
    }

    public void setQuantidadeG(int quantidadeG) {
        this.quantidadeG = quantidadeG;
    }

    public int total() {
        return Math.addExact(Math.addExact(quantidadeP, quantidadeM), quantidadeG);
    }
}
