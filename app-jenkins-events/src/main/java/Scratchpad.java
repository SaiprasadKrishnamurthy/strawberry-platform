import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.EventConfig;
import org.joda.time.DateTime;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by saipkri on 03/01/17.
 */
public class Scratchpad {

    public static void main(String[] args) throws Exception{

        new ObjectMapper().readValue(new File("/Users/saipkri/learning/new/strawberry/app-jenkins-events/src/main/resources/jenkins_slave_events_config.json"), EventConfig.class);


    }
}
