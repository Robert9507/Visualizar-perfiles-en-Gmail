package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class Main {

    private static final String APPLICATION_NAME = "Google People API Example";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport httpTransport;
    private static PeopleService peopleService;

    public static void main(String[] args) {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = authorize();
            peopleService = new PeopleService.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            String targetEmail = "target.email@gmail.com"; // Reemplaza con el correo electrónico de destino
            Person profile = getProfile(targetEmail);
            if (profile != null) {
                System.out.println("Display Name: " + profile.getNames().get(0).getDisplayName());
                System.out.println("Email: " + profile.getEmailAddresses().get(0).getValue());
                // Agrega más campos según sea necesario
            } else {
                System.out.println("Perfil no encontrado.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Credential authorize() throws Exception {
        InputStream in = GooglePeopleApiExample.class.getResourceAsStream("/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, Collections.singleton("https://www.googleapis.com/auth/contacts.readonly"))
                .setDataStoreFactory(new MemoryDataStoreFactory())
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    private static Person getProfile(String email) throws IOException {
        ListConnectionsResponse connectionsResponse = peopleService.people().connections()
                .list("people/me")
                .setPersonFields("names,emailAddresses")
                .setPageSize(10)
                .execute();

        List<Person> connections = connectionsResponse.getConnections();
        for (Person person : connections) {
            List<EmailAddress> emails = person.getEmailAddresses();
            for (EmailAddress emailAddress : emails) {
                if (emailAddress.getValue().equalsIgnoreCase(email)) {
                    return person;
                }
            }
        }

        return null;
    }
}