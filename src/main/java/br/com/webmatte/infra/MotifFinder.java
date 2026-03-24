package br.com.webmatte.infra;

import br.com.webmatte.domain.MotifMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MotifFinder {

    private static final Logger log = LoggerFactory.getLogger(MotifFinder.class);
    private final String sequence;
    private final List<MotifMatch> matches;

    public MotifFinder(String sequence) {
        this.sequence = sequence.toUpperCase();
        this.matches = new ArrayList<>();
    }

    public List<MotifMatch> findMotif(String motif) {
        List<MotifMatch> results = new ArrayList<>();
        String searchMotif = motif.toUpperCase();
        // Usa regex para encontrar todas as ocorrências
        Pattern pattern = Pattern.compile(searchMotif);
        Matcher matcher = pattern.matcher(sequence);
        while (matcher.find()) {
            results.add(new MotifMatch(searchMotif, matcher.start(), sequence));
        }
        return results;
    }

    public void printMotifReport(String motif) {
        List<MotifMatch> motifMatches = findMotif(motif);
        log.info("=== BUSCA POR MOTIF: {} ===", motif);
        log.info("Ocorrências encontradas: {}", motifMatches.size());
        if (motifMatches.isEmpty()) {
            log.info("Nenhuma ocorrência do motif '{}' foi encontrada.", motif);
            return;
        }
        // Análise das ocorrências
        log.info("Posições encontradas:");
        for (int i = 0; i < motifMatches.size(); i++) {
            MotifMatch match = motifMatches.get(i);
            log.info("{}. {}", (i + 1), match);
        }
        // Estatísticas das ocorrências
        analyzeMotifDistribution(motifMatches);
    }

    private void analyzeMotifDistribution(List<MotifMatch> matches) {
        if (matches.isEmpty()) return;
        log.info("Análise de distribuição:");
        // Distância média entre ocorrências
        if (matches.size() > 1) {
            int totalDistance = 0;
            for (int i = 1; i < matches.size(); i++) {
                totalDistance += matches.get(i).getPosition() - matches.get(i - 1).getPosition();
            }
            double avgDistance = (double) totalDistance / (matches.size() - 1);
            if (log.isInfoEnabled()) {
                log.info("Distância média entre ocorrências: {} nucleotídeos", String.format("%.1f", avgDistance));
            }
        }
        // Densidade (ocorrências por 1000 nucleotídeos)
        double density = (double) matches.size() / sequence.length() * 1000;
        if (log.isInfoEnabled()) {
            log.info("Densidade: {} ocorrências por 1000 nucleotídeos", String.format("%.2f", density));
        }
        // Primeira e última ocorrência
        if (!matches.isEmpty()) {
            log.info("Primeira ocorrência: posição {}", matches.get(0).getPosition());
            log.info("Última ocorrência: posição {}", matches.get(matches.size() - 1).getPosition());
        }
    }

    // Métodos para motifs biológicos comuns
    public void findStartCodons() {
        printMotifReport("ATG");
    }

    public void findStopCodons() {
        log.info("=== BUSCA POR CÓDONS DE PARADA ===");
        printMotifReport("TAA");
        printMotifReport("TAG");
        printMotifReport("TGA");
    }

    public void findTATABox() {
        printMotifReport("TATAAA");
    }

    public void findPromoterRegions() {
        log.info("=== BUSCA POR REGIÕES PROMOTORAS ===");
        // TATA box (promotor eucariótico)
        printMotifReport("TATAAA");
        // CAAT box (promotor eucariótico)
        printMotifReport("CAAT");
        // GC box (promotor eucariótico)
        printMotifReport("GGCGGG");
        // Promotor bacteriano (Pribnow box)
        printMotifReport("TATAAT");
    }

    public void findRestrictionSites() {
        log.info("=== SITES DE RESTRIÇÃO COMUNS ===");
        // EcoRI
        printMotifReport("GAATTC");
        // BamHI
        printMotifReport("GGATCC");
        // HindIII
        printMotifReport("AAGCTT");
        // NotI
        printMotifReport("GCGGCCGC");
    }

    // Biscar múltiplos motifs de uma vez
    public void findMultipleMotifs(List<String> motifs) {
        log.info("=== BUSCA POR MÚLTIPLOS MOTIFS ===");
        for (String motif : motifs) {
            List<MotifMatch> motifMatches = findMotif(motif);
            log.info("{}: {} ocorrências", motif, motifMatches.size());
            if (!motifMatches.isEmpty()) {
                log.info("  Posições: ");
                for (int i = 0; i < Math.min(motifMatches.size(), 10); i++) {
                    log.info("{}", motifMatches.get(i).getPosition());
                    if (i < Math.min(motifMatches.size(), 10) - 1) {
                        log.info(", ");
                    }
                }
                if (motifMatches.size() > 10) {
                    log.info(" ... ({} mais)", (motifMatches.size() - 10));
                }
            }
        }
        log.info("");
    }

    public String getSequence() {
        return sequence;
    }

    public List<MotifMatch> getAllMatches() {
        return new ArrayList<>(matches);
    }

}
