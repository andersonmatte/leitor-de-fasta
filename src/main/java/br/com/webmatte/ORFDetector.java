package br.com.webmatte;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ORFDetector {
    private static final Logger log = LoggerFactory.getLogger(ORFDetector.class);
    private String sequence;
    private List<ORF> orfs;
    private int minORFLength;

    public ORFDetector(String sequence, int minORFLength) {
        this.sequence = sequence.toUpperCase();
        this.minORFLength = minORFLength;
        this.orfs = new ArrayList<>();
        findORFs();
    }

    // Método estático para análise rápida
    public static void findORFs(String sequence) {
        ORFDetector detector = new ORFDetector(sequence, 300); // 300 nucleotídeos = 100 aminoácidos
        detector.printORFReport();
    }

    private void findORFs() {
        // Busca ORFs na fita positiva
        findORFsPositiveStrand();

        // Busca ORFs na fita negativa
        findORFsNegativeStrand();
    }

    private void findORFsPositiveStrand() {
        // Frame 1 (começa na posição 0)
        findORFsInFrame(ORF.Frame.POSITIVE_1, 0);

        // Frame 2 (começa na posição 1)
        findORFsInFrame(ORF.Frame.POSITIVE_2, 1);

        // Frame 3 (começa na posição 2)
        findORFsInFrame(ORF.Frame.POSITIVE_3, 2);
    }

    private void findORFsInFrame(ORF.Frame frame, int offset) {
        for (int i = offset; i <= sequence.length() - 3; i += 3) {
            String codon = sequence.substring(i, i + 3);

            if (codon.equals("ATG")) { // Start codon
                // Procura o próximo códon de parada
                int stopPos = findStopCodon(i + 3);

                if (stopPos != -1) {
                    int orfLength = stopPos - i + 3;
                    if (orfLength >= minORFLength) {
                        String orfSequence = sequence.substring(i, stopPos + 3);
                        orfs.add(new ORF(frame, i, stopPos + 2, orfSequence, true));
                        i = stopPos + 3; // Pula o ORF encontrado
                    }
                } else {
                    // ORF incompleto (sem códon de parada)
                    int remainingLength = sequence.length() - i;
                    if (remainingLength >= minORFLength) {
                        String orfSequence = sequence.substring(i);
                        orfs.add(new ORF(frame, i, sequence.length() - 1, orfSequence, false));
                    }
                    break;
                }
            }
        }
    }

    private int findStopCodon(int startPos) {
        Set<String> stopCodons = new HashSet<>();
        stopCodons.add("TAA");
        stopCodons.add("TAG");
        stopCodons.add("TGA");

        for (int i = startPos; i <= sequence.length() - 3; i += 3) {
            String codon = sequence.substring(i, i + 3);
            if (stopCodons.contains(codon)) {
                return i;
            }
        }
        return -1; // Não encontrou códon de parada
    }

    private void findORFsNegativeStrand() {
        String reverseComplement = getReverseComplement();

        // Frame -1 (começa na posição 0 do reverse complement)
        findORFsInReverseFrame(ORF.Frame.NEGATIVE_1, 0, reverseComplement);

        // Frame -2
        findORFsInReverseFrame(ORF.Frame.NEGATIVE_2, 1, reverseComplement);

        // Frame -3
        findORFsInReverseFrame(ORF.Frame.NEGATIVE_3, 2, reverseComplement);
    }

    private void findORFsInReverseFrame(ORF.Frame frame, int offset, String reverseComplement) {
        for (int i = offset; i <= reverseComplement.length() - 3; i += 3) {
            String codon = reverseComplement.substring(i, i + 3);

            if (codon.equals("ATG")) { // Start codon
                // Procura o próximo códon de parada
                int stopPos = findStopCodonReverse(i + 3, reverseComplement);

                if (stopPos != -1) {
                    int orfLength = stopPos - i + 3;
                    if (orfLength >= minORFLength) {
                        String orfSequence = reverseComplement.substring(i, stopPos + 3);

                        // Converte posições de volta para a sequência original
                        int originalStart = sequence.length() - (i + orfLength);
                        int originalEnd = sequence.length() - (i + 1);

                        orfs.add(new ORF(frame, originalStart, originalEnd, orfSequence, true));
                        i = stopPos + 3;
                    }
                }
            }
        }
    }

    private int findStopCodonReverse(int startPos, String reverseComplement) {
        Set<String> stopCodons = new HashSet<>();
        stopCodons.add("TAA");
        stopCodons.add("TAG");
        stopCodons.add("TGA");

        for (int i = startPos; i <= reverseComplement.length() - 3; i += 3) {
            String codon = reverseComplement.substring(i, i + 3);
            if (stopCodons.contains(codon)) {
                return i;
            }
        }
        return -1;
    }

    private String getReverseComplement() {
        StringBuilder reverseComplement = new StringBuilder();

        for (int i = sequence.length() - 1; i >= 0; i--) {
            char base = sequence.charAt(i);
            switch (base) {
                case 'A':
                    reverseComplement.append('T');
                    break;
                case 'T':
                    reverseComplement.append('A');
                    break;
                case 'C':
                    reverseComplement.append('G');
                    break;
                case 'G':
                    reverseComplement.append('C');
                    break;
                default:
                    reverseComplement.append('N');
                    break; // Para nucleotídeos ambíguos
            }
        }

        return reverseComplement.toString();
    }

    public void printORFReport() {
        log.info("=== DETECÇÃO DE ORFs (OPEN READING FRAMES) ===");
        log.info("Tamanho da sequência: {}", sequence.length());
        log.info("Tamanho mínimo de ORF: {} nucleotídeos", minORFLength);
        log.info("Total de ORFs encontrados: {}", orfs.size());
        log.info("");

        if (orfs.isEmpty()) {
            log.info("Nenhum ORF encontrado com o tamanho mínimo especificado.");
            return;
        }

        // Estatísticas por frame
        int[] frameCounts = new int[6];
        int completeCount = 0;
        int incompleteCount = 0;

        for (ORF orf : orfs) {
            switch (orf.getFrame()) {
                case POSITIVE_1:
                    frameCounts[0]++;
                    break;
                case POSITIVE_2:
                    frameCounts[1]++;
                    break;
                case POSITIVE_3:
                    frameCounts[2]++;
                    break;
                case NEGATIVE_1:
                    frameCounts[3]++;
                    break;
                case NEGATIVE_2:
                    frameCounts[4]++;
                    break;
                case NEGATIVE_3:
                    frameCounts[5]++;
                    break;
            }

            if (orf.isComplete()) {
                completeCount++;
            } else {
                incompleteCount++;
            }
        }

        log.info("Distribuição por frame:");
        log.info("Fita positiva:");
        log.info("  Frame +1: {} ORFs", frameCounts[0]);
        log.info("  Frame +2: {} ORFs", frameCounts[1]);
        log.info("  Frame +3: {} ORFs", frameCounts[2]);
        log.info("Fita negativa:");
        log.info("  Frame -1: {} ORFs", frameCounts[3]);
        log.info("  Frame -2: {} ORFs", frameCounts[4]);
        log.info("  Frame -3: {} ORFs", frameCounts[5]);
        log.info("");

        log.info("Completude:");
        log.info("ORFs completos: {}", completeCount);
        log.info("ORFs incompletos: {}", incompleteCount);
        log.info("");

        // Estatísticas de tamanho
        if (!orfs.isEmpty()) {
            double avgLength = orfs.stream().mapToInt(ORF::getLength).average().orElse(0.0);
            int maxLength = orfs.stream().mapToInt(ORF::getLength).max().orElse(0);
            int minLength = orfs.stream().mapToInt(ORF::getLength).min().orElse(0);

            log.info("Estatísticas de tamanho:");
            log.info("Comprimento médio: {} nucleotídeos", String.format("%.1f", avgLength));
            log.info("Comprimento máximo: {} nucleotídeos", maxLength);
            log.info("Comprimento mínimo: {} nucleotídeos", minLength);
            log.info("");
        }

        // Lista os maiores ORFs
        log.info("Maiores ORFs encontrados:");
        orfs.stream()
                .sorted((a, b) -> Integer.compare(b.getLength(), a.getLength()))
                .limit(10)
                .forEach(orf -> log.info("  {}", orf.toString()));
        log.info("");

        // Análise de proteínas potenciais
        analyzePotentialProteins();
    }

    private void analyzePotentialProteins() {
        log.info("ANÁLISE DE PROTEÍNAS POTENCIAIS:");

        int totalProteins = orfs.stream().mapToInt(orf -> orf.getProteinSequence().length()).sum();
        log.info("Total de aminoácidos em todas as proteínas: {}", totalProteins);

        // Busca por domínios conhecidos (simplificado)
        int cysteineCount = 0;
        int histidineCount = 0;
        int aromaticCount = 0;

        for (ORF orf : orfs) {
            for (char aa : orf.getProteinSequence().toCharArray()) {
                switch (aa) {
                    case 'C':
                        cysteineCount++;
                        break;
                    case 'H':
                        histidineCount++;
                        break;
                    case 'F':
                    case 'W':
                    case 'Y':
                        aromaticCount++;
                        break;
                }
            }
        }

        log.info("Resíduos de interesse:");
        log.info("Cisteína (C): {}", cysteineCount);
        log.info("Histidina (H): {}", histidineCount);
        log.info("Aromáticos (F, W, Y): {}", aromaticCount);
        log.info("");
    }

    // Getters
    public List<ORF> getORFs() {
        return new ArrayList<>(orfs);
    }

    public String getSequence() {
        return sequence;
    }

    public int getMinORFLength() {
        return minORFLength;
    }

    public static class ORF {
        private Frame frame;
        private int startPosition;
        private int endPosition;
        private int length;
        private String nucleotideSequence;
        private String proteinSequence;
        private boolean isComplete;
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

        public enum Frame {
            POSITIVE_1, POSITIVE_2, POSITIVE_3,
            NEGATIVE_1, NEGATIVE_2, NEGATIVE_3
        }
    }
}
