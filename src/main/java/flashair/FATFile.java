package flashair;

import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FATFile {
	private String parentFolder;
	private String fileName;
	private Integer size;
	private Boolean archive;
	private Boolean directory;
	private Boolean volume;
	private Boolean system;
	private Boolean hidden;
	private Boolean readOnly;
	private Date creationTimeStamp;

	public FATFile(String parentFolder, String fileName, Integer size, Boolean archive, Boolean directory,
			Boolean volume, Boolean system, Boolean hidden, Boolean readOnly, Date creationTimeStamp) {

		this.parentFolder = parentFolder;
		this.fileName = fileName;
		this.size = size;
		this.archive = archive;
		this.directory = directory;
		this.volume = volume;
		this.system = system;
		this.hidden = hidden;
		this.readOnly = readOnly;
		this.creationTimeStamp = creationTimeStamp;
	}

	public String getParentFolder() {
		return this.parentFolder;
	}

	public String getFileName() {
		return this.fileName;
	}

	public Integer getSize() {
		return this.size;
	}

	public Boolean isArchive() {
		return this.archive;
	}

	public Boolean isDirectory() {
		return this.directory;
	}

	public Boolean isVolumeLabel() {
		return this.volume;
	}

	public Boolean isSystemFile() {
		return this.system;
	}

	public Boolean isHiddenFile() {
		return this.hidden;
	}

	public Boolean isReadOnly() {
		return this.readOnly;
	}

	public Date getCreationTimestamp() {
		return creationTimeStamp;
	}

	/**
	 * Subtracts one list of {@link FATFile}s from the other (minuend -
	 * subtrahend) and returns the result. This method is useful when wanting to
	 * detect newly created files.
	 * 
	 * @param minuend
	 *            list from which the other list is subtracted
	 * @param subtrahend
	 *            list which is subtracted from the other
	 * @return remaining {@link FATFile} objects
	 */
	public static List<FATFile> subtractList(List<FATFile> minuend, List<FATFile> subtrahend) {
		Set<FATFile> resultSet = new TreeSet<>(new FATFileNameComparator());
		resultSet.addAll(minuend);
		resultSet.removeAll(subtrahend);
		return new ArrayList<>(resultSet);
	}

	/**
	 * Subtracts one list of {@link FATFile}s from the other (minuend -
	 * subtrahend) and returns the result ordered by creation timestamp.
	 * 
	 * @param minuend
	 *            list from which the other list is subtracted
	 * @param subtrahend
	 *            list which is subtracted from the other
	 * @return remaining {@link FATFile} objects sorted by creation timestamp
	 */
	public static List<FATFile> subtractListAndOrderResultByCreationTimestamp(List<FATFile> minuend,
			List<FATFile> subtrahend) {
		
		List<FATFile> result = subtractList(minuend, subtrahend);
		Collections.sort(result, new FATFileCreationDateComparator());
		return result;
	}
}
