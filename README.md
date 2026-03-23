# 🧬 Leitor de FASTA - Analisador de Sequências Biológicas

Um analisador completo de arquivos FASTA desenvolvido em Java que implementa as principais funcionalidades de bioinformática para análise de sequências de DNA.

## 📋 Funcionalidades

### 🎯 Análises Implementadas

1. **📏 Estatísticas Básicas da Sequência**
   - Tamanho total da sequência
   - Contagem de nucleotídeos (A, T, C, G, N)
   - GC Content (percentual de Guanina + Citosina)
   - Frequência relativa de cada nucleotídeo

2. **🔍 Validação e Limpeza de Dados**
   - Detecção de caracteres inválidos
   - Identificação de nucleotídeos ambíguos (N, R, Y, S, W, K, M, B, D, H, V)
   - Limpeza automática da sequência
   - Relatório de qualidade dos dados

3. **🧬 Tradução DNA → Proteína**
   - Tabela completa de códons genéticos
   - Tradução em múltiplos frames de leitura
   - Análise de aminoácidos resultantes
   - Identificação de códons de parada

4. **🧭 Busca por Padrões (Motifs)**
   - Start codons (ATG)
   - Stop codons (TAA, TAG, TGA)
   - Regiões promotoras (TATA box, CAAT box, GC box)
   - Sites de restrição (EcoRI, BamHI, HindIII, NotI)
   - Busca customizada de qualquer motif

5. **🧪 Detecção de ORFs (Open Reading Frames)**
   - Análise nos 6 frames de leitura (3 positivos, 3 negativos)
   - Configuração de tamanho mínimo de ORF
   - Identificação de ORFs completos e incompletos
   - Tradução automática para proteínas

6. **📊 Análise de Distribuição (Sliding Window)**
   - GC content por região
   - Identificação de hotspots de mutação
   - Análise de estabilidade genômica
   - Detecção de regiões de interesse

7. **🧬 Detecção de Mutações**
   - Identificação de SNPs (Single Nucleotide Polymorphisms)
   - Detecção de inserções e deleções
   - Comparação entre sequências
   - Análise de padrões de mutação

8. **📈 Exportação de Resultados**
   - Exportação CSV para análise em planilhas
   - Exportação JSON para integração com sistemas
   - Relatório detalhado em formato texto
   - Métricas consolidadas

## 🚀 Como Usar

### Pré-requisitos

- Java 17 ou superior
- Maven 3.6 ou superior

### Instalação e Compilação

```bash
# Clone o repositório
git clone <repositorio-do-projeto>
cd leitor-de-fasta

# Compile o projeto
mvn compile

# Execute a análise
mvn exec:java -Dexec.mainClass="br.com.webmatte.App"
```

### Modo de Uso

#### 1. Modo Interativo (Recomendado)

```bash
java -cp "target/classes;dependencias/*" br.com.webmatte.FastaAnalyzer
```

O programa apresentará um menu com as seguintes opções:
- Listar sequências carregadas
- Analisar todas as sequências
- Analisar sequência específica
- Comparar duas sequências
- Exportar métricas
- Sair

#### 2. Modo Linha de Comando

```bash
# Análise completa
java -cp "target/classes;dependencias/*" br.com.webmatte.FastaAnalyzer arquivo.fasta --all

# Análise com exportação
java -cp "target/classes;dependencias/*" br.com.webmatte.FastaAnalyzer arquivo.fasta --all resultados
```

#### 3. Usando a Classe Principal

```java
// Para uso programático
FastaAnalyzer analyzer = new FastaAnalyzer();
analyzer.loadFromFile("arquivo.fasta");
analyzer.analyzeAll();
analyzer.exportMetrics("resultados");
```

## 📁 Estrutura do Projeto

```
src/main/java/br/com/webmatte/
├── App.java                    # Ponto de entrada principal
├── FastaAnalyzer.java          # Classe principal orquestradora
├── SequenceStats.java          # Estatísticas básicas
├── SequenceValidator.java      # Validação e limpeza
├── ProteinTranslator.java     # Tradução DNA→Proteína
├── MotifFinder.java           # Busca de padrões
├── ORFDetector.java          # Detecção de ORFs
├── SlidingWindowAnalyzer.java # Análise por janelas
├── MutationDetector.java      # Detecção de mutações
└── MetricsExporter.java       # Exportação de resultados
```

## 📊 Formatos de Saída

### CSV
```csv
Sequence_ID,Length,A_Count,T_Count,C_Count,G_Count,N_Count,GC_Content,ORF_Count,Start_Codon_Count,Stop_Codon_Count,Avg_Protein_Length,Mutation_Count,Ambiguous_Percentage
NG_005905.2,193689,53732,52696,43262,43999,0,45.05,61,2890,9339,140.2,0,0.00
```

