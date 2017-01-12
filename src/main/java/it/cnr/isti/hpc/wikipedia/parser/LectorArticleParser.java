package it.cnr.isti.hpc.wikipedia.parser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.tudarmstadt.ukp.wikipedia.parser.Content;
import de.tudarmstadt.ukp.wikipedia.parser.Paragraph;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Span;
import it.cnr.isti.hpc.wikipedia.article.Article;
import it.cnr.isti.hpc.wikipedia.article.Link;

/**
 * A parser created to fill the fields that are considered in Lector.
 * 
 * In this class we override all the methods that need to produce a 
 * different output, compared to simple ArticleParser.
 * 
 * 
 * @author matteo
 *
 */
public class LectorArticleParser extends ArticleParser {

    public LectorArticleParser() {
	super();
    }

    public LectorArticleParser(String lang) {
	super(lang);
    }
    
    /**
     * Used to extract both the paragraphs (normally, as before) and the paragraphs with mentions.
     * We are interested in the second ones to:
     * - keep the wikilinks in the text
     * - remove template from the text
     * 
     * @param article
     * @param page
     */
    public void setParagraphs(Article article, ParsedPage page) {
	final List<String> paragraphs = new ArrayList<String>(page.nrOfParagraphs());
	final List<String> paragraphsWithMentions = new ArrayList<String>(page.nrOfParagraphs());
	final List<Link> links = new ArrayList<>();
	int paragraphId = 0;
	for (final Paragraph p : page.getParagraphs()) {
	    String text = p.getText();
	    text = text.replace("\n", " ").trim();
	    if (!text.isEmpty()){
		paragraphs.add(text);
		text = article.removeTemplates(text);
		List<de.tudarmstadt.ukp.wikipedia.parser.Link> linksInParagraph = p.getLinks();
		for(final de.tudarmstadt.ukp.wikipedia.parser.Link t: orderLinksByLength(linksInParagraph).values()){
		    if (t.getType() == de.tudarmstadt.ukp.wikipedia.parser.Link.type.INTERNAL){
			Link currentLink = new Link(t.getTarget(), t.getText(), t.getPos().getStart(), t.getPos().getEnd(), Link.Type.BODY, paragraphId);
			links.add(currentLink);
			// inserted to avoid links in citations
			if (t.getContext(1, 1).contains(" ")){
			    text = replaceLinksInText(text, currentLink);
			}
		    }
		}
		if(!text.isEmpty())
		    paragraphsWithMentions.add(text.trim());
	    }
	    paragraphId++;
	}
	article.setParagraphs(paragraphs);
	article.setParagraphsWithMentions(paragraphsWithMentions);
	updateLinks(article, links);
    }

   /**
    * Replace substring of text with a wikilink (ie. Mention)
    * 
    * @param text
    * @param link
    * @return
    */
    private String replaceLinksInText(String text, Link link){
	for (String subString : text.split("\\[\\[.*?\\]\\]")){
	    if(subString.contains(link.getAnchor())){
		String replacedString = subString.replaceAll(link.getAnchor(), link.getMention());
		text = text.replace(subString, replacedString);
	    }
	}
	return text;

    }
    
    /**
     * Order the links based on specificity: from the longest to the shortest one.
     * 
     * @param links
     * @return
     */
    private Map<String, de.tudarmstadt.ukp.wikipedia.parser.Link> orderLinksByLength(List<de.tudarmstadt.ukp.wikipedia.parser.Link> links){
	Map<String, de.tudarmstadt.ukp.wikipedia.parser.Link> text2links = new TreeMap<String, de.tudarmstadt.ukp.wikipedia.parser.Link>(
		new Comparator<String>() {
		    @Override
		    public int compare(String o1, String o2) {
			if(o2.equals(o1))
			    return 0;
			else
			    return o2.length() - o1.length();
		    }
		});

	for(final de.tudarmstadt.ukp.wikipedia.parser.Link t: links){
	    text2links.put(t.getText(), t);
	}
	return text2links;
    }


    /**
     * In the "highligths" we need only bold terms (not italic).
     * 
     * @param article
     * @param page
     */
    private void setHighlights(Article article, ParsedPage page) {
	final List<String> highlights = new ArrayList<String>(20);
	for (final Paragraph p : page.getParagraphs()) {
	    for (final Span t : p.getFormatSpans(Content.FormatType.BOLD)) {
		highlights.add(t.getText(p.getText()));
	    }
	    /* 
	     * matteo --> removes italic from highlighted words
	     * 
   			for (final Span t : p.getFormatSpans(Content.FormatType.ITALIC)) {
   				highlights.add(t.getText(p.getText()));
   			}
	     */
	}
	article.setHighlights(highlights);

    }

}
