package br.com.webmatte;

public class App {
    public static void main(String[] args) throws Exception {
        // Inicia o analisador FASTA completo
        FastaAnalyzer analyzer = new FastaAnalyzer();

        // Carrega o arquivo (caminho atualizado)
        analyzer.loadFromFile("src/NG_005905.2.fna");

        // Executa análise completa
        analyzer.analyzeAll();

        // Exporta resultados
        analyzer.exportMetrics("fasta_analysis_results");
    }
}