### JSON
```json
{
  "analysis_date": "2026-03-23",
  "total_sequences": 1,
  "sequences": [
    {
      "sequence_id": "NG_005905.2",
      "length": 193689,
      "nucleotide_counts": {
        "A": 53732, "T": 52696, "C": 43262, "G": 43999, "N": 0
      },
      "gc_content": 45.05,
      "orf_count": 61,
      "start_codon_count": 2890,
      "stop_codon_count": 9339,
      "avg_protein_length": 140.2
    }
  ],
  "global_statistics": {
    "average_length": 193689.0,
    "average_gc_content": 45.05,
    "total_orfs": 61,
    "total_mutations": 0
  }
}
```

## 🧪 Exemplo de Análise

### Arquivo de Entrada (example.fasta)
```
>NG_005905.2 Homo sapiens BRCA1 DNA repair associated
ATGGATTTATCTGCTCTTCGCGTTGAAGAAGTACAAAATGTCATTAATGCTATGC...
```

### Saída da Análise
```
=== ESTATÍSTICAS BÁSICAS DA SEQUÊNCIA ===
Sequence ID: NG_005905.2
Length: 193689

Contagem de Nucleotídeos:
A: 53732
T: 52696
C: 43262
G: 43999
N (ambíguos): 0

GC Content: 45.0%

=== DETECÇÃO DE ORFs ===
Total de ORFs encontrados: 61
Maiores ORFs:
  ORF POSITIVE_3: posição 123341-126652 (3312 nt, 1104 aa) [completo]
  ORF NEGATIVE_2: posição 72449-73027 (579 nt, 193 aa) [completo]
```

## 🔧 Configuração

### Parâmetros Configuráveis

- **Tamanho mínimo de ORF**: Padrão 300 nucleotídeos (100 aminoácidos)
- **Tamanho da janela Sliding Window**: Padrão 500 nucleotídeos
- **Tamanho do passo**: Padrão 100 nucleotídeos
- **Thresholds de GC Content**: Alto >60%, Baixo <30%

### Personalização

```java
// Configurar tamanho mínimo de ORF
ORFDetector orfDetector = new ORFDetector(sequence, 600); // 600 nucleotídeos

// Configurar sliding window
SlidingWindowAnalyzer windowAnalyzer = new SlidingWindowAnalyzer(sequence, 1000, 200);
```

## 📚 Conceitos Biológicos

### GC Content
Percentual de Guanina e Citosina na sequência. Importante para:
- Estabilidade do DNA
- Características do organismo
- Regiões codificantes

### ORF (Open Reading Frame)
Região que pode ser traduzida em proteína:
- Começa com códon de iniciação (ATG)
- Termina com códon de parada (TAA, TAG, TGA)
- Múltiplos frames de leitura

### Motifs
Padrões de sequência com função biológica:
- **ATG**: Start codon
- **TATAAA**: TATA box (promotor)
- **GAATTC**: Site de restrição EcoRI

## 🐛 Troubleshooting

### Problemas Comuns

1. **Arquivo não encontrado**
   ```
   FileNotFoundException: arquivo.fasta
   ```
   **Solução**: Verifique o caminho do arquivo e use caminhos absolutos se necessário.

2. **Memória insuficiente**
   ```
   OutOfMemoryError: Java heap space
   ```
   **Solução**: Aumente a memória JVM:
   ```bash
   java -Xmx4g -cp "target/classes;dependencias/*" br.com.webmatte.FastaAnalyzer
   ```

3. **Sequência muito grande**
   Para arquivos >100MB, considere processar em partes ou aumentar o tamanho da janela.

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está licenciado sob a MIT License - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 📖 Referências

- [BioJava Documentation](https://biojava.org/)
- [NCBI FASTA Format](https://www.ncbi.nlm.nih.gov/BLAST/fasta.shtml)
- [Genetic Code Table](https://www.ncbi.nlm.nih.gov/Taxonomy/Utils/wprintgc.cgi)

## 📊 Performance

### Tempos de Execução Típicos
- **Pequenas sequências** (<10KB): <1 segundo
- **Sequências médias** (10KB-1MB): 1-10 segundos
- **Grandes genomas** (>1MB): 10-60 segundos

### Uso de Memória
- **Base**: ~50MB
- **Por 100KB de sequência**: +10MB
- **Sliding Window**: +20-50MB dependendo do tamanho

---

**Desenvolvido para análise profissional de dados genômicos** 🧬✨
