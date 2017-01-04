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

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("GMT"));

        System.out.println(f.format(new Date()));


    }
}
