package br.ufg.marmitaria.estoques.servidor;

import br.ufg.marmitaria.estoques.concorrencia.FabricaThreadsNomeadas;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorPoolThreads {
    private static final int PORTA_PADRAO = 50053;
    private static final int TAMANHO_POOL_PADRAO = 4;

    public static void main(String[] args) throws Exception {
        int porta = args.length >= 1 ? Integer.parseInt(args[0]) : PORTA_PADRAO;
        int tamanhoPool = args.length >= 2 ? Integer.parseInt(args[1]) : TAMANHO_POOL_PADRAO;
        Path diretorioDados = args.length >= 3 ? Path.of(args[2]) : Path.of("dados");

        if (tamanhoPool <= 0) {
            throw new IllegalArgumentException("O tamanho do pool deve ser maior que zero.");
        }

        ExecutorService executor = Executors.newFixedThreadPool(
                tamanhoPool,
                new FabricaThreadsNomeadas("pool-worker-"));

        new ServidorGrpc(
                porta,
                "multi-threaded: pool fixo com " + tamanhoPool + " threads",
                executor,
                diretorioDados)
                .iniciarEBloquear();
    }
}
