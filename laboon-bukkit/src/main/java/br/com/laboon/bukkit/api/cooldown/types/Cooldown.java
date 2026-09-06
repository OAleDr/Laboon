package br.com.laboon.bukkit.api.cooldown.types;

public class Cooldown {

    private final String name;

    /**
     * Duração em segundos.
     */
    private final long duration;

    /**
     * Momento em que o cooldown foi iniciado, em milissegundos.
     */
    private final long startTime;

    public Cooldown(String name, long duration) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome do cooldown não pode ser vazio.");
        }

        if (duration <= 0) {
            throw new IllegalArgumentException("Duração do cooldown deve ser maior que zero.");
        }

        this.name = name;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    /**
     * Retorna a duração original em segundos.
     */
    public long getDuration() {
        return duration;
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * Retorna o tempo restante em segundos.
     */
    public double getRemaining() {

        long elapsed = System.currentTimeMillis() - startTime;

        double elapsedSeconds = elapsed / 1000D;

        return Math.max(0D, duration - elapsedSeconds);
    }

    /**
     * Retorna a porcentagem restante do cooldown.
     * <p>
     * 100 = acabou de começar
     * 0 = terminou
     */
    public double getPercentage() {

        double remaining = getRemaining();

        return Math.max(0D, Math.min(100D, (remaining * 100D) / duration));
    }

    /**
     * Verifica se o cooldown terminou.
     */
    public boolean expired() {

        long elapsed = System.currentTimeMillis() - startTime;

        return elapsed >= duration * 1000L;
    }
}