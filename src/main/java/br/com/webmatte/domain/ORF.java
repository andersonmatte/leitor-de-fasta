package br.com.webmatte.domain;

import br.com.webmatte.enums.Frame;
import br.com.webmatte.infra.ProteinTranslator;

public class ORF {

    private final Frame frame;
    private final int startPosition;
    private final int endPosition;
    private final int length;
    private final String nucleotideSequence;
    private final String proteinSequence;
    private final boolean isComplete;

    public ORF(Frame frame, int startPosition, int endPosition, String sequence, boolean isComplete) {
        this.frame = frame;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.length = endPosition - startPosition + 1;
        this.nucleotideSequence = sequence;
        this.isComplete = isComplete;
        this.proteinSequence = translateToProtein(sequence);
    }

    private String translateToProtein(String dnaSequence) {
        return ProteinTranslator.translateQuick(dnaSequence);
    }

    public Frame getFrame() {
        return frame;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public int getLength() {
        return length;
    }

    public String getNucleotideSequence() {
        return nucleotideSequence;
    }

    public String getProteinSequence() {
        return proteinSequence;
    }

    public boolean isComplete() {
        return isComplete;
    }

    @Override
    public String toString() {
        return String.format("ORF %s: posição %d-%d (%d nt, %d aa) %s",
                frame, startPosition, endPosition, length,
                proteinSequence.length(),
                isComplete ? "[completo]" : "[incompleto]");
    }

}
