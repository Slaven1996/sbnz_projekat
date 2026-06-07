
export type UserRole = 'ADMIN' | 'OPERATOR';
export const USER_ROLES: UserRole[] = ['ADMIN', 'OPERATOR'];

export type LocationType = 'RIVER' | 'CANAL' | 'RESERVOIR' | 'PUMP_STATION';
export const LOCATION_TYPES: LocationType[] = ['RIVER', 'CANAL', 'RESERVOIR', 'PUMP_STATION'];

export type SensorType = 'WATER_LEVEL' | 'FLOW_RATE' | 'PUMP_STATUS';
export const SENSOR_TYPES: SensorType[] = ['WATER_LEVEL', 'FLOW_RATE', 'PUMP_STATUS'];

export type ParameterType = 'WATER_LEVEL' | 'FLOW_RATE';
export const PARAMETER_TYPES: ParameterType[] = ['WATER_LEVEL', 'FLOW_RATE'];

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface UserTokenState {
  access_token: string;
  expires_in: number;
  authority: string;
  userId: number;
}

export interface DepartmentResponse {
  id: number;
  code: string;
  description: string | null;
}
export interface DepartmentRequest {
  code: string;
  description?: string | null;
}

export interface ZoneResponse {
  id: number;
  code: string;
  description: string | null;
  locationCount: number;
}
export interface ZoneRequest {
  code: string;
  description?: string | null;
}

export interface TagUnitResponse {
  id: number;
  code: string;
  unit: string;
  description: string | null;
}
export interface TagUnitRequest {
  code: string;
  unit: string;
  description?: string | null;
}

export interface WeatherConditionDto {
  id?: number | null;
  precipitation: number;
  lastUpdate?: string | null;
}

export interface LocationResponse {
  id: number;
  code: string;
  displayCode: string | null;
  type: LocationType;
  active: boolean;
  posX: number | null;
  posY: number | null;
  departmentId: number | null;
  departmentCode: string | null;
  zoneId: number | null;
  zoneCode: string | null;
  weatherCondition: WeatherConditionDto | null;
}
export interface LocationRequest {
  code: string;
  displayCode?: string | null;
  type: LocationType;
  departmentId?: number | null;
  zoneId?: number | null;
  posX?: number | null;
  posY?: number | null;
  active?: boolean;
  weatherCondition?: WeatherConditionDto | null;
}

export interface SensorResponse {
  id: number;
  tagName: string;
  displayCode: string | null;
  sensorType: SensorType;
  locationId: number | null;
  locationCode: string | null;
  unitId: number | null;
  unitCode: string | null;
  engLow: number | null;
  engHigh: number | null;
  rawLow: number | null;
  rawHigh: number | null;
  logInterval: number | null;
}
export interface SensorRequest {
  locationId: number;
  tagName: string;
  displayCode?: string | null;
  sensorType: SensorType;
  unitId?: number | null;
  engLow?: number | null;
  engHigh?: number | null;
  rawLow?: number | null;
  rawHigh?: number | null;
  logInterval?: number | null;
}

export interface ThresholdConfigResponse {
  id: number;
  locationType: LocationType;
  parameterType: ParameterType;
  normalMax: number;
  warningMax: number;
  criticalMax: number | null;
}
export interface ThresholdConfigRequest {
  locationType: LocationType;
  parameterType: ParameterType;
  normalMax: number;
  warningMax: number;
  criticalMax?: number | null;
}

export interface UserResponse {
  id: number;
  name: string | null;
  lastName: string | null;
  email: string;
  role: UserRole;
  active: boolean;
  departmentId: number | null;
  departmentCode: string | null;
}

export interface UserRequest {
  name?: string | null;
  lastName?: string | null;
  email: string;
  password?: string | null;
  role: UserRole;
  active?: boolean;
  departmentId?: number | null;
}

export interface TrendDataResponse {
  id: number;
  locationCode: string | null;
  tagName: string | null;
  logTime: string | null;
  tagValue: number;
  valid: boolean;
}

export interface TrendDataSearchParams extends PageParams {
  locationCode?: string;
  tagName?: string;
  startDate?: string;
  endDate?: string;
}

export interface ApiError {
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  timestamp?: string;
  fieldErrors?: Record<string, string>;
}
