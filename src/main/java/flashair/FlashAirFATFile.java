package flashair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlashAirFATFile extends FATFile {

	private static final String FILE_LIST_HEADER = "WLANSD_FILELIST";
	private static final Byte ARGUMENTS_PER_FILE_ENTRY = 6;
	private static final Integer UNSIGNED_SHORT_MAX_VALUE = 65535;

	public FlashAirFATFile(String parentFolder, String fileName, Integer size, Boolean archive, Boolean directory,
			Boolean volume, Boolean system, Boolean hidden, Boolean readOnly, LocalDateTime creationTimeStamp) {
		super(parentFolder, fileName, size, archive, directory, volume, system, hidden, readOnly, creationTimeStamp);
	}
	
	public static List<FATFile> parseFATFiles(List<String> fileList) {
		List<FATFile> result = new ArrayList<>();
		
		for (String line : fileList) {
			if (!line.isEmpty() && !line.contains(FILE_LIST_HEADER)) {
				result.add(FlashAirFATFile.parseFATFile(line));
			}
		}
		
		return result;
	}

	public static FATFile parseFATFile(String fileEntry) {
		String[] parts = fileEntry.split(",");

		if (parts.length != ARGUMENTS_PER_FILE_ENTRY) {
			throw new IllegalArgumentException("File entry was split in " + parts.length + "parts, but "
					+ ARGUMENTS_PER_FILE_ENTRY + " were expected.");
		}

		// strip leading '/' from parent directory
		String parentDirectory = parts[0].replaceAll("^/+", "");
		String fileName = parts[1];
		Integer size = Integer.parseInt(parts[2]);
		Integer attributes = Integer.parseInt(parts[3]);
		Integer date = Integer.parseInt(parts[4]);
		Integer time = Integer.parseInt(parts[5]);

		Boolean archive = FlashAirFATFile.extractIsArchive(attributes);
		Boolean directory = FlashAirFATFile.extractIsDirectory(attributes);
		Boolean volume = FlashAirFATFile.extractIsVolume(attributes);
		Boolean system = FlashAirFATFile.extractIsSystem(attributes);
		Boolean hidden = FlashAirFATFile.extractIsHidden(attributes);
		Boolean readOnly = FlashAirFATFile.extractIsReadOnly(attributes);
		LocalDateTime creationTimeStamp = FlashAirFATFile.extractCreationTimestamp(date, time);

		return new FATFile(parentDirectory, fileName, size, archive, directory, volume, system, hidden, readOnly,
				creationTimeStamp);
	}

	private static Boolean extractIsArchive(Integer attributes) {
		return extractAttribute(attributes, 5);
	}

	private static Boolean extractIsDirectory(Integer attributes) {
		return extractAttribute(attributes, 4);
	}

	private static Boolean extractIsVolume(Integer attributes) {
		return extractAttribute(attributes, 3);
	}

	private static Boolean extractIsSystem(Integer attributes) {
		return extractAttribute(attributes, 2);
	}

	private static Boolean extractIsHidden(Integer attributes) {
		return extractAttribute(attributes, 1);
	}

	private static Boolean extractIsReadOnly(Integer attributes) {
		return extractAttribute(attributes, 0);
	}

	/**
	 * Extracts the creation timestamp from a given date and time represented as
	 * packed integers.
	 * 
	 * @param date
	 *            packed integer representation where bits 15-9 represent the
	 *            year (0 = 1980), bits 8-5 represent the month, and bits 4-0
	 *            represent the day of month.
	 * @param time
	 *            packed integer representation where bits 15-11 rerepsent the
	 *            hour, bits 10-5 represent the minute, and bits 4-0 represent
	 *            the second/2.
	 * @return {@link LocalDateTime} representing the file creation timestamp
	 */
	private static LocalDateTime extractCreationTimestamp(Integer date, Integer time) {
		if (date > UNSIGNED_SHORT_MAX_VALUE) {
			throw new NumberFormatException("Date is greater than " + UNSIGNED_SHORT_MAX_VALUE);
		}

		if (time > UNSIGNED_SHORT_MAX_VALUE) {
			throw new NumberFormatException("Time is greater than " + UNSIGNED_SHORT_MAX_VALUE);
		}

		int year = (date >> 9) + 1980;
		int month = (date >> 5) & 15;
		int day = date & 31;
		int hour = (time >> 11) & 31;
		int minute = (time >> 5) & 63;
		int second = (time & 31) * 2;

		return LocalDateTime.of(year, month, day, hour, minute, second);
	}

	/**
	 * Extract boolean from Integer
	 * 
	 * Extracts a boolean value from a given integer at a given bit position by
	 * shifting the combined attributes to the right and masking it with 0x1 to
	 * only view the least significant bit. 0 equals false, 1 equals true.
	 * 
	 * @param attributes
	 *            attributes in decimal format
	 * @param shift
	 *            amount of bits to shift
	 * @return true if the attribute at position shift is set to 1, false
	 *         otherwise
	 */
	private static Boolean extractAttribute(Integer attributes, Integer shift) {
		Boolean result = false;
		int temp = attributes;

		temp = temp >>> shift;
		result = (temp & 0x1) == 1;

		return result;
	}
}
