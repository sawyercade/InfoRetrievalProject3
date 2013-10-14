import java.io.File;
import java.io.IOException;
import java.util.*;

public class Project3 {

    final static String tokensSortedByTermFilename = "sortedByToken.txt";
    final static String tokensSortedByFreqFilename = "sortedByFreq.txt";

    public static void main (String[] args) throws IOException, IRException {
        run(args);
    }

    public static void run(String[] args) throws IOException, IRException{
        long runTime = System.currentTimeMillis();
        //files
        File inDir = new File(args[0]);
        File[] inFiles = inDir.listFiles();
        int numFiles = inFiles.length;
        File outDir = new File(args[1]);
        TokenCollector collector = new TokenCollector(); //stores tokens and localHashTable across documents
        Map<Integer, String> fileNames = new HashMap<Integer, String>(); //Maps docId to filename

        InvertedFileBuilder invertedFileBuilder = new InvertedFileBuilder(); //Builds the inverted file

        long processTime = System.currentTimeMillis();
        long rtfTime = 0;
        //Process each file
        int fileCounter = 0;
        for(File inFile : inFiles){
            fileNames.put(fileCounter, inFile.getName());
            DocumentAnalyzer analyzer = new DocumentAnalyzer(); //processes and tokenizes the document
            LocalHashTable localHashTable = analyzer.tokenize(inFile.getAbsolutePath()); //tokenize the doc
            collector.addTokens(localHashTable.getFrequencies());

            //Weight with RTF
            for (Map.Entry<String, Integer> entry : localHashTable){
                float rtf = localHashTable.getRtf(entry.getKey());
                invertedFileBuilder.addEntry(entry.getKey(), fileCounter, rtf);
            }
            fileCounter++;
        }

        //prune low frequency and low length terms
        Pruner pruner = new Pruner(collector, invertedFileBuilder);
        pruner.prune(2, 2);

        processTime = System.currentTimeMillis() - processTime;

        Project1.writeSortedTokens(collector.sortFrequenciesByFreq(), outDir.getAbsolutePath()+"/"+ tokensSortedByFreqFilename);

        long writeTime = System.currentTimeMillis();
        invertedFileBuilder.printToFiles(outDir.getAbsolutePath().concat("\\dict.txt"), outDir.getAbsolutePath().concat("\\post.txt")); //write dict and post
        Project2.writeMapToFile(fileNames, outDir.getAbsolutePath().concat("\\names.txt")); //write docId-names file
        writeTime = System.currentTimeMillis() - writeTime;

        runTime = System.currentTimeMillis() - runTime;

        System.out.println("Number of files: " + (numFiles+1));
        System.out.println("Number of unique tokens: " + collector.getUniqueTokens());
        System.out.println("Number of non-unique tokens: " + collector.getNonuniqueTokens());
        System.out.println("Runtime of tokenizing and building in memory: " + processTime + "ms");
        System.out.println("Runtime of writing dict, post, and names to file (includes calculating posting location for each dict record): " + writeTime + "ms");
        System.out.println("RTF calculation time: " + rtfTime + "ms");
        System.out.println("Total runtime: " + runTime +"ms");
    }

    public static void runTrials (String[] args) throws IOException, IRException{

        //files
        File inDir = new File(args[0]);
        File[] inFiles = inDir.listFiles();
        int numFiles = inFiles.length;
        File outDir = new File(args[1]);
        TokenCollector collector = new TokenCollector(); //stores tokens and localHashTable across documents
        Map<Integer, String> fileNames = new HashMap<Integer, String>(); //Maps docId to filename

        InvertedFileBuilder invertedFileBuilder = new InvertedFileBuilder(); //Builds the inverted file
        StatsWriter statsWriter = new StatsWriter(new File(outDir.getAbsolutePath()+"\\stats.txt"));
        Random random = new Random(System.currentTimeMillis());
        //Process each file
        for (int i = 0; i < 50; i++){
            long runTime = System.currentTimeMillis();
            int fileCounter = 0;
            List<File> sampleFiles = new ArrayList<File>();
            for (int j = 0; j < (i+1)*10; j++){
                sampleFiles.add(inFiles[random.nextInt(505)]);
            }
            for(File inFile : sampleFiles){
                fileNames.put(fileCounter, inFile.getName());
                DocumentAnalyzer analyzer = new DocumentAnalyzer(); //processes and tokenizes the document
                LocalHashTable localHashTable = analyzer.tokenize(inFile.getAbsolutePath()); //tokenize the doc
                collector.addTokens(localHashTable.getFrequencies());

                //Weight with RTF
                for (Map.Entry<String, Integer> entry : localHashTable){
                    float rtf = localHashTable.getRtf(entry.getKey());
                    invertedFileBuilder.addEntry(entry.getKey(), fileCounter, rtf);
                }
                fileCounter++;
            }

            //prune
            Pruner pruner = new Pruner(collector, invertedFileBuilder);
            pruner.prune(2, 2);

            //Running 50 trials, too much File IO, so we're not actually writing dict, post, and names.
            //invertedFileBuilder.printToFiles(outDir.getAbsolutePath().concat("\\dict.txt"), outDir.getAbsolutePath().concat("\\post.txt")); //write dict and post
            //Project2.writeMapToFile(fileNames, outDir.getAbsolutePath().concat("\\names.txt")); //write docId-names file

            runTime = System.currentTimeMillis() - runTime;
            statsWriter.writeEntry(((i+1)*10), collector.getNonuniqueTokens(), collector.getUniqueTokens(), runTime);
            System.out.println("Files:\t" + ((i+1)*10) +"\tRuntime:\t" + runTime);
        }

        statsWriter.close();
    }
}
