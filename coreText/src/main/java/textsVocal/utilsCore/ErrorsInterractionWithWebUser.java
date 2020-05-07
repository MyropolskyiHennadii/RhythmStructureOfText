package textsVocal.utilsCore;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * class fo storing users errors during execution
 */
@Component
public class ErrorsInterractionWithWebUser {
    public static List<String> errors = new ArrayList<>();
}
