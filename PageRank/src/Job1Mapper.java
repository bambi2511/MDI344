/***
 * Class Job1Mapper
 * Job1 Mapper class
 * @author sgarouachi
 */

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


public class Job1Mapper extends Mapper<LongWritable, Text, Text, Text> {
    
	// Init pattern variable
    private static final Pattern linksPattern = Pattern.compile("\\[.+?\\]");

    /**
     * Job1 Map method
     */
    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    	
    	// Get title and text
        String[] titleAndText = parseTitleAndText(value);
        
        // Get page
        String pageString = titleAndText[0];
        
        // Is valid?
        if(notValidPage(pageString)) return;
        
        // Format page
        Text page = new Text(pageString.replace(' ', '_'));
        
        // Init matcher
        Matcher matcher = linksPattern.matcher(titleAndText[1]);
        
        // Loop through the matched links in [CONTENT]
        while (matcher.find()) {
            String otherPage = matcher.group();
            // Filter only wiki pages.
            //- some have [[realPage|linkName]], some single [realPage]
            //- some link to files or external pages.
            //- some link to paragraphs into other pages.
            
            otherPage = getWikiPageFromLink(otherPage);
            
            // Check & continue
            if(otherPage == null || otherPage.isEmpty()) continue;
            
            // Add valid otherPages to the map
            context.write(page, new Text(otherPage));
        }
    }
    
    /**
     * Check if not a valid page (contains :)
     * @param pageString
     * @return Boolean
     */
    private boolean notValidPage(String pageString) {
    	// Return
        return pageString.contains(":");
    }
   
    /**
     * Retrieve wiki page from link
     * @param aLink
     * @return String
     */
    private String getWikiPageFromLink(String aLink){
    	// Is wiki link?
        if(isNotWikiLink(aLink)) return null;
        
        // Init start & endLink
        int start = aLink.startsWith("[[") ? 2 : 1;
        int endLink = aLink.indexOf("]");
        
        // Check pipe position
        int pipePosition = aLink.indexOf("|");
        if(pipePosition > 0){
            endLink = pipePosition;
        }
        
        // Get part
        int part = aLink.indexOf("#");
        if(part > 0){
            endLink = part;
        }
        
        // Parse link
        aLink =  aLink.substring(start, endLink);
        aLink = aLink.replaceAll("\\s", "_");
        aLink = aLink.replaceAll(",", "");
        aLink = sweetify(aLink);
        
        // Return
        return aLink;
    }
    
    /**
     * Format a string
     * @param aLinkText
     * @return String
     */
    private String sweetify(String aLinkText) {
        if(aLinkText.contains("&amp;"))
            return aLinkText.replace("&amp;", "&");
        // Return
        return aLinkText;
    }
    
    /**
     * Parse Title & Text
     * @param value
     * @return 
     * 		String[0] Title
     * 		String[1] Text
     * @throws CharacterCodingException
     */
    private String[] parseTitleAndText(Text value) throws CharacterCodingException {
        // Get value
    	String[] titleAndText = new String[2];
        
    	// Init start/end for title
        int start = value.find("<title>");
        int end = value.find("</title>", start);
        start += 7; //add <title> length.
        
        // Retrieve title
        titleAndText[0] = Text.decode(value.getBytes(), start, end-start);
        
        // Init start/end for text
        start = value.find("<text");
        start = value.find(">", start);
        end = value.find("</text>", start);
        start += 1;
        if(start == -1 || end == -1) {
            return new String[]{"",""};
        }
        
        // Retrieve text
        titleAndText[1] = Text.decode(value.getBytes(), start, end-start);
        
        // Return title & text
        return titleAndText;
    }
    
    /**
     * Check if not a wiki link
     * @param aLink
     * @return Boolean
     */
    private boolean isNotWikiLink(String aLink) {
    	// Init
        int start = 1;
        if(aLink.startsWith("[[")){
            start = 2;
        }
        
        // Not valid
        if( aLink.length() < start+2 || aLink.length() > 100) return true;
        
        // Check first character
        char firstChar = aLink.charAt(start);
        if( firstChar == '#') return true;
        if( firstChar == ',') return true;
        if( firstChar == '.') return true;
        if( firstChar == '&') return true;
        if( firstChar == '\'') return true;
        if( firstChar == '-') return true;
        if( firstChar == '{') return true;
        
        // Check link
        if( aLink.contains(":")) return true; // Matches: external links and translations links
        if( aLink.contains(",")) return true; // Matches: external links and translations links
        if( aLink.contains("&")) return true;
        
        // Return valid
        return false;
    }
}
