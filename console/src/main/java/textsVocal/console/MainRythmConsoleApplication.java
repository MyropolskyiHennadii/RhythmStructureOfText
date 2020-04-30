package textsVocal.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import textsVocal.config.CommonConstants;
import textsVocal.structure.BuildingPortion;
import textsVocal.structure.Word;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

//import static textsVocal.structure.PortionOfTextAnalyser.portionsList;

@SpringBootApplication
@ConfigurationPropertiesScan("coreText.textsVocal.config")
public class MainRythmConsoleApplication {

    private static final Logger log = LoggerFactory.getLogger(MainRythmConsoleApplication.class);//logger

    /**
     * start
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {

        log.info("Beginning main console...");

        String testText = "Любви, надежды, тихой славы" + (char) 12
                + "Недолго нежил нас обман," + (char) 12
                + "Исчезли юные забавы," + (char) 12
                + "Как сон, как утренний туман;" + (char) 13
                + "Но в нас горит еще желанье," + (char) 10
                + "Под гнетом власти роковой" + (char) 10
                + "Нетерпеливою душой" + (char) 12
                + "Отчизны внемлем призыванье.";

        ApplicationContext context = SpringApplication.run(MainRythmConsoleApplication.class, args);
        CommonConstants commonConstants = (CommonConstants) context.getBean("commonConstants");
        commonConstants.setApplicationContext(context);
        commonConstants.setThisIsWebApp(false);

        BuildingPortion buildingPortion = context.getBean(BuildingPortion.class);
        buildingPortion.startPortionBuilding(testText, commonConstants);

        //publish unknown words
        if (CommonConstants.getUnKnownWords().size() > 0) {
            log.info("====== Unknown words ======");
            for (Word w : CommonConstants.getUnKnownWords()) {
                log.info(w.toString());
            }
        }

        log.info("End main console ...");
    }

}
