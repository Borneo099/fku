import java.net.*;
public class TestUrl {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.useSystemProxies", "false");
        System.setProperty("http.proxyHost", "");
        System.setProperty("https.proxyHost", "");
        try {
            URL url = new URL("https://maven.minecraftforge.net/net/minecraftforge/gradle/ForgeGradle/6.0.16/ForgeGradle-6.0.16.pom");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            int code = conn.getResponseCode();
            System.out.println("Code: " + code);
            System.out.println("OK");
        } catch (Exception e) {
            System.out.println("FAIL: " + e);
        }
    }
}
