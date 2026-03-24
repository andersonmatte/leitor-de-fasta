package br.com.webmatte.domain;

public class MotifMatch {

    private final String motif;
    private final int position;
    private final String context;
    private final int length;

    public MotifMatch(String motif, int position, String sequence) {
        this.motif = motif;
        this.position = position;
        this.length = motif.length();
        // Extrai contexto (10 nucleotídeos antes e depois, se possível)
        int start = Math.max(0, position - 10);
        int end = Math.min(sequence.length(), position + motif.length() + 10);
        this.context = sequence.substring(start, end);
    }

    public String getMotif() {
        return motif;
    }

    public int getPosition() {
        return position;
    }

    public String getContext() {
        return context;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return String.format("Posição %d: %s (contexto: ...%s...)",
                position, motif, context);
    }

}
