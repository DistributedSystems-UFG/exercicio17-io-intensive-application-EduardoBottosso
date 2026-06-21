package br.ufg.marmitaria.estoques.concorrencia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Executor didático que cria uma nova thread de plataforma para cada tarefa.
 * Diferentemente de um cached thread pool, as threads não são reutilizadas.
 */
public class ThreadPorRequisicaoExecutor extends AbstractExecutorService {
    private final Object monitor = new Object();
    private final Set<Thread> threadsAtivas = new java.util.HashSet<>();
    private final ThreadFactory threadFactory;
    private boolean encerrado;

    public ThreadPorRequisicaoExecutor(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public void execute(Runnable comando) {
        if (comando == null) {
            throw new NullPointerException("A tarefa não pode ser nula.");
        }

        synchronized (monitor) {
            if (encerrado) {
                throw new RejectedExecutionException("O executor já foi encerrado.");
            }

            Thread thread = threadFactory.newThread(() -> {
                try {
                    comando.run();
                } finally {
                    synchronized (monitor) {
                        threadsAtivas.remove(Thread.currentThread());
                        monitor.notifyAll();
                    }
                }
            });

            threadsAtivas.add(thread);
            thread.start();
        }
    }

    @Override
    public void shutdown() {
        synchronized (monitor) {
            encerrado = true;
            monitor.notifyAll();
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        List<Thread> copia;
        synchronized (monitor) {
            encerrado = true;
            copia = new ArrayList<>(threadsAtivas);
            monitor.notifyAll();
        }

        copia.forEach(Thread::interrupt);
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        synchronized (monitor) {
            return encerrado;
        }
    }

    @Override
    public boolean isTerminated() {
        synchronized (monitor) {
            return encerrado && threadsAtivas.isEmpty();
        }
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long restante = unit.toNanos(timeout);
        long limite = System.nanoTime() + restante;

        synchronized (monitor) {
            while (!(encerrado && threadsAtivas.isEmpty())) {
                if (restante <= 0) {
                    return false;
                }
                TimeUnit.NANOSECONDS.timedWait(monitor, restante);
                restante = limite - System.nanoTime();
            }
            return true;
        }
    }
}
