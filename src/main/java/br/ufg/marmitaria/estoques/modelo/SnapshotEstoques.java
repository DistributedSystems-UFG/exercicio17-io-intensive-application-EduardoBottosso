package br.ufg.marmitaria.estoques.modelo;

public class SnapshotEstoques {
    private final EstoqueMarmitas marmitas;
    private final EstoqueInsumos insumos;

    public SnapshotEstoques(EstoqueMarmitas marmitas, EstoqueInsumos insumos) {
        this.marmitas = marmitas;
        this.insumos = insumos;
    }

    public EstoqueMarmitas getMarmitas() {
        return marmitas;
    }

    public EstoqueInsumos getInsumos() {
        return insumos;
    }
}
