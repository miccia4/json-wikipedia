package it.uniroma3.wikiparser;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import it.cnr.isti.hpc.wikipedia.cli.MediawikiToJsonCLI;
import it.cnr.isti.hpc.wikipedia.reader.WikipediaArticleReader;

/**
 * Class to Parse the Single XML Page. This is an abstraction for calling
 * Json-WikiPedia created by Diego Ceccarelli
 * 
 * "https://github.com/diegoceccarelli/json-wikipedia"
 * 
 */
public class DumpParser {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory
	    .getLogger(MediawikiToJsonCLI.class);

    private static String dump_wikipedia_path = "/Users/matteo/Work/wikipedia-parsing/test/apple.bz2";
    private static String output_json_path = "/Users/matteo/Work/wikipedia-parsing/output/apple.json";
    private static String lang = "en";

    public static void main(String[] args){
	WikipediaArticleReader reader = new WikipediaArticleReader(dump_wikipedia_path, output_json_path, lang);
	try {
	    reader.start();
	} catch (Exception e) {
	    logger.error("parsing the mediawiki {}", e.toString());
	    System.exit(-1);
	}
	
	try {
	    
	    String json_output = FileUtils.readFileToString(new File(output_json_path), "UTF-8");
	    System.out.println(toPrettyFormat(json_output));
	    
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    /**
     * Convert a JSON string to pretty print version
     * @param jsonString
     * @return
     */
    public static String toPrettyFormat(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);
        return prettyJson;
    }

}