import java.util.List;
import com.opentok.api.Archive;
import com.opentok.api.OpenTok;
import com.opentok.exception.OpenTokException;

class ArchivingSample {
    public static int API_KEY = 472032; // Replace with your OpenTok API key.
    public static String API_SECRET = "034de16b3e6a241bfbcaec45c0d1e79be19ad919"; // Replace with your OpenTok API secret.
    public static void main(String argv[]) throws OpenTokException {
        
        OpenTok sdk = new OpenTok(ArchivingSample.API_KEY, ArchivingSample.API_SECRET);

        // Generate a session that uses the OpenTok Media Router
        System.out.println(sdk.createSession().getSessionId());
        System.out.println();

    }
    
    // The following method starts recording an archive of an OpenTok 2.0 session
    // and returns the archive ID (on success). Note that you can only start an archive 
    // on a session that has clients connected.
    java.util.UUID startArchive(OpenTok sdk, String sessionId, String name) {
        try {
            Archive archive = sdk.startArchive(sessionId, name);
            return archive.getId();
        } catch (OpenTokException exception){
            System.out.println(exception.toString());
            return null;
        }
    }

    // The following method stops the recording of an archive, returning
    // true on success, and false on failure.
    boolean stopArchive(OpenTok sdk, String archiveId) {
        try {
            Archive archive = sdk.stopArchive(archiveId);
            return true;
        } catch (OpenTokException exception){
            System.out.println(exception.toString());
            return false;
        }
    }

    // The following method logs information on a given archive.
    void logArchiveInfo(OpenTok sdk, String archiveId) {
        try {
            Archive archive = sdk.getArchive(archiveId);
            System.out.println(archive.toString());
        } catch (OpenTokException exception){
            System.out.println(exception.toString());
        }
    }

    // The following method logs information on all archives (up to 1000)
    // for your API key.
    void listArchives(OpenTok sdk) {
        try {
            List<Archive> archives = sdk.listArchives();
            for (int i = 0; i < archives.size(); i++) {
                Archive archive = archives.get(i);
                System.out.println(archive.toString());
            }
        } catch (OpenTokException exception){
            System.out.println(exception.toString());
        }
    }
}
