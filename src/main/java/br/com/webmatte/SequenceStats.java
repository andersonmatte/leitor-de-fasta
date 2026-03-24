package br.com.webmatte;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SequenceStats {
    private static final Logger log = LoggerFactory.getLogger(SequenceStats.class);
    private String sequenceId;
    private String sequence;
    private int length;
    private Map<Character, Integer> nucleotideCounts;
    private double gcContent;

    public SequenceStats(String sequenceId, String sequence) {
        this.sequenceId = sequenceId;
        this.sequence = sequence.toUpperCase().replaceAll("[^ATCGN]", "");
        this.length = this.sequence.length();
        this.nucleotideCounts = new HashMap<>();
        calculateStats();
    }

    private void calculateStats() {
        // Initialize counts
        nucleotideCounts.put('A', 0);
        nucleotideCounts.put('T', 0);
        nucleotideCounts.put('C', 0);
        nucleotideCounts.put('G', 0);
        nucleotideCounts.put('N', 0);

        // Count nucleotides
        for (char nucleotide : sequence.toCharArray()) {
            nucleotideCounts.put(nucleotide, nucleotideCounts.getOrDefault(nucleotide, 0) + 1);
        }

        // Calculate GC content
        int totalValid = nucleotideCounts.get('A') + nucleotideCounts.get('T') +
                nucleotideCounts.get('C') + nucleotideCounts.get('G');

        if (totalValid > 0) {
            int gcCount = nucleotideCounts.get('G') + nucleotideCounts.get('C');
            gcContent = (double) gcCount / totalValid * 100.0;
        } else {
            gcContent = 0.0;
        }
    }

    public void printBasicStats() {
        log.info("=== ESTATÍSTICAS BÁSICAS DA SEQUÊNCIA ===");
        log.info("Sequence ID: {}", sequenceId);
        log.info("Length: {}", length);
        log.info("");

        log.info("Contagem de Nucleotídeos:");
        log.info("A: {}", nucleotideCounts.get('A'));
        log.info("T: {}", nucleotideCounts.get('T'));
        log.info("C: {}", nucleotideCounts.get('C'));
        log.info("G: {}", nucleotideCounts.get('G'));
        log.info("N (ambíguos): {}", nucleotideCounts.get('N'));
        log.info("");

        if (log.isInfoEnabled()) {
            log.info("GC Content: {}%", String.format("%.1f", gcContent));
        }
        log.info("");

        // Frequência relativa
        log.info("Frequência Relativa:");
        if (length > 0 && log.isInfoEnabled()) {
            log.info("A: {}%", String.format("%.1f", (double) nucleotideCounts.get('A') / length * 100));
            log.info("T: {}%", String.format("%.1f", (double) nucleotideCounts.get('T') / length * 100));
            log.info("C: {}%", String.format("%.1f", (double) nucleotideCounts.get('C') / length * 100));
            log.info("G: {}%", String.format("%.1f", (double) nucleotideCounts.get('G') / length * 100));
            log.info("N: {}%", String.format("%.1f", (double) nucleotideCounts.get('N') / length * 100));
        }
        log.info("");
    }

    // Getters
    public String getSequenceId() {
        return sequenceId;
    }

    public String getSequence() {
        return sequence;
    }

    public int getLength() {
        return length;
    }

    public Map<Character, Integer> getNucleotideCounts() {
        return nucleotideCounts;
    }

    public double getGcContent() {
        return gcContent;
    }
}
