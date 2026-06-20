export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  keycloak: {
    issuer: 'http://localhost:8180/realms/report-system',
    clientId: 'report-system-frontend',
  },
  defaultTenantId: '00000000-0000-0000-0000-000000000001',
};
