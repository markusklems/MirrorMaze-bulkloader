package edu.kit.aifb.mirrormaze.bulkloader;

import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;

import edu.kit.aifb.mirrormaze.bulkloader.db.entity.AmiEntity;

/**
 * 
 * Collect AMI meta-data via EC2 service requests.
 * 
 * @author markus klems
 * 
 */
public class Client {

	public enum CommandLineArg {
		REGION_DEFAULT("ec2.us-east-1.amazonaws.com"), REGION_ALL("all");

		final String name;

		CommandLineArg(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public enum Attribute {
		KEY, IMAGE_ID, IMAGE_LOCATION, IMAGE_OWNER_ALIAS, OWNER_ID, DESCRIPTION, ARCHITECTURE, PLATFORM, IMAGE_TYPE, PRODUCT_CODES;
	}

	public enum Repository {
		US_EAST1("ec2.us-east-1.amazonaws.com"), US_WEST_1(
				"ec2.us-west-1.amazonaws.com"), US_WEST_2(
				"ec2.us-west-2.amazonaws.com"), EU_1(
				"ec2.eu-west-1.amazonaws.com"), SOUTH_ASIA_EAST_1(
				"ec2.ap-southeast-1.amazonaws.com"), NORTH_ASIA_EAST_1(
				"ec2.ap-southeast-1.amazonaws.com"), SOUTH_AMERICA_EAST_1(
				"ec2.sa-east-1.amazonaws.com");

		final String name;

		Repository(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	private static AmazonEC2 ec2;

	// Command line argument input.
	private static String argRegion;

	private static void init() throws Exception {
		AWSCredentials credentials = new PropertiesCredentials(
				AmazonEC2Client.class
						.getResourceAsStream("AwsCredentials.properties"));

		ec2 = new AmazonEC2Client(credentials);

	}

	public static void main(String[] args) throws Exception {
		init();

		// Parse the command line arguments.
		int argindex = 0;

		if (args.length == 0) {
			displayHelpMessage();
			System.exit(0);
		}
		while (args[argindex].startsWith("-")) {
			if (args[argindex].compareTo("-help") == 0) {
				displayHelpMessage();
				System.exit(0);
			} else if (args[argindex].compareTo("-region") == 0) {
				argindex++;
				if (argindex >= args.length) {
					displayHelpMessage();
					System.exit(0);
				}
				// Select the region (endpoints).
				argRegion = args[argindex];
				argindex++;
			} else {
				System.out.println("Unknown option " + args[argindex]);
				displayHelpMessage();
				System.exit(0);
			}

			if (argindex >= args.length) {
				break;
			}
		}

		if (argindex != args.length) {
			displayHelpMessage();
			System.exit(0);
		}

		// Now, run the crawler client with the arguments that have been passed
		// from the command line.
		run();
	}

	private static void run() {

		if (argRegion.equalsIgnoreCase("all")) {
			for (Repository repo : Repository.values())
				run(repo.getName());
		} else {
			run(argRegion);
		}

	}

	private static void run(String repository) {
		DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest();
		DescribeImagesResult result = ec2.describeImages(describeImagesRequest);
		int i = 0;
		int j = 0;
		for (Image image : result.getImages()) {

			// Save new AMI entity.
			// TODO: collect 100 or so AMIs and then bulk upload them.
			AmiManager.saveAmi(repository, image.getImageId(), image.getImageLocation(), image.getImageOwnerAlias(), image.getOwnerId(), image.getName(), image.getDescription(), image.getArchitecture(), image.getPlatform(), image.getImageType());

			i++;
			j++;
			if (i >= 100) {
				i=0;
				System.out.println("recordcount: " + j);
			}
		}
	}

	public static void displayHelpMessage() {
		System.out
				.println("Usage: java edu.kit.aifb.mirrormaze.bulkloader.Client [options]");
		System.out.println("[options]:");
		System.out
				.println("  -region endpoint: fire EC2 API calls against this endpoint (default: -region ec2.us-east-1.amazonaws.com). \n"
						+ "              You can run all regions with: -region all");
	}

}
