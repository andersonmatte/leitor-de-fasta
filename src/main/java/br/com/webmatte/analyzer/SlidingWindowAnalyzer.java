package br.com.webmatte.analyzer;

import br.com.webmatte.domain.WindowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SlidingWindowAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(SlidingWindowAnalyzer.class);
    private final String sequence;
    private final int windowSize;
    private final int stepSize;
    private final List<WindowResult> results;

    public SlidingWindowAnalyzer(String sequence, int windowSize, int stepSize) {
        this.sequence = sequence.toUpperCase();
        this.windowSize = windowSize;
        this.stepSize = stepSize;
        this.results = new ArrayList<>();
        analyze();
    }

    // Análise rápida
    public static void analyzeSequence(String sequence, int windowSize) {
        SlidingWindowAnalyzer analyzer = new SlidingWindowAnalyzer(sequence, windowSize, windowSize / 2);
        analyzer.printSlidingWindowReport();
    }

    private void analyze() {
        for (int i = 0; i <= sequence.length() - windowSize; i += stepSize) {
            int endPos = Math.min(i + windowSize, sequence.length());
            String windowSeq = sequence.substring(i, endPos);
            // Apenas processa janelas com nucleotídeos válidos
            if (containsValidNucleotides(windowSeq)) {
                results.add(new WindowResult(i, endPos - 1, windowSeq));
            }
        }
    }

    private boolean containsValidNucleotides(String windowSeq) {
        for (char c : windowSeq.toCharArray()) {
            if (c == 'A' || c == 'T' || c == 'C' || c == 'G') {
                return true;
            }
        }
        return false;
    }

    public void printSlidingWindowReport() {
        log.info("=== ANÁLISE DE SLIDING WINDOW ===");
        log.info("Tamanho da sequência: {}", sequence.length());
        log.info("Tamanho da janela: {}", windowSize);
        log.info("Tamanho do passo: {}", stepSize);
        log.info("Janelas analisadas: {}", results.size());
        if (results.isEmpty()) {
            log.info("Nenhuma janela válida encontrada para análise.");
            return;
        }
        // Estatísticas gerais
        double avgGC = results.stream().mapToDouble(WindowResult::getGcContent).average().orElse(0.0);
        double maxGC = results.stream().mapToDouble(WindowResult::getGcContent).max().orElse(0.0);
        double minGC = results.stream().mapToDouble(WindowResult::getGcContent).min().orElse(0.0);
        log.info("Estatísticas de GC Content:");
        log.info("Média: {}%", avgGC);
        log.info("Máximo: {}%", maxGC);
        log.info("Mínimo: {}%", minGC);
        log.info("Variação: {}%", maxGC - minGC);
        // Regiões de interesse
        findRegionsOfInterest();
        // Primeiras janelas (amostra)
        log.info("Amostra de janelas (primeiras 10):");
        for (int i = 0; i < Math.min(10, results.size()); i++) {
            log.info("{}. {}", (i + 1), results.get(i));
        }
        if (results.size() > 10) {
            log.info("... ({} janelas adicionais)", (results.size() - 10));
        }
    }

    private void findRegionsOfInterest() {
        log.info("Regiões de interesse:");
        // Encontra regiões com GC alto (> 60%)
        List<WindowResult> highGCRegions = new ArrayList<>();
        List<WindowResult> lowGCRegions = new ArrayList<>();
        for (WindowResult result : results) {
            if (result.getGcContent() > 60.0) {
                highGCRegions.add(result);
            } else if (result.getGcContent() < 30.0) {
                lowGCRegions.add(result);
            }
        }
        log.info("Regiões com GC alto (>60%): {}", highGCRegions.size());
        isHighGCRegions(highGCRegions);

        log.info("Regiões com GC baixo (<30%): {}", lowGCRegions.size());
        isHighGCRegions(lowGCRegions);
        log.info("");
    }

    private void isHighGCRegions(List<WindowResult> highGCRegions) {
        if (!highGCRegions.isEmpty()) {
            log.info("  Exemplos:");
            for (int i = 0; i < Math.min(3, highGCRegions.size()); i++) {
                WindowResult result = highGCRegions.get(i);
                log.info("    Posição {}-{}: {}%",
                        result.getStartPosition(), result.getEndPosition(), result.getGcContent());
            }
        }
    }

    // Encontrar regiões estáveis vs instáveis
    public void findStabilityRegions() {
        log.info("=== ANÁLISE DE ESTABILIDADE ===");
        double avgGC = results.stream().mapToDouble(WindowResult::getGcContent).average().orElse(0.0);
        List<WindowResult> stableRegions = new ArrayList<>();
        List<WindowResult> unstableRegions = new ArrayList<>();
        for (WindowResult result : results) {
            double deviation = Math.abs(result.getGcContent() - avgGC);
            if (deviation < 5.0) {
                stableRegions.add(result);
            } else if (deviation > 15.0) {
                unstableRegions.add(result);
            }
        }
        log.info("GC médio global: {}%", avgGC);
        log.info("Regiões estáveis (desvio < 5%): {}", stableRegions.size());
        log.info("Regiões instáveis (desvio > 15%): {}", unstableRegions.size());
        if (!unstableRegions.isEmpty()) {
            log.info("Regiões instáveis (potenciais hotspots):");
            for (int i = 0; i < Math.min(5, unstableRegions.size()); i++) {
                WindowResult result = unstableRegions.get(i);
                double deviation = Math.abs(result.getGcContent() - avgGC);
                log.info("  Posição {}-{}: {}% (desvio: {}%)",
                        result.getStartPosition(), result.getEndPosition(),
                        result.getGcContent(), deviation);
            }
        }
        log.info("");
    }

    // Detectar padrões de composição
    public void detectCompositionPatterns() {
        log.info("=== PADRÕES DE COMPOSIÇÃO ===");
        // Analisa variação ao longo da sequência
        if (results.size() < 2) return;
        List<Double> gcValues = new ArrayList<>();
        for (WindowResult result : results) {
            gcValues.add(result.getGcContent());
        }
        // Detecta tendências
        int increasingCount = 0;
        int decreasingCount = 0;
        for (int i = 1; i < gcValues.size(); i++) {
            if (gcValues.get(i) > gcValues.get(i - 1)) {
                increasingCount++;
            } else if (gcValues.get(i) < gcValues.get(i - 1)) {
                decreasingCount++;
            }
        }
        log.info("Tendências de variação:");
        log.info("Aumentos: {}", increasingCount);
        log.info("Diminuições: {}", decreasingCount);
        if (increasingCount > decreasingCount * 1.5) {
            log.info("Padrão detectado: Tendência de aumento de GC ao longo da sequência");
        } else if (decreasingCount > increasingCount * 1.5) {
            log.info("Padrão detectado: Tendência de diminuição de GC ao longo da sequência");
        } else {
            log.info("Padrão detectado: Variação estável de GC ao longo da sequência");
        }
    }

    // Getters
    public List<WindowResult> getResults() {
        return new ArrayList<>(results);
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getStepSize() {
        return stepSize;
    }

}
