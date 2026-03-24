package br.com.webmatte.detector;

import br.com.webmatte.domain.ORF;
import br.com.webmatte.enums.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ORFDetector {

    private static final Logger log = LoggerFactory.getLogger(ORFDetector.class);
    private final String sequence;
    private final List<ORF> orfs;
    private final int minORFLength;

    public ORFDetector(String sequence, int minORFLength) {
        this.sequence = sequence.toUpperCase();
        this.minORFLength = minORFLength;
        this.orfs = new ArrayList<>();
        findORFs();
    }

    // Análise rápida
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
        findORFsInFrame(Frame.POSITIVE_1, 0);
        // Frame 2 (começa na posição 1)
        findORFsInFrame(Frame.POSITIVE_2, 1);
        // Frame 3 (começa na posição 2)
        findORFsInFrame(Frame.POSITIVE_3, 2);
    }

    private void findORFsInFrame(Frame frame, int offset) {
        int i = offset;
        while (i <= sequence.length() - 3) {
            if (!isStartCodon(i)) {
                i += 3;
                continue;
            }
            int stopPos = findStopCodon(i + 3);
            if (stopPos == -1) {
                handleIncompleteORF(frame, i);
                return;
            }
            i = handleCompleteORF(frame, i, stopPos);
        }
    }

    private boolean isStartCodon(int position) {
        return sequence.startsWith("ATG", position);
    }

    private int handleCompleteORF(Frame frame,
                                  int startPos,
                                  int stopPos) {
        int orfLength = stopPos - startPos + 3;
        if (orfLength < minORFLength) {
            return startPos + 3;
        }
        String orfSequence =
                sequence.substring(startPos, stopPos + 3);
        orfs.add(
                new ORF(
                        frame,
                        startPos,
                        stopPos + 2,
                        orfSequence,
                        true
                )
        );
        return stopPos + 3;
    }

    private void handleIncompleteORF(Frame frame,
                                     int startPos) {
        int remainingLength =
                sequence.length() - startPos;
        if (remainingLength < minORFLength) {
            return;
        }
        String orfSequence =
                sequence.substring(startPos);
        orfs.add(
                new ORF(
                        frame,
                        startPos,
                        sequence.length() - 1,
                        orfSequence,
                        false
                )
        );
    }

    private int findStopCodon(int startPos) {
        return stopCodons(startPos, sequence);
    }

    private int stopCodons(int startPos, String sequence) {
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
        findORFsInReverseFrame(Frame.NEGATIVE_1, 0, reverseComplement);
        // Frame -2
        findORFsInReverseFrame(Frame.NEGATIVE_2, 1, reverseComplement);
        // Frame -3
        findORFsInReverseFrame(Frame.NEGATIVE_3, 2, reverseComplement);
    }

    private void findORFsInReverseFrame(Frame frame, int offset, String reverseComplement) {
        int i = offset;
        while (i <= reverseComplement.length() - 3) {
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
                        continue;
                    }
                }
            }
            i += 3;
        }
    }

    private int findStopCodonReverse(int startPos, String reverseComplement) {
        return stopCodons(startPos, reverseComplement);
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
        log.info("Completude:");
        log.info("ORFs completos: {}", completeCount);
        log.info("ORFs incompletos: {}", incompleteCount);
        // Estatísticas de tamanho
        if (!orfs.isEmpty()) {
            double avgLength = orfs.stream().mapToInt(ORF::getLength).average().orElse(0.0);
            int maxLength = orfs.stream().mapToInt(ORF::getLength).max().orElse(0);
            int minLength = orfs.stream().mapToInt(ORF::getLength).min().orElse(0);
            log.info("Estatísticas de tamanho:");
            log.info("Comprimento médio: {} nucleotídeos", avgLength);
            log.info("Comprimento máximo: {} nucleotídeos", maxLength);
            log.info("Comprimento mínimo: {} nucleotídeos", minLength);
            log.info("");
        }
        // Lista os maiores ORFs
        log.info("Maiores ORFs encontrados:");
        orfs.stream()
                .sorted((a, b) -> Integer.compare(b.getLength(), a.getLength()))
                .limit(10)
                .forEach(orf -> log.info("  {}", orf));
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
                    case 'F', 'W', 'Y':
                        aromaticCount++;
                        break;
                    default:
                        // Other amino acids are not counted for this analysis
                        break;
                }
            }
        }
        log.info("Resíduos de interesse:");
        log.info("Cisteína (C): {}", cysteineCount);
        log.info("Histidina (H): {}", histidineCount);
        log.info("Aromáticos (F, W, Y): {}", aromaticCount);
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

}
