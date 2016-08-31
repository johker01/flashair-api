package flashair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlashAir {
	private static final Logger LOGGER = LoggerFactory.getLogger(FlashAir.class);
	
	private static final String DEFAULT_URI = "http://192.168.0.1/";
	private static final String COMMAND_CGI = "command.cgi";
	
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
			LOGGER.error("URI '{}' is invalid", uri.toString());
		}
	}

	public FlashAir(java.net.URI uri) {
		this.uri = uri;
	}

	private List<String> executeCommand(String command) throws IOException {
		List<String> result = new ArrayList<>();
		
		URL url = null;
		try {
			url = new URL(this.uri + COMMAND_CGI + "?" + command);
			
			try (InputStream inputStream = url.openStream()) {
				result = IOUtils.readLines(inputStream, "UTF-8");
			}
		} catch (MalformedURLException e) {
			LOGGER.error("Malformed URI '{}': {}", uri.toString(), e.toString());
			throw e;
		} catch (IOException e) {
			LOGGER.error("Receiving results from '{}' failed: {}", url.toString(), e.toString());
			throw e;
		}
		
		return result;
	}
	
	public List<FATFile> getFileList(String directory) throws IOException {
		List<String> fileStringList = executeCommand("op=" + Operation.GET_FILE_LIST.operation() + "&DIR=/" + directory);
		
		return FlashAirFATFile.parseFATFiles(fileStringList);
	}
	
	public Integer getNumberOfFiles(String directory) throws Exception {
		List<String> resultList = executeCommand("op=" + Operation.GET_NUMER_OF_FILES.operation() + "&DIR=/" + directory);
		
		checkResultSize(1, resultList.size());
		return Integer.parseInt(resultList.get(0));
	}
	
	public Boolean getUpdateStatus() throws Exception {
		Boolean result = false;
		List<String> resultList = executeCommand("op=" + Operation.GET_UPDATE_STATUS.operation());
		
		checkResultSize(1, resultList.size());
		if (resultList.get(0).trim().equals("1")) {
			result = true;
		}
		
		return result;
	}
	
	private void checkResultSize(Integer expectedResultSize, Integer actualResultSize) throws Exception {
		if (actualResultSize != expectedResultSize) {
			// TODO throw a more specific exception
			LOGGER.error("ERROR: Expected list size of {}, but got: {}", expectedResultSize, actualResultSize);
			throw new Exception("Expected list size of " + expectedResultSize + ", but got: " + actualResultSize);
		}
	}
	
	public byte[] downloadFileTo(String remoteFilePath) throws IOException {
		byte[] result = {};
		
		URL url = null;
		try {
			url = new URL(this.uri + "/" + remoteFilePath);
			
			try (InputStream inputStream = url.openStream()) {
				result = IOUtils.toByteArray(inputStream);
			}
		} catch (MalformedURLException e) {
			LOGGER.error("Malformed URI '{}': {}", url.toString(), e.toString());
			throw e;
		} catch (IOException e) {
			LOGGER.error("Downloading file '{}' failed: {}", remoteFilePath, e.toString());
			throw e;
		}
		
		return result;
	}
	
	public void downloadFileTo(String remoteFilePath, String localFilePath) throws IOException {
		try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			byte[] fileContent = downloadFileTo(remoteFilePath);
			FileUtils.writeByteArrayToFile(new File(localFilePath), fileContent);
		}
	}
}
