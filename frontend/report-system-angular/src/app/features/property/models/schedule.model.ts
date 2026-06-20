export interface Schedule {
  id: string;
  tenantId: string;
  branchId: string;
  unitId: string;
  title: string;
  description: string;
  type: string;
  intervalType: string;
  startTime: string;
  endTime: string;
  recurringRule: string;
  status: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface ScheduleRequest {
  unitId: string;
  title: string;
  type: string;
  intervalType: string;
  startTime: string;
  endTime?: string;
  description?: string;
}
