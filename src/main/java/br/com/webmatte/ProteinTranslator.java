package br.com.webmatte;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ProteinTranslator {
    private static final Logger log = LoggerFactory.getLogger(ProteinTranslator.class);
    private static final Map<String, Character> CODON_TABLE = new HashMap<>();

    static {
        // Tabela de códons padrão (tabela de tradução 1 - nuclear)
        initializeCodonTable();
    }

    private String dnaSequence;
    private String proteinSequence;
    private int validCodons;
    private int invalidCodons;
    public ProteinTranslator(String dnaSequence) {
        this.dnaSequence = dnaSequence.toUpperCase();
        this.validCodons = 0;
        this.invalidCodons = 0;
        translate();
    }

    private static void initializeCodonTable() {
        // Fenilalanina
        CODON_TABLE.put("TTT", 'F');
        CODON_TABLE.put("TTC", 'F');

        // Leucina
        CODON_TABLE.put("TTA", 'L');
        CODON_TABLE.put("TTG", 'L');
        CODON_TABLE.put("CTT", 'L');
        CODON_TABLE.put("CTC", 'L');
        CODON_TABLE.put("CTA", 'L');
        CODON_TABLE.put("CTG", 'L');

        // Isoleucina
        CODON_TABLE.put("ATT", 'I');
        CODON_TABLE.put("ATC", 'I');
        CODON_TABLE.put("ATA", 'I');

        // Metionina (Start)
        CODON_TABLE.put("ATG", 'M');

        // Valina
        CODON_TABLE.put("GTT", 'V');
        CODON_TABLE.put("GTC", 'V');
        CODON_TABLE.put("GTA", 'V');
        CODON_TABLE.put("GTG", 'V');

        // Serina
        CODON_TABLE.put("TCT", 'S');
        CODON_TABLE.put("TCC", 'S');
        CODON_TABLE.put("TCA", 'S');
        CODON_TABLE.put("TCG", 'S');
        CODON_TABLE.put("AGT", 'S');
        CODON_TABLE.put("AGC", 'S');

        // Prolina
        CODON_TABLE.put("CCT", 'P');
        CODON_TABLE.put("CCC", 'P');
        CODON_TABLE.put("CCA", 'P');
        CODON_TABLE.put("CCG", 'P');

        // Treonina
        CODON_TABLE.put("ACT", 'T');
        CODON_TABLE.put("ACC", 'T');
        CODON_TABLE.put("ACA", 'T');
        CODON_TABLE.put("ACG", 'T');

        // Alanina
        CODON_TABLE.put("GCT", 'A');
        CODON_TABLE.put("GCC", 'A');
        CODON_TABLE.put("GCA", 'A');
        CODON_TABLE.put("GCG", 'A');

        // Tirosina
        CODON_TABLE.put("TAT", 'Y');
        CODON_TABLE.put("TAC", 'Y');

        // Stop
        CODON_TABLE.put("TAA", '*');
        CODON_TABLE.put("TAG", '*');

        // Histidina
        CODON_TABLE.put("CAT", 'H');
        CODON_TABLE.put("CAC", 'H');

        // Glutamina
        CODON_TABLE.put("CAA", 'Q');
        CODON_TABLE.put("CAG", 'Q');

        // Asparagina
        CODON_TABLE.put("AAT", 'N');
        CODON_TABLE.put("AAC", 'N');

        // Lisina
        CODON_TABLE.put("AAA", 'K');
        CODON_TABLE.put("AAG", 'K');

        // Ácido Aspártico
        CODON_TABLE.put("GAT", 'D');
        CODON_TABLE.put("GAC", 'D');

        // Ácido Glutâmico
        CODON_TABLE.put("GAA", 'E');
        CODON_TABLE.put("GAG", 'E');

        // Cisteína
        CODON_TABLE.put("TGT", 'C');
        CODON_TABLE.put("TGC", 'C');

        // Stop
        CODON_TABLE.put("TGA", '*');

        // Triptofano
        CODON_TABLE.put("TGG", 'W');

        // Arginina
        CODON_TABLE.put("CGT", 'R');
        CODON_TABLE.put("CGC", 'R');
        CODON_TABLE.put("CGA", 'R');
        CODON_TABLE.put("CGG", 'R');
        CODON_TABLE.put("AGA", 'R');
        CODON_TABLE.put("AGG", 'R');

        // Glicina
        CODON_TABLE.put("GGT", 'G');
        CODON_TABLE.put("GGC", 'G');
        CODON_TABLE.put("GGA", 'G');
        CODON_TABLE.put("GGG", 'G');
    }

    // Método estático para tradução rápida
    public static String translateQuick(String dnaSequence) {
        ProteinTranslator translator = new ProteinTranslator(dnaSequence);
        return translator.getProteinSequence();
    }

    private void translate() {
        StringBuilder protein = new StringBuilder();

        // Processa apenas códons completos
        for (int i = 0; i <= dnaSequence.length() - 3; i += 3) {
            String codon = dnaSequence.substring(i, i + 3);

            // Verifica se o códon contém apenas nucleotídeos válidos
            if (isValidCodon(codon)) {
                Character aminoAcid = CODON_TABLE.get(codon);
                if (aminoAcid != null) {
                    protein.append(aminoAcid);
                    validCodons++;
                } else {
                    protein.append('X'); // Desconhecido
                    invalidCodons++;
                }
            } else {
                protein.append('X'); // Códon inválido
                invalidCodons++;
            }
        }

        this.proteinSequence = protein.toString();
    }

    private boolean isValidCodon(String codon) {
        if (codon.length() != 3) return false;

        for (char c : codon.toCharArray()) {
            if (c != 'A' && c != 'T' && c != 'C' && c != 'G') {
                return false;
            }
        }
        return true;
    }

    public void printTranslationReport() {
        log.info("=== RELATÓRIO DE TRADUÇÃO DNA → PROTEÍNA ===");
        log.info("Tamanho DNA: {}", dnaSequence.length());
        log.info("Códons processados: {}", (validCodons + invalidCodons));
        log.info("Códons válidos: {}", validCodons);
        log.info("Códons inválidos: {}", invalidCodons);
        log.info("Tamanho da proteína: {}", proteinSequence.length());
        log.info("");

        if (proteinSequence.length() <= 100) {
            log.info("Sequência proteica:");
            log.info(proteinSequence);
            log.info("");
        } else {
            log.info("Sequência proteica (primeiros 100 aa):");
            log.info(proteinSequence.substring(0, 100) + "...");
            log.info("");
        }

        // Análise da proteína
        analyzeProtein();
    }

    private void analyzeProtein() {
        Map<Character, Integer> aminoAcidCounts = new HashMap<>();
        int stopCodons = 0;

        for (char aa : proteinSequence.toCharArray()) {
            if (aa == '*') {
                stopCodons++;
            } else {
                aminoAcidCounts.put(aa, aminoAcidCounts.getOrDefault(aa, 0) + 1);
            }
        }

        log.info("Análise da proteína:");
        log.info("Códons de parada (*): {}", stopCodons);

        if (!aminoAcidCounts.isEmpty()) {
            log.info("Aminoácidos mais frequentes:");
            aminoAcidCounts.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(5)
                    .forEach(entry -> log.info("  {}: {}", entry.getKey(), entry.getValue()));
        }
        log.info("");
    }

    public String getProteinSequence() {
        return proteinSequence;
    }

    public int getValidCodons() {
        return validCodons;
    }

    public int getInvalidCodons() {
        return invalidCodons;
    }
}
