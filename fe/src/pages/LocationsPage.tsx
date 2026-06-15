import { useEffect, useMemo } from 'react';
import { Box, Divider, Typography } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import type { ColumnDef } from '@tanstack/react-table';
import { locationsResource, zonesResource } from '@/api/resources';
import { LOCATION_TYPES, type LocationRequest, type LocationResponse } from '@/api/types';
import { PageHeader } from '@/components/PageHeader';
import { DataTable } from '@/components/DataTable';
import { FormDialog } from '@/components/FormDialog';
import { ConfirmDialog } from '@/components/ConfirmDialog';
import { RHFTextField } from '@/components/form/RHFTextField';
import { RHFSelectField } from '@/components/form/RHFSelectField';
import { RHFCheckboxField } from '@/components/form/RHFCheckboxField';
import { LocationPicker } from '@/components/locations/LocationPicker';
import { useTableState } from '@/hooks/useTableState';
import { useCrudController } from '@/hooks/useCrudController';
import { useIsAdmin } from '@/store/hooks';

const schema = z
  .object({
    code: z.string().min(1, 'Code is required').max(50),
    displayCode: z.string().max(50).optional().or(z.literal('')),
    type: z.enum(['RIVER', 'CANAL', 'RESERVOIR', 'PUMP_STATION']),
    zoneId: z.number().nullable().optional(),
    posX: z.number().nullable().optional(),
    posY: z.number().nullable().optional(),
    active: z.boolean(),
    hasWeather: z.boolean(),
    precipitation: z.number().nullable().optional(),
  })
  .refine((v) => !v.hasWeather || v.precipitation != null, {
    message: 'Precipitation is required when weather data is enabled',
    path: ['precipitation'],
  });
type FormValues = z.infer<typeof schema>;

const emptyValues: FormValues = {
  code: '',
  displayCode: '',
  type: 'RIVER',
  zoneId: null,
  posX: null,
  posY: null,
  active: true,
  hasWeather: false,
  precipitation: null,
};

export function LocationsPage() {
  const isAdmin = useIsAdmin();
  const table = useTableState(10, [{ id: 'code', desc: false }]);
  const { data, isFetching } = locationsResource.useList({
    page: table.page,
    size: table.pageSize,
    sort: table.sortParam,
  });
  const { data: zones } = zonesResource.useOptions();
  const crud = useCrudController<LocationResponse, LocationRequest>(locationsResource, {
    entity: 'Location',
  });

  const { control, handleSubmit, reset, watch, setValue } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: emptyValues,
  });
  const hasWeather = watch('hasWeather');
  const posX = watch('posX');
  const posY = watch('posY');

  useEffect(() => {
    if (crud.formOpen) {
      const e = crud.editing;
      reset(
        e
          ? {
              code: e.code,
              displayCode: e.displayCode ?? '',
              type: e.type,
              zoneId: e.zoneId,
              posX: e.posX,
              posY: e.posY,
              active: e.active,
              hasWeather: Boolean(e.weatherCondition),
              precipitation: e.weatherCondition?.precipitation ?? null,
            }
          : emptyValues,
      );
    }
  }, [crud.formOpen, crud.editing, reset]);

  const columns = useMemo<ColumnDef<LocationResponse, any>[]>(
    () => [
      { accessorKey: 'code', header: 'Code' },
      { accessorKey: 'type', header: 'Type' },
      { accessorKey: 'zoneCode', header: 'Zone', enableSorting: false, cell: (c) => c.getValue() || '-' },
      {
        id: 'precipitation',
        header: 'Precip.',
        enableSorting: false,
        cell: (c) => c.row.original.weatherCondition?.precipitation ?? '-',
      },
      {
        accessorKey: 'active',
        header: 'Active',
        enableSorting: false,
        cell: (c) => (c.getValue() ? 'Yes' : 'No'),
      },
    ],
    [],
  );

  const onValid = (v: FormValues) =>
    crud.submit({
      code: v.code,
      displayCode: v.displayCode || null,
      type: v.type,
      zoneId: v.zoneId ?? null,
      posX: v.posX ?? null,
      posY: v.posY ?? null,
      active: v.active,
      weatherCondition: v.hasWeather ? { precipitation: v.precipitation ?? 0 } : null,
    });

  return (
    <Box>
      <PageHeader
        title="Locations"
        subtitle="Monitoring sites - weather is managed here, not separately"
        addLabel="Add Location"
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
        title={crud.editing ? 'Edit Location' : 'Add Location'}
        onClose={crud.closeForm}
        onSubmit={handleSubmit(onValid)}
        submitting={crud.saving}
        errorMessage={crud.submitError}
        maxWidth="md"
      >
        <RHFTextField name="code" control={control} label="Code" />
        <RHFTextField name="displayCode" control={control} label="Display Code" />
        <RHFSelectField
          name="type"
          control={control}
          label="Type"
          options={LOCATION_TYPES.map((t) => ({ value: t, label: t }))}
        />
        <RHFSelectField
          name="zoneId"
          control={control}
          label="Zone"
          numeric
          allowEmpty
          options={(zones ?? []).map((z) => ({
            value: z.id,
            label: z.name ? `${z.code} - ${z.name}` : z.code,
          }))}
        />
        <LocationPicker
          lat={posX}
          lng={posY}
          onChange={(lat, lng) => {
            setValue('posX', lat, { shouldValidate: true, shouldDirty: true });
            setValue('posY', lng, { shouldValidate: true, shouldDirty: true });
          }}
        />
        <Box sx={{ display: 'flex', gap: 2 }}>
          <RHFTextField name="posX" control={control} label="Position X (lat)" type="number" />
          <RHFTextField name="posY" control={control} label="Position Y (lng)" type="number" />
        </Box>
        <RHFCheckboxField name="active" control={control} label="Active" />

        <Divider />
        <Typography variant="subtitle2" color="text.secondary">
          Weather condition
        </Typography>
        <RHFCheckboxField name="hasWeather" control={control} label="This location has weather data" />
        {hasWeather && (
          <RHFTextField
            name="precipitation"
            control={control}
            label="Precipitation (mm)"
            type="number"
          />
        )}
      </FormDialog>

      <ConfirmDialog
        open={Boolean(crud.deleteTarget)}
        message={`Delete location "${crud.deleteTarget?.code}"? This cannot be undone.`}
        loading={crud.deleting}
        onConfirm={crud.confirmDelete}
        onClose={() => crud.setDeleteTarget(null)}
      />
    </Box>
  );
}
