import { useEffect, useMemo } from 'react';
import { Box } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import type { ColumnDef } from '@tanstack/react-table';
import { departmentsResource, usersResource } from '@/api/resources';
import { USER_ROLES, type UserRequest, type UserResponse } from '@/api/types';
import { PageHeader } from '@/components/PageHeader';
import { DataTable } from '@/components/DataTable';
import { FormDialog } from '@/components/FormDialog';
import { ConfirmDialog } from '@/components/ConfirmDialog';
import { RHFTextField } from '@/components/form/RHFTextField';
import { RHFSelectField } from '@/components/form/RHFSelectField';
import { RHFCheckboxField } from '@/components/form/RHFCheckboxField';
import { useTableState } from '@/hooks/useTableState';
import { useCrudController } from '@/hooks/useCrudController';

const schema = z
  .object({
    userCode: z.string().min(1, 'User code is required').max(50),
    name: z.string().max(100).optional().or(z.literal('')),
    lastName: z.string().max(100).optional().or(z.literal('')),
    email: z.string().email('Enter a valid e-mail'),
    password: z.string().optional().or(z.literal('')),
    role: z.enum(['ADMIN', 'OPERATOR']),
    active: z.boolean(),
    departmentId: z.number().nullable().optional(),
  })
  .superRefine((v, ctx) => {
    if (v.password && v.password.length > 0 && v.password.length < 6) {
      ctx.addIssue({ code: 'custom', path: ['password'], message: 'Min 6 characters' });
    }
    if (v.role === 'OPERATOR' && v.departmentId == null) {
      ctx.addIssue({
        code: 'custom',
        path: ['departmentId'],
        message: 'Operators must belong to a department',
      });
    }
  });
type FormValues = z.infer<typeof schema>;

const emptyValues: FormValues = {
  userCode: '',
  name: '',
  lastName: '',
  email: '',
  password: '',
  role: 'OPERATOR',
  active: true,
  departmentId: null,
};

export function UsersPage() {
  const table = useTableState(10, [{ id: 'userCode', desc: false }]);
  const { data, isFetching } = usersResource.useList({
    page: table.page,
    size: table.pageSize,
    sort: table.sortParam,
  });
  const { data: departments } = departmentsResource.useOptions();
  const crud = useCrudController<UserResponse, UserRequest>(usersResource, { entity: 'User' });

  const { control, handleSubmit, reset, setError } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: emptyValues,
  });

  useEffect(() => {
    if (crud.formOpen) {
      const e = crud.editing;
      reset(
        e
          ? {
              userCode: e.userCode,
              name: e.name ?? '',
              lastName: e.lastName ?? '',
              email: e.email,
              password: '',
              role: e.role,
              active: e.active,
              departmentId: e.departmentId,
            }
          : emptyValues,
      );
    }
  }, [crud.formOpen, crud.editing, reset]);

  const columns = useMemo<ColumnDef<UserResponse, any>[]>(
    () => [
      { accessorKey: 'userCode', header: 'User Code' },
      {
        id: 'fullName',
        header: 'Name',
        enableSorting: false,
        cell: (c) => [c.row.original.name, c.row.original.lastName].filter(Boolean).join(' ') || '—',
      },
      { accessorKey: 'email', header: 'E-mail' },
      { accessorKey: 'role', header: 'Role' },
      {
        accessorKey: 'departmentCode',
        header: 'Department',
        enableSorting: false,
        cell: (c) => c.getValue() || '—',
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

  const onValid = (v: FormValues) => {
    if (!crud.editing && (!v.password || v.password.length < 6)) {
      setError('password', { message: 'Password (min 6 chars) is required' });
      return;
    }
    const body: UserRequest = {
      userCode: v.userCode,
      name: v.name || null,
      lastName: v.lastName || null,
      email: v.email,
      role: v.role,
      active: v.active,
      departmentId: v.departmentId ?? null,
    };
    if (v.password) body.password = v.password;
    crud.submit(body);
  };

  return (
    <Box>
      <PageHeader
        title="Users"
        subtitle="Application accounts (ADMIN-only management)"
        addLabel="Add User"
        onAdd={crud.openCreate}
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
        onEdit={crud.openEdit}
        onDelete={crud.setDeleteTarget}
      />

      <FormDialog
        open={crud.formOpen}
        title={crud.editing ? 'Edit User' : 'Add User'}
        onClose={crud.closeForm}
        onSubmit={handleSubmit(onValid)}
        submitting={crud.saving}
        errorMessage={crud.submitError}
      >
        <RHFTextField name="userCode" control={control} label="User Code" />
        <RHFTextField name="name" control={control} label="First Name" />
        <RHFTextField name="lastName" control={control} label="Last Name" />
        <RHFTextField name="email" control={control} label="E-mail" type="email" />
        <RHFTextField
          name="password"
          control={control}
          label={crud.editing ? 'New Password (leave blank to keep)' : 'Password'}
          type="password"
        />
        <RHFSelectField
          name="role"
          control={control}
          label="Role"
          options={USER_ROLES.map((r) => ({ value: r, label: r }))}
        />
        <RHFSelectField
          name="departmentId"
          control={control}
          label="Department"
          numeric
          allowEmpty
          options={(departments ?? []).map((d) => ({ value: d.id, label: d.code }))}
        />
        <RHFCheckboxField name="active" control={control} label="Active" />
      </FormDialog>

      <ConfirmDialog
        open={Boolean(crud.deleteTarget)}
        message={`Delete user "${crud.deleteTarget?.userCode}" (${crud.deleteTarget?.email})? This cannot be undone.`}
        loading={crud.deleting}
        onConfirm={crud.confirmDelete}
        onClose={() => crud.setDeleteTarget(null)}
      />
    </Box>
  );
}
