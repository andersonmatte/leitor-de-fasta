package br.com.webmatte.domain;

import br.com.webmatte.enums.Type;

public class Mutation {

    private final Type type;
    private final int position;
    private final String reference;
    private final String alternative;
    private final String context;

    public Mutation(Type type, int position, String reference, String alternative, String sequence) {
        this.type = type;
        this.position = position;
        this.reference = reference;
        this.alternative = alternative;
        // Extrai contexto
        int start = Math.max(0, position - 5);
        int end = Math.min(sequence.length(), position + Math.max(reference.length(), alternative.length()) + 5);
        this.context = sequence.substring(start, end);
    }

    public Type getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public String getReference() {
        return reference;
    }

    public String getAlternative() {
        return alternative;
    }

    public String getContext() {
        return context;
    }

    @Override
    public String toString() {
        String typeName = type.name();
        if (type == Type.SNP) {
            return String.format("SNP na posição %d: %s→%s (contexto: ...%s...)",
                    position, reference, alternative, context);
        } else {
            return String.format("%s na posição %d: %s→%s",
                    typeName, position, reference, alternative);
        }
    }

}
