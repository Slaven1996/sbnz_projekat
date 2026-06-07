import { createCrudResource } from '@/hooks/createCrudResource';
import type {
  DepartmentRequest,
  DepartmentResponse,
  LocationRequest,
  LocationResponse,
  SensorRequest,
  SensorResponse,
  TagUnitRequest,
  TagUnitResponse,
  ThresholdConfigRequest,
  ThresholdConfigResponse,
  UserRequest,
  UserResponse,
  ZoneRequest,
  ZoneResponse,
} from './types';

export const departmentsResource = createCrudResource<DepartmentResponse, DepartmentRequest>({
  key: 'departments',
});

export const zonesResource = createCrudResource<ZoneResponse, ZoneRequest>({
  key: 'zones',
});

export const tagUnitsResource = createCrudResource<TagUnitResponse, TagUnitRequest>({
  key: 'tag-units',
});

export const locationsResource = createCrudResource<LocationResponse, LocationRequest>({
  key: 'locations',
});

export const sensorsResource = createCrudResource<SensorResponse, SensorRequest>({
  key: 'sensors',
});

export const thresholdConfigsResource = createCrudResource<
  ThresholdConfigResponse,
  ThresholdConfigRequest
>({
  key: 'threshold-configs',
});

export const usersResource = createCrudResource<UserResponse, UserRequest>({
  key: 'users',
});
