export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  keycloak: {
    issuer: 'http://localhost:8180/realms/demo-corp',
    clientId: 'report-system-web',
  },
  defaultTenantId: '00000000-0000-0000-0000-000000000001',
};
