import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Project3 {

    public static void main (String[] args) throws IOException, IRException {
        //files
        File inDir = new File(args[0]);
        File[] inFiles = inDir.listFiles();
        int numFiles = inFiles.length;
        File outDir = new File(args[1]);

        TokenCollector collector = new TokenCollector(); //stores tokens and frequencies across documents
        Map<Integer, String> fileNames = new HashMap<Integer, String>(); //Maps docId to filename

        InvertedFileBuilder invertedFileBuilder = new InvertedFileBuilder(); //Builds the inverted file

        //Process each file
        int fileCounter = 0;
        for(File inFile : inFiles){
            fileNames.put(fileCounter, inFile.getName());
            DocumentAnalyzer analyzer = new DocumentAnalyzer(); //processes and tokenizes the document
            Map<String, Integer> freqs = analyzer.tokenize(inFile.getAbsolutePath()); //tokenize the doc
            collector.addTokens(freqs);

            //Weight with RTF
            for (Map.Entry<String, Integer> entry : freqs.entrySet()){
                float rtf = invertedFileBuilder.calculateRtf(entry.getValue(), analyzer.getNumNonUniqueTokens());
                invertedFileBuilder.addEntry(entry.getKey(), fileCounter, rtf);
            }
            fileCounter++;
        }

        invertedFileBuilder.printToFiles(outDir.getAbsolutePath().concat("\\dict.txt"), outDir.getAbsolutePath().concat("\\post.txt")); //write dict and post
        Project2.writeMapToFile(fileNames, outDir.getAbsolutePath().concat("\\names.txt")); //write docId-names file


        System.out.println("Number of files: " + numFiles);
        System.out.println("Number of unique tokens: " + collector.getUniqueTokens());
        System.out.println("Number of non-unique tokens: " + collector.getNonuniqueTokens());
    }
}
