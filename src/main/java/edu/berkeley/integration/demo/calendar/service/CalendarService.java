package edu.berkeley.integration.demo.calendar.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.cxf.annotations.GZIP;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;

import edu.berkeley.integration.demo.calendar.model.BK_EVT;

@Path("/api")
@GZIP(threshold = 1)
public class CalendarService {

	private Logger logger = Logger.getLogger(CalendarService.class.getSimpleName());

	/** Email of the Service Account */
	private static final String SERVICE_ACCOUNT_EMAIL = "836814093469@developer.gserviceaccount.com";

	/** Path to the Service Account's Private Key file */
	private static final String SERVICE_ACCOUNT_PKCS12_FILE_PATH = "/Users/bewong/Downloads/ea02923bb2df57c98672ea58fa52375f60999dc2-privatekey.p12";

	/** Primary calendar constant */
	private static final String PRIMARY = "primary";

	@POST
	@Path("/")
	@Consumes("application/x-www-form-urlencoded")
	public Response test(@QueryParam("classUID") String classUID, @QueryParam("email") String email) throws Exception {
		HttpClient client = new DefaultHttpClient();
		String uri = String.format("https://apis-qa.berkeley.edu/cxf/asws/classoffering?classUID=%s", classUID);
		logger.log(Level.INFO, uri);
		HttpGet request = new HttpGet(uri);
		HttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() == 200) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(response.getEntity().getContent());
			NodeList lst = dom.getFirstChild().getFirstChild().getChildNodes();
			boolean persist = false;
			BK_EVT bk_evt = new BK_EVT();
			for (int i = 0; i < lst.getLength(); i++) {
				Node node = lst.item(i);
				if (node.getNodeName().equalsIgnoreCase("sections")) {
					NodeList sec_lst = node.getChildNodes();
					for (int k = 0; k < sec_lst.getLength(); k++) {
						Node sec_node = sec_lst.item(k);
						if (sec_node.getNodeName().equalsIgnoreCase("sectionId")) {
							bk_evt.setSectionId(sec_node.getTextContent());
						}
						if (sec_node.getNodeName().equalsIgnoreCase("sectionFormat") && sec_node.getTextContent().equalsIgnoreCase("LEC")) {
							persist = true;
						}
						if (persist && sec_node.getNodeName().equalsIgnoreCase("sectionMeetings")) {
							NodeList mt_lst = sec_node.getChildNodes();
							for (int j = 0; j < mt_lst.getLength(); j++) {
								Node mt_node = mt_lst.item(j);
								// System.out.println(String.format("%s: %s", mt_node.getNodeName(), mt_node.getTextContent()));
								if (mt_node.getNodeName().equalsIgnoreCase("building")) {
									bk_evt.setBuilding(mt_node.getTextContent());
								}
								if (mt_node.getNodeName().equalsIgnoreCase("startTime")) {
									bk_evt.setStartTime(mt_node.getTextContent());
								}
								if (mt_node.getNodeName().equalsIgnoreCase("endTime")) {
									bk_evt.setEndTime(mt_node.getTextContent());
								}
								if (mt_node.getNodeName().equalsIgnoreCase("meetingDay")) {
									bk_evt.setMeetingDay(mt_node.getTextContent());
								}
								if (mt_node.getNodeName().equalsIgnoreCase("room")) {
									bk_evt.setRoom(mt_node.getTextContent());
								}
							}
						}
					}
					if (persist) {
						ArrayList<EventAttendee> attendees = new ArrayList<EventAttendee>();
						attendees.add(new EventAttendee().setEmail(email));

						Event evt = bk_evt.toEvent();
						logger.info(evt.toString());
						if (evt != null) {
							Event recurringEvent = getService(email).events().insert(PRIMARY, evt.setAttendees(attendees)).execute();
							logger.info(recurringEvent.getId());
						}
					}
					persist = false;
				}
			}
		} else {
			logger.log(Level.SEVERE, String.format("%s: %s", response.getStatusLine().getStatusCode(), uri));
		}
		return Response.ok().build();
	}
	
	private Calendar getService(String email) throws GeneralSecurityException, IOException {
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory).setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
				.setServiceAccountScopes(CalendarScopes.CALENDAR).setServiceAccountUser(email)
				.setServiceAccountPrivateKeyFromP12File(new java.io.File(SERVICE_ACCOUNT_PKCS12_FILE_PATH)).build();
		return new Calendar.Builder(httpTransport, jsonFactory, null).setApplicationName("Calendar Demo").setHttpRequestInitializer(credential).build();
	}

	public static void main(String[] argv) throws Exception {
		CalendarService service = new CalendarService();
		service.test("COMPSCI.170.Spring.2013", "bernardw@testg.berkeley.edu");
//		System.out.println(new Integer("0912 PM".substring(0, 2)));
	}

}
