import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

public class SheetsQuickstart {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static boolean _manual;
    private static ArrayList<Object> names = new ArrayList<>();
    private static HashMap<Object, int[]> namesToPoints = new HashMap<>();

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = SheetsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Accesses the consultants and their preferences.
     * spreadsheetId: Portion of spreadsheet URL after d/ and before /edit
     * range: "TabName!TopLeftCorner:BottomRightCorner"
     */
    public static void main(String... args) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        Scanner scanner = new Scanner(System.in);
//        System.out.println();
//        System.out.println("Type 0 for auto, 1 for manual");
//        _manual = scanner.nextInt() != 0;
        final String spreadsheetId = "1QE3lsJfEIAFb9ayiClkxnFG3UdjQ4T2qYAI6_DF9MNQ";
        final String range = "B2:G19";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            //test(values);
            for (List row : values) {
                names.add(row.get(0));
            }
            int[][] assignments = new int[5][names.size()];
            int counter = 0;
            //create assignemts --> rows are divisions, cols are consultants
            for (List row : values) {
                assignments[0][counter] = Integer.parseInt((String) row.get(1));
                assignments[1][counter] = Integer.parseInt((String) row.get(2));
                assignments[2][counter] = Integer.parseInt((String) row.get(3));
                assignments[3][counter] = Integer.parseInt((String) row.get(4));
                assignments[4][counter] = Integer.parseInt((String) row.get(5));
                counter++;
            }
            int[][] assignmentsTransposed = transposeMatrix(assignments);
            for (int i = 0; i < assignmentsTransposed.length; i++) {
                namesToPoints.put(names.get(i), assignmentsTransposed[i]);
            }
            Assignment assignment = new Assignment(_manual, assignments, names, namesToPoints);
        }
    }

    /**
     * To test the pulling of spreadsheet data.
     * Will print name and division preference points.
     * Order: "Corporate", "Marketing", "Startup", "Finance", "Pro Bono"
     *
     * @param values values
     */
    private static void test(List<List<Object>> values) {
        System.out.println("Name, Preference Points");
        for (List row : values) {
            System.out.printf("%s: %s, %s, %s, %s, %s\n", row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5));
        }
    }

    /**
     * Returns the transpose of a 2d-array
     * @param matrix array
     * @return transposeMatrix
     */
    static int[][] transposeMatrix(int[][] matrix){
        int m = matrix.length;
        int n = matrix[0].length;
        int[][] transposedMatrix = new int[n][m];
        for(int x = 0; x < n; x++) {
            for(int y = 0; y < m; y++) {
                transposedMatrix[x][y] = matrix[y][x];
            }
        }
        return transposedMatrix;
    }
}
