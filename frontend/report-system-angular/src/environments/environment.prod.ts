export const environment = {
  production: true,
  apiUrl: '/api',
  keycloak: {
    issuer: '/auth/realms/report-system',
    clientId: 'report-system-frontend',
  },
  defaultTenantId: '00000000-0000-0000-0000-000000000001',
};
