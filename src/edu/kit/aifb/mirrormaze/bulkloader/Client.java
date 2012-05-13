package edu.kit.aifb.mirrormaze.bulkloader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;

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
				edu.kit.aifb.mirrormaze.bulkloader.Client.class
						.getResourceAsStream("AwsCredentials.properties"));

		ec2 = new AmazonEC2Client(credentials);

	}

	public static void main(String[] args) throws Exception {
		init();

		// Parse the command line arguments.
		if (args.length > 0) {
			int argindex = 0;

			while (args[argindex].startsWith("-")) {
				if (args[argindex].compareTo("--help") == 0) {
					displayHelpMessage();
					System.exit(0);
				} else if (args[argindex].compareTo("--region") == 0) {
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
		}

		// Now, run the crawler client with the arguments that have been passed
		// from the command line.
		run();
	}

	private static void run() throws IOException {

		if (argRegion == null || argRegion.isEmpty()) {
			// Run in default region
			argRegion = CommandLineArg.REGION_DEFAULT.getName();
			run(argRegion);
		} else if (argRegion.equalsIgnoreCase("all")) {
			for (Repository repo : Repository.values())
				run(repo.getName());
		} else {
			run(argRegion);
		}

	}

	private static void run(String repository) throws IOException {
		System.out.println("Get virtual machine image info from repository "
				+ repository);
		DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest();
		DescribeImagesResult result = ec2.describeImages(describeImagesRequest);
		System.out.println("Fetched all vm image descriptions from "
				+ repository);

		StringBuffer csvText = new StringBuffer(csvHeader());

		for (Image image : result.getImages()) {

			csvText.append(csvLine(repository,image));
			
		}
		// Save the csv file to the local file system.
		final String fileName = "data/ami-list-" + repository
				+ ".csv";
		FileWriter csvFileWriter = new FileWriter(fileName);
		BufferedWriter csvBufferedWriter = new BufferedWriter(
				csvFileWriter);
		csvBufferedWriter.write(csvText.toString());
		csvBufferedWriter.close();
		System.out.println("Created new file " + fileName);
		// Reset the csvText StringBuffer.
		csvText = new StringBuffer(csvHeader());
	}


	private static String csvLine(String repository, Image image) {
		// Create a text line for each image, where the attributes are
		// comma-separated.
		final StringBuffer toReturn = new StringBuffer();
		toReturn.append(repository);
		toReturn.append(",");
		toReturn.append(image.getImageId());
		toReturn.append(",");
		toReturn.append(format(image.getImageLocation()));
		toReturn.append(",");
		toReturn.append(format(image.getImageOwnerAlias()));
		toReturn.append(",");
		toReturn.append(format(image.getOwnerId()));
		toReturn.append(",");
		toReturn.append(format(image.getName()));
		toReturn.append(",");
		toReturn.append(format(image.getDescription()));
		toReturn.append(",");
		toReturn.append(format(image.getArchitecture()));
		toReturn.append(",");
		toReturn.append(format(image.getPlatform()));
		toReturn.append(",");
		toReturn.append(format(image.getImageType()));
		toReturn.append("\n");
		return toReturn.toString();
	}

	private static Object format(String str) {
		if(str!=null)
			return str.replace(",", ".");
		else
			return "";
	}

	private static String csvHeader() {
		// The attributes in the csv must be ordered like this:
		StringBuffer csvHeader = new StringBuffer(
				"repository,imageId,imageLocation,imageOwnerAlias,ownerId,name,description,architecture,platform,imageType");
		csvHeader.append("\n");
		return csvHeader.toString();
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
