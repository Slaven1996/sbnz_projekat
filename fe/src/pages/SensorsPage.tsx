import { useEffect, useMemo } from 'react';
import { Box } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import type { ColumnDef } from '@tanstack/react-table';
import { locationsResource, sensorsResource, tagUnitsResource } from '@/api/resources';
import { SENSOR_TYPES, type SensorRequest, type SensorResponse } from '@/api/types';
import { PageHeader } from '@/components/PageHeader';
import { DataTable } from '@/components/DataTable';
import { FormDialog } from '@/components/FormDialog';
import { ConfirmDialog } from '@/components/ConfirmDialog';
import { RHFTextField } from '@/components/form/RHFTextField';
import { RHFSelectField } from '@/components/form/RHFSelectField';
import { useTableState } from '@/hooks/useTableState';
import { useCrudController } from '@/hooks/useCrudController';
import { useIsAdmin } from '@/store/hooks';

const schema = z.object({
  locationId: z.number({ invalid_type_error: 'Location is required' }),
  tagName: z.string().min(1, 'Tag name is required').max(100),
  displayCode: z.string().max(100).optional().or(z.literal('')),
  sensorType: z.enum(['WATER_LEVEL', 'FLOW_RATE', 'PUMP_STATUS']),
  unitId: z.number().nullable().optional(),
  engLow: z.number().nullable().optional(),
  engHigh: z.number().nullable().optional(),
  rawLow: z.number().nullable().optional(),
  rawHigh: z.number().nullable().optional(),
  logInterval: z.number().nullable().optional(),
});
type FormValues = z.infer<typeof schema>;

const emptyValues: Partial<FormValues> = {
  locationId: undefined,
  tagName: '',
  displayCode: '',
  sensorType: 'WATER_LEVEL',
  unitId: null,
  engLow: null,
  engHigh: null,
  rawLow: null,
  rawHigh: null,
  logInterval: null,
};

export function SensorsPage() {
  const isAdmin = useIsAdmin();
  const table = useTableState(10, [{ id: 'tagName', desc: false }]);
  const { data, isFetching } = sensorsResource.useList({
    page: table.page,
    size: table.pageSize,
    sort: table.sortParam,
  });
  const { data: locations } = locationsResource.useOptions();
  const { data: units } = tagUnitsResource.useOptions();
  const crud = useCrudController<SensorResponse, SensorRequest>(sensorsResource, {
    entity: 'Sensor',
  });

  const { control, handleSubmit, reset } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: emptyValues as FormValues,
  });

  useEffect(() => {
    if (crud.formOpen) {
      const e = crud.editing;
      reset(
        e
          ? {
              locationId: e.locationId ?? undefined,
              tagName: e.tagName,
              displayCode: e.displayCode ?? '',
              sensorType: e.sensorType,
              unitId: e.unitId,
              engLow: e.engLow,
              engHigh: e.engHigh,
              rawLow: e.rawLow,
              rawHigh: e.rawHigh,
              logInterval: e.logInterval,
            }
          : (emptyValues as FormValues),
      );
    }
  }, [crud.formOpen, crud.editing, reset]);

  const columns = useMemo<ColumnDef<SensorResponse, any>[]>(
    () => [
      { accessorKey: 'tagName', header: 'Tag Name' },
      { accessorKey: 'sensorType', header: 'Type' },
      {
        accessorKey: 'locationCode',
        header: 'Location',
        enableSorting: false,
        cell: (c) => c.getValue() || '—',
      },
      { accessorKey: 'unitCode', header: 'Unit', enableSorting: false, cell: (c) => c.getValue() || '—' },
      {
        accessorKey: 'logInterval',
        header: 'Log Interval',
        enableSorting: false,
        cell: (c) => c.getValue() ?? '—',
      },
    ],
    [],
  );

  const onValid = (v: FormValues) =>
    crud.submit({
      locationId: v.locationId,
      tagName: v.tagName,
      displayCode: v.displayCode || null,
      sensorType: v.sensorType,
      unitId: v.unitId ?? null,
      engLow: v.engLow ?? null,
      engHigh: v.engHigh ?? null,
      rawLow: v.rawLow ?? null,
      rawHigh: v.rawHigh ?? null,
      logInterval: v.logInterval ?? null,
    });

  return (
    <Box>
      <PageHeader
        title="Sensors"
        subtitle="Measurement tags attached to locations"
        addLabel="Add Sensor"
        onAdd={isAdmin ? crud.openCreate : undefined}
      />

      <DataTable
        columns={columns}
        data={data?.content ?? []}
        loading={isFetching}
        page={table.page}
        pageSize={table.pageSize}
        totalElements={data?.totalElements ?? 0}
        onPageChange={table.setPage}
        onPageSizeChange={table.setPageSize}
        sorting={table.sorting}
        onSortingChange={table.onSortingChange}
        onEdit={isAdmin ? crud.openEdit : undefined}
        onDelete={isAdmin ? crud.setDeleteTarget : undefined}
      />

      <FormDialog
        open={crud.formOpen}
        title={crud.editing ? 'Edit Sensor' : 'Add Sensor'}
        onClose={crud.closeForm}
        onSubmit={handleSubmit(onValid)}
        submitting={crud.saving}
        errorMessage={crud.submitError}
      >
        <RHFSelectField
          name="locationId"
          control={control}
          label="Location"
          numeric
          options={(locations ?? []).map((l) => ({ value: l.id, label: l.code }))}
        />
        <RHFTextField name="tagName" control={control} label="Tag Name" />
        <RHFTextField name="displayCode" control={control} label="Display Code" />
        <RHFSelectField
          name="sensorType"
          control={control}
          label="Sensor Type"
          options={SENSOR_TYPES.map((t) => ({ value: t, label: t }))}
        />
        <RHFSelectField
          name="unitId"
          control={control}
          label="Unit"
          numeric
          allowEmpty
          options={(units ?? []).map((u) => ({ value: u.id, label: `${u.code} (${u.unit})` }))}
        />
        <RHFTextField name="engLow" control={control} label="Eng Low" type="number" />
        <RHFTextField name="engHigh" control={control} label="Eng High" type="number" />
        <RHFTextField name="rawLow" control={control} label="Raw Low" type="number" />
        <RHFTextField name="rawHigh" control={control} label="Raw High" type="number" />
        <RHFTextField name="logInterval" control={control} label="Log Interval (s)" type="number" />
      </FormDialog>

      <ConfirmDialog
        open={Boolean(crud.deleteTarget)}
        message={`Delete sensor "${crud.deleteTarget?.tagName}"? This cannot be undone.`}
        loading={crud.deleting}
        onConfirm={crud.confirmDelete}
        onClose={() => crud.setDeleteTarget(null)}
      />
    </Box>
  );
}
