package br.com.webmatte;

import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class FastaAnalyzer {

    private static final String SEPARATOR_60 = "=".repeat(60);
    private static final String SEPARATOR_80 = "=".repeat(80);

    private static final Logger log = LoggerFactory.getLogger(FastaAnalyzer.class);
    private final MetricsExporter metricsExporter;
    private LinkedHashMap<String, DNASequence> sequences;

    public FastaAnalyzer() {
        this.sequences = new LinkedHashMap<>();
        this.metricsExporter = new MetricsExporter();
    }

    public static void main(String[] args) {
        try {
            FastaAnalyzer analyzer = new FastaAnalyzer();

            if (args.length > 0) {
                // Modo comando de linha
                String filePath = args[0];
                analyzer.loadFromFile(filePath);

                if (args.length > 1 && args[1].equals("--all")) {
                    analyzer.analyzeAll();
                    if (args.length > 2) {
                        analyzer.exportMetrics(args[2]);
                    }
                } else {
                    analyzer.showMenu();
                }
            } else {
                // Modo interativo
                Scanner scanner = new Scanner(System.in);
                log.info("Digite o caminho do arquivo FASTA: ");
                String filePath = scanner.nextLine();

                analyzer.loadFromFile(filePath);
                analyzer.showMenu();
                scanner.close();
            }

        } catch (Exception e) {
            log.error("Erro durante a execução: {}", e.getMessage(), e);
        }
    }

    public void loadFromFile(String filePath) throws IOException {
        log.info("Carregando arquivo FASTA: {}", filePath);
        File file = new File(filePath);

        if (!file.exists()) {
            throw new IOException("Arquivo não encontrado: " + filePath);
        }

        sequences = FastaReaderHelper.readFastaDNASequence(file);
        log.info("✅ {} sequência(s) carregada(s) com sucesso!", sequences.size());
    }

    public void analyzeAll() {
        if (sequences.isEmpty()) {
            log.info("Nenhuma sequência para analisar.");
            return;
        }

        log.info("=== INICIANDO ANÁLISE COMPLETA FASTA ===");
        log.info("Total de sequências: {}", sequences.size());

        int sequenceCount = 0;

        for (Map.Entry<String, DNASequence> entry : sequences.entrySet()) {

            sequenceCount++;

            String sequenceId = entry.getKey();
            DNASequence dnaSequence = entry.getValue();

            String sequence = dnaSequence
                    .getSequenceAsString()
                    .toUpperCase()
                    .trim();

            log.info("🧬 ANALISANDO SEQUÊNCIA {}/{}: {}",
                    sequenceCount,
                    sequences.size(),
                    sequenceId);

            if (log.isInfoEnabled()) {
                log.info("{}", SEPARATOR_60);
            }

            // 1. Validação e limpeza
            SequenceValidator validator =
                    new SequenceValidator(sequenceId, sequence);

            validator.printValidationReport();

            // 2. Estatísticas básicas
            String cleanedSequence = validator.getCleanedSequence();

            if (cleanedSequence.isEmpty()) {
                log.info("⚠️ Sequência vazia após validação. Pulando análise.");
                continue;
            }

            SequenceStats stats =
                    new SequenceStats(sequenceId, cleanedSequence);

            stats.printBasicStats();

            // 3. Tradução DNA → Proteína
            ProteinTranslator translator =
                    new ProteinTranslator(cleanedSequence);

            translator.printTranslationReport();

            // 4. Busca por motifs
            MotifFinder motifFinder =
                    new MotifFinder(cleanedSequence);

            motifFinder.findStartCodons();

            // 5. Detecção de ORFs
            ORFDetector orfDetector =
                    new ORFDetector(cleanedSequence, 300);

            orfDetector.printORFReport();

            // 6. Sliding window
            if (cleanedSequence.length() > 1000) {

                SlidingWindowAnalyzer windowAnalyzer =
                        new SlidingWindowAnalyzer(
                                cleanedSequence,
                                500,
                                100
                        );

                windowAnalyzer.printSlidingWindowReport();
                windowAnalyzer.findStabilityRegions();
            }

            // 7. Métricas
            MetricsExporter.SequenceMetrics metrics =
                    MetricsExporter.createMetricsFromAnalysis(
                            sequenceId,
                            stats,
                            validator,
                            orfDetector,
                            motifFinder
                    );

            metricsExporter.addSequenceMetrics(metrics);

            extracted(sequenceId);

            if (log.isInfoEnabled()) {
                log.info("\n{}\n", SEPARATOR_80);
            }
        }

        log.info("🎉 ANÁLISE COMPLETA CONCLUÍDA!");

        metricsExporter.printSummary();
    }

    public void analyzeSpecificSequence(String sequenceId) {
        if (!sequences.containsKey(sequenceId)) {
            log.info("Sequência não encontrada: {}", sequenceId);
            return;
        }

        DNASequence dnaSequence = sequences.get(sequenceId);
        String sequence = dnaSequence.getSequenceAsString().toUpperCase().trim();

        log.info("\uD83E\uDDEC ANALISANDO SEQUÊNCIA ESPECÍFICA: {}", sequenceId);
        if (log.isInfoEnabled()) {
            log.info("={}", "=".repeat(50));
        }

        // Análise completa da sequência específica
        SequenceValidator validator = new SequenceValidator(sequenceId, sequence);
        validator.printValidationReport();

        String cleanedSequence = validator.getCleanedSequence();
        if (!cleanedSequence.isEmpty()) {
            SequenceStats stats = new SequenceStats(sequenceId, cleanedSequence);
            stats.printBasicStats();

            ProteinTranslator translator = new ProteinTranslator(cleanedSequence);
            translator.printTranslationReport();

            MotifFinder motifFinder = new MotifFinder(cleanedSequence);
            motifFinder.findStartCodons();
            motifFinder.findStopCodons();

            ORFDetector orfDetector = new ORFDetector(cleanedSequence, 300);
            orfDetector.printORFReport();

            if (cleanedSequence.length() > 1000) {
                SlidingWindowAnalyzer windowAnalyzer = new SlidingWindowAnalyzer(cleanedSequence, 500, 100);
                windowAnalyzer.printSlidingWindowReport();
            }
        }

        extracted(sequenceId);
    }

    public void compareSequences(String seq1Id, String seq2Id) {
        if (!sequences.containsKey(seq1Id) || !sequences.containsKey(seq2Id)) {
            log.info("Uma ou ambas as sequências não foram encontradas.");
            return;
        }

        String seq1 = sequences.get(seq1Id).getSequenceAsString();
        String seq2 = sequences.get(seq2Id).getSequenceAsString();

        MutationDetector.compareSequences(seq1, seq2, seq1Id, seq2Id);
    }

    public void exportMetrics(String baseFilename) throws IOException {
        if (metricsExporter.getAllMetrics().isEmpty()) {
            log.info("Nenhuma métrica para exportar. Execute a análise primeiro.");
            return;
        }

        // Exportar para diferentes formatos
        String csvFilename = baseFilename + ".csv";
        String jsonFilename = baseFilename + ".json";
        String reportFilename = baseFilename + "_report.txt";

        metricsExporter.exportToCSV(csvFilename);
        metricsExporter.exportToJSON(jsonFilename);
        metricsExporter.exportSummaryReport(reportFilename);

        log.info("📊 Todos os arquivos de exportação foram gerados com sucesso!");
        log.info("   - CSV: {}", csvFilename);
        log.info("   - JSON: {}", jsonFilename);
        log.info("   - Relatório: {}", reportFilename);
    }

    public void listSequences() {
        log.info("=== SEQUÊNCIAS CARREGADAS ===");
        if (sequences.isEmpty()) {
            log.info("Nenhuma sequência carregada.");
            return;
        }

        for (Map.Entry<String, DNASequence> entry : sequences.entrySet()) {
            String sequenceId = entry.getKey();
            DNASequence seq = entry.getValue();

            log.info("{}: {} nucleotídeos", sequenceId, seq.getLength());
        }
    }

    public void showMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            log.info("=== MENU DE ANÁLISE FASTA ===");
            log.info("1. Listar sequências");
            log.info("2. Analisar todas as sequências");
            log.info("3. Analisar sequência específica");
            log.info("4. Comparar duas sequências");
            log.info("5. Exportar métricas");
            log.info("6. Sair");
            log.info("Escolha uma opção: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consumir newline

                switch (choice) {
                    case 1:
                        listSequences();
                        break;
                    case 2:
                        analyzeAll();
                        break;
                    case 3:
                        log.info("Digite o ID da sequência: ");
                        String seqId = scanner.nextLine();
                        analyzeSpecificSequence(seqId);
                        break;
                    case 4:
                        log.info("Digite o ID da primeira sequência: ");
                        String seq1Id = scanner.nextLine();
                        log.info("Digite o ID da segunda sequência: ");
                        String seq2Id = scanner.nextLine();
                        compareSequences(seq1Id, seq2Id);
                        break;
                    case 5:
                        log.info("Digite o nome base para os arquivos: ");
                        String filename = scanner.nextLine();
                        exportMetrics(filename);
                        break;
                    case 6:
                        log.info("👋 Encerrando o programa...");
                        scanner.close();
                        return;
                    default:
                        log.info("Opção inválida. Tente novamente.");
                }
            } catch (Exception e) {
                log.info("Erro: {}", e.getMessage());
                scanner.nextLine(); // Limpar buffer
            }
        }
    }

    private static void extracted(String sequenceId) {
        log.info("✅ Análise da sequência {} concluída!", sequenceId);
    }

}
