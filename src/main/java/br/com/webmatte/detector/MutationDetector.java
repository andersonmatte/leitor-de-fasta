package br.com.webmatte.detector;

import br.com.webmatte.domain.Mutation;
import br.com.webmatte.enums.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MutationDetector {
    private static final Logger log = LoggerFactory.getLogger(MutationDetector.class);
    private final String referenceSequence;
    private final String mutatedSequence;
    private final List<Mutation> mutations;

    public MutationDetector(String referenceSequence, String mutatedSequence) {
        this.referenceSequence = referenceSequence.toUpperCase();
        this.mutatedSequence = mutatedSequence.toUpperCase();
        this.mutations = new ArrayList<>();
        detectMutations();
    }

    // Comparar com uma sequência de referência específica
    public static void compareSequences(String seq1, String seq2, String name1, String name2) {
        log.info("=== COMPARAÇÃO ENTRE SEQUÊNCIAS ===");
        log.info("{}: {} nucleotídeos", name1, seq1.length());
        log.info("{}: {} nucleotídeos", name2, seq2.length());
        log.info("");
        MutationDetector detector = new MutationDetector(seq1, seq2);
        detector.printMutationReport();
    }

    private void detectMutations() {
        int refLength = referenceSequence.length();
        int mutLength = mutatedSequence.length();
        int i = 0;
        int j = 0;
        while (i < refLength && j < mutLength) {
            char refBase = referenceSequence.charAt(i);
            char mutBase = mutatedSequence.charAt(j);
            if (refBase == mutBase) {
                i++;
                j++;
            } else {
                // Detecta tipo de mutação
                Mutation mutation = analyzeMutation(i, j);
                if (mutation != null) {
                    mutations.add(mutation);
                    // Avança os ponteiros baseado no tipo de mutação
                    switch (mutation.getType()) {
                        case SNP, SUBSTITUTION:
                            i++;
                            j++;
                            break;
                        case INSERTION:
                            j += mutation.getAlternative().length();
                            break;
                        case DELETION:
                            i += mutation.getReference().length();
                            break;
                        case INDEL:
                            // Complex case, avança ambos
                            i += mutation.getReference().length();
                            j += mutation.getAlternative().length();
                            break;
                    }
                } else {
                    // Fallback simples
                    i++;
                    j++;
                }
            }
        }
        // Detecta deleções no final da sequência
        while (i < refLength) {
            mutations.add(new Mutation(Type.DELETION, i,
                    referenceSequence.substring(i, Math.min(i + 1, refLength)),
                    "", referenceSequence));
            i++;
        }
        // Detecta inserções no final da sequência
        while (j < mutLength) {
            mutations.add(new Mutation(Type.INSERTION, i, "",
                    mutatedSequence.substring(j, Math.min(j + 1, mutLength)),
                    referenceSequence));
            j++;
        }
    }

    private Mutation analyzeMutation(int refPos, int mutPos) {
        // Verifica se é SNP (mudança de um nucleotídeo)
        if (refPos + 1 <= referenceSequence.length() && mutPos + 1 <= mutatedSequence.length()) {
            char refBase = referenceSequence.charAt(refPos);
            char mutBase = mutatedSequence.charAt(mutPos);
            if (isValidNucleotide(refBase) && isValidNucleotide(mutBase)) {
                return new Mutation(Type.SNP, refPos,
                        String.valueOf(refBase),
                        String.valueOf(mutBase),
                        referenceSequence);
            }
        }
        // Para casos mais complexos, implementar lógica adicional
        // Por enquanto, trata como substituição simples
        if (refPos < referenceSequence.length() && mutPos < mutatedSequence.length()) {
            return new Mutation(Type.SUBSTITUTION, refPos,
                    String.valueOf(referenceSequence.charAt(refPos)),
                    String.valueOf(mutatedSequence.charAt(mutPos)),
                    referenceSequence);
        }
        return null;
    }

    private boolean isValidNucleotide(char c) {
        return c == 'A' || c == 'T' || c == 'C' || c == 'G';
    }

    public void printMutationReport() {
        log.info("=== RELATÓRIO DE DETECÇÃO DE MUTAÇÕES ===");
        log.info("Sequência de referência: {} nucleotídeos", referenceSequence.length());
        log.info("Sequência mutada: {} nucleotídeos", mutatedSequence.length());
        log.info("Total de mutações detectadas: {}", mutations.size());
        if (mutations.isEmpty()) {
            log.info("Nenhuma mutação detectada. As sequências são idênticas.");
            return;
        }
        // Contagem por tipo
        int snpCount = 0;
        int insertionCount = 0;
        int deletionCount = 0;
        int substitutionCount = 0;
        int indelCount = 0;
        for (Mutation mutation : mutations) {
            switch (mutation.getType()) {
                case SNP:
                    snpCount++;
                    break;
                case INSERTION:
                    insertionCount++;
                    break;
                case DELETION:
                    deletionCount++;
                    break;
                case SUBSTITUTION:
                    substitutionCount++;
                    break;
                case INDEL:
                    indelCount++;
                    break;
            }
        }
        log.info("Distribuição por tipo:");
        log.info("SNPs: {}", snpCount);
        log.info("Inserções: {}", insertionCount);
        log.info("Deleções: {}", deletionCount);
        log.info("Substituições: {}", substitutionCount);
        log.info("Indels: {}", indelCount);
        // Detalhes das mutações
        log.info("Detalhes das mutações:");
        for (int i = 0; i < mutations.size(); i++) {
            log.info("{}. {}", (i + 1), mutations.get(i));
        }
        // Análise adicional
        analyzeMutationPatterns();
    }

    private void analyzeMutationPatterns() {
        if (mutations.isEmpty()) return;
        log.info("Análise de padrões de mutação:");
        // Taxa de mutação
        double mutationRate = (double) mutations.size() / referenceSequence.length() * 100;
        if (log.isInfoEnabled()) {
            log.info(String.format("Taxa de mutação: %.4f%%", mutationRate));
        }
        // Tipos de substituições mais comuns (para SNPs)
        List<String> substitutionTypes = new ArrayList<>();
        for (Mutation mutation : mutations) {
            if (mutation.getType() == Type.SNP) {
                substitutionTypes.add(mutation.getReference() + "→" + mutation.getAlternative());
            }
        }
        if (!substitutionTypes.isEmpty()) {
            log.info("Tipos de substituição mais comuns:");
            substitutionTypes.stream()
                    .distinct()
                    .sorted((a, b) -> Long.compare(
                            substitutionTypes.stream().filter(x -> x.equals(b)).count(),
                            substitutionTypes.stream().filter(x -> x.equals(a)).count()))
                    .limit(5)
                    .forEach(type -> {
                        long count = substitutionTypes.stream().filter(x -> x.equals(type)).count();
                        log.info("  {}: {} ocorrências", type, count);
                    });
        }
        log.info("");
    }

    // Getters
    public List<Mutation> getMutations() {
        return new ArrayList<>(mutations);
    }

    public String getReferenceSequence() {
        return referenceSequence;
    }

    public String getMutatedSequence() {
        return mutatedSequence;
    }

    public int getSnpCount() {
        return (int) mutations.stream().filter(m -> m.getType() == Type.SNP).count();
    }

    public int getInsertionCount() {
        return (int) mutations.stream().filter(m -> m.getType() == Type.INSERTION).count();
    }

    public int getDeletionCount() {
        return (int) mutations.stream().filter(m -> m.getType() == Type.DELETION).count();
    }

}
