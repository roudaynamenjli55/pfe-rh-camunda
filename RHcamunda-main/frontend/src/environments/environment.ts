export const environment = {
  production: false,
  apiUrl: 'http://localhost:8081/api',  // ← CHANGE 8080 → 8081 ✅
  keycloak: {
    url: 'http://localhost:8080',        // ← Keycloak reste sur 8080 ✅
    realm: 'rhcamunda-realm',
    clientId: 'angular-frontend'
  }
};