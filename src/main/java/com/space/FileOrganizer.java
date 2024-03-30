package com.space;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class FileOrganizer {
	private final Path directory;
	private final WatchService watcherService;
	private Map<String, Set<String>> fileHashes = new HashMap<>();

	public FileOrganizer(
			String watchedDir) throws IOException {
		// get user home directory
		String homeDirectory = System.getProperty("user.home");

		// set directory from home dir, Documents dir && sync dir
		this.directory = Path.of(homeDirectory, watchedDir);

		// check if directory exists
		if (!Files.exists(directory) || !Files.isDirectory(directory)) {
			try {

				// create directory if not exists
				Files.createDirectories(directory);

			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
		}

		try {
			// initialize watcher service
			this.watcherService = FileSystems.getDefault().newWatchService();
			// register events
			this.directory.register(
					watcherService,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void watchDirectory() throws NoSuchAlgorithmException, IOException {

		// registered directory token
		WatchKey key;

		for (;;) {

			try {
				// set directory token
				key = this.watcherService.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}

			for (WatchEvent<?> event : key.pollEvents()) {

				// get the type of event emitted
				WatchEvent.Kind<?> kind = event.kind();

				if (kind == StandardWatchEventKinds.OVERFLOW) {
					// ignore OVERFLOW events
					continue;
				}

				// context of file that caused the emission of the event
				WatchEvent<Path> ev = (WatchEvent<Path>) event;
				// name of the file
				Path filename = ev.context();

				// path of the file from directory path
				Path filePath = directory.resolve(filename);

				// hash algorithm
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				// new buffured input stream
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(filePath.toString()));

				// calculate file content hash
				String hash = hashFileContent(digest, in, 8192);

				// close InputStream
				in.close();

				// TODO: handle filePath
				System.out.println(filePath);

			}
		}
	}

	private String hashFileContent(MessageDigest digest, BufferedInputStream in, int bufferSize) throws IOException {
		// create a buffer of size [bufferSize]
		byte[] buffer = new byte[bufferSize];

		int sizeRead = -1;

		while ((sizeRead = in.read(buffer)) != -1) {
			// update message digest with new content
			digest.update(buffer, 0, sizeRead);
		}

		// close BufferedInputStream
		in.close();

		// create a digest | hash byte[]
		byte[] hash = digest.digest();

		// convert byte[] to String && return it
		return toHexString(hash);
	}

	public void checkDuplicates() throws IOException {
		try (Stream<Path> paths = Files.walk(this.directory)) {
			paths.filter(Files::isRegularFile).forEach(path -> {
				try {
					// new buffered input stream
					BufferedInputStream in = new BufferedInputStream(new FileInputStream(path.toString()));
					// hash algorithm
					MessageDigest digest = MessageDigest.getInstance("SHA-256");

					// calculate hash
					String hash = hashFileContent(digest, in, 8192);

					// close buffered input stream
					in.close();

					Set<String> files = fileHashes.computeIfAbsent(hash, k -> new HashSet<>());
					files.add(path.toString());

				} catch (IOException | NoSuchAlgorithmException e) {
					e.printStackTrace();
					return;
				}
			});

			// Print duplicate files
			fileHashes.entrySet().stream()
					.filter(entry -> entry.getValue().size() > 1)
					.forEach(entry -> {
						System.out.println("Duplicate files with hash: " + entry.getKey());
						entry.getValue().forEach(System.out::println);
					});
		}
	}

	public void deleteDuplicates() {
		fileHashes.values().forEach(files -> {

			// check if file has duplicates
			if (files.size() > 1) {
				Iterator<String> iterator = files.iterator();
				// Keep the first file, delete the rest
				iterator.next();
				while (iterator.hasNext()) {
					String fileToDelete = iterator.next();
					try {
						Files.delete(Paths.get(fileToDelete));
						System.out.println("Deleted: " + fileToDelete);
					} catch (IOException e) {
						System.err.println("Failed to delete: " + fileToDelete);
						e.printStackTrace();
					}
				}
			}
		});
	}

	private String toHexString(byte[] bytes) {
		// convert byte[] to hex String
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}

		return hexString.toString();
	}

}
