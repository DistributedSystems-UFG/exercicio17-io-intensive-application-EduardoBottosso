package br.ufg.marmitaria.estoques.servidor;

import br.ufg.marmitaria.estoques.concorrencia.FabricaThreadsNomeadas;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorSingleThread {
    private static final int PORTA_PADRAO = 50051;

    public static void main(String[] args) throws Exception {
        int porta = args.length >= 1 ? Integer.parseInt(args[0]) : PORTA_PADRAO;
        Path diretorioDados = args.length >= 2 ? Path.of(args[1]) : Path.of("dados");

        ExecutorService executor = Executors.newSingleThreadExecutor(
                new FabricaThreadsNomeadas("single-worker-"));

        new ServidorGrpc(
                porta,
                "single-threaded",
                executor,
                diretorioDados)
                .iniciarEBloquear();
    }
}
