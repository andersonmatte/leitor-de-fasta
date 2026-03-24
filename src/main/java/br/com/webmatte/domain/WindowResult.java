package br.com.webmatte.domain;

public class WindowResult {

    private final int startPosition;
    private final int endPosition;
    private final String windowSequence;
    private final int length;
    private double gcContent;
    private int aCount;
    private int tCount;
    private int cCount;
    private int gCount;

    public WindowResult(int startPosition, int endPosition, String windowSequence) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.windowSequence = windowSequence;
        this.length = windowSequence.length();
        calculateStats();
    }

    private void calculateStats() {
        aCount = tCount = cCount = gCount = 0;

        for (char nucleotide : windowSequence.toCharArray()) {
            switch (nucleotide) {
                case 'A':
                    aCount++;
                    break;
                case 'T':
                    tCount++;
                    break;
                case 'C':
                    cCount++;
                    break;
                case 'G':
                    gCount++;
                    break;
                default:
                    // Handle unexpected characters - could log or count them
                    break;
            }
        }

        int totalValid = aCount + tCount + cCount + gCount;
        if (totalValid > 0) {
            gcContent = (double) (cCount + gCount) / totalValid * 100.0;
        } else {
            gcContent = 0.0;
        }
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public String getWindowSequence() {
        return windowSequence;
    }

    public double getGcContent() {
        return gcContent;
    }

    public int getLength() {
        return length;
    }

    public int getACount() {
        return aCount;
    }

    public int getTCount() {
        return tCount;
    }

    public int getCCount() {
        return cCount;
    }

    public int getGCount() {
        return gCount;
    }

    @Override
    public String toString() {
        return String.format("Posição %d-%d: GC=%.1f%% (A:%d T:%d C:%d G:%d)",
                startPosition, endPosition, gcContent,
                aCount, tCount, cCount, gCount);
    }

}
