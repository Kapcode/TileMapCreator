
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

public class AssetHandler {
    public static Level loadLevel(String levelName) {
        Gson gson = new Gson();
        try (Reader reader = new FileReader("src/" + levelName)) {
            return gson.fromJson(reader, Level.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveLevel(Level level, String levelName) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter("src/" + levelName)) {
            gson.toJson(level, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
