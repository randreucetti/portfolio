package com.randreucetti.portfolio.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.EmailValidator;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.opensymphony.xwork2.ActionSupport;

public class ContactAction extends ActionSupport {

	private static final long serialVersionUID = 1250672710718682278L;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	static final String FROM = "contact@randreucetti.com";
	static final String TO = "ross.andreucetti@gmail.com";

	private String name;
	private String email;
	private String subject;
	private String message;
	private String recaptchaResponse;

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
		SendEmailRequest request = new SendEmailRequest().withSource(FROM).withDestination(destination).withMessage(message)
				.withReplyToAddresses(email);

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

	public void validate() {
		logger.info("Validate being called.");
		if (StringUtils.isBlank(name)) {
			this.addFieldError("name", "Name must not be blank");
		}
		if (EmailValidator.getInstance().isValid(email)) {
			this.addFieldError("email", "Please enter a valid email");
		}
		if (StringUtils.isBlank(subject)) {
			this.addFieldError("subject", "Subject must not be blank");
		}
		if (StringUtils.isBlank(message)) {
			this.addFieldError("message", "Message must not be empty");
		}
		if(!isCaptchaValid(recaptchaResponse)){
			this.addFieldError("captcha", "Recaptcha could not validate you");
		}

		logger.info("recaptcha: {}", recaptchaResponse);
	}

	private boolean isCaptchaValid(String recaptchaResponse) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("https://www.google.com/recaptcha/api/siteverify");
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("secret", "6LeYcwUTAAAAABQ04HpD7qlnRfGKiUrLB9Yge5Yw"));
			nameValuePairs.add(new BasicNameValuePair("response", recaptchaResponse));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
			String json_string = EntityUtils.toString(response.getEntity());
			JSONObject jsonResponse = new JSONObject(json_string);
			if(jsonResponse.getBoolean("success")){
				logger.info("Recaptcha validation has succeeded");
				return true;
			} else {
				logger.error("Recaptcha validation failed with error codes: {}", jsonResponse.get("error-codes"));
				return false;
			}
		} catch (IOException e) {
			logger.error("Caught IOException: {}", e.getMessage());
			return false;
		} catch (JSONException e) {
			logger.error("Caught JSONException: {}", e.getMessage());
			return false;
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

	public String getRecaptchaResponse() {
		return recaptchaResponse;
	}

	public void setRecaptchaResponse(String recaptchaResponse) {
		this.recaptchaResponse = recaptchaResponse;
	}

}
