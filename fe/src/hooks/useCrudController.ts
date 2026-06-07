import { useState } from 'react';
import { extractErrorMessage } from '@/api/axios';
import { useNotify } from '@/components/Notifications';

interface WithId {
  id: number;
}

interface CrudResourceLike<TRequest> {
  useCreate: () => { mutateAsync: (body: TRequest) => Promise<unknown>; isPending: boolean };
  useUpdate: () => {
    mutateAsync: (args: { id: number; body: TRequest }) => Promise<unknown>;
    isPending: boolean;
  };
  useRemove: () => { mutateAsync: (id: number) => Promise<unknown>; isPending: boolean };
}

export function useCrudController<TResponse extends WithId, TRequest>(
  resource: CrudResourceLike<TRequest>,
  labels: { entity: string },
) {
  const create = resource.useCreate();
  const update = resource.useUpdate();
  const remove = resource.useRemove();
  const notify = useNotify();

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<TResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<TResponse | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const openCreate = () => {
    setEditing(null);
    setSubmitError(null);
    setFormOpen(true);
  };

  const openEdit = (row: TResponse) => {
    setEditing(row);
    setSubmitError(null);
    setFormOpen(true);
  };

  const closeForm = () => setFormOpen(false);

  const submit = async (body: TRequest): Promise<boolean> => {
    setSubmitError(null);
    try {
      if (editing) {
        await update.mutateAsync({ id: editing.id, body });
        notify.success(`${labels.entity} updated`);
      } else {
        await create.mutateAsync(body);
        notify.success(`${labels.entity} created`);
      }
      setFormOpen(false);
      return true;
    } catch (err) {
      setSubmitError(extractErrorMessage(err));
      return false;
    }
  };

  const confirmDelete = async () => {
    if (!deleteTarget) return;
    try {
      await remove.mutateAsync(deleteTarget.id);
      notify.success(`${labels.entity} deleted`);
      setDeleteTarget(null);
    } catch (err) {
      notify.error(extractErrorMessage(err));
    }
  };

  return {
    formOpen,
    editing,
    deleteTarget,
    submitError,
    openCreate,
    openEdit,
    closeForm,
    submit,
    setDeleteTarget,
    confirmDelete,
    saving: create.isPending || update.isPending,
    deleting: remove.isPending,
  };
}
