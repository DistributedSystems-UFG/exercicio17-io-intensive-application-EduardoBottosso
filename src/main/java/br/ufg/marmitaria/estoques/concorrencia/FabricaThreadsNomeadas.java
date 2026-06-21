package br.ufg.marmitaria.estoques.concorrencia;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class FabricaThreadsNomeadas implements ThreadFactory {
    private final String prefixo;
    private final AtomicInteger contador = new AtomicInteger(1);

    public FabricaThreadsNomeadas(String prefixo) {
        this.prefixo = prefixo;
    }

    @Override
    public Thread newThread(Runnable tarefa) {
        Thread thread = new Thread(tarefa, prefixo + contador.getAndIncrement());
        thread.setDaemon(false);
        return thread;
    }
}
