package br.com.webmatte.domain;

public class SequenceMetrics {

    private final String sequenceId;
    private int length;
    private int aCount;
    private int tCount;
    private int cCount;
    private int gCount;
    private int nCount;
    private double gcContent;
    private int orfCount;
    private int startCodonCount;
    private int stopCodonCount;
    private double avgProteinLength;
    private int mutationCount;
    private double ambiguousPercentage;

    public SequenceMetrics(String sequenceId) {
        this.sequenceId = sequenceId;
        this.length = 0;
        this.aCount = this.tCount = this.cCount = this.gCount = this.nCount = 0;
        this.gcContent = 0.0;
        this.orfCount = 0;
        this.startCodonCount = 0;
        this.stopCodonCount = 0;
        this.avgProteinLength = 0.0;
        this.mutationCount = 0;
        this.ambiguousPercentage = 0.0;
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getACount() {
        return aCount;
    }

    public void setACount(int aCount) {
        this.aCount = aCount;
    }

    public int getTCount() {
        return tCount;
    }

    public void setTCount(int tCount) {
        this.tCount = tCount;
    }

    public int getCCount() {
        return cCount;
    }

    public void setCCount(int cCount) {
        this.cCount = cCount;
    }

    public int getGCount() {
        return gCount;
    }

    public void setGCount(int gCount) {
        this.gCount = gCount;
    }

    public int getNCount() {
        return nCount;
    }

    public void setNCount(int nCount) {
        this.nCount = nCount;
    }

    public double getGcContent() {
        return gcContent;
    }

    public void setGcContent(double gcContent) {
        this.gcContent = gcContent;
    }

    public int getOrfCount() {
        return orfCount;
    }

    public void setOrfCount(int orfCount) {
        this.orfCount = orfCount;
    }

    public int getStartCodonCount() {
        return startCodonCount;
    }

    public void setStartCodonCount(int startCodonCount) {
        this.startCodonCount = startCodonCount;
    }

    public int getStopCodonCount() {
        return stopCodonCount;
    }

    public void setStopCodonCount(int stopCodonCount) {
        this.stopCodonCount = stopCodonCount;
    }

    public double getAvgProteinLength() {
        return avgProteinLength;
    }

    public void setAvgProteinLength(double avgProteinLength) {
        this.avgProteinLength = avgProteinLength;
    }

    public int getMutationCount() {
        return mutationCount;
    }

    public void setMutationCount(int mutationCount) {
        this.mutationCount = mutationCount;
    }

    public double getAmbiguousPercentage() {
        return ambiguousPercentage;
    }

    public void setAmbiguousPercentage(double ambiguousPercentage) {
        this.ambiguousPercentage = ambiguousPercentage;
    }

}
