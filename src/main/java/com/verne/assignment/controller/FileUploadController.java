package com.verne.assignment.controller;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.verne.assignment.model.Video;
import com.verne.assignment.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Handles requests for the application file upload requests
 */
@Controller
public class FileUploadController {
	private static final double TEN_MINUTES_SECONDS = 600;
	@Autowired
	FileUploadService fileUploadService;

	private final Logger logger = LoggerFactory
			.getLogger(FileUploadController.class);

	/**
	 * Upload single file using Spring Controller
	 */
	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	public
	ResponseEntity<Video> uploadFileHandler(@RequestParam(value = "file", required = true)
											MultipartFile file) throws IOException, NoSuchAlgorithmException {

		logger.debug("file: " + file.getOriginalFilename());
		// Must retrieve temp file content asap or it will be deleted
		byte[] bytes = file.getBytes();
		// Validates input
		fileValidation(file, bytes);

		return new ResponseEntity<Video>(fileUploadService.uploadFile(file, bytes),HttpStatus.OK);
	}

	/**
	 * Validate service input
	 * @param file
	 * @param bytes
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private boolean fileValidation(MultipartFile file, byte[] bytes)throws IOException, NoSuchAlgorithmException{

		if(file == null){
			throw new IllegalArgumentException("file doesn't exist");
		}
		if (file.isEmpty()){
			throw new IllegalArgumentException("file is empty");
		}

		if (!file.getContentType().equals("video/mp4")){
			throw new IllegalArgumentException("content-type must be video/mp4");
		}

		// Split file name along the "."
		String[] nameArray = file.getOriginalFilename().split("\\.");
		File convFile = File.createTempFile(nameArray[0],nameArray[1]);

		BufferedOutputStream stream = new BufferedOutputStream(
				new FileOutputStream(convFile));
		stream.write(bytes);
		stream.close();

		// Get video file length
		FileDataSourceImpl fileDataSource = new FileDataSourceImpl(convFile);
		IsoFile isoFile = new IsoFile(fileDataSource);
		double lengthInSeconds = (double)
				isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
				isoFile.getMovieBox().getMovieHeaderBox().getTimescale();

		// Reject video if more than 10 seconds
		if (lengthInSeconds > TEN_MINUTES_SECONDS){
			throw new IllegalArgumentException("file length is greater than 10 minutes");
		}
		return true;
	}
}
