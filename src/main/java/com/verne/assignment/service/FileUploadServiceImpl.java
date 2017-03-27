package com.verne.assignment.service;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient;
import com.amazonaws.services.elastictranscoder.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.verne.assignment.exception.ServiceException;
import com.verne.assignment.model.Video;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FileUploadServiceImpl implements FileUploadService{
    // HLS Segment duration that will be targeted.
    private static final String SEGMENT_DURATION = "2";
    private static final String PIPELINE_ID = "1489438000359-7f9dvq";
    private static final String HLS_1000K_PRESET_ID = "1351620000001-200030";
    // All outputs will have this prefix prepended to their output key.
    private static final String OUTPUT_KEY_PREFIX = "output/hls/";

    /**
     * Upload and transcode file
     * @param file
     * @param bytes
     * @return
     */
    public Video uploadFile(MultipartFile file, byte[] bytes) {
        String date = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss/").format(new Date());
        String fileName = date + file.getOriginalFilename();
        Video video = null;
        try {
            uploadFileS3(fileName, bytes);
            fileName = file.getOriginalFilename();
            video = createTranscodeJob(file, fileName, date);
        } catch (Exception e) {
            throw new ServiceException(e);
        }


        return video;
    }

    /**
     * Upload file to S3
     * @param fileName
     * @param bytes
     * @throws IOException
     */
    private void uploadFileS3(String fileName, byte[] bytes) throws IOException {
        AmazonS3 s3 = new AmazonS3Client();
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        s3.setRegion(usEast1);
        ObjectMetadata meta = new ObjectMetadata();

        s3.putObject("verne-media-upload", fileName, new ByteArrayInputStream(bytes), meta);
    }

    /**
     * Start transcoding job
     * @param file
     * @param fileName
     * @param date
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws InterruptedException
     */
    private Video createTranscodeJob(MultipartFile file, String fileName, String date) throws UnsupportedEncodingException, NoSuchAlgorithmException, InterruptedException {
        AmazonElasticTranscoder amazonElasticTranscoder = new AmazonElasticTranscoderClient();
        // Setup the job outputs using the HLS presets.

        String INPUT_KEY = date + fileName;

        String[] splitFileName = (fileName).split("\\.");
        //String outputKey = TranscoderUtilities.inputKeyToOutputKey(splitFileName[0] + ".ts");
        String outputKey = splitFileName[0];

        JobInput input = new JobInput().withKey(INPUT_KEY);

        CreateJobOutput hls1000k = new CreateJobOutput()
                //.withKey("hls1000k/" + outputKey)
                .withKey(outputKey)
                .withPresetId(HLS_1000K_PRESET_ID)
                .withSegmentDuration(SEGMENT_DURATION);

        List<CreateJobOutput> outputs = Arrays.asList(hls1000k);

        // Create the job.
        CreateJobRequest createJobRequest = new CreateJobRequest()
                .withPipelineId(PIPELINE_ID)
                .withInput(input)
                .withOutputKeyPrefix("hls1000k/" + date)
                .withOutput(hls1000k);

        Job finishedJob = amazonElasticTranscoder.createJob(createJobRequest).getJob();

        ReadJobResult result = amazonElasticTranscoder.readJob(new ReadJobRequest().withId(finishedJob.getId()));

        while (!"Complete".equalsIgnoreCase(result.getJob().getStatus())) {
            // Would not typically do this. Normally long running processes should be sent to a queue so that
            // the api can return a response quickly
            Thread.sleep(2000);
            result = amazonElasticTranscoder.readJob(new ReadJobRequest().withId(finishedJob.getId()));
        }

        AmazonS3 s3 = new AmazonS3Client();
        ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
                .withBucketName("verne-media-transcoded")
                .withPrefix("hls1000k/" + date));

        addAttributes(objectListing);

        return addAttributes(objectListing);
    }

    /**
     * Add data to video object
     * @param objectListing
     * @return
     */
    private Video addAttributes(ObjectListing objectListing){
        AmazonS3 s3 = new AmazonS3Client();
        int i = 0;
        if (objectListing.getObjectSummaries().size() > 1 ){
            i = 1;

        }
        String key = objectListing.getObjectSummaries().get(i).getKey();
        String bucket = objectListing.getObjectSummaries().get(i).getBucketName();
        S3Object object = s3.getObject(new GetObjectRequest("verne-media-transcoded",
                objectListing.getObjectSummaries().get(i).getKey()));
        String contentType = object.getObjectMetadata().getContentType();
        Video video =  new Video();
        video.setContentType(contentType);
        video.setKey(key);
        video.setBucketName(bucket);

        return video;
    }
}
