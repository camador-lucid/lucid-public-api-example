# Lucid Example App

This application exemplifies how to build an integration using Lucid's public API.

## General Overview

The application was built using [Sprig Boot 3.0.0](https://spring.io/projects/spring-boot). Refer to the HELP.md file for more instructions about building/running the application.

The application supports creating multiple application users, you can then get an OAuth2 access token for each of them using the OAuth2 Authorization Code Grant.
Once a user has a valid access token, it will be used to get their Lucid profile information, which will be displayed in the main homepage. 

## Project structure

* `src/main/kotlin/com/camador/oauth2/springboot/`:
  * `config`: contains the classes which information is populated via `*.properties` files.
  * `model`: contains the data classes used to represent the information exchanged in API requests and some application data.
  * `repository`: it is the data-access layer, in our cases the repositories use in-memory storage.
  * `service`: contains class responsible for communicating to external services, in our case the Lucid API.
  * `controller`: contain the application controller based on the MVC pattern.
* `src/resources/`
  * `application.properties`: configuration values for out application.
  * `public`: contains the js script in charge of handling the OAuth2 authentication flow in the client.
  * `templates`: contains the application html templates.

## OAuth2 Authentication

### Registering a Lucid OAuth2 App
It is a pre-requisite to register a Lucid OAuth2 to use this example application. Look at Lucid's documentation for more information on [how to register an app](https://developer.lucid.co/api/v1/#app-registration).

Once the app is registered. Update the following congiration values in `application.properties`:
* `lucid.oauth2.clientId`: use your application id.
* `lucid.oauth2.clientSecret`: use your application secret.

> **Note:** Remember to keep your Lucid OAuth2 client secret safe.

Additionally, make sure you registered this application redirect URI. You can specify which port the application uses by changing the `server.port` value in `application.properties`.

> If you don't change the port, the redirect URI you should register is: `http://localhost:8081/oauth2/redirect`

### Getting an OAuth2 token
This project implements the [OAuth2 Authorization Code Grant](https://www.rfc-editor.org/rfc/rfc6749#section-4.1) to get access tokens and performs requests to the [Lucid API](https://developer.lucid.co/api/v1/#authentication).

![OAuth2 Authorization Code Grant](https://images.ctfassets.net/cdy7uua7fh8z/2nbNztohyR7uMcZmnUt0VU/2c017d2a2a2cdd80f097554d33ff72dd/auth-sequence-auth-code.png)

This is how each step occurs in the application:
1. The user clicks on the button with the id `oauth2-flow-start`.
2. The `startAuthorizationFlow` of the `connect.js` script executes when the button is clicked. It opens a popup window and loads the `/oauth2/authorize/:userId` route.
3. The request of the popup window is handle by the `authorize` method in the `OAuth2Controller`. This method adds a couple security cookies, the OAuth2 `state` parameter, and redirects to Lucid's authorization url.
4. The user is presented with the Lucid authorization page. They can either grant or deny the access for the application.
5. If the user granted access, the authorization server will redirect to the provided redirect URI, the authorization code and the provided state will be provided via query parameters.
6. The redirect is handled by the `handleRedirect` method on the `OAuth2Controller`. It will do the following steps:
   1. It verifies the `state` using the [Double Submit Cookie](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html#double-submit-cookie) strategy.
   2. Verifies the information set via cookies to tie the granted token to an application user.
   3. Executes the OAuth2 Authorization Code Grant request to the `/oauth2/token` route of Lucid's authorization server.
7. Lucid's authorization server validates the OAuth2 client credentials provided as well as the authorization code. 
8. If the information is valid, the authorization server issues an OAuth2 token.
   1. The application persists the OAuth2 token information and ties it to the application user.
   2. Then, redirects to the `handleConnectionResult` view. Which communicates to the opener window signaling the finalization of the OAuth2 flow.
   3. The parent window then closes the popup window and reloads the page.
9. When the homepage is loaded: 
   1. The application fetches all the persisted users. 
   2. Then for each of them that have a valid access token, it performs a [Get User Profile](https://developer.lucid.co/api/v1/#get-profile84) request.
   3. Then loads the homepage application, this time displaying the profile information for the connected users.

> **Note:** For more in-depth information on each step, look at the comments in the code. 

### Maintaining OAuth2 tokens

OAuth2 tokens must be stored in a safe location, usually a database. 
It is important to know which OAuth2 token to use for a certain application user, this application uses the `OAuth2Repository` to persist data tying a application user id to an `OAuth2AuthenticationData` instance.

Access tokens are short-lived, look at [Lucid's documentation](https://developer.lucid.co/api/v1/#refreshing-access-tokens) for up-to-date information on access token lifetime.
If your application require to have access over a long period of time you must include the `offline_access` in the requested scopes when granting a token. 
If you do, the token response will include an [OAuth2 Refresh Token](https://datatracker.ietf.org/doc/html/rfc6749#section-1.5) which you can use to get a new access token.

The `OAuth2TokenRefresher` class encapsulates the logic of getting a valid access token:
1. It checks if the access token is expired. 
2. If it is not, it will return the existent access token.
3. Otherwise, it uses the refresh token to get a new access token (look at the [Refreshing the access token](https://developer.lucid.co/api/v1/#refreshing-access-tokens) section).

### Using an access token

Once you have obtained a valid access token for a given application user, you can use it make a Lucid API request. The `LucidWebService` class implements the [Get Profile](https://developer.lucid.co/api/v1/#get-profile84) API request.
Considerations:
1. Setting the `Lucid-Api-Version` header to specify the [API version](https://developer.lucid.co/api/v1/#headers).
2. Setting the `Accept` header value to `application/json`.
3. Setting the valid access token with the Bearer format in the `Authorization` header.

---
Further use cases will be added to this application in the future. Please let us know which feature you would like to see in the application.