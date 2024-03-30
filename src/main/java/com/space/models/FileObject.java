package com.space.models;

import java.nio.file.Path;

public class FileObject {
	final byte[] hash;
	final Path path;

	public FileObject(Path path, byte[] hash) {
		this.hash = hash;
		this.path = path;
	}

}
