package textsVocal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * class with static variables for web-output
 */
@ConfigurationProperties
public final class HeaderAnFooterListsForWebOutput {

    //filling by portions (verse or prose).
    //List of portions is in PortionOfTextAnalyzer class

    private static List<List<String>> portionHeaders = new ArrayList<>();//list of headers
    private static List<List<String>>  portionFooters = new ArrayList<>();//list of footers

    public static List<List<String>> getPortionHeaders() {
        return portionHeaders;
    }
    public static List<List<String>> getPortionFooters() {
        return portionFooters;
    }

}
