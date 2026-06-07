import { useEffect, useMemo } from 'react';
import { Box } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import type { ColumnDef } from '@tanstack/react-table';
import { thresholdConfigsResource } from '@/api/resources';
import {
  LOCATION_TYPES,
  PARAMETER_TYPES,
  type ThresholdConfigRequest,
  type ThresholdConfigResponse,
} from '@/api/types';
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
  locationType: z.enum(['RIVER', 'CANAL', 'RESERVOIR', 'PUMP_STATION']),
  parameterType: z.enum(['WATER_LEVEL', 'FLOW_RATE']),
  normalMax: z.number({ invalid_type_error: 'Required' }),
  warningMax: z.number({ invalid_type_error: 'Required' }),
  criticalMax: z.number().optional(),
});
type FormValues = z.infer<typeof schema>;

const emptyValues: Partial<FormValues> = {
  locationType: undefined,
  parameterType: undefined,
  normalMax: undefined,
  warningMax: undefined,
  criticalMax: undefined,
};

export function ThresholdConfigsPage() {
  const isAdmin = useIsAdmin();
  const table = useTableState(10, [{ id: 'locationType', desc: false }]);
  const { data, isFetching } = thresholdConfigsResource.useList({
    page: table.page,
    size: table.pageSize,
    sort: table.sortParam,
  });
  const crud = useCrudController<ThresholdConfigResponse, ThresholdConfigRequest>(
    thresholdConfigsResource,
    { entity: 'Threshold config' },
  );

  const { control, handleSubmit, reset } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: emptyValues as FormValues,
  });

  useEffect(() => {
    if (crud.formOpen) {
      reset(
        crud.editing
          ? {
              locationType: crud.editing.locationType,
              parameterType: crud.editing.parameterType,
              normalMax: crud.editing.normalMax,
              warningMax: crud.editing.warningMax,
              criticalMax: crud.editing.criticalMax ?? undefined,
            }
          : (emptyValues as FormValues),
      );
    }
  }, [crud.formOpen, crud.editing, reset]);

  const columns = useMemo<ColumnDef<ThresholdConfigResponse, any>[]>(
    () => [
      { accessorKey: 'locationType', header: 'Location Type' },
      { accessorKey: 'parameterType', header: 'Parameter' },
      { accessorKey: 'normalMax', header: 'Normal Max' },
      { accessorKey: 'warningMax', header: 'Warning Max' },
      { accessorKey: 'criticalMax', header: 'Critical Max', cell: (c) => c.getValue() ?? '-' },
    ],
    [],
  );

  const onValid = (values: FormValues) =>
    crud.submit({
      locationType: values.locationType,
      parameterType: values.parameterType,
      normalMax: values.normalMax,
      warningMax: values.warningMax,
      criticalMax: values.criticalMax ?? null,
    });

  return (
    <Box>
      <PageHeader
        title="Threshold Configs"
        subtitle="Normal / warning / critical limits per location & parameter"
        addLabel="Add Threshold"
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
        title={crud.editing ? 'Edit Threshold' : 'Add Threshold'}
        onClose={crud.closeForm}
        onSubmit={handleSubmit(onValid)}
        submitting={crud.saving}
        errorMessage={crud.submitError}
      >
        <RHFSelectField
          name="locationType"
          control={control}
          label="Location Type"
          options={LOCATION_TYPES.map((t) => ({ value: t, label: t }))}
        />
        <RHFSelectField
          name="parameterType"
          control={control}
          label="Parameter Type"
          options={PARAMETER_TYPES.map((t) => ({ value: t, label: t }))}
        />
        <RHFTextField name="normalMax" control={control} label="Normal Max" type="number" />
        <RHFTextField name="warningMax" control={control} label="Warning Max" type="number" />
        <RHFTextField
          name="criticalMax"
          control={control}
          label="Critical Max (optional)"
          type="number"
        />
      </FormDialog>

      <ConfirmDialog
        open={Boolean(crud.deleteTarget)}
        message={`Delete this ${crud.deleteTarget?.locationType} / ${crud.deleteTarget?.parameterType} threshold? This cannot be undone.`}
        loading={crud.deleting}
        onConfirm={crud.confirmDelete}
        onClose={() => crud.setDeleteTarget(null)}
      />
    </Box>
  );
}
