import { useEffect, useMemo } from 'react';
import { Box } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import type { ColumnDef } from '@tanstack/react-table';
import { zonesResource } from '@/api/resources';
import type { ZoneRequest, ZoneResponse } from '@/api/types';
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
  name: z.string().max(100).optional().or(z.literal('')),
  description: z.string().max(255).optional().or(z.literal('')),
});
type FormValues = z.infer<typeof schema>;

const emptyValues: FormValues = { code: '', name: '', description: '' };

export function ZonesPage() {
  const isAdmin = useIsAdmin();
  const table = useTableState(10, [{ id: 'code', desc: false }]);
  const { data, isFetching } = zonesResource.useList({
    page: table.page,
    size: table.pageSize,
    sort: table.sortParam,
  });
  const crud = useCrudController<ZoneResponse, ZoneRequest>(zonesResource, { entity: 'Zone' });

  const { control, handleSubmit, reset } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: emptyValues,
  });

  useEffect(() => {
    if (crud.formOpen) {
      reset(
        crud.editing
          ? {
              code: crud.editing.code,
              name: crud.editing.name ?? '',
              description: crud.editing.description ?? '',
            }
          : emptyValues,
      );
    }
  }, [crud.formOpen, crud.editing, reset]);

  const columns = useMemo<ColumnDef<ZoneResponse, any>[]>(
    () => [
      { accessorKey: 'code', header: 'Code' },
      { accessorKey: 'name', header: 'Name', cell: (c) => c.getValue() || '-' },
      { accessorKey: 'description', header: 'Description', cell: (c) => c.getValue() || '-' },
      { accessorKey: 'locationCount', header: 'Locations', enableSorting: false },
    ],
    [],
  );

  const onValid = (values: FormValues) =>
    crud.submit({
      code: values.code,
      name: values.name || null,
      description: values.description || null,
    });

  return (
    <Box>
      <PageHeader
        title="Zones"
        addLabel="Add Zone"
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
        title={crud.editing ? 'Edit Zone' : 'Add Zone'}
        onClose={crud.closeForm}
        onSubmit={handleSubmit(onValid)}
        submitting={crud.saving}
        errorMessage={crud.submitError}
      >
        <RHFTextField name="code" control={control} label="Code" />
        <RHFTextField name="name" control={control} label="Name" />
        <RHFTextField name="description" control={control} label="Description" multiline minRows={2} />
      </FormDialog>

      <ConfirmDialog
        open={Boolean(crud.deleteTarget)}
        message={`Delete zone "${crud.deleteTarget?.code}"? This cannot be undone.`}
        loading={crud.deleting}
        onConfirm={crud.confirmDelete}
        onClose={() => crud.setDeleteTarget(null)}
      />
    </Box>
  );
}
