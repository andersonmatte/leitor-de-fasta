package br.com.webmatte.infra;

import br.com.webmatte.detector.ORFDetector;
import br.com.webmatte.domain.GlobalStatistics;
import br.com.webmatte.domain.SequenceMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MetricsExporter {

    private static final Logger log = LoggerFactory.getLogger(MetricsExporter.class);
    private final List<SequenceMetrics> allMetrics;

    public MetricsExporter() {
        this.allMetrics = new ArrayList<>();
    }

    // Criar métricas a partir de análises existentes
    public static SequenceMetrics createMetricsFromAnalysis(String sequenceId,
                                                            SequenceStats stats,
                                                            SequenceValidator validator,
                                                            ORFDetector orfDetector,
                                                            MotifFinder motifFinder) {
        SequenceMetrics metrics = new SequenceMetrics(sequenceId);
        // Estatísticas básicas
        metrics.setLength(stats.getLength());
        metrics.setACount(stats.getNucleotideCounts().get('A'));
        metrics.setTCount(stats.getNucleotideCounts().get('T'));
        metrics.setCCount(stats.getNucleotideCounts().get('C'));
        metrics.setGCount(stats.getNucleotideCounts().get('G'));
        metrics.setNCount(stats.getNucleotideCounts().get('N'));
        metrics.setGcContent(stats.getGcContent());
        // Validação
        metrics.setAmbiguousPercentage(validator.getAmbiguousPercentage());
        // ORFs
        metrics.setOrfCount(orfDetector.getORFs().size());
        // Motifs
        metrics.setStartCodonCount(motifFinder.findMotif("ATG").size());
        metrics.setStopCodonCount(motifFinder.findMotif("TAA").size() +
                motifFinder.findMotif("TAG").size() +
                motifFinder.findMotif("TGA").size());
        return metrics;
    }

    public void addSequenceMetrics(SequenceMetrics metrics) {
        allMetrics.add(metrics);
    }

    public void exportToCSV(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Cabeçalho CSV
            writer.println("Sequence_ID,Length,A_Count,T_Count,C_Count,G_Count,N_Count,GC_Content,ORF_Count,Start_Codon_Count,Stop_Codon_Count,Avg_Protein_Length,Mutation_Count,Ambiguous_Percentage");
            // Dados
            for (SequenceMetrics metrics : allMetrics) {
                writer.printf("%s,%d,%d,%d,%d,%d,%d,%.2f,%d,%d,%d,%.1f,%d,%.2f%n",
                        metrics.getSequenceId(),
                        metrics.getLength(),
                        metrics.getACount(),
                        metrics.getTCount(),
                        metrics.getCCount(),
                        metrics.getGCount(),
                        metrics.getNCount(),
                        metrics.getGcContent(),
                        metrics.getOrfCount(),
                        metrics.getStartCodonCount(),
                        metrics.getStopCodonCount(),
                        metrics.getAvgProteinLength(),
                        metrics.getMutationCount(),
                        metrics.getAmbiguousPercentage());
            }
            log.info("Dados exportados para CSV: {}", filename);
        }
    }

    public void exportToJSON(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("{");
            writer.println("  \"analysis_date\": \"" + java.time.LocalDate.now() + "\",");
            writer.println("  \"total_sequences\": " + allMetrics.size() + ",");
            writer.println("  \"sequences\": [");
            for (int i = 0; i < allMetrics.size(); i++) {
                SequenceMetrics metrics = allMetrics.get(i);
                writer.println("    {");
                writer.println("      \"sequence_id\": \"" + metrics.getSequenceId() + "\",");
                writer.println("      \"length\": " + metrics.getLength() + ",");
                writer.println("      \"nucleotide_counts\": {");
                writer.println("        \"A\": " + metrics.getACount() + ",");
                writer.println("        \"T\": " + metrics.getTCount() + ",");
                writer.println("        \"C\": " + metrics.getCCount() + ",");
                writer.println("        \"G\": " + metrics.getGCount() + ",");
                writer.println("        \"N\": " + metrics.getNCount());
                writer.println("      },");
                writer.println("      \"gc_content\": " + String.format("%.2f", metrics.getGcContent()) + ",");
                writer.println("      \"orf_count\": " + metrics.getOrfCount() + ",");
                writer.println("      \"start_codon_count\": " + metrics.getStartCodonCount() + ",");
                writer.println("      \"stop_codon_count\": " + metrics.getStopCodonCount() + ",");
                writer.println("      \"avg_protein_length\": " + String.format("%.1f", metrics.getAvgProteinLength()) + ",");
                writer.println("      \"mutation_count\": " + metrics.getMutationCount() + ",");
                writer.println("      \"ambiguous_percentage\": " + String.format("%.2f", metrics.getAmbiguousPercentage()));
                writer.print("    }");
                if (i < allMetrics.size() - 1) {
                    writer.println(",");
                } else {
                    writer.println();
                }
            }
            writer.println("  ],");
            // Estatísticas globais
            writer.println("  \"global_statistics\": {");
            if (!allMetrics.isEmpty()) {
                GlobalStatistics stats = calculateGlobalStatistics();
                writer.println("    \"average_length\": " + String.format("%.1f", stats.avgLength()) + ",");
                writer.println("    \"average_gc_content\": " + String.format("%.2f", stats.avgGC()) + ",");
                writer.println("    \"total_orfs\": " + stats.totalORFs() + ",");
                writer.println("    \"total_mutations\": " + stats.totalMutations());
            }
            writer.println("  }");
            writer.println("}");
            log.info("Dados exportados para JSON: {}", filename);
        }
    }

    public void exportSummaryReport(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("=== RELATÓRIO DE ANÁLISE FASTA ===");
            writer.println("Data: " + java.time.LocalDate.now());
            writer.println("Total de sequências analisadas: " + allMetrics.size());
            writer.println();
            if (allMetrics.isEmpty()) {
                writer.println("Nenhuma sequência para analisar.");
                return;
            }
            // Estatísticas globais
            GlobalStatistics stats = calculateGlobalStatistics();
            writer.println("ESTATÍSTICAS GLOBAIS:");
            writer.printf("Comprimento total: %,d nucleotídeos%n", stats.totalLength());
            writer.printf("Comprimento médio: %.1f nucleotídeos%n", stats.avgLength());
            writer.printf("GC content médio: %.2f%%%n", stats.avgGC());
            writer.printf("Total de ORFs: %d%n", stats.totalORFs());
            writer.printf("Total de mutações: %d%n", stats.totalMutations());
            writer.println();
            // Top 5 maiores sequências
            writer.println("MAIORES SEQUÊNCIAS:");
            allMetrics.stream()
                    .sorted((a, b) -> Integer.compare(b.getLength(), a.getLength()))
                    .limit(5)
                    .forEach(metrics -> writer.printf("%s: %,d nucleotídeos (GC: %.1f%%)%n",
                            metrics.getSequenceId(),
                            metrics.getLength(),
                            metrics.getGcContent()));
            writer.println();
            // Top 5 maior GC content
            writer.println("MAIOR GC CONTENT:");
            allMetrics.stream()
                    .sorted((a, b) -> Double.compare(b.getGcContent(), a.getGcContent()))
                    .limit(5)
                    .forEach(metrics -> writer.printf("%s: %.2f%% (%,d nucleotídeos)%n",
                            metrics.getSequenceId(),
                            metrics.getGcContent(),
                            metrics.getLength()));
            writer.println();
            // Detalhes individuais
            writer.println("DETALHES INDIVIDUAIS:");
            for (SequenceMetrics metrics : allMetrics) {
                writer.printf("%n%s:%n", metrics.getSequenceId());
                writer.printf("  Comprimento: %,d nucleotídeos%n", metrics.getLength());
                writer.printf("  GC Content: %.2f%%%n", metrics.getGcContent());
                writer.printf("  Nucleotídeos: A=%d, T=%d, C=%d, G=%d, N=%d%n",
                        metrics.getACount(), metrics.getTCount(),
                        metrics.getCCount(), metrics.getGCount(), metrics.getNCount());
                writer.printf("  ORFs: %d%n", metrics.getOrfCount());
                writer.printf("  Códons start: %d, stop: %d%n",
                        metrics.getStartCodonCount(), metrics.getStopCodonCount());
                if (metrics.getMutationCount() > 0) {
                    writer.printf("  Mutações: %d%n", metrics.getMutationCount());
                }
                if (metrics.getAmbiguousPercentage() > 0) {
                    writer.printf("  Nucleotídeos ambíguos: %.2f%%%n", metrics.getAmbiguousPercentage());
                }
            }
            log.info("Relatório exportado: {}", filename);
        }
    }

    public void printSummary() {
        log.info("=== RESUMO DAS MÉTRICAS EXPORTADAS ===");
        log.info("Total de sequências: {}", allMetrics.size());
        if (!allMetrics.isEmpty()) {
            GlobalStatistics stats = calculateGlobalStatistics();
            String avgLengthStr = String.format("%.1f", stats.avgLength());
            String avgGCStr = String.format("%.2f", stats.avgGC());
            log.info("Comprimento médio: {} nucleotídeos", avgLengthStr);
            log.info("GC content médio: {}%", avgGCStr);
            log.info("Total de ORFs: {}", stats.totalORFs());
        }
    }

    private GlobalStatistics calculateGlobalStatistics() {
        if (allMetrics.isEmpty()) {
            return new GlobalStatistics(0.0, 0.0, 0, 0, 0);
        }
        double avgLength = allMetrics.stream().mapToInt(SequenceMetrics::getLength).average().orElse(0.0);
        double avgGC = allMetrics.stream().mapToDouble(SequenceMetrics::getGcContent).average().orElse(0.0);
        int totalORFs = allMetrics.stream().mapToInt(SequenceMetrics::getOrfCount).sum();
        int totalMutations = allMetrics.stream().mapToInt(SequenceMetrics::getMutationCount).sum();
        int totalLength = allMetrics.stream().mapToInt(SequenceMetrics::getLength).sum();
        return new GlobalStatistics(avgLength, avgGC, totalORFs, totalMutations, totalLength);
    }

    // Getters
    public List<SequenceMetrics> getAllMetrics() {
        return new ArrayList<>(allMetrics);
    }

}
