package com.randreucetti.portfolio.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleemail.*;
import com.amazonaws.services.simpleemail.model.*;
import com.amazonaws.regions.*;
import com.opensymphony.xwork2.ActionSupport;

public class ContactAction extends ActionSupport {

	private static final long serialVersionUID = 1250672710718682278L;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	static final String FROM = "ross.andreucetti@gmail.com";
	static final String TO = "ross.andreucetti@gmail.com";

	private String name;
	private String email;
	private String subject;
	private String message;

	public void contact() {
		logger.info("Name: {}", name);
		logger.info("email: {}", email);
		logger.info("subject: {}", subject);
		logger.info("message: {}", message);

		Destination destination = new Destination().withToAddresses(new String[] { TO });

		// Create the subject and body of the message.
		Content subjectCtx = new Content().withData(subject);
		Content textBody = new Content().withData(message);
		Body body = new Body().withText(textBody);

		// Create a message with the specified subject and body.
		Message message = new Message().withSubject(subjectCtx).withBody(body);

		// Assemble the email.
		SendEmailRequest request = new SendEmailRequest().withSource(FROM).withDestination(destination)
				.withMessage(message).withReplyToAddresses(email);

		try {
			logger.info("Attempting to send an email through Amazon SES by using the AWS SDK for Java...");

			// Instantiate an Amazon SES client, which will make the service
			// call. The service call requires your AWS credentials.
			// Because we're not providing an argument when instantiating the
			// client, the SDK will attempt to find your AWS credentials
			// using the default credential provider chain. The first place the
			// chain looks for the credentials is in environment variables
			// AWS_ACCESS_KEY_ID and AWS_SECRET_KEY.
			// For more information, see
			// http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html
			AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient();

			// Choose the AWS region of the Amazon SES endpoint you want to
			// connect to. Note that your sandbox
			// status, sending limits, and Amazon SES identity-related settings
			// are specific to a given AWS
			// region, so be sure to select an AWS region in which you set up
			// Amazon SES. Here, we are using
			// the US West (Oregon) region. Examples of other regions that
			// Amazon SES supports are US_EAST_1
			// and EU_WEST_1. For a complete list, see
			// http://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html
			Region REGION = Region.getRegion(Regions.US_EAST_1);
			client.setRegion(REGION);

			// Send the email.
			client.sendEmail(request);
			logger.info("Email sent!");
		} catch (Exception ex) {
			logger.error("The email was not sent.");
			logger.error("Error message: " + ex.getMessage());
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
