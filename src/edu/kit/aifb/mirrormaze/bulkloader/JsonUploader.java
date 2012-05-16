/**
 * 
 */
package edu.kit.aifb.mirrormaze.bulkloader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HTTP;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * @author mugglmenzel
 * 
 */
public class JsonUploader {

	private static Logger log = Logger.getLogger(JsonUploader.class.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		assert args.length > 2 : "Usage: " + JsonUploader.class.getName()
				+ " S3Bucket URL";
		if (args.length < 2) {
			System.err.println("Usage: " + JsonUploader.class.getName()
					+ " S3Bucket URL");
			return;
		}

		String S3Bucket = args[0];
		String URL = args[1];
		log.info("Got args " + args[0] + ", " + args[1]);

		try {
			AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(
					JsonUploader.class
							.getResourceAsStream("AwsCredentials.properties")));
			if (!s3.doesBucketExist(S3Bucket)) {
				log.warning("S3 bucket doesn't exist. Creating.");
				s3.createBucket(S3Bucket);
			}

			List<S3ObjectSummary> jsonFiles = s3.listObjects(
					new ListObjectsRequest().withBucketName(S3Bucket))
					.getObjectSummaries();
			if (jsonFiles.size() > 0) {
				try {
					for (S3ObjectSummary s3JSON : jsonFiles) {
						S3Object jsonFile = s3.getObject(new GetObjectRequest(
								S3Bucket, s3JSON.getKey()));
						HttpPost post = new HttpPost(URL);
						post.setEntity(new InputStreamEntity(jsonFile
								.getObjectContent(), jsonFile
								.getObjectMetadata().getContentLength()));
						log.info("Sent file " + jsonFile.getKey() + " to "
								+ URL);
					}

				} catch (Exception e) {
					log.severe("Couldn't read files from s3 bucket " + S3Bucket
							+ "!");
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
			log.severe("Can't access S3 due to missing credentials file.");
		} catch (Exception ex) {
			ex.printStackTrace();
			log.severe("Accessing S3 failed.");
		}
	}
}
