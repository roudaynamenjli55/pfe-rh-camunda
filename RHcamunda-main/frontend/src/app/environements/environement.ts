export const environment = {
  production: false,
  apiUrl: 'http://localhost:8081/api',  // ← Backend URL
  keycloak: {
    url: 'http://localhost:8080',
    realm: 'rhcamunda-realm',
    clientId: 'angular-frontend'
  }
};