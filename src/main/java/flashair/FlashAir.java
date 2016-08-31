package flashair;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class FlashAir {
	private static final String DEFAULT_URI = "http://192.168.0.1/";
	private static final String COMMAND_CGI = "command.cgi";
	
	private static final Integer DOWNLOAD_CHUNK_SIZE = 4096;

	public enum Operation {
		GET_FILE_LIST(100), GET_NUMER_OF_FILES(101), GET_UPDATE_STATUS(102);

		private Integer operation;

		private Operation(Integer operation) {
			this.operation = operation;
		}

		public Integer operation() {
			return operation;
		}
	}

	private URI uri;

	public FlashAir() {
		try {
			uri = new URI(DEFAULT_URI);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public FlashAir(java.net.URI uri) {
		this.uri = uri;
	}

	private List<String> executeCommand(String command) {
		List<String> result = new ArrayList<>();
		try {
			URL url = new URL(this.uri + COMMAND_CGI + "?" + command);
			URLConnection urlCon = url.openConnection();
			urlCon.connect();
			
			try (InputStream inputStream = urlCon.getInputStream();
					BufferedReader bufreader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));) {
			
				String line;
				while ((line = bufreader.readLine()) != null) {
					result.add(line);
				}
			}
		} catch (MalformedURLException e) {
			// TODO handle exception
			System.err.println("ERROR: " + e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO handle exception
			System.err.println("ERROR: " + e.toString());
			e.printStackTrace();
		}
		return result;
	}
	
	public List<FATFile> getFileList(String directory) {
		List<String> fileStringList = executeCommand("op=" + Operation.GET_FILE_LIST.operation() + "&DIR=/" + directory);
		
		return FlashAirFATFile.parseFATFiles(fileStringList);
	}
	
	public List<FATFile> getFileListRecursive(String directory) {
		List<String> fileStringList = executeCommand("op=" + Operation.GET_FILE_LIST.operation() + "&DIR=/" + directory);
		
		// TODO implement
		
		return FlashAirFATFile.parseFATFiles(fileStringList);
	}
	
	public Integer getNumberOfFiles(String directory) throws Exception {
		List<String> resultList = executeCommand("op=" + Operation.GET_NUMER_OF_FILES.operation() + "&DIR=/" + directory);
		
		if (resultList.size() != 1) {
			// TODO throw a more specific exception
			System.err.println("ERROR: Expected list size of 1, but found: " + resultList.size());
			throw new Exception("Expected list size of 1, but found: " + resultList.size());
		}

		Integer result = Integer.parseInt(resultList.get(0));
		
		return result;
	}
	
	public Boolean getUpdateStatus() throws Exception {
		Boolean result = false;
		List<String> resultList = executeCommand("op=" + Operation.GET_UPDATE_STATUS.operation());
		
		if (resultList.size() != 1) {
			// TODO throw a more specific exception
			System.err.println("ERROR: Expected list size of 1, but found: " + resultList.size());
			throw new Exception("Expected list size of 1, but found: " + resultList.size());
		}
		
		if (resultList.get(0).trim().equals("1")) {
			result = true;
		}
		
		return result;
	}
	
	public void downloadFileTo(String remoteFilePath, OutputStream outputStream) {
		try {
			URL url = new URL(this.uri + "/" + remoteFilePath);
			
			try (InputStream inputStream = url.openStream()) {

				byte[] chunk = new byte[DOWNLOAD_CHUNK_SIZE];
				int bytesRead;

				while ((bytesRead = inputStream.read(chunk)) > 0) {
					outputStream.write(chunk, 0, bytesRead);
				}

			}
		} catch (MalformedURLException e) {
			// TODO handle exception
			System.err.println("ERROR: " + e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO handle exception
			System.err.println("ERROR: " + e.toString());
			e.printStackTrace();
		}
	}
	
	public void downloadFileTo(String remoteFilePath, String localFilePath) throws IOException {
		try(ByteArrayOutputStream out = new ByteArrayOutputStream();
				OutputStream outputStream = new FileOutputStream(localFilePath)) {
			
			downloadFileTo(remoteFilePath, outputStream);
		}
	}
}
