package br.ufg.marmitaria.estoques.servidor;

import br.ufg.marmitaria.estoques.concorrencia.FabricaThreadsNomeadas;
import br.ufg.marmitaria.estoques.concorrencia.ThreadPorRequisicaoExecutor;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

public class ServidorThreadPorRequisicao {
    private static final int PORTA_PADRAO = 50052;

    public static void main(String[] args) throws Exception {
        int porta = args.length >= 1 ? Integer.parseInt(args[0]) : PORTA_PADRAO;
        Path diretorioDados = args.length >= 2 ? Path.of(args[1]) : Path.of("dados");

        ExecutorService executor = new ThreadPorRequisicaoExecutor(
                new FabricaThreadsNomeadas("requisicao-worker-"));

        new ServidorGrpc(
                porta,
                "multi-threaded: uma nova thread por requisição",
                executor,
                diretorioDados)
                .iniciarEBloquear();
    }
}
