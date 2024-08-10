package cn.itcast.hotel.constants;

import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class MappingsUtils {
    public String getHotelJson() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("\\mappings\\hotelMappings.json");
        if (inputStream == null) {
            System.out.println("File not found!");
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String str = br.readLine();
            while (str != null && !str.isEmpty()) {
                stringBuilder.append(str);
                stringBuilder.append("\n");
                str = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return stringBuilder.toString();
    }
}
