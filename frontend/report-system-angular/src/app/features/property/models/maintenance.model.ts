export interface MaintenanceTicket {
  id: string;
  tenantId: string;
  branchId: string;
  unitId: string;
  reportedBy: string;
  title: string;
  description: string;
  priority: string;
  category: string;
  status: string;
  assignedTo: string;
  costEstimate: number;
  actualCost: number;
  completedAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface MaintenanceRequest {
  unitId: string;
  title: string;
  description?: string;
  priority: string;
  category?: string;
}

export interface MaintenanceStatusRequest {
  status: string;
}

export interface MaintenanceAssignRequest {
  assignedTo: string;
}

export interface MaintenanceCompleteRequest {
  actualCost?: number;
}
