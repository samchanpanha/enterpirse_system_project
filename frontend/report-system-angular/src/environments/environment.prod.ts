export const environment = {
  production: true,
  apiUrl: '/api',
  keycloak: {
    issuer: '/auth/realms/demo-corp',
    clientId: 'report-system-web',
  },
  defaultTenantId: '00000000-0000-0000-0000-000000000001',
};
