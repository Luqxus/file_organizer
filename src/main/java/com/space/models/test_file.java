// package com.space;

// import java.io.BufferedInputStream;
// import java.io.FileInputStream;
// import java.io.FileNotFoundException;
// import java.io.IOException;
// import java.nio.file.FileSystems;
// import java.nio.file.Path;
// import java.nio.file.StandardWatchEventKinds;
// import java.nio.file.WatchEvent;
// import java.nio.file.WatchKey;
// import java.nio.file.WatchService;
// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.Map;

// public class Main {

// static ArrayList<Map<String, Path>> checkedFiles = new ArrayList<Map<String,
// Path>>();
// static ArrayList<Map<String, Path>> duplicateFiles = new
// ArrayList<Map<String, Path>>();;

// public static void main(String[] args) {
// // TODO: crawl throw home directories
// // and match file digests to get file with similar content

// // String homeDirectory = System.getProperty("user.home");

// // System.out.printf("Home directory : %s", homeDirectory);

// // try (Stream<Path> paths = Files.walk(Paths.get(homeDirectory))) {
// // paths.forEach(path -> {
// // if (!path.getFileName().startsWith(".") && Files.isRegularFile(path)) {
// // // check for duplicates
// // // System.out.println("-----------");
// // // System.out.println(path.getFileName());
// // // System.out.println("------------");

// // handleFile(path);
// // }
// // });

// // } catch (IOException e) {
// // e.printStackTrace();
// // return;
// // }

// directoryWatcher();
// }

// static void directoryWatcher() {
// // instanciate a watch service
// WatchService watchService;

// try {
// watchService = FileSystems.getDefault().newWatchService();
// } catch (IOException err) {
// System.err.println(err.toString());
// return;
// }

// // get users home directory
// String homeDirectory = System.getProperty("user.home");
// Path directory = Path.of(homeDirectory, "Documents", "sync");

// try {
// directory.register(
// watchService,
// StandardWatchEventKinds.ENTRY_CREATE,
// StandardWatchEventKinds.ENTRY_DELETE,
// StandardWatchEventKinds.ENTRY_MODIFY);

// } catch (IOException err) {
// System.err.println(err.toString());
// return;
// }

// WatchKey key;

// for (;;) {
// try {
// key = watchService.take();
// } catch (InterruptedException err) {
// System.err.println(err.toString());
// return;
// }

// for (WatchEvent<?> event : key.pollEvents()) {
// WatchEvent.Kind<?> kind = event.kind();

// if (kind == StandardWatchEventKinds.OVERFLOW) {
// continue;
// }

// WatchEvent<Path> ev = (WatchEvent<Path>) event;
// Path filename = ev.context();

// Path changedFile = directory.resolve(filename);

// // TODO: handle
// if (ev.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
// System.out.println(changedFile);
// handleFile(changedFile);
// }

// System.out.println(changedFile.getFileName());

// }

// boolean valid = key.reset();
// if (!valid) {
// break;
// }
// }
// }

// static String fromBytes(byte[] bytes) {
// StringBuilder hexString = new StringBuilder();
// for (byte b : bytes) {
// String hex = Integer.toHexString(0xff & b);
// if (hex.length() == 1) {
// hexString.append('0');
// }
// hexString.append(hex);
// }

// return hexString.toString();
// }

// static void handleDuplicate(byte[] hash, Path filename) {
// // convert provided hash bytes to string
// String stringHash = fromBytes(hash);
// boolean isDuplicate = false;
// String foundkey = "";
// int index = 0;

// if (checkedFiles.size() == 0) {
// Map<String, Path> checkFile = new HashMap<String, Path>() {
// {
// put(fromBytes(hash), filename);
// }
// };

// checkedFiles.add(checkFile);

// return;
// }

// for (Map<String, Path> file : checkedFiles) {

// // for every checkFile element | compare checkFile hash to string hash
// for (String key : file.keySet()) {
// // System.out.printf("Key : %s\n", key);
// // System.out.printf("Key == Hash : %s", Boolean.toString(key ==
// // fromBytes(hash)));
// if (key == fromBytes(hash)) {
// isDuplicate = true;
// foundkey = key;
// break;
// }
// }
// }

// if (isDuplicate) {
// // if hashes are equal provided [filename] is a duplicate of the checkedFile
// Map<String, Path> file = checkedFiles.get(index);
// System.out.printf("File %s contents are a duplicate of File %s's contents",
// filename,
// file.get(foundkey));
// // add provided file to duplicate
// Map<String, Path> duplicate = new HashMap<String, Path>();

// duplicate.put(foundkey, file.get(foundkey));
// duplicate.put(fromBytes(hash), filename);

// duplicateFiles.add(duplicate);

// } else {
// // provided file does not exist in the checkedFile
// // so add provided file to checkedFile
// Map<String, Path> checkFile = new HashMap<String, Path>() {
// {
// put(fromBytes(hash), filename);
// }
// };

// checkedFiles.add(checkFile);
// }

// }

// static void handleFile(Path filename) {

// try {
// BufferedInputStream input;
// MessageDigest messageDigest;

// input = new BufferedInputStream(new FileInputStream(filename.toString()));
// messageDigest = MessageDigest.getInstance("SHA-256");
// byte[] hash = hash(messageDigest, input, 8192);
// input.close();

// System.out.println(hash);
// System.out.printf("File hash : %s\n\n", fromBytes(hash));

// handleDuplicate(hash, filename);

// } catch (FileNotFoundException e) {
// e.printStackTrace();
// return;
// } catch (NoSuchAlgorithmException e) {
// e.printStackTrace();
// return;
// } catch (IOException e) {
// e.printStackTrace();
// return;
// }

// }

// static byte[] hash(MessageDigest digest, BufferedInputStream in, int
// bufferSize) throws IOException {
// byte[] buffer = new byte[bufferSize];

// int sizeRead = -1;

// while ((sizeRead = in.read(buffer)) != -1) {
// digest.update(buffer, 0, sizeRead);
// }

// in.close();

// byte[] hash = digest.digest();

// return hash;
// }

// }