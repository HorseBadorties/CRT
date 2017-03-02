package de.toto.google;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDrive {

    /**
     * Build and return an authorized Drive client service.
     * @return an authorized Drive client service
     * @throws IOException
     */
    public static Drive getDriveService() throws IOException {
        Credential credential = GoogleCredentials.authorize();
        return new Drive.Builder(
        		GoogleCredentials.HTTP_TRANSPORT, GoogleCredentials.JSON_FACTORY, credential)
                .setApplicationName("CRT")
                .build();
    }

    public static void main(String[] args) throws IOException {
    	downloadPGNs(new java.io.File("c:/temp"));
    }
    
	public static void downloadPGNs(java.io.File targetDir) throws IOException {
		Drive service = getDriveService();
		// Find our 'Repertoire' folder
		FileList repList = service.files().list().setQ("mimeType='application/vnd.google-apps.folder'")
				.setQ("name='Repertoire'").setFields("nextPageToken, files(id, name)").setPageToken(null).execute();
		File rep = repList.getFiles().get(0);

		FileList result = service.files().list()
				.setQ(String.format("'%s' in parents and (name contains '%s')", rep.getId(), ".pgn"))
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents)").setPageToken(null).execute();
		List<File> files = result.getFiles();
		if (files == null || files.size() == 0) {
			System.out.println("No files found.");
		} else {
			for (File file : files) {				
				System.out.printf("downloading %s \n", file.getName());
				java.io.File targetFile = new java.io.File(targetDir, file.getName());
				service.files().get(file.getId()).executeMediaAndDownloadTo(new FileOutputStream(targetFile));
			}			
		}
	}

}
