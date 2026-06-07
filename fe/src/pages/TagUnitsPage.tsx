import { useEffect, useMemo } from 'react';
import { Box } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import type { ColumnDef } from '@tanstack/react-table';
import { tagUnitsResource } from '@/api/resources';
import type { TagUnitRequest, TagUnitResponse } from '@/api/types';
import { PageHeader } from '@/components/PageHeader';
import { DataTable } from '@/components/DataTable';
import { FormDialog } from '@/components/FormDialog';
import { ConfirmDialog } from '@/components/ConfirmDialog';
import { RHFTextField } from '@/components/form/RHFTextField';
import { useTableState } from '@/hooks/useTableState';
import { useCrudController } from '@/hooks/useCrudController';
import { useIsAdmin } from '@/store/hooks';

const schema = z.object({
  code: z.string().min(1, 'Code is required').max(50),
  unit: z.string().min(1, 'Unit is required').max(50),
  description: z.string().max(255).optional().or(z.literal('')),
});
type FormValues = z.infer<typeof schema>;

const emptyValues: FormValues = { code: '', unit: '', description: '' };

export function TagUnitsPage() {
  const isAdmin = useIsAdmin();
  const table = useTableState(10, [{ id: 'code', desc: false }]);
  const { data, isFetching } = tagUnitsResource.useList({
    page: table.page,
    size: table.pageSize,
    sort: table.sortParam,
  });
  const crud = useCrudController<TagUnitResponse, TagUnitRequest>(tagUnitsResource, {
    entity: 'Tag unit',
  });

  const { control, handleSubmit, reset } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: emptyValues,
  });

  useEffect(() => {
    if (crud.formOpen) {
      reset(
        crud.editing
          ? { code: crud.editing.code, unit: crud.editing.unit, description: crud.editing.description ?? '' }
          : emptyValues,
      );
    }
  }, [crud.formOpen, crud.editing, reset]);

  const columns = useMemo<ColumnDef<TagUnitResponse, any>[]>(
    () => [
      { accessorKey: 'code', header: 'Code' },
      { accessorKey: 'unit', header: 'Unit' },
      { accessorKey: 'description', header: 'Description', cell: (c) => c.getValue() || '-' },
    ],
    [],
  );

  const onValid = (values: FormValues) =>
    crud.submit({ code: values.code, unit: values.unit, description: values.description || null });

  return (
    <Box>
      <PageHeader
        title="Tag Units"
        subtitle="Engineering units used by sensors (e.g. m, m³/s)"
        addLabel="Add Tag Unit"
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
        title={crud.editing ? 'Edit Tag Unit' : 'Add Tag Unit'}
        onClose={crud.closeForm}
        onSubmit={handleSubmit(onValid)}
        submitting={crud.saving}
        errorMessage={crud.submitError}
      >
        <RHFTextField name="code" control={control} label="Code" />
        <RHFTextField name="unit" control={control} label="Unit" />
        <RHFTextField name="description" control={control} label="Description" multiline minRows={2} />
      </FormDialog>

      <ConfirmDialog
        open={Boolean(crud.deleteTarget)}
        message={`Delete tag unit "${crud.deleteTarget?.code}"? This cannot be undone.`}
        loading={crud.deleting}
        onConfirm={crud.confirmDelete}
        onClose={() => crud.setDeleteTarget(null)}
      />
    </Box>
  );
}
