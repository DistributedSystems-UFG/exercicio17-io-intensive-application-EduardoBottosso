package br.ufg.marmitaria.estoques.servidor;

import br.ufg.marmitaria.estoques.repositorio.EstoqueArquivoRepository;
import br.ufg.marmitaria.estoques.servico.SistemaDeEstoquesService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ServidorGrpc {
    private final int porta;
    private final String modeloConcorrencia;
    private final ExecutorService executorAplicacao;
    private final Server server;

    public ServidorGrpc(
            int porta,
            String modeloConcorrencia,
            ExecutorService executorAplicacao,
            Path diretorioDados) throws IOException {
        this.porta = porta;
        this.modeloConcorrencia = modeloConcorrencia;
        this.executorAplicacao = executorAplicacao;

        EstoqueArquivoRepository repository = new EstoqueArquivoRepository(diretorioDados);
        SistemaDeEstoquesService service =
                new SistemaDeEstoquesService(executorAplicacao, repository);

        // O callback do gRPC apenas encaminha a tarefa ao executor da versão escolhida.
        this.server = ServerBuilder.forPort(porta)
                .directExecutor()
                .addService(service)
                .build();
    }

    public void iniciarEBloquear() throws IOException, InterruptedException {
        server.start();

        System.out.println("====================================================");
        System.out.println("Sistema de Estoques gRPC iniciado");
        System.out.println("Modelo: " + modeloConcorrencia);
        System.out.println("Porta: " + porta);
        System.out.println("====================================================");

        Runtime.getRuntime().addShutdownHook(new Thread(
                this::encerrar,
                "shutdown-servidor-estoques"));

        server.awaitTermination();
    }

    public void encerrar() {
        System.out.println("Encerrando servidor " + modeloConcorrencia + "...");
        server.shutdown();

        try {
            if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
                server.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            server.shutdownNow();
        }

        executorAplicacao.shutdown();
        try {
            if (!executorAplicacao.awaitTermination(5, TimeUnit.SECONDS)) {
                executorAplicacao.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorAplicacao.shutdownNow();
        }
    }
}
