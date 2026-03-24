package br.com.webmatte.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SequenceValidator {

    private static final Logger log = LoggerFactory.getLogger(SequenceValidator.class);
    private static final Set<Character> VALID_NUCLEOTIDES = new HashSet<>();
    private static final Set<Character> AMBIGUOUS_NUCLEOTIDES = new HashSet<>();

    static {
        // Nucleotídeos válidos
        VALID_NUCLEOTIDES.add('A');
        VALID_NUCLEOTIDES.add('T');
        VALID_NUCLEOTIDES.add('C');
        VALID_NUCLEOTIDES.add('G');
        // Nucleotídeos ambíguos
        AMBIGUOUS_NUCLEOTIDES.add('N'); // qualquer nucleotídeo
        AMBIGUOUS_NUCLEOTIDES.add('R'); // A ou G (purina)
        AMBIGUOUS_NUCLEOTIDES.add('Y'); // C ou T (pirimidina)
        AMBIGUOUS_NUCLEOTIDES.add('S'); // G ou C
        AMBIGUOUS_NUCLEOTIDES.add('W'); // A ou T
        AMBIGUOUS_NUCLEOTIDES.add('K'); // G ou T
        AMBIGUOUS_NUCLEOTIDES.add('M'); // A ou C
        AMBIGUOUS_NUCLEOTIDES.add('B'); // C ou G ou T
        AMBIGUOUS_NUCLEOTIDES.add('D'); // A ou G ou T
        AMBIGUOUS_NUCLEOTIDES.add('H'); // A ou C ou T
        AMBIGUOUS_NUCLEOTIDES.add('V'); // A ou C ou G
    }

    private final String sequenceId;
    private final String originalSequence;
    private final List<String> validationErrors;
    private final List<Integer> invalidPositions;
    private String cleanedSequence;
    private int ambiguousCount;

    public SequenceValidator(String sequenceId, String sequence) {
        this.sequenceId = sequenceId;
        this.originalSequence = sequence;
        this.validationErrors = new ArrayList<>();
        this.invalidPositions = new ArrayList<>();
        this.ambiguousCount = 0;
        validateAndClean();
    }

    private void validateAndClean() {
        if (originalSequence == null || originalSequence.trim().isEmpty()) {
            validationErrors.add("Sequência vazia ou nula");
            cleanedSequence = "";
            return;
        }
        StringBuilder cleaned = new StringBuilder();
        String upperSequence = originalSequence.toUpperCase().trim();
        for (int i = 0; i < upperSequence.length(); i++) {
            char c = upperSequence.charAt(i);
            if (VALID_NUCLEOTIDES.contains(c)) {
                cleaned.append(c);
            } else if (AMBIGUOUS_NUCLEOTIDES.contains(c)) {
                cleaned.append(c); // mantém ambíguos para análise posterior
                ambiguousCount++;
            } else {
                invalidPositions.add(i);
                // Ignora caracteres inválidos como espaços, números, etc.
            }
        }
        cleanedSequence = cleaned.toString();
        // Adiciona erros de validação
        if (!invalidPositions.isEmpty()) {
            validationErrors.add("Caracteres inválidos encontrados nas posições: " + invalidPositions.size());
        }
        if (ambiguousCount > 0) {
            validationErrors.add("Nucleotídeos ambíguos encontrados: " + ambiguousCount);
        }
        double ambiguousPercentage = (double) ambiguousCount / cleanedSequence.length() * 100;
        if (ambiguousPercentage > 5.0) {
            validationErrors.add(String.format("Alta porcentagem de nucleotídeos ambíguos: %.1f%%", ambiguousPercentage));
        }
    }

    public void printValidationReport() {
        log.info("=== RELATÓRIO DE VALIDAÇÃO ===");
        log.info("Sequence ID: {}", sequenceId);
        log.info("Tamanho original: {}", (originalSequence != null ? originalSequence.length() : 0));
        log.info("Tamanho limpo: {}", cleanedSequence.length());
        log.info("Caracteres inválidos removidos: {}", invalidPositions.size());
        log.info("Nucleotídeos ambíguos: {}", ambiguousCount);
        if (validationErrors.isEmpty()) {
            log.info("Sequência válida!");
        } else {
            log.warn("Problemas encontrados:");
            for (String error : validationErrors) {
                log.warn("  - {}", error);
            }
        }
    }

    public String getCleanedSequence() {
        return cleanedSequence;
    }

    public boolean isValid() {
        return validationErrors.isEmpty() && !cleanedSequence.isEmpty();
    }

    public List<String> getValidationErrors() {
        return new ArrayList<>(validationErrors);
    }

    public int getAmbiguousCount() {
        return ambiguousCount;
    }

    public double getAmbiguousPercentage() {
        if (cleanedSequence.isEmpty()) return 0.0;
        return (double) ambiguousCount / cleanedSequence.length() * 100;
    }

}
